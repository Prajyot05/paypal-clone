package com.paypal.transaction_service.client;

import com.atlaspay.grpc.user.UserServiceGrpc;
import com.atlaspay.grpc.user.ValidateUserRequest;
import com.atlaspay.grpc.user.ValidateUserResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * Acts as the gRPC client to communicate with the user-service.
 * This abstracts away the gRPC stub complexity from our main business logic.
 */
@Service
public class UserValidationClient {

    // Injects the blocking stub configured to connect to "user-service"
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcStub;

    /**
     * Synchronously calls the user-service over gRPC to validate a user.
     * @param userId The ID of the user to validate.
     * @return true if valid, false otherwise.
     */
    public boolean validateUser(Long userId) {
        System.out.println("🚀 [gRPC Client] Validating User ID: " + userId + " with user-service...");
        
        ValidateUserRequest request = ValidateUserRequest.newBuilder()
                .setUserId(userId)
                .build();
                
        // Execute the synchronous RPC call
        ValidateUserResponse response = userGrpcStub.validateUser(request);
        
        if (!response.getIsValid()) {
            System.err.println("❌ [gRPC Client] User validation failed: " + response.getErrorMessage());
        }
        
        return response.getIsValid();
    }
}
