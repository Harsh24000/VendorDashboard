package com.festival.vendor.test;

import com.festival.vendor.exception.OrderPersistenceException;
import com.festival.vendor.model.Order;
import com.festival.vendor.persistence.FileOrderRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileOrderRepositoryTest {

    private Order sampleOrder(int id) {
        Map<String, Integer> items = new LinkedHashMap<>();
        items.put("Momos", 2);
        return new Order(id, "TestCustomer" + id, "Table 9", "9998887770", "", items, 6);
    }

    private String tempDir() throws IOException {
        return Files.createTempDirectory("vendor-repo-test").toString();
    }

    public void testSnapshotRoundTrip() throws Exception {
        FileOrderRepository repo = new FileOrderRepository(tempDir());
        List<Order> original = List.of(sampleOrder(1), sampleOrder(2), sampleOrder(3));

        repo.saveSnapshot(original);
        List<Order> loaded = repo.loadSnapshot();

        TestRunner.assertEquals(original.size(), loaded.size(), "Loaded snapshot should have same order count");
        TestRunner.assertTrue(loaded.containsAll(original), "Loaded orders should match saved orders (by ID equality)");
    }

    public void testLoadSnapshotReturnsEmptyListWhenNoFileExists() throws Exception {
        FileOrderRepository repo = new FileOrderRepository(tempDir());
        List<Order> loaded = repo.loadSnapshot();
        TestRunner.assertTrue(loaded.isEmpty(), "loadSnapshot() with no prior save should return an empty list, not throw");
    }

    public void testCorruptedSnapshotThrowsPersistenceException() throws Exception {
        String dir = tempDir();
        // Write garbage bytes where orders.dat is expected.
        Files.write(Paths.get(dir, "orders.dat"), "not a real object stream".getBytes());
        FileOrderRepository repo = new FileOrderRepository(dir);
        TestRunner.assertThrows(OrderPersistenceException.class, repo::loadSnapshot,
                "A corrupted snapshot file should surface as OrderPersistenceException, not crash the app");
    }

    public void testExportCsvWritesHeaderAndOneRowPerOrder() throws Exception {
        String dir = tempDir();
        FileOrderRepository repo = new FileOrderRepository(dir);
        List<Order> orders = List.of(sampleOrder(10), sampleOrder(11));

        repo.exportCsv(orders);

        List<String> lines = Files.readAllLines(Paths.get(dir, "orders_export.csv"));
        TestRunner.assertEquals(3, lines.size(), "CSV should have 1 header row + 2 order rows");
        TestRunner.assertTrue(lines.get(0).startsWith("OrderID,CustomerName"), "First line should be the CSV header");
    }

    public void testAppendLogAccumulatesAcrossCalls() throws Exception {
        String dir = tempDir();
        FileOrderRepository repo = new FileOrderRepository(dir);

        repo.appendLog("CREATED,1,Burger x1");
        repo.appendLog("STATUS_CHANGE,1,RECEIVED->PREPARING");

        List<String> lines = Files.readAllLines(Paths.get(dir, "sync.log"));
        TestRunner.assertEquals(2, lines.size(), "Each appendLog call should add exactly one line");
    }
}
