package com.festival.vendor.event;

import com.festival.vendor.model.Order;

public class OrderCreatedEvent extends OrderEvent {

    public OrderCreatedEvent(Order order) {
        super(order);
    }

    @Override
    public String describe() {
        return "CREATED," + getOrder().getOrderId() + "," + getOrder().itemSummary();
    }
}
