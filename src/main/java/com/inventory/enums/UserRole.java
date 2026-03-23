package com.inventory.enums;

public enum UserRole {
    ADMIN("Quản trị viên"),
    MANAGER("Quản lý"),
    STAFF("Nhân viên");

    private final String displayName;
    UserRole(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
