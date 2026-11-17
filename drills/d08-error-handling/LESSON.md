---
id: d08
module: OOP
kind: drill
title: Error handling with @RestControllerAdvice
difficulty: 2
timeMinutes: 15
tasks:
  - { name: "IllegalArgumentException -> 400 BAD_REQUEST", marks: 3 }
  - { name: "NotFoundException -> 404 NOT_FOUND", marks: 3 }
  - { name: "ConflictException -> 409 CONFLICT", marks: 2 }
  - { name: "ApiError body carries the exception's message", marks: 2 }
---

# d08 · Error handling with @RestControllerAdvice

> **Exam link:** every paper has this in Task 3 or 4. P1: *"map IllegalArgumentException to HTTP 400 and ApiError (
code="BAD_REQUEST", message=...)"* (4 marks) and *"map this to HTTP 404 with ApiError (code="NOT_FOUND", message=...)"*.
> P3: *"same runKey with changed values must produce HTTP 409 / ApiError (code="CONFLICT")"*. The starter always gives you
> an empty `ApiExceptionHandler` with a TODO.

## The idea

```java

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> badRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(new ApiError("BAD_REQUEST", e.getMessage()));
  }
}
```

That's the whole pattern. Each part has one job:

- **`@RestControllerAdvice`** on the class: "this class handles exceptions for *every* controller, and what it returns
  becomes JSON".
- **`@ExceptionHandler(X.class)`** on a method: "call me when an `X` is thrown".
- **`ResponseEntity<ApiError>`**: the return type that lets you pick the **status code** *and* the **body**.
- **`e.getMessage()`**: the text the service put in the exception (`"customer is required"`). Pass it on, so the user
  knows what was wrong.

### How an exception travels

The controller and service have **no try/catch**. The exception just flies out, and Spring catches it:

```
POST /api/pets  {"name":"  "}
  -> PetController.add(...)
       -> PetService.add(...)  throws new IllegalArgumentException("name is required")
  <- the exception flies back out through the controller
  -> Spring asks: "is there an @ExceptionHandler for IllegalArgumentException?"
       -> ApiExceptionHandler.badRequest(e) returns status 400 + ApiError
  -> Jackson turns the ApiError record into JSON
HTTP 400  {"code":"BAD_REQUEST","message":"name is required"}
```

If **no** handler matches, Spring gives up and answers **500 Internal Server Error**. Section B loses the marks.

### Which status?

| Exception                  | Status | `code`        | Meaning                                                                                          |
|----------------------------|--------|---------------|--------------------------------------------------------------------------------------------------|
| `IllegalArgumentException` | 400    | `BAD_REQUEST` | The request itself is wrong (blank name, negative number).                                       |
| `XNotFoundException`       | 404    | `NOT_FOUND`   | The id doesn't exist.                                                                            |
| `XConflictException`       | 409    | `CONFLICT`    | The request is fine, but it clashes with what's already stored (seat already taken, key reused). |

### Reading a stack trace

Without a handler, IntelliJ's Run window shows something like this:

```
ERROR ... Servlet.service() ... threw exception [Request processing failed:
      java.lang.IllegalArgumentException: name is required] with root cause

java.lang.IllegalArgumentException: name is required                         <- 1. WHAT went wrong
	at edu.practice.shelter.service.PetService.add(PetService.java:27)          <- 2. WHERE (your code)
	at edu.practice.shelter.controller.PetController.add(PetController.java:31) <- 3. who called it
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(...)
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(...)
	... 50 more lines of org.springframework / org.apache ...                   <- skip these
```

1. Read the **first line**: the exception type and its message.
2. Find the first `at` line in **your** package (`edu.practice...`). Click the blue `PetService.java:27` link and
   IntelliJ jumps to that line.
3. Ignore the `org.springframework` and `org.apache` lines. That's Spring's plumbing.
4. If you see `Caused by:`, the **last** `Caused by:` is the real problem.

## Worked example

This one uses a different scenario: a pet shelter with `PetNotFoundException` and `PetAlreadyAdoptedException`.

```java

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(PetNotFoundException.class)
  public ResponseEntity<ApiError> notFound(PetNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)                  // 404
      .body(new ApiError("NOT_FOUND", e.getMessage()));
  }

  @ExceptionHandler(PetAlreadyAdoptedException.class)
  public ResponseEntity<ApiError> adopted(PetAlreadyAdoptedException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)                   // 409
      .body(new ApiError("CONFLICT", e.getMessage()));
  }
}
```

**The other way: `@ResponseStatus`.** Instead of `ResponseEntity`, you can return the `ApiError` directly and put the
status in an annotation. Both earn the marks. Pick one style and use it for every method.

```java

@ExceptionHandler(PetNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public ApiError notFound(PetNotFoundException e) {
  return new ApiError("NOT_FOUND", e.getMessage());
}
```

## Your task

The ticket desk API is finished except for its error handling. Read `service/TicketService.java` to see which exception
is thrown when. Then open `src/main/java/edu/practice/d08/error/ApiExceptionHandler.java` and add three methods:

1. **d08-1:** `IllegalArgumentException` → **400**, `ApiError("BAD_REQUEST", message)`.
2. **d08-2:** `TicketNotFoundException` → **404**, `ApiError("NOT_FOUND", message)`.
3. **d08-3:** `TicketConflictException` → **409**, `ApiError("CONFLICT", message)`.

The `message` must be the exception's own message (`e.getMessage()`).

Run `ApiExceptionHandlerTest`. It starts the app and sends real requests through it. Work until all 6 tests are green.
Then try it for real: run `TicketDeskApplication` and open `http://localhost:8080/api/tickets/99` in a browser.

## Hints

<details><summary>Hint 1: where do I start?</summary>

Copy the `badRequest` method from **The idea** into the class. Run the tests: tests 1 and 2 should go green. Then copy
it twice more, and change the exception class, the `HttpStatus` and the code.
</details>

<details><summary>Hint 2: the test says "Nothing handled TicketNotFoundException"</summary>

Spring found no method for that exception. Check the method has `@ExceptionHandler(TicketNotFoundException.class)` with
the exact class name, and that it's inside the class with `@RestControllerAdvice`.
</details>

<details><summary>Hint 3: "Status expected:&lt;404&gt; but was:&lt;400&gt;"</summary>

One of your handlers catches too much. For example, `@ExceptionHandler(RuntimeException.class)` also catches
`TicketNotFoundException`, because it *is a* `RuntimeException`. Give each exception its own handler, with its own
`HttpStatus`.
</details>

<details><summary>Hint 4: the status is right but the message doesn't match</summary>

Don't write your own text. Pass on the service's text: `new ApiError("NOT_FOUND", e.getMessage())`. The test expects,
for example, `"Ticket not found: 99"`, which only the exception knows.
</details>

<details><summary>Hint 5: the 409 method (nearly the answer)</summary>

```java

@ExceptionHandler(TicketConflictException.class)
public ResponseEntity<ApiError> conflict(TicketConflictException e) {
  return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT", e.getMessage()));
}
```

</details>

## Viva questions

<details><summary>What does <code>@RestControllerAdvice</code> do?</summary>

It makes the class a global exception handler for all controllers. When a controller method throws, Spring looks in this
class for an `@ExceptionHandler` that matches, and turns what it returns into the JSON response.
</details>

<details><summary>Why not put a try/catch in every controller method?</summary>

Every endpoint would repeat the same catch blocks, and it's easy to forget one. One handler class keeps the mapping in
one place, and the controllers stay thin.
</details>

<details><summary>When do you return 400, 404 and 409?</summary>

400 when the request itself is invalid. 404 when the id it names doesn't exist. 409 when the request is valid but
clashes with the current state, like booking a seat that is already taken.
</details>

<details><summary>What happens if no handler matches an exception?</summary>

Spring answers 500 Internal Server Error with its own default error body, not our `ApiError`. The stack trace is printed
in the console.
</details>

<details><summary><code>ResponseEntity</code> or <code>@ResponseStatus</code>: which one?</summary>

Both set the status. `ResponseEntity.status(...).body(...)` sets it in code, so it can change at run time.
`@ResponseStatus(HttpStatus.NOT_FOUND)` fixes it on the method, and the method returns the `ApiError` directly. For a
handler that always returns the same status, both are fine.
</details>
