---
id: d06
module: OOP
kind: drill
title: Records and DTOs
difficulty: 3
timeMinutes: 20
tasks:
  - { name: "write a record with the right components", marks: 3 }
  - { name: "compact constructor validation", marks: 2 }
  - { name: "private helper: model -> DTO", marks: 2 }
  - { name: "service: filter, map, sort into a List of records", marks: 3 }
---

# d06 · Records and DTOs

> **Exam link:** every LBP starter keeps its API shapes as records in `dto/` (P1:
> `record Quote(long id, String registration, String passType, int stayMinutes, int priceCents)`, plus `ApiError` and
> `NewHourlyPass`), and the service says *"You may add a private helper method for constructing Quote objects."* P2 Task 2
> ends with *"Return DispatchPlan"*, and P3 Task 3 with *"produce SchedulePlan with selected reservations…"*. Each one
> means building records from your model objects.

## The idea

### 1. A record is a class in one line

```java
public record MenuItem(long id, String name, int priceCents) {
}
```

From that one line Java writes all of this for you:

| You get                                                 | Example                                                                       |
|---------------------------------------------------------|-------------------------------------------------------------------------------|
| `private final` fields                                  | `id`, `name`, `priceCents`                                                    |
| a constructor taking every component, in order          | `new MenuItem(1, "Soup", 450)`                                                |
| an accessor per component, **named like the component** | `item.id()`, `item.name()`, `item.priceCents()` (**not** `getId()`)           |
| `equals` and `hashCode` that compare the values         | `new MenuItem(1, "Soup", 450).equals(new MenuItem(1, "Soup", 450))` is `true` |
| a readable `toString`                                   | `MenuItem[id=1, name=Soup, priceCents=450]`                                   |

A record is **immutable**: there are no setters. To "change" one, you create a new one.

### 2. A compact constructor validates

```java
public record MenuItem(long id, String name, int priceCents) {
  public MenuItem {                                  // no brackets after the name
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
    if (priceCents < 0) throw new IllegalArgumentException("price cannot be negative");
  }                                                  // no this.name = name: Java does that after your checks
}
```

It runs **every time** the record is created, so a bad `MenuItem` can never exist.

### 3. DTO = what the outside world sees

A **model** (`model/`) is everything your program knows. A **DTO**, or *data transfer object* (`dto/`), is only what you
send out or take in.

```java
public class Dish {                       // MODEL: has secrets
  private final long id;
  private final String name;
  private final int costPriceCents;     // what the restaurant pays: must NOT be shown to customers
  private final int marginPercent;

  public int salePriceCents() { ...}   // worked out from cost + margin
}

public record MenuItem(long id, String name, int priceCents) {
}   // DTO: just what the customer sees
```

Why bother?

- **It hides internals.** The cost price never leaves the server.
- **It holds computed values.** `priceCents` is calculated, not stored in `Dish`.
- **It keeps the API stable.** You can redesign `Dish` without changing the JSON clients rely on.

### 4. How Spring turns a record into JSON

Spring uses a library called **Jackson**. When a controller returns a record, Jackson writes one JSON field per
component, **using the component's name**:

```java

@GetMapping("/menu/{id}")
public MenuItem one(@PathVariable long id) { ...}
// -> {"id":1,"name":"Soup","priceCents":450}
```

So the component names **are** your API. Rename `priceCents` to `price`, and the JSON changes. Jackson also works in the
other direction: for a `@RequestBody` record it calls the record's constructor, so your compact-constructor checks run
on incoming JSON too.

(No Spring is needed in this drill. The tests check the record's components directly, which is the same thing Jackson
reads.)

## Worked example

This uses a *different* domain, a restaurant, so you can copy the pattern but not the answer. The service maps `Dish` →
`MenuItem` in **one private helper** and reuses it:

```java
public class MenuService {
  private final DishRepository repo;

  public MenuService(DishRepository repo) {
    this.repo = repo;
  }

  public MenuItem item(long id) {
    Dish dish = repo.get(id);
    if (dish == null) throw new IllegalArgumentException("no dish with id " + id);
    return toItem(dish);                                    // reuse the helper
  }

  public List<MenuItem> menuUnder(int maxPriceCents) {
    return repo.all().stream()
      .filter(d -> d.salePriceCents() <= maxPriceCents)
      .map( ::toItem)                              // reuse the helper again
      .sorted(Comparator.comparingInt(MenuItem::priceCents)
        .thenComparingLong(MenuItem::id))
      .toList();
  }

  private MenuItem toItem(Dish d) {                           // model -> DTO, written ONCE
    return new MenuItem(d.getId(), d.getName(), d.salePriceCents());
  }
}
```

Notice the two styles of getter: `d.getName()` on the **class** `Dish`, and `MenuItem::priceCents` on the **record**.

## Your task

A hotel quotes prices for rooms. The model is finished: `Room` (abstract) with `StandardRoom` and `Suite`, each with its
own `priceCents(nights)`, plus `RoomRepository`. `StayRequest` is a finished record. Read it, because it's your example
of a compact constructor.

Example: a `StayRequest(1, 2)` (1 night, 2 guests) gives room 101 at 9,000, 102 at 12,500, 301 at 15,000 and 302 at
19,000. Room 103 only sleeps 1, so it's left out.

**Why is `RoomQuote.java` already there, but empty?** `QuoteService` returns `RoomQuote`, so the class must exist or
nothing compiles. You write everything inside it. The tests reach its constructor and accessors through `Reflect`, so
they compile before you start.

1. **TODO d06-1** (`dto/RoomQuote.java`): give the record these components, in this order, with these exact names:
   `long roomId`, `String number`, `String type`, `int nights`, `int totalCents`.
2. **TODO d06-2:** add a compact constructor. Throw `IllegalArgumentException` if `number` is null or blank,
   `nights <= 0`, or `totalCents < 0`. A total of `0` is allowed.
3. **TODO d06-3** (`service/QuoteService.java`): write `private RoomQuote toQuote(Room room, int nights)`. Use `type()`
   for the type and `priceCents(nights)` for the total.
4. **TODO d06-4: `quote(roomId, nights)`.** Look the room up. If it's missing, throw `IllegalArgumentException`.
   Otherwise return `toQuote(...)`.
5. **TODO d06-5: `quotes(request)`.** Keep rooms whose capacity is **at least** `request.guests()`, map each with
   `toQuote`, sort by `totalCents` ascending then `roomId` ascending, and return the list.

Run `RoomQuoteTest`. Work until all 8 tests are green.

## Hints

<details><summary>Hint 1: where do the components go?</summary>

Between the brackets after the record's name, written like method parameters, separated by commas:

```java
public record RoomQuote(long roomId, String number /* , ... the other three */) {
}
```

Test 1 checks the names, types and order exactly, because they become the JSON field names.
</details>

<details><summary>Hint 2: "RoomQuote needs a public constructor taking Long, String, String, Integer, Integer"</summary>

The test tried `new RoomQuote(3L, "301", "SUITE", 3, 37000)` and found no match. Your components are missing, in the
wrong order, or the wrong types (for example `int roomId` instead of `long roomId`).
</details>

<details><summary>Hint 3: the compact constructor</summary>

Copy the shape of `StayRequest`: `public RoomQuote { ... }`, with no brackets after the name and no `this.x = x`. Check
`number == null || number.isBlank()` **before** anything that calls a method on `number`.
</details>

<details><summary>Hint 4: "cannot find symbol: method getNights()"</summary>

`StayRequest` is a record, so its accessors are `request.nights()` and `request.guests()`. `Room` is a normal class, so
it has `room.getNumber()`, `room.getCapacity()` and `room.getId()`.
</details>

<details><summary>Hint 5: the service (nearly the answer)</summary>

```java
private RoomQuote toQuote(Room room, int nights) {
  return new RoomQuote(room.getId(), room.getNumber(), room.type(), nights, room.priceCents(nights));
}

public List<RoomQuote> quotes(StayRequest request) {
  return repo.all().stream()
    .filter(room -> room.getCapacity() >= request.guests())
    .map(room -> toQuote(room, request.nights()))
    .sorted(Comparator.comparingInt(RoomQuote::totalCents)
      .thenComparingLong(RoomQuote::roomId))
    .toList();
}
```

</details>

## Viva questions

<details><summary>What does a record give you for free?</summary>

Private final fields, a constructor with every component, accessors named after the components (`roomId()`), and
`equals`, `hashCode` and `toString` based on the values.
</details>

<details><summary>Why is the accessor <code>roomId()</code> and not <code>getRoomId()</code>? Does the JSON still work?</summary>

Records name accessors after the component, without "get". Jackson understands records, so the JSON field is still
called `roomId`, taken from the component's name.
</details>

<details><summary>What is a compact constructor, and when does it run?</summary>

It's a constructor without a parameter list, used to validate (or normalise) values. It runs every time the record is
created, including when Jackson builds one from a `@RequestBody`, before the fields are assigned. So an invalid record
can't exist.
</details>

<details><summary>Why not return the <code>Room</code> model straight from the API?</summary>

The model holds internal data (rates, fees, capacity rules) and can change shape. The DTO shows only what the client
needs, including computed values like `totalCents`, and keeps the JSON stable.
</details>

<details><summary>Why put the mapping in a private helper?</summary>

Both `quote` and `quotes` build quotes, so writing the mapping once means they can never disagree. It's private because
it's an internal detail of the service, not part of its API.
</details>
