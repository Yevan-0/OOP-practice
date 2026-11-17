---
id: d07
module: OOP
kind: drill
title: Service validation and not-found errors
difficulty: 2
timeMinutes: 20
tasks:
  - { name: "get(id) with a custom NotFoundException", marks: 2 }
  - { name: "guard clauses: positive id, non-blank text, capacity range", marks: 3 }
  - { name: "trim text before storing", marks: 2 }
  - { name: "unique id, and nothing saved when the input is invalid", marks: 3 }
---

# d07 · Service validation and not-found errors

> **Exam link:** this is LBP **Task 3**. P1: *"missing IDs must raise PassNotFoundException"* (6 marks) and *"addHourly
must validate positive unique ID, nonblank registration and positive hourly rate; trim registration before storing"* (6
> marks). P2 asks the same for `addAir` (*"validates positive ID and distance … unique ID, and values within 0–10,000"*, 7
> marks). Section B then checks that an invalid POST *"returns 400 without persisting"*.

## The idea

```java
public Course get(long id) {
  Course course = repo.get(id);                 // null if there is no such course
  if (course == null) {
    throw new CourseNotFoundException(id);    // never hand null back to the caller
  }
  return course;
}
```

In an LBP project, the **service** is where the thinking happens. The controller passes the request in, the repository
stores things, and the service decides if the request is OK.

Most service methods follow the same three steps:

1. **Check everything first.** Use one `if` per rule. When a rule fails, `throw` straight away. These `if`s are called
   **guard clauses**.
2. **Clean the data.** `trim()` the text, so `"  OOP101 "` is stored as `"OOP101"`.
3. **Save it last.** Only call `repo.save(...)` when every check has passed.

If you save first and check afterwards, a bad course is already in the repository when the exception is thrown. Section
B checks for exactly that: *"returns 400 without persisting"*.

### Which exception?

| Problem                                                  | Throw                                       | Later (d08) becomes |
|----------------------------------------------------------|---------------------------------------------|---------------------|
| The input is wrong: bad number, blank text, duplicate id | `IllegalArgumentException("what is wrong")` | **400** Bad Request |
| The id doesn't exist                                     | `CourseNotFoundException(id)`               | **404** Not Found   |

Always give the exception a **message**. It becomes the `message` in the JSON error the user sees.

### Blank text: `null`, `""` and `"   "`

```java
if(code ==null||code.

isBlank()){...}   // null first! null.isBlank() crashes
```

`isBlank()` is true for `""`, `"   "` and tabs/newlines. `isEmpty()` is only true for `""`, so it lets `"   "` through.

### Duplicate ids

`repo.save(...)` uses `map.put`, which **silently replaces** an existing course. So check `repo.contains(id)` yourself,
and throw before saving.

## Worked example

This one uses a different scenario: a pet shelter, with a `PetService` that adds new pets.

```java
public record NewPet(long id, String name, int ageMonths) {
}

@Service
public class PetService {
  private final PetRepository repo;

  public PetService(PetRepository repo) {
    this.repo = repo;
  }   // constructor injection

  public Pet get(long id) {
    Pet pet = repo.get(id);
    if (pet == null) throw new PetNotFoundException(id);         // -> 404 later
    return pet;
  }

  public Pet add(NewPet body) {
    // 1. guard clauses: check everything, throw at the first problem
    if (body.id() <= 0) throw new IllegalArgumentException("id must be positive");
    if (body.name() == null || body.name().isBlank()) throw new IllegalArgumentException("name is required");
    if (body.ageMonths() < 0 || body.ageMonths() > 360) throw new IllegalArgumentException("age must be 0-360 months");
    if (repo.contains(body.id())) throw new IllegalArgumentException("pet " + body.id() + " already exists");

    // 2. clean   3. save LAST
    Pet pet = new Pet(body.id(), body.name().trim(), body.ageMonths());
    repo.save(pet);
    return pet;
  }
}
```

Read it out loud: *"Check the id, the name, the age and the duplicate. Only then trim, build and save."*

## Your task

Open `src/main/java/edu/practice/d07/service/CourseService.java`. The model, DTO, repository and exception are given.

1. **d07-1: `get(long id)`.** Return the course from the repository. If there isn't one, throw
   `new CourseNotFoundException(id)`.
2. **d07-2: `add(NewCourse body)`.** Throw `IllegalArgumentException` (with a message) when:
    - `id` is `0` or negative,
    - `code` or `title` is `null` or blank,
    - `capacity` is less than `1` or more than `MAX_CAPACITY` (300),
    - a course with that `id` already exists.

   Otherwise, build a `Course` with the code and title **trimmed**, save it, and return it.

Run `CourseServiceTest`. Work until all 9 tests are green.

## Hints

<details><summary>Hint 1: where do I start?</summary>

Start with `get`. The repository's `get(id)` returns `null` when the id is missing. Check for `null`, and throw. Then do
`add`, one rule at a time, running the tests after each one.
</details>

<details><summary>Hint 2: "a rejected course must NOT be saved"</summary>

Your `repo.save(...)` runs before one of your checks. Move every `if (...) throw ...` to the **top** of `add`, and make
`repo.save(...)` the second-to-last line (just before `return`).
</details>

<details><summary>Hint 3: the duplicate test says the old course was replaced</summary>

`save` replaces silently. Ask first:

```java
if(repo.contains(body.id())){
  throw new

IllegalArgumentException("a course with id "+body.id() +" already exists");
  }
```

</details>

<details><summary>Hint 4: the trim test fails</summary>

Trim when you **build** the course, not just when you check it:

```java
Course course = new Course(body.id(), body.code().trim(), body.title().trim(), body.capacity());
repo.

save(course);
return course;
```

</details>

## Viva questions

<details><summary>Why do you validate before saving?</summary>

If I saved first, an invalid course would already be stored when the exception is thrown. The client gets a 400, but the
bad data stays. Checking everything first means a rejected request changes nothing.
</details>

<details><summary>Why <code>isBlank()</code> and not <code>isEmpty()</code>?</summary>

`isEmpty()` is only true for `""`. A title of `"   "` would pass it. `isBlank()` also treats spaces, tabs and newlines
as empty. I check `null` first, because calling a method on `null` throws a `NullPointerException`.
</details>

<details><summary>Why throw a <code>CourseNotFoundException</code> instead of returning <code>null</code>?</summary>

`null` would travel up to the controller and come out as an empty 200 response, or crash later with a
`NullPointerException`. A specific exception says exactly what went wrong, and the exception handler can turn it into a
404.
</details>

<details><summary>Why does a duplicate id throw <code>IllegalArgumentException</code>, but an unknown id throws <code>CourseNotFoundException</code>?</summary>

A duplicate id means the client sent bad input, which is a 400. An unknown id means the thing they asked for doesn't
exist, which is a 404. Different exceptions let the handler choose different status codes.
</details>

<details><summary>What is constructor injection?</summary>

The service asks for its `CourseRepository` in its constructor. Spring creates the repository (it's a `@Repository`) and
passes it in. The field can be `final`, and a test can pass in its own repository with `new CourseService(repo)`, which
is what `CourseServiceTest` does.
</details>
