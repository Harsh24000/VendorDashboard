package com.festival.vendor.event;

import com.festival.vendor.model.Order;
import java.time.Instant;

/**
 * Base class for all events broadcast by the OrderQueueService (Observer pattern).
 * Kept abstract to demonstrate inheritance/polymorphism across concrete event types.
 */
public abstract class OrderEvent {

    private final Order order;
    private final Instant occurredAt;

    protected OrderEvent(Order order) {
        this.order = order;
        this.occurredAt = Instant.now();
    }

    public Order getOrder() { return order; }
    public Instant getOccurredAt() { return occurredAt; }

    /** Human readable description used for logging/sync entries. */
    public abstract String describe();
}
