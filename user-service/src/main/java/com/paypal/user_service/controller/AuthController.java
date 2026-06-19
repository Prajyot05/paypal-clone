package com.paypal.user_service.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.user_service.dto.JwtResponse;
import com.paypal.user_service.dto.LoginRequest;
import com.paypal.user_service.dto.SignupRequest;
import com.paypal.user_service.entity.User;
import com.paypal.user_service.repository.UserRepository;
import com.paypal.common.util.JWTUtil;
import com.paypal.user_service.kafka.KafkaEventProducer;
import com.paypal.common.dto.UserCreatedEvent;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final KafkaEventProducer kafkaEventProducer;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil, KafkaEventProducer kafkaEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        
        UserCreatedEvent event = new UserCreatedEvent(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
        kafkaEventProducer.sendUserCreatedEvent(String.valueOf(savedUser.getId()), event);
        
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(claims, user.getEmail());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
