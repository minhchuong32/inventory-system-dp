package com.inventory.enums;

public enum OrderStatus {
    PENDING("Chờ xử lý", "badge-pending"),
    COMPLETED("Hoàn thành", "badge-completed"),
    CANCELLED("Đã hủy", "badge-cancelled");

    private final String displayName;
    private final String cssClass;

    OrderStatus(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() { return displayName; }
    public String getCssClass() { return cssClass; }
    public boolean isFinal() { return this == COMPLETED || this == CANCELLED; }
    public boolean isPending() { return this == PENDING; }
}
