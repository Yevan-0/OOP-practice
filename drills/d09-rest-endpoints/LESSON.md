---
id: d09
module: OOP
kind: drill
title: REST endpoints with Spring
difficulty: 2
timeMinutes: 20
tasks:
  - { name: "GET list and GET by id with @PathVariable", marks: 2 }
  - { name: "@RequestParam (required)", marks: 2 }
  - { name: "@RequestParam with defaultValue", marks: 2 }
  - { name: "POST with @RequestBody returning 201 Created", marks: 3 }
  - { name: "every endpoint delegates to the service", marks: 1 }
---

# d09 · REST endpoints with Spring

> **Exam link:** this is LBP **Task 4** (20 marks). P1: *"POST returns HTTP 201; all handlers delegate business
decisions to service methods, with the correct Spring annotations"* (5 marks). P2: *"GET
/api/missions/plan?capacity=60&urgentOnly=false … urgentOnly defaults to false."* Section B then checks that a *"Valid
POST returns 201 and the added pass appears in a later list/quote request"* (4 marks).

## The idea

```java

@RestController
@RequestMapping("/api/plants")                 // every URL in this class starts with /api/plants
public class PlantController {
  private final PlantService service;

  public PlantController(PlantService service) {
    this.service = service;
  }

  @GetMapping("/{id}")                       // GET /api/plants/4
  public Plant get(@PathVariable long id) {  // id = 4
    return service.get(id);                // one line: ask the service
  }
}
```

A controller method has three parts:

1. **The mapping annotation** says which HTTP method and URL it answers: `@GetMapping`, `@PostMapping`. The path is
   added to the class's `@RequestMapping`.
2. **The parameters** say where each value comes from (see the table).
3. **The body** is one line that calls the service. What it returns, Spring turns into JSON.

| Annotation                                                  | Value comes from               | Example request                                       | Java gets             |
|-------------------------------------------------------------|--------------------------------|-------------------------------------------------------|-----------------------|
| `@PathVariable long id`                                     | a `{id}` part of the path      | `GET /api/plants/4`                                   | `id = 4`              |
| `@RequestParam int qty`                                     | the query string, **required** | `GET /api/plants/4/price?qty=3`                       | `qty = 3`             |
| `@RequestParam(defaultValue = "false") boolean inStockOnly` | the query string, **optional** | `GET /api/plants/search?maxPriceCents=500`            | `inStockOnly = false` |
| `@RequestBody NewPlant body`                                | the JSON body                  | `POST /api/plants` + `{"id":6,"name":"Rosemary",...}` | a `NewPlant` record   |

The parameter name must match the name in the URL (`qty` ↔ `?qty=`). If a **required** `@RequestParam` is missing,
Spring answers **400** by itself.

### 201 Created

A POST that creates something should answer **201**, not the default 200. There are two ways to do it:

```java

@PostMapping
public ResponseEntity<Plant> add(@RequestBody NewPlant body) {
  return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
}

// or
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Plant add(@RequestBody NewPlant body) {
  return service.add(body);
}
```

### The controller does no thinking

The controller translates HTTP into a Java call and back. Nothing else. No `if`, no loops, no maths, no repository.
Every decision (validation, prices, filtering, sorting, "not found") is in the **service**, and errors become status
codes in the **exception handler** (d08). The papers give marks for exactly this: *"handlers delegate business decisions
to service methods"*.

## Worked example

This one uses a different scenario: a board-game library.

```java

@RestController
@RequestMapping("/api/games")
public class GameController {
  private final GameService service;

  public GameController(GameService service) {
    this.service = service;
  }

  @GetMapping                                            // GET /api/games
  public List<Game> all() {
    return service.all();
  }

  @GetMapping("/{id}/rental")                            // GET /api/games/3/rental?days=2
  public RentalQuote rental(@PathVariable long id, @RequestParam int days) {
    return service.rental(id, days);
  }

  @GetMapping("/filter")                                 // GET /api/games/filter?players=4
  public List<Game> filter(@RequestParam int players,    //   ...&familyOnly=true is optional
                           @RequestParam(defaultValue = "false") boolean familyOnly) {
    return service.filter(players, familyOnly);
  }

  @PostMapping                                           // POST /api/games  {"id":9,"title":"Catan"}
  public ResponseEntity<Game> add(@RequestBody NewGame body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
  }
}
```

`/filter` and `/{id}` don't clash. Spring always prefers the fixed path (`/filter`) over a `{variable}`.

## Your task

Open `src/main/java/edu/practice/d09/controller/PlantController.java`. The service, repository and exception handler are
finished. Add five endpoint methods, each **one line** calling `PlantService`:

| TODO  | Request                                                                                               | Calls                                        |
|-------|-------------------------------------------------------------------------------------------------------|----------------------------------------------|
| d09-1 | `GET /api/plants`                                                                                     | `service.all()`                              |
| d09-2 | `GET /api/plants/{id}`                                                                                | `service.get(id)`                            |
| d09-3 | `GET /api/plants/{id}/price?qty=3`                                                                    | `service.price(id, qty)`                     |
| d09-4 | `GET /api/plants/search?maxPriceCents=500&inStockOnly=true` (`inStockOnly` optional, default `false`) | `service.search(maxPriceCents, inStockOnly)` |
| d09-5 | `POST /api/plants` with a `NewPlant` JSON body, answering **201**                                     | `service.add(body)`                          |

Run `PlantApiTest`. It starts the whole app on a random port and sends real HTTP requests, the same way Section B is
checked. Work until all 10 tests are green.

**Then try it for real.** Run `NurseryApplication` (green triangle). It listens on port 8080. In **Git Bash** (or
macOS/Linux):

```bash
curl -i http://localhost:8080/api/plants/4
curl -i "http://localhost:8080/api/plants/1/price?qty=3"
curl -i "http://localhost:8080/api/plants/search?maxPriceCents=500&inStockOnly=true"
curl -i -X POST http://localhost:8080/api/plants -H "Content-Type: application/json" \
     -d '{"id":6,"name":"Rosemary","priceCents":399,"stock":7}'
```

In **Windows PowerShell**, type `curl.exe`, not `curl`, because plain `curl` is a different PowerShell command. JSON
quotes are awkward in PowerShell, so put the body in a file (`rosemary.json`) and send it with `@`:

```powershell
curl.exe -i "http://localhost:8080/api/plants/search?maxPriceCents=500"
curl.exe -i -X POST http://localhost:8080/api/plants -H "Content-Type: application/json" --data "@rosemary.json"
```

`-i` prints the status line (`HTTP/1.1 201`) above the body. Always put a URL that contains `&` in quotes.

## Hints

<details><summary>Hint 1: where do I start?</summary>

With the list. The class already has `@RequestMapping("/api/plants")`, so the method needs no path:

```java

@GetMapping
public List<Plant> all() {
  return service.all();
}
```

Run the tests. Test 1 should go green.
</details>

<details><summary>Hint 2: the test says "should answer 200" but the body shows "status":404, "error":"Not Found"</summary>

Spring found no method for that URL. Check the path in the annotation: `@GetMapping("/{id}/price")` with braces around
`id`, and no repeated `/api/plants` (the class already adds it).
</details>

<details><summary>Hint 3: search works with inStockOnly, but answers 400 without it</summary>

A plain `@RequestParam` is required. Give it a default: `@RequestParam(defaultValue = "false") boolean inStockOnly`.
</details>

<details><summary>Hint 4: "POST /api/plants (Rosemary) should answer 201" but it got 200</summary>

Returning the plant directly gives 200. Wrap it: `ResponseEntity.status(HttpStatus.CREATED).body(...)`, or add
`@ResponseStatus(HttpStatus.CREATED)` to the method.
</details>

<details><summary>Hint 5: search and POST (nearly the answer)</summary>

```java

@GetMapping("/search")
public List<Plant> search(@RequestParam int maxPriceCents,
                          @RequestParam(defaultValue = "false") boolean inStockOnly) {
  return service.search(maxPriceCents, inStockOnly);
}

@PostMapping
public ResponseEntity<Plant> add(@RequestBody NewPlant body) {
  return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
}
```

</details>

## Viva questions

<details><summary>What is the difference between <code>@PathVariable</code> and <code>@RequestParam</code>?</summary>

`@PathVariable` reads a piece of the path, like the `4` in `/api/plants/4`. It names *which* resource. `@RequestParam`
reads the query string after `?`, like `?qty=3`. It's an option or an input for the operation.
</details>

<details><summary>Why return 201 and not 200 from the POST?</summary>

201 Created tells the client that a new resource was made. 200 only means "OK". The papers ask for 201 by name, and
Section B checks it.
</details>

<details><summary>What does <code>@RequestBody</code> do?</summary>

It tells Spring to read the request's JSON body and turn it into the parameter's type. Jackson matches JSON field names
to the record's component names, so `{"name":"Rosemary"}` fills `name`.
</details>

<details><summary>Why should the controller do no thinking?</summary>

All rules live in one place, the service, so they are tested once and every endpoint uses the same rules. The controller
only translates between HTTP and Java. The papers give marks for delegating business decisions to the service.
</details>

<details><summary>What happens if the client leaves out a required <code>@RequestParam</code>?</summary>

Spring rejects the request with 400 Bad Request before my method runs. With `defaultValue`, the parameter becomes
optional and the default is used.
</details>
