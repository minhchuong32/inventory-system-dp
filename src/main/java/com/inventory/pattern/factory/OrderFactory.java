package com.inventory.pattern.factory;

import com.inventory.entity.base.AbstractOrder;

/**
 * FACTORY METHOD PATTERN – Creator interface.
 * Định nghĩa method tạo đối tượng AbstractOrder
 * mà không cần biết cụ thể class nào sẽ được tạo.
 */
public interface OrderFactory<T extends AbstractOrder> {
    /**
     * Factory Method: tạo và khởi tạo đối tượng order mới.
     * @param request chứa dữ liệu cần thiết để tạo order
     */
    T createOrder(OrderRequest request);

    /** Loại order mà factory này tạo ra */
    String getOrderType();
}
