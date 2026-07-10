package com.festival.vendor.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiny JUnit-free test harness. We deliberately avoid pulling in the JUnit jar
 * since the project is built with plain javac and no dependency manager -
 * this keeps "clone and run" to two commands. Swap in real JUnit later by
 * annotating these same methods with @Test if the course setup allows adding jars.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Running unit tests...\n");

        run("OrderTest", new OrderTest());
        run("FileOrderRepositoryTest", new FileOrderRepositoryTest());

        System.out.println("\n----------------------------------------");
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (!failures.isEmpty()) {
            System.out.println("Failures:");
            for (String f : failures) System.out.println("  - " + f);
            System.exit(1);
        }
    }

    private static void run(String suiteName, Object suiteInstance) {
        for (var method : suiteInstance.getClass().getMethods()) {
            if (method.getName().startsWith("test")) {
                String testId = suiteName + "." + method.getName();
                try {
                    method.invoke(suiteInstance);
                    System.out.println("PASS  " + testId);
                    passed++;
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    System.out.println("FAIL  " + testId + "  -> " + cause);
                    failed++;
                    failures.add(testId + " -> " + cause);
                }
            }
        }
    }

    // --- tiny assertion helpers used by the test classes ---
    static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    static void assertThrows(Class<? extends Exception> expected, ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Exception e) {
            if (expected.isInstance(e)) return;
            throw new AssertionError(message + " (wrong exception type: " + e.getClass()) ;
        }
        throw new AssertionError(message + " (no exception thrown)");
    }

    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
