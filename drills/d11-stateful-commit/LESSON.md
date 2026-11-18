---
id: d11
module: OOP
kind: drill
title: Preview, commit and idempotent retries
difficulty: 5
timeMinutes: 25
tasks:
  - { name: "overlaps(): half-open intervals", marks: 2 }
  - { name: "bookings(): deterministic order (room, start, id)", marks: 1 }
  - { name: "preview(): accept or reject without changing state", marks: 3 }
  - { name: "commit(): a new key saves the accepted bookings", marks: 1 }
  - { name: "commit(): same key + same request returns the stored result", marks: 2 }
  - { name: "commit(): same key + different request -> 409 CONFLICT", marks: 1 }
---

# d11 · Preview, commit and idempotent retries

> **Exam link:** this is the hard end of LBP **Tasks 3–4**, at P3 level. P3: *"Each reservation uses a half-open
interval [startTick,endTickExclusive)"*, *"Preview must not mutate confirmed state"*, and *"identical retry (same
runKey, same request) must return the identical original plan without duplicating reservations; same runKey with changed
values must produce HTTP 409 / ApiError (code="CONFLICT")"* (7 marks). Its Section B checks *"Preview makes no
persistent reservations, even after repeated calls"* (3) and *"Commit stores once; identical retry returns original plan
and never double-books"* (4). This is a stretch drill: do d07–d09 first.

## The idea

Three ideas, one at a time.

### 1. Half-open intervals: `[start, end)`

```java
// [9,11) means from 9:00 up to, but NOT including, 11:00. It covers hours 9 and 10.
boolean overlaps = aStart < bEnd && bStart < aEnd;
```

```
hour:     8    9    10   11   12   13
[9,11)         |=========|
[11,12)                  |====|          touch at 11: NO overlap  -> back-to-back meetings are fine
[10,12)             |=========|          share hour 10: overlap
[8,9)     |====|                         touch at 9: NO overlap
```

Read the formula as: *"A starts before B ends, **and** B starts before A ends."* Both must be true. Use `<`, never `<=`.

Why do the papers use half-open intervals? Because they make off-by-one mistakes much harder:

- **Length is just `end - start`.** `[9,11)` is 2 hours. With closed `[9,10]` it would be `end - start + 1`, and that
  `+ 1` gets forgotten.
- **Back-to-back intervals share an endpoint without clashing.** The 9–11 meeting ends at 11, and the next one starts at
  11.
- **A day splits cleanly.** `[8,13)` + `[13,18)` is exactly `[8,18)`: no gap, and no hour counted twice.

| Wrong version                       | Why it's wrong                                                    |
|-------------------------------------|-------------------------------------------------------------------|
| `aStart <= bEnd && bStart <= aEnd`  | says `[9,11)` and `[11,12)` clash, but they only touch            |
| `aStart < bEnd` on its own          | says `[8,9)` and `[17,18)` clash, because it only checks one side |
| `aStart >= bStart && aStart < bEnd` | misses a long A that starts **before** B and covers it            |

And remember: two bookings can only clash if they are in the **same room**.

### 2. Preview vs commit

A **preview** answers *"what would happen if I committed this now?"* and **changes nothing**. It's like the basket total
you see before you press Pay. A **commit** makes the same decisions and then saves them.

The easy way to get them to agree is to make commit **call** preview, then save what preview accepted. Then they can't
disagree.

Inside one preview, a request must also be checked against the requests already **accepted earlier in the same
preview**. Two people asking for ASPEN 12–13 in one batch can't both get it. Keep those in a **new local list**:

```java
List<BookingRequest> accepted = new ArrayList<>();   // local: it disappears when preview returns
```

**Trap 1: saving to "remember".** Calling `repo.save(...)` inside preview to remember an accepted request makes it a
real booking. The next preview, or a `GET /bookings`, will see it. Section B calls preview twice and checks that nothing
changed.

**Trap 2: a shared list.** `=` copies the *reference*, not the list:

```java
List<Booking> taken = confirmedList;          // NOT a copy: two names for the SAME list
taken.

add(newOne);                            // ...so the confirmed list just changed too

List<Booking> taken = new ArrayList<>(confirmedList);   // a real copy: changing it is safe
```

An `int` is different: `int free = repo.freeCount(); free -= 2;` changes only the local copy. Lists and other objects
are shared, so copy them before changing them. (Here `repo.all()` already returns a read-only copy, so `.add(...)` on it
would throw. That's on purpose.)

**All or nothing.** A commit validates and decides **everything first**, then saves. If request 3 of 3 is invalid,
requests 1 and 2 must not be saved either. `synchronized` on preview and commit means two requests arriving at the same
moment can't both pass the check, then both save.

### 3. Idempotency: the same request twice has the effect of once

You press **Pay**, the page hangs, and you press it again. Or your phone loses signal, and the app quietly retries. The
server receives **two** identical requests. Without protection, you pay twice.

The fix is an **idempotency key**. The client makes up a unique string once per action (`"pay-7f3a"`) and sends it with
the first try **and with every retry**. The server remembers each key it has seen:

| The server sees…                     | It does…                                                                      | Status           |
|--------------------------------------|-------------------------------------------------------------------------------|------------------|
| a **new** key                        | the work, then stores *request + result* under the key                        | 200              |
| a used key + an **equal** request    | returns the **stored** result. Does nothing again, and decides nothing again. | 200              |
| a used key + a **different** request | refuses: the client reused a key for something else                           | **409 CONFLICT** |

```java
private final Map<String, CommitRequest> requestsByKey = new HashMap<>();
private final Map<String, CommitResult> resultsByKey = new HashMap<>();
```

- **Why return the stored result instead of running it again?** If you ran the commit again, its bookings would now
  clash with *themselves*, and the retry would answer "all rejected". The client asked the same question, so it must get
  the same answer.
- **Why `equals` and not `==`?** Every HTTP request is parsed into a **new** object, so `==` is always false. A record's
  `equals` compares every component, and `List.equals` compares element by element. So two requests with the same values
  are equal, even if the JSON had different spacing or field order.
- **Why 409 and not 400?** The request itself is valid. It **conflicts** with what the server already stored under that
  key.

## Worked example

This one is a different desk: **laptop loans**. A request asks for some laptops. Earlier accepted requests in the same
batch use laptops up. It has the same three parts: a preview with a local working count, a commit that calls the
preview, and an idempotency key.

```java
public record LoanRequest(String student, int laptops) {
}

public record LoanCommit(String key, List<LoanRequest> requests) {
}

public record LoanPlan(List<LoanRequest> accepted, List<LoanRequest> refused) {
}

@Service
public class LoanService {
  private final LaptopRepository repo;
  private final Map<String, LoanCommit> commitsByKey = new HashMap<>();
  private final Map<String, LoanPlan> plansByKey = new HashMap<>();

  public LoanService(LaptopRepository repo) {
    this.repo = repo;
  }

  public synchronized LoanPlan preview(List<LoanRequest> requests) {
    int free = repo.freeLaptops();                       // READ the stored state; `free` is a local copy
    List<LoanRequest> accepted = new ArrayList<>();      // NEW local lists
    List<LoanRequest> refused = new ArrayList<>();
    for (LoanRequest r : requests) {
      if (r.laptops() <= free) {
        accepted.add(r);
        free -= r.laptops();                         // later requests see what earlier ones used
      } else {
        refused.add(r);                              // refused: uses nothing up
      }
    }
    return new LoanPlan(List.copyOf(accepted), List.copyOf(refused));
  }

  public synchronized LoanPlan commit(LoanCommit request) {
    LoanCommit earlier = commitsByKey.get(request.key());
    if (earlier != null) {
      if (earlier.equals(request)) return plansByKey.get(request.key());   // a retry: the SAME answer
      throw new KeyConflictException(request.key());                        // key reused: 409
    }
    LoanPlan plan = preview(request.requests());         // decide with exactly the preview's logic
    for (LoanRequest r : plan.accepted()) {
      repo.lend(r.student(), r.laptops());             // only NOW change the stored state
    }
    commitsByKey.put(request.key(), request);            // remember the question...
    plansByKey.put(request.key(), plan);                 // ...and the answer
    return plan;
  }
}
```

Trace it with 5 free laptops and the batch `[Ana 2, Ben 4, Cai 3]`:

| Call                              | Answer                                                     | Free laptops afterwards              |
|-----------------------------------|------------------------------------------------------------|--------------------------------------|
| `preview`                         | Ana ✔ (3 left), Ben ✘ (4 > 3), Cai ✔ (0 left)           | 5, because a preview changes nothing |
| `preview` again                   | the same plan                                              | 5                                    |
| `commit("k-7", same batch)`       | the same plan                                              | 0                                    |
| `commit("k-7", same batch)` again | the **stored** plan. Re-running would now refuse everyone! | 0                                    |
| `commit("k-7", [Ana 1])`          | **409**: k-7 belongs to a different request                | 0                                    |

Ben was refused, so he used nothing up. That's why Cai still fits.

## Your task

The meeting-room API in `src/main/java/edu/practice/d11` is finished except for **four methods in
`service/BookingService.java`**. The model, DTO records, repository, controller and exception handler are given. So are
the service's `validate(...)` helper and its two idempotency maps.

> **Meeting-room bookings.** Rooms `ASPEN`, `BIRCH` and `CEDAR`, open 8:00–18:00. Each booking is a half-open interval
> `[start, end)` in whole hours, with `8 <= start < end <= 18`. Confirmed bookings when the app starts:
>
> | id | room | interval |
> |---|---|---|
> | 1 | BIRCH | [13,15) |
> | 2 | ASPEN | [14,16) |
> | 3 | ASPEN | [9,11) |
> | 4 | CEDAR | [10,12) |
>
> **REST operations.**
> - `GET /api/rooms/bookings`: every confirmed booking, ordered by room A→Z, then start, then id.
> - `POST /api/rooms/preview` with `{"bookings":[{"room":"ASPEN","start":11,"end":12}, ...]}` returns
    `{"accepted":[...],"rejected":[{"room":..,"start":..,"end":..,"reason":..}]}`. It handles requests in the order
    given. A request overlapping a confirmed booking in its room is rejected with `CLASHES_WITH_CONFIRMED` (checked
    first). One overlapping a request **accepted earlier in the same preview** is rejected with
    `CLASHES_WITH_EARLIER_REQUEST`. Anything else is accepted. **Saves nothing.**
> - `POST /api/rooms/commit` with `{"key":"team-sync-1","bookings":[...]}` returns
    `{"key":..,"booked":[{"id":5,"room":..,"start":..,"end":..}],"rejected":[...]}`. With a new key, it saves the
    accepted bookings with ids from `repo.nextId()` (5, 6, …) in request order. The same key and an equal request get
    the **stored** result back, and nothing new is saved. The same key with a different request → **409** `CONFLICT`.
> - Bad hours, an empty list or a blank key → **400** `BAD_REQUEST`. An unknown room → **404** `NOT_FOUND`
    (`Room not found: ZEUS`). Either way, nothing is saved.
>
> **Illustrative values.** Preview `[ASPEN 11–12, ASPEN 10–12, BIRCH 11–13, BIRCH 12–13]`:
> - ASPEN `[11,12)` is accepted (it only touches `[9,11)`).
> - ASPEN `[10,12)` is rejected, `CLASHES_WITH_CONFIRMED`.
> - BIRCH `[11,13)` is accepted (it only touches `[13,15)`).
> - BIRCH `[12,13)` is rejected, `CLASHES_WITH_EARLIER_REQUEST`.
>
> Committing that same batch under key `team-sync-1` books ids **5** (ASPEN 11–12) and **6** (BIRCH 11–13). Then
> `GET /api/rooms/bookings` lists ids **3, 5, 2, 6, 1, 4**.

Write the four methods, in this order. The TODOs in the file spell out each step.

1. **d11-1** `overlaps(aStart, aEnd, bStart, bEnd)`: one line.
2. **d11-2** `bookings()`: a Comparator chain (d05).
3. **d11-3** `preview(requests)`: `validate`, then the loop with two local lists.
4. **d11-4** `commit(request)`: the key check, then `preview`, then save and store.

Run `BookingServiceTest` first (12 tests, no Spring, fast, precise messages), then `RoomBookingApiTest` (8 Section B
style checks over HTTP). Work until all 20 are green.

**Then try it for real.** Run `MeetingRoomsApplication`. Save this as `standup.json`:

```json
{
  "key": "standup-1",
  "bookings": [
    {
      "room": "ASPEN",
      "start": 11,
      "end": 12
    },
    {
      "room": "CEDAR",
      "start": 12,
      "end": 13
    }
  ]
}
```

```bash
curl -i -X POST http://localhost:8080/api/rooms/commit -H "Content-Type: application/json" --data "@standup.json"
curl -i -X POST http://localhost:8080/api/rooms/commit -H "Content-Type: application/json" --data "@standup.json"
curl -i http://localhost:8080/api/rooms/bookings
```

(In PowerShell, type `curl.exe`.) Both commits should print the **same** body, and the bookings list should have 6
entries, not 8. Now change `"end":13` to `"end":14` in the file and commit again: `HTTP/1.1 409`.

## Hints

<details><summary>Hint 1: overlaps and bookings (start here)</summary>

`overlaps` is the formula from **The idea**: `return aStart < bEnd && bStart < aEnd;`. Tests 1 and 2 should go green.

For `bookings()`, chain three keys:
`Comparator.comparing(Booking::room).thenComparingInt(Booking::start).thenComparingLong(Booking::id)`. Use it in
`repo.all().stream().sorted(...).toList()`.
</details>

<details><summary>Hint 2: the shape of preview</summary>

```java
validate(requests);

List<Booking> confirmed = repo.all();
List<BookingRequest> accepted = new ArrayList<>();
List<Rejection> rejected = new ArrayList<>();
for(
BookingRequest r :requests){
boolean hitsConfirmed = confirmed.stream()
  .anyMatch(b -> b.room().equals(r.room()) && overlaps(r.start(), r.end(), b.start(), b.end()));
// hitsEarlier: the same idea, but over `accepted`
// if / else if / else: add to rejected (with the right reason) or to accepted
}
  return new

Plan(accepted, rejected);
```

Compare rooms with `.equals`, not `==`.
</details>

<details><summary>Hint 3: "the same preview twice must give the same answer" / "preview must not even use up booking ids"</summary>

Your preview changes state. Look for `repo.save(...)` or `repo.nextId()` inside `preview`, or a list you got from
somewhere else and then `.add(...)`-ed to. Preview may only **read** the repository. Remembering accepted requests is
the job of the local `accepted` list.
</details>

<details><summary>Hint 4: "ASPEN [10,12) is rejected, so it must NOT block ASPEN [11,12)"</summary>

Only **accepted** requests go into `accepted`. A rejected request goes into `rejected` and nowhere else, so it can't
block later ones. Also check the order of your `if`s: confirmed first, then earlier requests.
</details>

<details><summary>Hint 5: "a retry must get exactly the first result back"</summary>

Three usual causes:

- You don't **return** the stored result, so the retry runs preview again, and now everything clashes with itself.
- You compare requests with `==`. Use `earlier.equals(request)`.
- You stored the key before validating. Store the request and result **after** the bookings are saved, so a failed
  commit leaves the key unused.

</details>

<details><summary>Hint 6: commit (nearly the answer)</summary>

```java
if(request ==null||request.

key() ==null||request.

key().

isBlank())throw new

IllegalArgumentException("key is required");

CommitRequest earlier = requestsByKey.get(request.key());
if(earlier !=null){
  if(earlier.

equals(request))return resultsByKey.

get(request.key());
  throw new

CommitConflictException(request.key());
  }
Plan plan = preview(request.bookings());
List<Booking> booked = new ArrayList<>();
for(
BookingRequest r :plan.

accepted())booked.

add(new Booking(repo.nextId(),r.

room(),r.

start(),r.

end()));
  booked.

forEach(repo::save);

CommitResult result = new CommitResult(request.key(), List.copyOf(booked), plan.rejected());
requestsByKey.

put(request.key(),request);
  resultsByKey.

put(request.key(),result);
  return result;
```

</details>

## Viva questions

<details><summary>Why do you store intervals as half-open <code>[start, end)</code>?</summary>

The length is just `end - start`. Back-to-back intervals like `[9,11)` and `[11,12)` touch without overlapping. And a
range splits into pieces with no gap and no double counting. The overlap test is then one line,
`aStart < bEnd && bStart < aEnd`, with no `+ 1` or `<=` to get wrong.
</details>

<details><summary>What does idempotent mean, and why does a commit endpoint need it?</summary>

Sending the same request once or many times has the same effect as sending it once. Networks retry, and users
double-click, so a server can get the same commit twice. With an idempotency key, the server spots the retry and returns
the first result instead of booking (or charging) twice.
</details>

<details><summary>Why must preview not change state, and how did you make sure it doesn't?</summary>

A preview is a "what if". Users and checkers can call it many times, and only a commit should reserve anything. My
preview only reads `repo.all()`. It keeps the accepted requests in a new local list, which disappears when the method
returns, and it never calls `save` or `nextId`. The test calls preview twice and checks that the bookings are unchanged.
</details>

<details><summary>On a retry, why return the stored result instead of running the commit again?</summary>

Running it again would give a different answer, because the first commit's bookings are now confirmed and would clash
with themselves. The client asked the same question, so it must get the same answer. I store the request and the result
in two maps keyed by the idempotency key, and compare requests with the record's `equals`.
</details>

<details><summary>Why 409 for a reused key with a different request, and not 400?</summary>

400 means the request itself is invalid. Here the request is fine on its own, but it conflicts with what the server
already stored under that key. That's what 409 Conflict means. The server refuses rather than guess which request the
client meant.
</details>
