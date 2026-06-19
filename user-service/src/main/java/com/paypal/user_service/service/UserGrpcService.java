package com.paypal.user_service.service;

import com.atlaspay.grpc.user.UserServiceGrpc;
import com.atlaspay.grpc.user.ValidateUserRequest;
import com.atlaspay.grpc.user.ValidateUserResponse;
import com.paypal.user_service.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Handles incoming gRPC requests from other microservices.
 * We use gRPC here instead of REST because internal service-to-service communication
 * needs to be extremely fast and lightweight. Protocol Buffers give us that performance boost!
 */
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    public UserGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void validateUser(ValidateUserRequest request, StreamObserver<ValidateUserResponse> responseObserver) {
        System.out.println("⚡ [gRPC] Received validation request for User ID: " + request.getUserId());
        
        // 1. Check if the user exists in our Postgres database
        boolean exists = userRepository.existsById(request.getUserId());
        
        // 2. Build the protobuf response
        ValidateUserResponse response;
        if (exists) {
            response = ValidateUserResponse.newBuilder()
                    .setIsValid(true)
                    .build();
        } else {
            response = ValidateUserResponse.newBuilder()
                    .setIsValid(false)
                    .setErrorMessage("User account does not exist or is suspended.")
                    .build();
        }

        // 3. Send the response back to the client (e.g., transaction-service)
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
