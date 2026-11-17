---
id: d03
module: OOP
kind: drill
title: Integer pricing maths without overflow
difficulty: 2
timeMinutes: 20
tasks:
  - { name: "ceiling division that survives Integer.MAX_VALUE", marks: 3 }
  - { name: "tiered pricing", marks: 3 }
  - { name: "Math.multiplyExact / addExact -> IllegalArgumentException", marks: 2 }
  - { name: "reject non-positive input", marks: 2 }
---

# d03 · Integer pricing maths without overflow

> **Exam link:** every paper hides marks in one formula. P1: *"ceiling division into billable hours: 1–60 minutes is one
hour, 61–120 is two"* and *"prevent arithmetic overflow"*. P2: *"using long intermediate arithmetic and safe conversion
to int"*. P3: *"preventing overflow"*. This drill trains that formula.

## The idea

### 1. Integer division throws the remainder away

`150 / 100` is `1`, not `1.5`. Java's `int / int` always rounds **towards zero**.

### 2. Rounding UP ("any started block/hour counts")

For `a >= 1`:

```java
int up = (a - 1) / b + 1;
```

Try it: a=100, b=100 → `99/100 + 1 = 1` ✔. a=101 → `100/100 + 1 = 2` ✔.

The other common version, `(a + b - 1) / b`, **overflows** when `a` is close to `Integer.MAX_VALUE`, because `a + b`
goes past the top. Exam tests love that value.

### 3. Overflow is silent

```java
int big = 2_000_000_000;
int wrong = big * 2;        // -294967296  (no error!)
```

An `int` holds up to **2,147,483,647**. Past that, the value wraps around to a negative number, and Java doesn't warn
you.

### 4. The fix: `Math.*Exact`

```java
int safe = Math.multiplyExact(big, 2);   // throws ArithmeticException instead of lying
```

`Math.addExact`, `Math.subtractExact` and `Math.multiplyExact` exist for `int` and `long`. The papers want an
**IllegalArgumentException** (which becomes HTTP 400), so catch and convert:

```java
try{
  return Math.addExact(Math.multiplyExact(a, b),c);
  }catch(
ArithmeticException e){
  throw new IllegalArgumentException("result too large",e);
}
```

P2's alternative is to compute in `long`, then `Math.toIntExact(longValue)`, which throws `ArithmeticException` if it
doesn't fit.

### 5. Validate first

Reject `<= 0` (or `< 0`, if the paper says "non-negative") **before** any maths.

## Worked example

Here's a taxi charge with a different domain: *every started 15 minutes costs `rate`; after the first 4 slots each slot
costs double.*

```java
public static int slots(int minutes) {
  if (minutes <= 0) throw new IllegalArgumentException("minutes must be positive");
  return (minutes - 1) / 15 + 1;                         // 1..15 -> 1, 16..30 -> 2
}

public static int fareCents(int minutes, int rate) {
  if (rate <= 0) throw new IllegalArgumentException("rate must be positive");
  int n = slots(minutes);
  int normal = Math.min(n, 4);
  int extra = n - normal;                               // 0 if n <= 4
  try {
    int doubled = Math.multiplyExact(rate, 2);
    return Math.addExact(Math.multiplyExact(normal, rate),
      Math.multiplyExact(extra, doubled));
  } catch (ArithmeticException e) {
    throw new IllegalArgumentException("fare too large", e);
  }
}
```

## Your task

Open `src/main/java/edu/practice/d03/StorageBilling.java` and write the three methods:

1. **`blocksFor(gb)`.** Count the 100 GB blocks, rounding up. Reject `gb <= 0`. It must work for `Integer.MAX_VALUE`.
2. **`halfRoundedUp(cents)`.** Halve and round up (999 → 500). Reject `cents <= 0`.
3. **`priceCents(gb, centsPerBlock)`.** The first **10** blocks cost the full rate. Every block after that costs
   `halfRoundedUp(rate)`. Reject a bad rate. Overflow must throw `IllegalArgumentException`.

Run `StorageBillingTest`.

## Hints

<details><summary>Hint 1: blocksFor(2147483647) gives a negative number</summary>

You used `(gb + 99) / 100`, and `gb + 99` overflowed. Use `(gb - 1) / 100 + 1`.
</details>

<details><summary>Hint 2: splitting into full-price and half-price blocks</summary>

```java
int full = Math.min(blocks, FULL_PRICE_BLOCKS);   // at most 10
int rest = blocks - full;                         // 0 when blocks <= 10
```

</details>

<details><summary>Hint 3: the overflow test still fails</summary>

Every `*` and `+` in `priceCents` must go through `Math.multiplyExact` / `Math.addExact`, inside one `try`, with
`catch (ArithmeticException e) { throw new IllegalArgumentException(...); }`.
</details>

<details><summary>Hint 4: halfRoundedUp</summary>

It's the same ceiling formula with `b = 2`: `(cents - 1) / 2 + 1`.
</details>

## Viva questions

<details><summary>Why not use <code>Math.ceil(gb / 100.0)</code>?</summary>

It converts to `double` and back. That works for small numbers but hides rounding issues, and it still needs a cast.
Integer ceiling division is exact, and it's what the papers expect.
</details>

<details><summary>What happens if you don't use <code>multiplyExact</code>?</summary>

A big result wraps around to a negative number with no error, and the API would return a nonsense price. `multiplyExact`
turns that into an exception, which we map to HTTP 400.
</details>

<details><summary>Why convert ArithmeticException to IllegalArgumentException?</summary>

From the caller's point of view, the *input* was unacceptable, so it's a client error. The papers map
`IllegalArgumentException` to **400 Bad Request** in the exception handler.
</details>

<details><summary>Why is <code>(a + b - 1) / b</code> risky?</summary>

`a + b - 1` can exceed `Integer.MAX_VALUE` before the division happens. `(a - 1) / b + 1` never goes above `a`.
</details>
