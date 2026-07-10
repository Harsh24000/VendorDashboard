# Smart Food Vendor Order Dashboard

Java Swing desktop app for vendor stalls to track incoming food-festival orders in
real time, update status (Received → Preparing → Ready → Served), and keep working
through network drop-outs. Built for OOP Track 4 (Application Development with Java
Frameworks), matching the problem formulation PPT.

## Run it

Needs a JDK (17+) on your PATH — no Maven/Gradle, no external libraries.

```bash
./build.sh        # compiles src/ and test/ into ./out
./run.sh          # launches the dashboard GUI
./run_tests.sh     # runs the unit test suite (13 tests)
```

Orders start flowing automatically ~1-4s apart (simulated feed, standing in for the
real ordering app). Select a row and click **Advance Status** to move it through
its lifecycle. **Go Offline (simulate)** pauses syncing to disk without pausing the
dashboard itself, then **Go Online** catches sync back up — this is the offline
resilience demo for the video.

Data files land in `./data/`:
- `orders.dat` — full snapshot (Java serialization), used to recover state if the
  app is killed and restarted
- `orders_export.csv` — the integration payload a real backend would ingest
- `sync.log` — append-only log of every create/status-change event

## Architecture

```
model/       Order, OrderStatus            — domain objects
event/       OrderEvent (abstract) + 2 subclasses — Observer pattern payloads
exception/   InvalidOrderException, OrderPersistenceException — checked, custom
persistence/ OrderRepository<T> (DAO interface) + FileOrderRepository — file I/O
service/     OrderQueueService (Singleton, Subject), OrderGenerator (Producer),
             SyncService (Consumer + Observer)
ui/          VendorDashboard (JFrame), OrderTableModel (AbstractTableModel)
```

**Flow:** `OrderGenerator` (producer thread) builds random orders and pushes them
onto a `BlockingQueue` inside `OrderQueueService`. An internal dispatcher thread
(consumer) drains that queue, adds each order to the active list, and fires an
Observer event to every registered `OrderListener`. Both the Swing dashboard and
`SyncService` are listeners — the dashboard repaints itself (hopping onto the EDT
via `SwingUtilities.invokeLater`), and `SyncService` writes the event to the log
and, every 8s, checkpoints a full snapshot + CSV to disk.

## How this maps to the rubric

| Requirement | Where |
|---|---|
| OOP (classes, inheritance, polymorphism) | `Order`, `OrderEvent` → `OrderCreatedEvent`/`OrderStatusChangedEvent` |
| Generics | `OrderRepository<T>` |
| Collections | `BlockingQueue`, `CopyOnWriteArrayList`, `Collections.sort` w/ `Comparable` |
| Multithreading | `OrderGenerator`, internal dispatcher thread, `SyncService`, Swing EDT handling |
| File I/O & Serialization | `FileOrderRepository` (ObjectOutputStream snapshot + CSV export) |
| Exception handling | `InvalidOrderException`, `OrderPersistenceException`, validation in `OrderQueueService` |
| Regex | contact-number validation (`Pattern`/`Matcher`) in `OrderQueueService` |
| Inner classes | `StatusCellRenderer` inside `VendorDashboard` |
| Design patterns | Singleton (`OrderQueueService`), Observer (`OrderListener`), DAO (`OrderRepository`), Producer-Consumer (`OrderGenerator` → dispatcher), MVC-ish (model/ui split) |
| Integration with platform | File-based CSV export + BlockingQueue standing in for a WebSocket/REST push, per the spec's allowed integration options |
| Unit tests | `test/` — 13 tests, no external framework (see note below) |

## Note on testing

The spec's example mentions JUnit, but since the build is plain `javac` with zero
dependencies, `test/` ships a ~50-line custom runner (`TestRunner.java`) instead —
reflection-based, prints PASS/FAIL, no jar to manage. If your course setup allows
adding a JUnit jar to the classpath, the same test methods can be annotated with
`@Test` with almost no rewriting.

## Before you submit

- Rename this folder to your group number (per the course instructions).
- Fill in real names/IDs in the demo video and report.
- The report needs a UML class diagram — the package layout above maps directly to
  one if you want a starting point.
