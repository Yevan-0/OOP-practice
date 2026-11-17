---
id: d04
module: OOP
kind: drill
title: Polymorphism over a list of parents
difficulty: 2
timeMinutes: 20
tasks:
  - { name: "loop over List<Vehicle> and add up costCents (addExact)", marks: 3 }
  - { name: "build one CostQuote per vehicle", marks: 3 }
  - { name: "sort by cost ASC, then id ASC", marks: 2 }
  - { name: "no instanceof / getClass() / casts (source scan)", marks: 2 }
---

# d04 · Polymorphism over a list of parents

> **Exam link:** LBP **Task 2**, last part. P1: *"8 marks: implement ParkingService.allQuotes (minutes). Invoke
priceCents through ParkingPass references; return quotes sorted by priceCents ASC, then ID ASC. Do not use instanceof or
branch on concrete pass types."* P2 says the same thing: *"Do not use concrete subclass type checks in the planning
algorithm."*

## The idea

Look at this code first:

```java
Vehicle v = new Truck(3, "TRUCK-03", 1_500, 20);
int cost = v.costCents(10);      // 1700: the TRUCK rule runs
```

Every variable has **two types**:

|                   | What is it?                  | Here      | What it decides                                                              |
|-------------------|------------------------------|-----------|------------------------------------------------------------------------------|
| **Variable type** | the type written on the left | `Vehicle` | which methods you are *allowed to call* (`v.costCents(...)`, `v.getPlate()`) |
| **Object type**   | the class after `new`        | `Truck`   | which version of the method *actually runs*                                  |

So `v.costCents(10)` compiles because `Vehicle` declares `costCents`. When the program runs, Java looks at the real
object (a `Truck`) and runs `Truck.costCents`. This is called **dynamic dispatch**, and it's what **polymorphism**
means: one call, many behaviours.

It works the same way inside a loop:

```java
List<Vehicle> convoy = List.of(new Van(1, "VAN-01", 30), new Truck(3, "TRUCK-03", 1_500, 20));
for(
Vehicle v :convoy){
  System.out.

println(v.costCents(10));   // prints 300, then 1700
  }
```

The loop never asks "are you a Van?". It doesn't need to. **Each object already knows its own rule.**

### The wrong way (it loses marks)

```java
for(Vehicle v :convoy){
  if(v instanceofVan van)total +=km *van.
    getCentsPerKm();          // ✘
  else if(v instanceof Truck t)
    total +=t.

getBaseCents() +km *t.

getCentsPerKm();   // ✘
}
```

This copies each rule a second time, into the service. Worse, the day someone adds a `Scooter`, this loop silently
ignores it. The papers say "do not use instanceof" for exactly this reason, and so do the tests here: one of them
**reads your `FleetService.java`** and fails if it finds `instanceof`, `getClass()` or a subclass name such as `Van`.

## Worked example

This uses a *different* domain, phone plans, so you can copy the pattern but not the answer.

```java
public abstract class Plan {
  // id + name fields, constructor and getters left out
  public abstract String label();

  public abstract int billCents(int minutes);
}

public class PayPerMinute extends Plan {        // 5 c per minute
  @Override
  public int billCents(int minutes) {
    return Math.multiplyExact(minutes, 5);
  }
}

public class Unlimited extends Plan {           // always 2000 c
  @Override
  public int billCents(int minutes) {
    return 2_000;
  }
}

public record BillLine(long planId, String label, int cents) {
}
```

The service only ever says `Plan`:

```java
// 1) add up over a List<Plan>
public int totalBillCents(List<Plan> plans, int minutes) {
  if (minutes <= 0) throw new IllegalArgumentException("minutes must be positive");
  int total = 0;
  for (Plan p : plans) {
    try {
      total = Math.addExact(total, p.billCents(minutes));   // each plan's own rule
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("total too large", e);
    }
  }
  return total;
}

// 2) one DTO per object, sorted cheapest first, then by id
public List<BillLine> bills(int minutes) {
  if (minutes <= 0) throw new IllegalArgumentException("minutes must be positive");
  return repo.all().stream()
    .map(p -> toLine(p, minutes))                         // Plan -> BillLine
    .sorted(Comparator.comparingInt(BillLine::cents)      // cheapest first
      .thenComparingLong(BillLine::planId))         // equal price -> lower id first
    .toList();
}

private BillLine toLine(Plan p, int minutes) {
  return new BillLine(p.getId(), p.label(), p.billCents(minutes));
}
```

Read it out loud: *"For each plan, call billCents. Java runs the right version for the real object, so I never check the
type."* That sentence is also your viva answer.

**Why the tie-break on id?** `repo.all()` comes from a `HashMap`, so its order is not guaranteed. If two plans cost the
same, only the second key (`planId`) makes the answer the same every time. Tests (and Section B) check this.

## Your task

The repository (`VehicleRepository`) starts with four vehicles. All the subclasses are finished. Read them, but don't
change them.

| id | plate    | class       | rule (`costCents(km)`)                          |
|----|----------|-------------|-------------------------------------------------|
| 1  | VAN-01   | `Van`       | 30 c per km                                     |
| 2  | BIKE-02  | `CargoBike` | 80 c per **started** 25 km (one battery charge) |
| 3  | TRUCK-03 | `Truck`     | 1500 c base + 20 c per km                       |
| 4  | VAN-04   | `Van`       | 22 c per km                                     |

At **10 km** that's bike 80, van #4 220, van #1 300 and truck 1700.

Open `src/main/java/edu/practice/d04/service/FleetService.java`:

1. **TODO d04-1: `convoyCostCents(List<Vehicle> convoy, int km)`.** Add up `costCents(km)` for every vehicle in the
   list. Reject `km <= 0` first, even if the list is empty. An empty list costs `0`. Use `Math.addExact`, so a total too
   big for an `int` throws `IllegalArgumentException`.
2. **TODO d04-2: `quotes(int km)`.** For every vehicle in `repo.all()`, make a
   `new CostQuote(id, plate, kind, km, costCents)`. Reject `km <= 0`.
3. **TODO d04-3:** return the quotes sorted by `costCents` **ascending**, then `vehicleId` **ascending**.

Use only the `Vehicle` type. Call `kind()` for the label (not `getClass()`).

Run `FleetServiceTest`. Work until all 8 tests are green.

## Hints

<details><summary>Hint 1: where do I start?</summary>

Start with `convoyCostCents`. It's a normal `for` loop, like adding up numbers in an array:

```java
int total = 0;
for(
Vehicle v :convoy){
total =total +v.

costCents(km);    // make this overflow-safe in a minute
}
  return total;
```

Put the `km <= 0` check *above* the loop, or an empty list will happily return 0 for `km = 0`.
</details>

<details><summary>Hint 2: test 3 (overflow) still fails</summary>

`total + ...` silently wraps around to a negative number. Use `Math.addExact(total, v.costCents(km))` inside a `try`,
and in the `catch (ArithmeticException e)`, `throw new IllegalArgumentException("total cost too large", e);`.
</details>

<details><summary>Hint 3: building a CostQuote</summary>

A record is created like any class: `new CostQuote(v.getId(), v.getPlate(), v.kind(), km, v.costCents(km))`. The order
must match the record header: `(long vehicleId, String plate, String kind, int km, int costCents)`. Put it in a
`private CostQuote toQuote(Vehicle v, int km)` helper to keep `quotes` short.
</details>

<details><summary>Hint 4: test 8 fails but I never wrote instanceof</summary>

Test 8 also fails if the file mentions `Van`, `Truck` or `CargoBike` anywhere outside a comment (a cast like `(Van) v`,
an import, a variable type). It also fails until the service actually calls `.costCents(` on something.
`import edu.practice.d04.model.Vehicle;` is fine.
</details>

<details><summary>Hint 5: the sorting (nearly the answer)</summary>

Stream version:

```java
return repo.all().

stream()
        .

map(v ->

toQuote(v, km))
  .

sorted(Comparator.comparingInt(CostQuote::costCents)
                .

thenComparingLong(CostQuote::vehicleId))
  .

toList();
```

Loop version, if streams still feel strange:

```java
List<CostQuote> list = new ArrayList<>();
for(
Vehicle v :repo.

all())list.

add(toQuote(v, km));
  list.

sort(Comparator.comparingInt(CostQuote::costCents).

thenComparingLong(CostQuote::vehicleId));
  return list;
```

Test 6 fails if you forget `thenComparingLong(...)`: two vans with the same rate then come out in `HashMap` order.
</details>

## Viva questions

<details><summary>In <code>Vehicle v = new Truck(...)</code>, what's the variable type and what's the object type?</summary>

The variable type is `Vehicle`, and it decides which methods I can call. The object type is `Truck`, and it decides
which version runs. So `v.costCents(10)` runs `Truck`'s rule.
</details>

<details><summary>What is polymorphism / dynamic dispatch?</summary>

One method call that behaves differently depending on the real object. Java picks the overriding method at run time,
from the object's class, not from the variable's type.
</details>

<details><summary>Why not use <code>instanceof</code> in the service?</summary>

It duplicates each subclass's rule in the service and breaks when a new subclass is added. With polymorphism, adding a
`Scooter` needs no change to `FleetService` at all: the new class brings its own `costCents`.
</details>

<details><summary>Why sort by id after cost?</summary>

The repository is a `HashMap`, so its order isn't guaranteed. When two vehicles cost the same, the id tie-break makes
the output deterministic, so the same request always gives the same list.
</details>

<details><summary>Why use <code>kind()</code> instead of <code>getClass().getSimpleName()</code>?</summary>

`kind()` is part of the model's design: each subclass chooses its own label, and that's polymorphism again. `getClass()`
is a type check in disguise, and it ties the API output to Java class names.
</details>
