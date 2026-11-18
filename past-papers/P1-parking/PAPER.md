---
id: p1
module: OOP
kind: past-paper
title: "P1 · Simple · Parking Access & Fee Quotation API"
difficulty: 2
timeMinutes: 100
tasks:
  - { name: "Task 1 – Complete HourlyPass", marks: 18 }
  - { name: "Task 2 – Create WeekendPass and polymorphic quotes", marks: 22 }
  - { name: "Task 3 – Service validation and HTTP errors", marks: 20 }
  - { name: "Task 4 – Complete REST endpoints", marks: 20 }
  - { name: "Section B – Six functional checks", marks: 20 }
---

# 5COSC019W | Object Oriented Programming — Simple Practice Exam

**100-minute sample practical | Parking Access & Fee Quotation API**

*Student practice paper • Entirely fictional example; NOT part of the live assessment*

## Exam instructions

- Individual timed practice: 100 minutes; maximum 100 marks. Challenge classification: **Introductory**: direct rules,
  limited state and straightforward HTTP operations.
- Use the corresponding starter ZIP. Java 17 and the Maven Spring Boot project are required. Do not replace the parent
  class, DTO contracts or repository API unless a task explicitly instructs you to.
- This sample allows you to consult learning material while studying. The official examination may impose different
  restrictions: follow the approved Blackboard rules for the real sitting.
- Write and test your own solution before viewing the staff reference. Save frequently and submit a complete project ZIP
  in practice.
- Section A = 80 marks across four programming tasks (18/22/20/20); Section B = 20 marks across six functional checks.
  Attempt all tasks even if some parts do not compile.

## Scenario

A car park issues an `HourlyPass` and a `WeekendPass`. Each pass computes its charge using a different rule. Your REST
API supplies quotations and supports registering new hourly passes.

Seed after completing Task 2: `HourlyPass(1, "CAR-101", 250)` and `WeekendPass(2, "VAN-202", 800)`. All prices are
integer cents; no external database is required.

## Files supplied

The starter includes a parent model class, one incomplete concrete model, in-memory repository, DTOs, a service class, a
Spring REST controller and API error-handler scaffolding. Implement the stated TODOs; create the second concrete model
class yourself. Use the repository seed comment to add sample objects after creating the new class.

## Assessment and marking

| Component                                  |   Marks | Assessed outcome             |
|--------------------------------------------|--------:|------------------------------|
| Task 1 – First subclass                    |      18 | LO1 – OOP principles         |
| Task 2 – New subclass + object processing  |      22 | LO1 – OOP principles         |
| Task 3 – Service implementation and errors |      20 | LO2 – API implementation     |
| Task 4 – Controller/API integration        |      20 | LO2 – API implementation     |
| Section B – Functional testing             |      20 | LO3 – Running implementation |
| **Total**                                  | **100** |                              |

## Task 1 – Complete HourlyPass [18 marks, LO1]

- **4 marks:** preserve inheritance from `ParkingPass`, final hourly rate field, constructor chaining and public getter.
- **6 marks:** implement `priceCents(stayMinutes)` using ceiling division into billable hours: 1–60 minutes is one hour,
  61–120 is two.
- **4 marks:** multiply the rounded hour count by `rateCents`, returning an integer amount in cents.
- **4 marks:** reject zero or negative `stayMinutes` with `IllegalArgumentException` and prevent arithmetic overflow.

## Task 2 – Create WeekendPass and polymorphic quotes [22 marks, LO1]

- **6 marks:** implement a new `WeekendPass extends ParkingPass` with `(long id, String registration, int flatCents)`
  and `getFlatCents()`.
- **8 marks:** `priceCents(stayMinutes)` = `flatCents` for 1–480 minutes; above 480, add 150 cents for each commenced
  additional hour; reject non-positive stays.
- **8 marks:** implement `ParkingService.allQuotes(minutes)`. Invoke `priceCents` through `ParkingPass` references;
  return quotes sorted by `priceCents` ASC, then ID ASC. Do not use `instanceof` or branch on concrete pass types.

Uncomment the `WeekendPass` sample record in `PassRepository` once your class exists.

## Task 3 – Service validation and HTTP errors [20 marks, LO2]

- **4 marks:** `ParkingService.quote(id, minutes)` must validate positive minutes and calculate the selected pass
  charge.
- **6 marks:** missing IDs must raise `PassNotFoundException`; map this to HTTP 404 with
  `ApiError(code="NOT_FOUND", message=...)`.
- **6 marks:** `addHourly` must validate positive unique ID, nonblank registration and positive hourly rate; trim
  registration before storing.
- **4 marks:** map `IllegalArgumentException` to HTTP 400 and `ApiError(code="BAD_REQUEST", message=...)`.

## Task 4 – Complete REST endpoints [20 marks, LO2]

- **5 marks:** `GET /api/passes/{id}/quote?minutes=61` returns a `Quote` object.
- **5 marks:** `GET /api/passes/quotes?minutes=61` returns sorted quotes for all passes.
- **5 marks:** `POST /api/passes/hourly` accepts `{"id":3,"registration":"CAR-303","rateCents":300}` and returns the
  created pass.
- **5 marks:** POST returns HTTP 201; all handlers delegate business decisions to service methods, with the correct
  Spring annotations.

## Illustrative calculations and input

- Hourly pass: 60 minutes → 250 cents; 61 minutes → 500 cents.
- Weekend pass: 480 minutes → 800 cents; 481 minutes → 950 cents.
- At 481 minutes, WeekendPass 2 costs 950 cents and HourlyPass 1 costs 2,250 cents; WeekendPass appears first in quotes.

## REST operations to demonstrate

```
GET  /api/passes
GET  /api/passes/1/quote?minutes=61
GET  /api/passes/quotes?minutes=481
POST /api/passes/hourly   JSON {"id":3,"registration":"CAR-303","rateCents":300}
```

## Section B – Six functional checks [20 marks, LO3]

Each functional check is scored when the running application demonstrates the required output/behaviour. The checks are
independent of Section A source-code credit.

| Marks | Check                                                                               |
|------:|-------------------------------------------------------------------------------------|
|     3 | An hourly stay of 61 minutes at 250 cents/hour produces 500 cents.                  |
|     3 | A WeekendPass stay of 481 minutes with an 800-cent flat rate produces 950 cents.    |
|     3 | `GET /api/passes/quotes?minutes=61` returns deterministic ascending charges.        |
|     3 | A missing pass quotation returns 404 and the correct error code.                    |
|     4 | Invalid stay minutes or invalid POST payload returns 400 without persisting a pass. |
|     4 | Valid POST returns 201 and the added pass appears in a later list/quote request.    |

## Submission and final checks

- Start the application and exercise both normal and invalid cases using Postman, curl or IntelliJ HTTP Client.
- Zip the complete Maven project folder (including `pom.xml`, sources, resources and any test files). Do not submit only
  `.java` files.
- Before submission, confirm that your ZIP contains the most recent source files and can be extracted successfully.

---

## How to attempt and mark

1. Open `past-papers/P1-parking` in IntelliJ (the folder with `pom.xml`), set a 100-minute timer, and work only in this
   project.
2. When time is up, mark your attempt from the repo root:

   ```
   java marking/Mark.java p1 past-papers/P1-parking
   ```

   or on Windows: `marking\mark.cmd p1 past-papers\P1-parking`

   The marker copies its tests into `src/test/java/marking/`, runs them through the Maven wrapper, and prints a mark per
   task out of 100. It checks the rules and illustrative values above plus extra inputs not shown in the paper, so
   hard-coded answers fail. It's an estimate: staff marking and the viva can differ.
