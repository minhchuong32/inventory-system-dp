package com.inventory.enums;

public enum MovementType {
    IN("Nhập kho"),
    OUT("Xuất kho"),
    ADJUST("Điều chỉnh"),
    TRANSFER("Chuyển kho");

    private final String displayName;
    MovementType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
