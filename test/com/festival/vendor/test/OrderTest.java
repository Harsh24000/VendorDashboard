package com.festival.vendor.test;

import com.festival.vendor.exception.InvalidOrderException;
import com.festival.vendor.model.Order;
import com.festival.vendor.model.OrderStatus;
import com.festival.vendor.service.OrderQueueService;

import java.util.LinkedHashMap;
import java.util.Map;

public class OrderTest {

    private Order makeOrder(int id, String name, int prepMinutes, String contact) {
        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("Burger", 1);
        return new Order(id, name, "Table 1", contact, "", items, prepMinutes);
    }

    public void testEqualsIsBasedOnOrderId() {
        Order a = makeOrder(1, "Aarav", 5, "");
        Order b = makeOrder(1, "DifferentName", 10, "");
        TestRunner.assertTrue(a.equals(b), "Orders with same ID should be equal regardless of other fields");
        TestRunner.assertEquals(a.hashCode(), b.hashCode(), "Equal orders must have equal hashCode");
    }

    public void testDefaultStatusIsReceived() {
        Order o = makeOrder(2, "Diya", 5, "");
        TestRunner.assertEquals(OrderStatus.RECEIVED, o.getStatus(), "New orders should start as RECEIVED");
    }

    public void testStatusNextProgression() {
        TestRunner.assertEquals(OrderStatus.PREPARING, OrderStatus.RECEIVED.next(), "RECEIVED -> PREPARING");
        TestRunner.assertEquals(OrderStatus.READY, OrderStatus.PREPARING.next(), "PREPARING -> READY");
        TestRunner.assertEquals(OrderStatus.SERVED, OrderStatus.READY.next(), "READY -> SERVED");
        TestRunner.assertTrue(OrderStatus.SERVED.next() == null, "SERVED is terminal");
    }

    public void testCompareToOrdersByEstimatedReadyTime() throws InterruptedException {
        Order slow = makeOrder(3, "Kabir", 20, "");
        Thread.sleep(5); // ensure a distinct timestamp
        Order fast = makeOrder(4, "Meera", 2, "");
        TestRunner.assertTrue(fast.compareTo(slow) < 0,
                "An order with a much shorter prep time and a later timestamp should still sort earlier here"
                        + " only if its ready time is sooner - verifying comparator uses ready time, not raw timestamp");
    }

    public void testItemSummaryFormatting() {
        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("Burger", 2);
        items.put("Fries", 1);
        Order o = new Order(5, "Rohan", "Table 2", "", "", items, 8);
        TestRunner.assertEquals("Burger x2, Fries x1", o.itemSummary(), "Item summary should list each item with quantity");
    }

    public void testSubmitOrderRejectsMissingCustomerName() {
        OrderQueueService qs = OrderQueueService.getInstance();
        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("Burger", 1);
        Order bad = new Order(999, "", "Table 1", "", "", items, 5);
        TestRunner.assertThrows(InvalidOrderException.class, () -> qs.submitOrder(bad),
                "Order with blank customer name must be rejected");
    }

    public void testSubmitOrderRejectsInvalidContact() {
        OrderQueueService qs = OrderQueueService.getInstance();
        Order bad = makeOrder(998, "Neha", 5, "12345"); // too short, wrong prefix
        TestRunner.assertThrows(InvalidOrderException.class, () -> qs.submitOrder(bad),
                "Order with malformed contact number must be rejected");
    }

    public void testSubmitOrderAcceptsValidOrder() throws InvalidOrderException {
        OrderQueueService qs = OrderQueueService.getInstance();
        Order good = makeOrder(997, "Yash", 5, "9876543210");
        qs.submitOrder(good); // should not throw
    }
}
