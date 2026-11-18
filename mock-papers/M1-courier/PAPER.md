---
id: m1
module: OOP
kind: mock
title: "M1 · Mock · Courier Parcel Pricing & Dispatch API"
difficulty: 3
timeMinutes: 100
tasks:
  - { name: "Task 1 – Complete StandardParcel", marks: 18 }
  - { name: "Task 2 – Create ExpressParcel and plan the van load", marks: 22 }
  - { name: "Task 3 – Service lookup, validation and HTTP errors", marks: 20 }
  - { name: "Task 4 – Expose three REST operations", marks: 20 }
  - { name: "Section B – Six functional checks", marks: 20 }
---

# 5COSC019W | OBJECT ORIENTED PROGRAMMING

## MOCK PRACTICE EXAM M1

**100-minute mock practical | Courier Parcel Pricing & Dispatch API**

**STUDENT PRACTICE PAPER • Entirely fictional. Written for this practice pack; NOT part of the live assessment and NOT a
prediction of it.**

## Exam instructions

- Individual timed practice: 100 minutes; maximum 100 marks. Challenge classification: between Simple and Moderate. Two
  pricing rules with ceiling rounding, a polymorphic sort-and-load routine, validation with three error codes, and four
  REST operations.
- Use the starter project in this folder (`mock-papers/M1-courier`). Java 17 and the Maven Spring Boot project are
  required. Do not replace the parent class, DTO contracts or repository API unless a task explicitly tells you to.
- Sit it like the real thing: no notes, no lessons, no solutions while the timer runs.
- Save often. Keep the project compiling: a project that doesn't compile can't pass Section B.
- Section A = 80 marks across four programming tasks (18/22/20/20). Section B = 20 marks across six functional checks.
  Attempt every task, even if some parts don't work.

## Scenario

A same-day courier prices parcels and loads its delivery van. A **StandardParcel** pays a rate for every started 500 g
block. An **ExpressParcel** pays a base fee plus a charge for every started 250 g block, and more if a signature is
required. Each parcel kind also has a dispatch priority. The van has a limited weight capacity, and a deterministic
greedy routine decides which parcels go on it. All prices are integer **pence**, and all weights are integer **grams**.
No external database is needed.

## Seed data

The repository already holds `StandardParcel(1, "CP-1001", 1200, 180)` and `StandardParcel(2, "CP-1002", 500, 250)`.

Once you finish Task 2, add `ExpressParcel(3, "CP-1003", 900, 400, true)` and
`ExpressParcel(4, "CP-1004", 300, 350, false)` at the TODO in `ParcelRepository`.

## Files supplied

The starter (package `edu.practice.courier`) contains:

| Package      | Files                                                                                                                                                                                                                                              |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| (root)       | `CourierApplication` (port 8084)                                                                                                                                                                                                                   |
| `model`      | `Parcel` (abstract parent: `id`, `trackingCode`, `weightGrams`, abstract `pricePence()` and `priority()`), `StandardParcel` (incomplete)                                                                                                           |
| `dto`        | `ApiError(code, message)`, `Quote(id, trackingCode, weightGrams, priority, pricePence)`, `NewStandardParcel(id, trackingCode, weightGrams, ratePerBlockPence)`, `VanLoad(capacityGrams, weightUsed, weightRemaining, totalPence, loaded, skipped)` |
| `repo`       | `ParcelRepository`, an in-memory `ConcurrentHashMap` with a seed TODO                                                                                                                                                                              |
| `service`    | `ParcelService` with TODO methods `quote`, `addStandard`, `loadVan`                                                                                                                                                                                |
| `controller` | `ParcelController` with `GET /api/parcels` and a TODO for the rest                                                                                                                                                                                 |
| `error`      | `ParcelNotFoundException`, `DuplicateParcelException`, `ApiExceptionHandler` (TODO)                                                                                                                                                                |

Implement the stated TODOs. Create the second concrete model class, `ExpressParcel`, yourself. Use the repository seed
comment to add the sample objects after creating it.

## Assessment and marking

| Component                                  | Marks   | Assessed outcome             |
|--------------------------------------------|---------|------------------------------|
| Task 1 – First subclass                    | 18      | LO1 – OOP principles         |
| Task 2 – New subclass + object processing  | 22      | LO1 – OOP principles         |
| Task 3 – Service implementation and errors | 20      | LO2 – API implementation     |
| Task 4 – Controller/API integration        | 20      | LO2 – API implementation     |
| Section B – Functional testing             | 20      | LO3 – Running implementation |
| **Total**                                  | **100** |                              |

## Task 1 – Complete StandardParcel [18 marks, LO1]

- **4 marks:** keep `StandardParcel extends Parcel`. Add a `private final int ratePerBlockPence` field and store it in
  the constructor, which already chains to `super(id, trackingCode, weightGrams)`. Expose it through a public
  `getRatePerBlockPence()`.
- **6 marks:** `pricePence()` = `ratePerBlockPence` × the number of **started** 500 g blocks (ceiling division). 1–500 g
  is one block, and 501–1,000 g is two. The rounding must stay correct for any positive `int` weight, including weights
  close to `Integer.MAX_VALUE`.
- **2 marks:** `priority()` returns 1 for every standard parcel.
- **6 marks:** the constructor rejects `weightGrams ≤ 0` or `ratePerBlockPence ≤ 0` with `IllegalArgumentException`.
  `pricePence()` uses `long` intermediate arithmetic and throws `IllegalArgumentException` if the price does not fit in
  an `int`.

## Task 2 – Create ExpressParcel and plan the van load [22 marks, LO1]

- **7 marks:** create `ExpressParcel extends Parcel` in `edu.practice.courier.model`, with a public constructor
  `(long id, String trackingCode, int weightGrams, int basePence, boolean signatureRequired)`, `private final` fields
  `basePence` and `signatureRequired`, and getters `getBasePence()` and `isSignatureRequired()`.
- **7 marks:** `pricePence()` = `basePence` + 90 for every started 250 g block, + 300 if `signatureRequired`.
  `priority()` = 3 if a signature is required, otherwise 2. Reject `weightGrams ≤ 0`, `basePence < 0`, and a price above
  `Integer.MAX_VALUE` with `IllegalArgumentException`.
- **8 marks:** implement `ParcelService.loadVan(capacityGrams)`:
    - build a `Quote` for every parcel, calling `pricePence()` and `priority()` through `Parcel` references
    - sort by priority DESC, then `pricePence` ASC, then ID ASC
    - walk that order and greedily load every parcel whose `weightGrams` fits in the remaining capacity. Keep going
      after a parcel doesn't fit, because a lighter one later may still fit.
    - record the IDs that did not fit in `skipped`, in the order they were considered
    - return `VanLoad(capacityGrams, weightUsed, weightRemaining, totalPence, loaded, skipped)`, where `totalPence` is
      the sum of the loaded prices.

**Do not use `instanceof`, `getClass()` or any other concrete subclass check in `ParcelService`.** Add the two
ExpressParcel seed records to `ParcelRepository` once your class exists.

## Task 3 – Service lookup, validation and HTTP errors [20 marks, LO2]

- **4 marks:** `quote(id)` returns `Quote(id, trackingCode, weightGrams, priority, pricePence)` for the selected parcel.
  An unknown ID raises `ParcelNotFoundException`.
- **6 marks:** `addStandard(NewStandardParcel)` validates:
    - a positive ID
    - a non-blank tracking code, trimmed before storing
    - `weightGrams` from 1 to 30,000
    - `ratePerBlockPence` from 1 to 10,000 (both ranges inclusive)

  Invalid input raises `IllegalArgumentException` and nothing is saved. Valid input is saved, and the new
  `StandardParcel` is returned.
- **4 marks:** once the fields are valid, a duplicate ID or a tracking code that already exists (compare the trimmed
  code, ignoring case) raises `DuplicateParcelException`. The existing parcel must not be replaced.
- **6 marks:** `loadVan` rejects `capacityGrams ≤ 0` with `IllegalArgumentException`. `ApiExceptionHandler` maps:
    - `IllegalArgumentException` → HTTP 400 with `ApiError("BAD_REQUEST", message)`
    - `ParcelNotFoundException` → HTTP 404 with `ApiError("NOT_FOUND", message)`
    - `DuplicateParcelException` → HTTP 409 with `ApiError("CONFLICT", message)`

## Task 4 – Expose three REST operations [20 marks, LO2]

- **5 marks:** `GET /api/parcels/{id}/quote` returns the `Quote` for that parcel (`@PathVariable`).
- **6 marks:** `POST /api/parcels/standard` accepts a `NewStandardParcel` JSON body (`@RequestBody`) and returns **HTTP
  201 Created** with the created parcel.
- **6 marks:** `GET /api/parcels/van?capacityGrams=2000` returns the `VanLoad`. `capacityGrams` is a `@RequestParam`that
  **defaults to 2000** when it is omitted.
- **3 marks:** every handler only delegates to `ParcelService`. No pricing, sorting, validation or repository access in
  the controller.

## Illustrative calculations and input

- **StandardParcel 1:** 1,200 g = 3 started blocks × 180 p = **540 p**. **StandardParcel 2:** exactly 500 g = 1 block ×
  250 p = **250 p**. At 501 g it would be 2 blocks = 500 p.
- **ExpressParcel 3:** 900 g = 4 started 250 g blocks, so 400 + 4 × 90 + 300 = **1,060 p**, priority 3. **ExpressParcel
  4:** 300 g = 2 blocks, so 350 + 2 × 90 = **530 p**, priority 2.
- **loadVan (2000):** sorted order is [3, 4, 2, 1]. Parcels 2 and 1 tie on priority 1, so the cheaper parcel 2 comes
  first. Load 3 (900 g, 1,100 g left), then 4 (300 g, 800 g left), then 2 (500 g, 300 g left). Skip 1, because 1,200 g
  doesn't fit. The result is weightUsed 1,700, weightRemaining 300, totalPence 1,840, skipped [1].
- **POST** parcel 5, `CP-1005`, 2,600 g at 150 p: 6 started blocks × 150 = **900 p**.

Expected shape of `GET /api/parcels/van?capacityGrams=2000`:

```json
{
  "capacityGrams": 2000,
  "weightUsed": 1700,
  "weightRemaining": 300,
  "totalPence": 1840,
  "loaded": [
    {
      "id": 3,
      "trackingCode": "CP-1003",
      "weightGrams": 900,
      "priority": 3,
      "pricePence": 1060
    },
    {
      "id": 4,
      "trackingCode": "CP-1004",
      "weightGrams": 300,
      "priority": 2,
      "pricePence": 530
    },
    {
      "id": 2,
      "trackingCode": "CP-1002",
      "weightGrams": 500,
      "priority": 1,
      "pricePence": 250
    }
  ],
  "skipped": [
    1
  ]
}
```

## REST operations to demonstrate

```
GET  /api/parcels
GET  /api/parcels/1/quote
GET  /api/parcels/3/quote
GET  /api/parcels/van?capacityGrams=2000
GET  /api/parcels/van
POST /api/parcels/standard   JSON {"id":5,"trackingCode":"CP-1005","weightGrams":2600,"ratePerBlockPence":150}
```

`requests.http` in this folder has these and the error cases ready to run (IntelliJ Ultimate). With Community, use curl,
for example:

```
curl -i http://localhost:8084/api/parcels/van?capacityGrams=2000
curl -i -X POST http://localhost:8084/api/parcels/standard -H "Content-Type: application/json" -d "{\"id\":5,\"trackingCode\":\"CP-1005\",\"weightGrams\":2600,\"ratePerBlockPence\":150}"
```

## Section B – Six functional checks [20 marks, LO3]

Each functional check is scored when the running application shows the required output or behaviour. The checks are
independent of Section A source-code credit.

1. **3** – StandardParcel 1 (1,200 g at 180 p per block) quotes 540 pence with priority 1.
2. **3** – ExpressParcel 3 (900 g, base 400 p, signature required) quotes 1,060 pence with priority 3.
3. **3** – At `capacityGrams=2000` the van loads [3, 4, 2], skips [1], uses 1,700 g and totals 1,840 pence.
4. **3** – Without `capacityGrams` the van plan uses 2000, and repeated calls return identical results.
5. **4** – An unknown ID returns 404 `NOT_FOUND`. A bad capacity or an invalid POST returns 400 `BAD_REQUEST`. A
   duplicate ID or tracking code returns 409 `CONFLICT`. Nothing invalid is stored.
6. **4** – A valid POST returns 201, and the new parcel is quoted and loaded in later requests.

## Submission and final checks

- Start the application and exercise both normal and invalid cases using curl, Postman or the IntelliJ HTTP Client.
- In the real LBP you zip the complete Maven project folder (including `pom.xml`, sources, resources and any test
  files). Do not submit only `.java` files.
- Before submission, confirm that your ZIP contains the most recent source files and can be extracted successfully.

## How to sit and mark this mock

1. Set a **100-minute timer**. Work only in `mock-papers/M1-courier`. Use no hints, lessons or solutions until the timer
   ends.
2. When time is up, stop typing and mark it from the repo root:

   ```
   java marking/Mark.java m1 mock-papers/M1-courier
   ```

   On Windows: `marking\mark.cmd m1 mock-papers\M1-courier`. The JDK must be 17. If your PATH default is another JDK,
   set `JAVA_HOME` to `C:\Program Files\Java\jdk-17` first.
3. The runner copies the marking tests into `src/test/java/marking`, runs them, and prints a mark per line, per task and
   out of 100. Read every `[ ]` line, then fix and re-mark if you want to practise the gaps.
4. Compare with the model solution: `git diff main solutions -- mock-papers/M1-courier`.
5. Practise the viva: explain out loud why `pricePence()` is abstract in `Parcel`, how your sort comparator works, and
   why the controller holds no logic.

**This is a fictional practice paper written for this pack. It is not the real exam and does not predict its content.
The score is an estimate from the rules stated above. Staff marking and the viva can differ.**
