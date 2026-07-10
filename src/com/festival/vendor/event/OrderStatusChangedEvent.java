package com.festival.vendor.event;

import com.festival.vendor.model.Order;
import com.festival.vendor.model.OrderStatus;

public class OrderStatusChangedEvent extends OrderEvent {

    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        super(order);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public OrderStatus getPreviousStatus() { return previousStatus; }
    public OrderStatus getNewStatus() { return newStatus; }

    @Override
    public String describe() {
        return "STATUS_CHANGE," + getOrder().getOrderId() + "," + previousStatus + "->" + newStatus;
    }
}
