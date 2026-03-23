package com.inventory.enums;

public enum StockStatus {
    IN_STOCK("Đủ hàng", "badge-ok", "#2e7d32"),
    LOW_STOCK("Sắp hết", "badge-low", "#f57f17"),
    OUT_OF_STOCK("Hết hàng", "badge-cancelled", "#c62828");

    private final String displayName;
    private final String cssClass;
    private final String color;

    StockStatus(String displayName, String cssClass, String color) {
        this.displayName = displayName;
        this.cssClass = cssClass;
        this.color = color;
    }
    public String getDisplayName() { return displayName; }
    public String getCssClass() { return cssClass; }
    public String getColor() { return color; }
}
