---
id: d05
module: OOP
kind: drill
title: Sorting with comparators and streams
difficulty: 3
timeMinutes: 20
tasks:
  - { name: "Comparator: points DESC, goal difference DESC, name ASC", marks: 4 }
  - { name: "sorted copy, source list untouched", marks: 2 }
  - { name: "streams: filter -> sorted -> map -> toList", marks: 3 }
  - { name: "reject a negative minimum", marks: 1 }
---

# d05 · Sorting with comparators and streams

> **Exam link:** every paper has a multi-key sort hidden in LBP **Task 2**. P1: *"return quotes sorted by priceCents
ASC, then ID ASC"*. P2: *"sort priority DESC, energy ASC, ID ASC"*. P3: *"sorting all workloads by urgency at that tick
DESC and ID ASC"*. Mixing DESC and ASC keys is where marks get lost.

## The idea

### 1. A Comparator answers "who goes first?"

```java
Comparator<Team> byPoints = Comparator.comparingInt(Team::getPoints);   // smallest first (ASC)
List<Team> sorted = teams.stream().sorted(byPoints).toList();
```

`compare(a, b)` returns a **negative** number if `a` goes first, **0** for a tie, and **positive** if `b` goes first.
You almost never write that by hand. You build it from **keys**:

| You want                                   | Write                                                                |
|--------------------------------------------|----------------------------------------------------------------------|
| sort by a number, small → big              | `Comparator.comparingInt(Team::getPoints)`                           |
| sort by text, A → Z                        | `Comparator.comparing(Team::getName)`                                |
| a tie-break (2nd key)                      | `.thenComparing(Team::getName)` or `.thenComparingInt(...)`          |
| big → small for the **whole chain so far** | `.reversed()`                                                        |
| big → small for **one key only**           | `.thenComparing(Team::getGoalDifference, Comparator.reverseOrder())` |

### 2. The `reversed()` trap

`reversed()` flips **everything chained before it**, not just the last key:

```java
// WANT: points DESC, then name A→Z
Comparator.comparingInt(Team::getPoints)
          .

thenComparing(Team::getName)
          .

reversed();                         // ✘ points DESC, but names come out Z→A
```

Two safe patterns:

```java
// a) reverse the FIRST key straight away, before anything else is chained
Comparator.comparingInt(Team::getPoints).

reversed()        // only points is flipped
          .

thenComparing(Team::getName);                   // ✔ name stays A→Z

// b) give a later key its own direction
Comparator.

comparing(Team::getName)
          .

thenComparing(Team::getPoints, Comparator.reverseOrder());   // ✔ points DESC as tie-break
```

Use **method references** (`Team::getPoints`) in these chains. A lambda like `comparing(t -> t.getPoints()).reversed()`
often fails to compile ("cannot find symbol getPoints () in Object"), because Java can't work out what `t` is.

### 3. Streams: filter → sorted → map → toList

```java
List<String> names = teams.stream()                  // 1. start a stream over the list
  .filter(t -> t.getPoints() >= 10)             // 2. keep only some
  .sorted(order)                                // 3. put them in order
  .map(t -> t.getName())                        // 4. turn each one into something else
  .toList();                                    // 5. collect into a NEW list
```

A stream **never changes the list it came from**. That matters, because the papers' repositories hand you
`List.copyOf(...)` (a read-only list), and a caller doesn't expect their list to be reshuffled.

### 4. Don't sort the caller's list

```java
teams.sort(order);                     // ✘ changes the caller's list, and crashes on List.of(...)

List<Team> copy = new ArrayList<>(teams);
copy.

sort(order);                      // ✔ sort your own copy
teams.

stream().

sorted(order).

toList(); // ✔ or let a stream build a new list
```

`List.copyOf(teams)` is a **read-only** copy. It's great for *returning* data safely, but you can't `.sort()` it.

## Worked example

This uses a *different* domain, a bookshop, so you can copy the pattern but not the answer. Order: **price ASC**, then
**rating DESC**, then **title A→Z**.

```java
public Comparator<Book> shopOrder() {
  return Comparator.comparingInt(Book::getPriceCents)                     // cheapest first
    .thenComparing(Book::getRating, Comparator.reverseOrder())       // best rated first
    .thenComparing(Book::getTitle);                                  // A→Z
}

public List<BookLine> shelf(List<Book> books, int maxPriceCents) {
  if (maxPriceCents <= 0) throw new IllegalArgumentException("maxPriceCents must be positive");
  return books.stream()
    .filter(b -> b.getPriceCents() <= maxPriceCents)
    .sorted(shopOrder())
    .map(b -> new BookLine(b.getTitle(), b.getPriceCents()))
    .toList();
}
```

| position | title      | price | rating | why                                                                         |
|----------|------------|-------|--------|-----------------------------------------------------------------------------|
| 1st      | Persuasion | 650   | 3      | cheapest, so rating doesn't matter                                          |
| 2nd      | Dune       | 899   | 5      | ties on price with Emma and Ivanhoe, best rating                            |
| 3rd      | Emma       | 899   | 4      | ties with Ivanhoe on price **and** rating, so the title decides: E before I |
| 4th      | Ivanhoe    | 899   | 4      |                                                                             |

Go key by key. Only look at the next key when everything before it is equal.

## Your task

A five-a-side tournament. `Team` (given) gives you `getPoints()` (3 per win + 1 per draw), `getGoalDifference()` and
`getName()`.

Leaderboard order:

1. **points**, highest first
2. equal points → **goal difference**, highest first
3. equal points and goal difference → **name**, A to Z

| team    | points | GD |                                         |
|---------|--------|----|-----------------------------------------|
| Owls    | 12     | −1 | 1st: most points                        |
| Hawks   | 10     | +7 | 2nd: best GD of the 10-point teams      |
| Eagles  | 10     | +4 | 3rd: ties Falcons, but E comes before F |
| Falcons | 10     | +4 | 4th                                     |

Open `src/main/java/edu/practice/d05/service/LeaderboardService.java`:

1. **TODO d05-1: `leaderboardOrder()`.** Return that order as one `Comparator<Team>`.
2. **TODO d05-2: `sorted(teams)`.** Return a **new** sorted list. The list passed in must not change, and it may be a
   read-only `List.of(...)`.
3. **TODO d05-3: `table(teams, minPoints)`.** Keep teams with **at least** `minPoints` points, sort them, and turn each
   into `new Standing(name, points, goalDifference)`. Reject `minPoints < 0` with `IllegalArgumentException`.

Run `LeaderboardServiceTest`. Tests 1–3 check one rule each, so fix them in order. Work until all 8 are green.

## Hints

<details><summary>Hint 1: where do I start?</summary>

Get test 1 green first, with only the points key:

```java
return Comparator.comparingInt(Team::getPoints);
```

Run it. It's backwards (fewest points first). Add `.reversed()` straight after it. Then chain the next key with
`.thenComparing(...)`.
</details>

<details><summary>Hint 2: test 2 or test 3 fails after adding the other keys</summary>

- **Test 2 fails** (lower GD first): `thenComparingInt(Team::getGoalDifference)` is ascending. Give this key its own
  direction: `.thenComparing(Team::getGoalDifference, Comparator.reverseOrder())`.
- **Test 3 fails** (names Z→A): you have a `.reversed()` at the **end** of the chain, and it flipped the name too. Move
  it so it only touches the points key.

</details>

<details><summary>Hint 3: test 5 fails, or you get UnsupportedOperationException</summary>

You sorted the list you were given (`teams.sort(...)`). `List.of(...)` can't be changed, so it throws. Use
`teams.stream().sorted(leaderboardOrder()).toList()`, which builds a new list.
</details>

<details><summary>Hint 4: the comparator (nearly the answer)</summary>

```java
return Comparator.comparingInt(Team::getPoints).

reversed()
        .

thenComparing(Team::getGoalDifference, Comparator.reverseOrder())
  .

thenComparing(Team::getName);
```

</details>

<details><summary>Hint 5: table() (nearly the answer)</summary>

```java
if(minPoints< 0)throw new

IllegalArgumentException("minPoints cannot be negative");
return teams.

stream()
        .

filter(t ->t.

getPoints() >=minPoints)      // AT LEAST: >=, not >
  .

sorted(leaderboardOrder())
  .

map(t ->new

Standing(t.getName(),t.

getPoints(),t.

getGoalDifference()))
  .

toList();
```

</details>

## Viva questions

<details><summary>What does <code>.reversed()</code> reverse?</summary>

The whole comparator built so far, so every key before it. That's why I call it straight after the first key, or use
`thenComparing(key, Comparator.reverseOrder())` to flip just one later key.
</details>

<details><summary>Why does the order of the <code>thenComparing</code> calls matter?</summary>

Each key is only looked at when all the keys before it are equal. Points decide first, goal difference only breaks a
points tie, and the name only breaks a points-and-GD tie.
</details>

<details><summary>What's the difference between <code>list.sort(c)</code> and <code>list.stream().sorted(c).toList()</code>?</summary>

`list.sort` changes that list in place, and throws on an unmodifiable list. The stream version leaves the original alone
and returns a new list.
</details>

<details><summary>Why add a last tie-break on the name (or the id)?</summary>

Without it, fully tied items come out in whatever order the input had, and from a `HashMap` that isn't guaranteed. The
last key makes the result deterministic, so the same request always gives the same answer.
</details>
