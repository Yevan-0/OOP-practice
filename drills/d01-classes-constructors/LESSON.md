---
id: d01
module: OOP
kind: drill
title: Classes, constructors and encapsulation
difficulty: 1
timeMinutes: 15
tasks:
  - { name: "extends + constructor chaining with super(...)", marks: 3 }
  - { name: "private final fields + getters", marks: 3 }
  - { name: "validation with IllegalArgumentException", marks: 2 }
  - { name: "override the abstract method", marks: 2 }
---

# d01 · Classes, constructors and encapsulation

> **Exam link:** this is LBP **Task 1** ("preserve inheritance, final field, constructor chaining and public getter",
> worth 4 of P1's 18 marks), plus the validation marks.

## The idea

A **class** is a blueprint. An **object** is one thing built from it with `new`.

```java
Circle c = new Circle(2.0);   // c is an object; Circle is the class
```

Five rules come up in every LBP paper:

1. **`private` fields.** Only the class itself can touch its data. Other code goes through methods. This is
   *encapsulation*.
2. **`final` fields.** The field is set once, in the constructor, and can never change afterwards.
3. **`extends`.** `class Book extends LibraryItem` means a Book *is a* LibraryItem. It gets the parent's fields and
   methods.
4. **`super(...)` must be the first line** of a child constructor. The parent part of the object has to be built before
   the child part. This is *constructor chaining*.
5. **Validate before you store.** If the data is bad, `throw new IllegalArgumentException("message")`. The object is
   then never created.

An **abstract** class (`public abstract class LibraryItem`) can't be created with `new`. An **abstract method**
(`public abstract int loanDays();`) has no body. Every concrete child **must** write that method, or the code won't
compile.

## Worked example

This uses a *different* scenario, so you can copy the pattern but not the answer.

```java
public abstract class Vehicle {
  private final String plate;

  protected Vehicle(String plate) {
    if (plate == null || plate.isBlank()) {
      throw new IllegalArgumentException("plate is required");
    }
    this.plate = plate.trim();
  }

  public String getPlate() {
    return plate;
  }

  public abstract int wheels();          // each kind of vehicle decides
}

public class Car extends Vehicle {          // a Car IS A Vehicle
  private final int seats;                // private + final

  public Car(String plate, int seats) {
    super(plate);                       // 1st line: build the Vehicle part
    if (seats <= 0) {                   // then validate my own data
      throw new IllegalArgumentException("seats must be positive");
    }
    this.seats = seats;                 // then store it
  }

  public int getSeats() {
    return seats;
  }

  @Override
  public int wheels() {
    return 4;
  }       // the abstract method, filled in
}
```

Read it out loud: *"Car extends Vehicle. Its constructor calls super with the plate first, then checks seats, then
stores it."* That sentence is also your viva answer.

## Your task

Open `src/main/java/edu/practice/d01/model/`.

1. **`LibraryItem` (TODO d01-A).** Reject `id <= 0` and a `null` or blank `title`, and store the title trimmed.
2. **`Book`:**
    - **d01-1:** make it extend `LibraryItem`.
    - **d01-2:** add `private final` fields `author` (`String`) and `pages` (`int`).
    - **d01-3:** in the constructor, call `super(id, title)` first. Then validate the author (not null or blank, stored
      trimmed) and pages (`< 0`).
    - **d01-4:** add the getters `getAuthor()` and `getPages()`.
    - **d01-5:** write `loanDays()`: **21** if pages is **over 500**, otherwise **14**.

Run `BookTest` (right-click → Run). Work until all 8 tests are green.

## Hints

<details><summary>Hint 1: where do I start?</summary>

Start with `class Book extends LibraryItem`. IntelliJ will underline the class red until you add `loanDays()`, so add an
empty version that returns 0 for now. Then fix the constructor.
</details>

<details><summary>Hint 2: "Call to super must be first statement"</summary>

`super(id, title);` has to be the very first line inside `public Book(...) { ... }`. You can validate the author and
pages *after* it.
</details>

<details><summary>Hint 3: checking for blank text</summary>

`author == null || author.isBlank()` is true for `null`, `""` and `"   "`. Check for `null` first. Calling `.isBlank()`
on `null` throws a `NullPointerException`.
</details>

<details><summary>Hint 4: the loan rule (nearly the answer)</summary>

```java

@Override
public int loanDays() {
  return pages > 500 ? 21 : 14;   // "over 500" means > 500, not >= 500
}
```

</details>

## Viva questions

<details><summary>Why are the fields private?</summary>

That's encapsulation. Other classes can't put an object into a bad state, because every change goes through code I
control: the constructor, which validates.
</details>

<details><summary>Why is <code>super(...)</code> the first line?</summary>

The parent part of the object has to exist before the child part. Java enforces it: the parent's constructor runs first
and validates and stores the inherited fields.
</details>

<details><summary>What does <code>final</code> on a field give you?</summary>

The value is set once in the constructor and can't be reassigned. That makes the object safer and easier to reason
about, because nothing can change its author after it's created.
</details>

<details><summary>Why can't you write <code>new LibraryItem(...)</code>?</summary>

It's abstract. It describes what every item has, but `loanDays()` has no body, so only concrete subclasses like `Book`
can be created.
</details>
