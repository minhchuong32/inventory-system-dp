package com.inventory.enums;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán"),
    PARTIAL("Thanh toán một phần"),
    PAID("Đã thanh toán");

    private final String displayName;
    PaymentStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
