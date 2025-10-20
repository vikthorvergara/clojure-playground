# Clojure Learning Path

A hands-on approach to learning Clojure through focused projects. Each project explores a specific concept with working code examples.

## Learning Order

### 1. **vectors-fun**
Persistent vectors, subvec, vector operations, performance characteristics
- Understanding Clojure's primary sequential data structure
- Vector operations and performance
- Persistent data structures concepts

### 2. **higher-order-fun**
map, filter, reduce, comp, partial, apply
- Core functional programming patterns
- Function composition
- Working with collections functionally

### 3. **macros-fun**
Macro basics, syntax-quote, gensym, macro hygiene
- Metaprogramming fundamentals
- Code as data
- Creating DSLs and syntactic abstractions

### 4. **multimethods-fun**
Polymorphism, hierarchies
- Multiple dispatch
- Creating extensible systems
- Clojure's approach to polymorphism

### 5. **spec-fun**
clojure.spec.alpha for data validation and generative testing
- Data validation
- Generative testing
- Contract-based programming

### 6. **atoms-fun**
Atomic state management, swap!, compare-and-set!
- Managing state in Clojure
- Atomic updates
- State coordination patterns

### 7. **performance-fun**
Benchmarking, profiling, optimization techniques
- Performance measurement
- Identifying bottlenecks
- Optimization strategies

## Project Structure

Each project follows this structure:
```
topic-fun/
├── README.md           # Overview, concepts, running instructions
├── deps.edn           # Dependencies
├── src/topic_fun/
│   ├── core.clj       # Main examples
│   ├── advanced.clj   # Advanced patterns
│   └── benchmarks.clj # Performance comparisons (if applicable)
└── test/topic_fun/
    └── core_test.clj  # Tests and examples
```

## Additional Topics for Later

### Data Structures
- `maps-fun` - Hash maps, sorted maps, records, update operations, transients
- `sets-fun` - Set operations, sorted sets, relations
- `sequences-fun` - Lazy sequences, sequence operations, chunking

### Functional Programming
- `transducers-fun` - Composable transformation pipelines
- `recursion-fun` - Loop/recur, tail call optimization, trampolining
- `destructuring-fun` - Sequential and associative destructuring patterns

### Concurrency & State
- `refs-fun` - STM (Software Transactional Memory), coordinated state
- `agents-fun` - Asynchronous state updates
- `core-async-fun` - Go blocks, channels, async patterns

### Metaprogramming
- `protocols-fun` - Protocol-based polymorphism vs. multimethods

### Validation & Contracts
- `schema-fun` - Plumatic schema (alternative to spec)

### Java Interop
- `interop-fun` - Calling Java, type hints, arrays, exceptions

### Advanced Topics
- `reducers-fun` - Parallel fold operations
- `core-logic-fun` - Logic programming with core.logic
- `error-handling-fun` - Exception handling, error patterns

### Practical Patterns
- `namespaces-fun` - Namespace organization, require, import, alias
- `testing-fun` - clojure.test, property-based testing with test.check

## Running the Examples

Each project can be run independently:

```bash
cd topic-fun
clj -M -m topic-fun.core
```

Or run tests:

```bash
clj -X:test
```
