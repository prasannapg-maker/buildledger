package com.buildledger.iam.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User not found with id: " + id);
    }
    public static ResourceNotFoundException vendor(Long id) {
        return new ResourceNotFoundException("Vendor not found with id: " + id);
    }
    public static ResourceNotFoundException client(Long id) {
        return new ResourceNotFoundException("Client not found with id: " + id);
    }
    public static ResourceNotFoundException document(Long id) {
        return new ResourceNotFoundException("Document not found with id: " + id);
    }
}
