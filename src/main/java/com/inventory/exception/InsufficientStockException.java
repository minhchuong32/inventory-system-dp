package com.inventory.exception;

public class InsufficientStockException extends BusinessException {
    private final String productName;
    private final int required;
    private final int available;

    public InsufficientStockException(String productName, int required, int available) {
        super("INSUFFICIENT_STOCK",
            String.format("Sản phẩm '%s' không đủ tồn kho. Yêu cầu: %d, Hiện có: %d",
                productName, required, available));
        this.productName = productName;
        this.required = required;
        this.available = available;
    }
    public String getProductName() { return productName; }
    public int getRequired()  { return required; }
    public int getAvailable() { return available; }
}
