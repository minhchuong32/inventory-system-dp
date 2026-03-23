package com.inventory.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceType, Long id) {
        super("NOT_FOUND", "Không tìm thấy " + resourceType + " với ID: " + id);
    }
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
