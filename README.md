# java-project
Java Cheet-Sheet

Quick Summary of Functional Programming in Java (with examples)

Functional programming in Java is based on lambdas, method references, the Stream API, Optional, and functional interfaces (Function, Predicate, Supplier, etc.).
The main idea: write small, side-effect-free functions that transform immutable data and can be composed together.

### 1) Lambdas and method references
```
// Lambda
Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());

// Method reference
List<String> names = List.of("Ana","Luis","Sofía","Pablo");
names.sort(Comparator.comparingInt(String::length));
```
Common functional interfaces:

- Predicate<T> → boolean (filter)
- Function<T,R> → transform (map)
- Consumer<T> → consume (no return)
- Supplier<T> → supply values
- UnaryOperator<T> / BinaryOperator<T> → operations of the same type

### 2) Stream API: map, filter, reduce, collect

We’ll use a record as the base example:
```
record Person(String name, int age, String dept, double salary) {}

List<Person> people = List.of(
  new Person("Ana", 28, "IT", 32000),
  new Person("Luis", 41, "Sales", 42000),
  new Person("Sofía", 35, "IT", 52000),
  new Person("Pablo", 29, "Marketing", 38000),
  new Person("Eva", 41, "IT", 61000)
);
```
### Filter + map + collect

```
// Names in IT over 30, alphabetically sorted
List<String> itOver30 =
  people.stream()
        .filter(p -> p.dept().equals("IT"))
        .filter(p -> p.age() > 30)
        .map(Person::name)
        .sorted()
        .toList(); // since Java 16 (before: collect(Collectors.toList()))
```

### Reduce (sums, aggregations)

```
    int totalAge =
  people.stream()
        .map(Person::age)
        .reduce(0, Integer::sum); // or mapToInt(...).sum()
```

### Distinct, sorted, limit, skip
```
List<Integer> uniqueSortedAges =
  people.stream()
        .map(Person::age)
        .distinct()
        .sorted()
        .toList();
```

### 3) Useful Collectors

```
import static java.util.stream.Collectors.*;

// a) Group by department
Map<String, List<Person>> byDept =
  people.stream().collect(groupingBy(Person::dept));

// b) Count by department
Map<String, Long> countByDept =
  people.stream().collect(groupingBy(Person::dept, counting()));

// c) Average salary by department
Map<String, Double> avgSalaryByDept =
  people.stream().collect(groupingBy(Person::dept, averagingDouble(Person::salary)));

// d) Highest paid per department (returns Optional<Person>)
Map<String, Optional<Person>> topPaidByDept =
  people.stream().collect(groupingBy(Person::dept,
                                     maxBy(Comparator.comparingDouble(Person::salary))));

// e) Transform while collecting (mapping) into a set
Map<String, Set<String>> namesByDept =
  people.stream().collect(groupingBy(Person::dept,
                                     mapping(Person::name, toSet())));

// f) Partition (boolean predicate)
Map<Boolean, List<Person>> over40 =
  people.stream().collect(partitioningBy(p -> p.age() >= 40));

// g) joining (concatenate strings)
String allNames =
  people.stream().map(Person::name).collect(joining(", ", "[", "]"));
```

Tip: if you want to remove the Optional when using maxBy while grouping, use collectingAndThen:
```
Map<String, Person> topPaidByDept2 =
  people.stream().collect(groupingBy(Person::dept,
    collectingAndThen(
      maxBy(Comparator.comparingDouble(Person::salary)),
      Optional::orElseThrow // or provide a default
)));
```

### 4) flatMap: “flatten” structures
```
List<List<String>> words = List.of(
  List.of("hello", "world"),
  List.of("java", "stream")
);

List<String> flat =
  words.stream()
       .flatMap(List::stream)
       .toList(); // ["hello","world","java","stream"]
```

### 5) Optional to avoid null

```
Optional<Person> youngIT =
  people.stream()
        .filter(p -> p.dept().equals("IT"))
        .filter(p -> p.age() < 30)
        .findFirst();

String name =
  youngIT.map(Person::name)
         .orElse("No young IT employee found");
```

### 6) Function composition

```
Function<Person, String> name = Person::name;
Function<String, String> upper = String::toUpperCase;
Function<Person, String> nameInUpper = name.andThen(upper);

List<String> upperNames =
  people.stream().map(nameInUpper).toList();
```

### 7) Parallelism (parallelStream) – use carefully

```
double sum =
  people.parallelStream()               // only for CPU-bound, large collections
        .mapToDouble(Person::salary)    // avoid shared mutable state
        .sum();
```

### 8) Quick best practices

- Prefer immutability and pure functions.
- Keep pipelines short and readable (name intermediate steps if needed).
- Don’t overuse peek; it’s mainly for debugging.
- Use specialized methods (mapToInt, mapToDouble) for performance in aggregations.
- Document complex predicates and comparators with static helper methods.

### 9) Mini end-to-end example

```
// Top 3 IT names by salary, in uppercase, joined with " | "
String top3IT =
  people.stream()
        .filter(p -> p.dept().equals("IT"))
        .sorted(Comparator.comparingDouble(Person::salary).reversed())
        .limit(3)
        .map(Person::name)
        .map(String::toUpperCase)
        .collect(Collectors.joining(" | "));
```