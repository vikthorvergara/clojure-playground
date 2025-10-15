# 20.OCT.2025 - Code Kata

## Implement:
- https://www.geeksforgeeks.org/generate-bitonic-sequence-of-length-n-from-integers-in-a-given-range/

## Do:
- Implementation
- Unit tests
- Performance Test / Benchmarks
- Proper Documentation
- Expose Solution via REST API
- Store Results into a Database (Redis with Docker)

## Redis
Used to store test results

### Carmine
- Carmine auto-generates idiomatic functions for Redis commands and encourages a single pool/spec with wcar for concise, efficient command batching patterns.
- The same Carmine workflow applies to both designs, so choice should hinge on data mutability, query shapes, and retention/consumption requirements rather than client capabilities.

#### Performance notes
Sorted-set writes are O(logN) per member, and range reads are O(logN+M), which scales well for newest-first pagination over indexed timestamps.
Stream range reads are O(N) for entries returned, which is effectively constant for small COUNT windows; XADD is optimized for fast appends and ordered reads.

#### Hash + ZSET
Prefer when results are discrete records that must be updated/read by key and listed by a timestamp index with predictable O(log N) insert and already-sorted retrieval semantics.

#### Streams
Prefer for append-only event logs, simple ingestion with auto IDs, range queries by time/ID, and future consumer-group processing or log trimming needs.

##### Why Streams fits better
- Simpler implementation: Test results are naturally append-only (once a test runs, the result is immutable), which matches Streams' design perfectly. You write with a single XADD command instead of coordinating HSET + ZADD, reducing code complexity and potential consistency bugs during the POC phase.
- Fast performance path: XADD is optimized for ordered appends, and XREVRANGE fetches the latest N results in one call without needing to fetch keys first then retrieve hashes. This eliminates the two-step pattern Hash+ZSET requires (ZREVRANGE for keys, then HGETALL per key), cutting latency and round trips.
- Built-in retention: Use XTRIM with MAXLEN to automatically cap how many test results are kept in memory—critical for a performance-focused POC where you care about recent results, not historical archives. With Hash+ZSET, you'd need to build custom pruning logic for both structures.
- Better observability: Redis Insight provides native stream browsing, XLEN tracking, and entry inspection out of the box, making it easier to debug test result ingestion and query patterns during development.
- Lower maintenance overhead: One data structure means less bookkeeping—no need to keep a hash and index synchronized or worry about orphaned keys if cleanup fails partially.

#### wcar (from carmine-sentinel/carmine)
- This is a macro used in carmine, a Clojure library for interacting with Redis.
- It's designed to execute multiple Redis commands in a single network request, which is known as pipelining.
- Pipelining improves performance by reducing network latency, and wcar returns a single vector of replies for easy destructuring.
- In this context, wcar stands for "wrapped client asynchronous request" or a similar name, and it allows you to set connection options like password and timeout-ms.

---

## Prerequisites

- [Clojure CLI tools](https://clojure.org/guides/install_clojure) (version 1.11.1 or higher)
- [Docker](https://www.docker.com/get-started) and Docker Compose
- Java 11 or higher

## Setup

### 1. Start Redis

The project uses Redis as its database. Start Redis using Docker Compose:

```bash
docker-compose up -d
```

This will start a Redis instance on port 6379.

To verify Redis is running:

```bash
docker ps
```

You should see the `bitonic-redis` container running.

### 2. Install Dependencies

Dependencies are managed via `deps.edn` and will be automatically downloaded when you run the project.

## Running the Project

### Option 1: Using Clojure CLI

Run the server with:

```bash
clj -M -m bitonic.server
```

The server will start on port 3000 by default. You can specify a custom port:

```bash
clj -M -m bitonic.server 8080
```

The API will be available at `http://localhost:3000/api` (or your custom port).

### Option 2: Using REPL

Start a REPL:

```bash
clj
```

Then in the REPL:

```clojure
(require '[bitonic.server :as server])
(server/start-server)
```

To stop the server:

```clojure
(server/stop-server)
```

To restart:

```clojure
(server/restart-server)
```

## Running Tests

Run all tests with:

```bash
clj -M:test -m cognitect.test-runner
```

Or run specific test namespaces:

```bash
clj -M:test -m cognitect.test-runner -n bitonic.core-test
clj -M:test -m cognitect.test-runner -n bitonic.db-test
```

### Running Tests in REPL

Start a REPL with the test alias:

```bash
clj -M:test
```

Then run tests:

```clojure
(require '[clojure.test :as test])
(require '[bitonic.core-test])
(require '[bitonic.db-test])

;; Run all tests
(test/run-all-tests)

;; Run tests for a specific namespace
(test/run-tests 'bitonic.core-test)
```

## Stopping the Project

### Stop the Server

If running via CLI, press `Ctrl+C`.

If running via REPL:

```clojure
(server/stop-server)
```

### Stop Redis

```bash
docker-compose down
```

To also remove the Redis data volume:

```bash
docker-compose down -v
```

## Project Structure

```
bitonic/
├── deps.edn              # Project dependencies and configuration
├── docker-compose.yml    # Docker Compose configuration for Redis
├── src/
│   └── bitonic/
│       ├── api.clj       # REST API routes and handlers
│       ├── core.clj      # Core application logic
│       ├── db.clj        # Redis database operations
│       └── server.clj    # HTTP server setup
└── test/
    └── bitonic/
        ├── api_test_temp.clj
        ├── core_test.clj
        └── db_test.clj
```

## Troubleshooting

### Redis Connection Issues

If you encounter Redis connection errors:

1. Ensure Docker is running
2. Check Redis container status: `docker ps`
3. View Redis logs: `docker logs bitonic-redis`
4. Verify Redis is responding: `docker exec -it bitonic-redis redis-cli ping`

### Port Already in Use

If port 3000 is already in use, you can:

1. Start the server on a different port (see "Running the Project" above)
2. Or stop the process using port 3000

Find the process using port 3000:

```bash
lsof -i :3000
```

Kill the process:

```bash
kill -9 <PID>
```
