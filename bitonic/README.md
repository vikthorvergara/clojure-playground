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
