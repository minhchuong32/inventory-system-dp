package com.inventory.exception;

public class DuplicateCodeException extends BusinessException {
    public DuplicateCodeException(String fieldName, String value) {
        super("DUPLICATE_CODE", fieldName + " đã tồn tại: " + value);
    }
}
