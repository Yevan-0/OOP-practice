package edu.practice.d07.service;

import edu.practice.d07.dto.NewCourse;
import edu.practice.d07.error.CourseNotFoundException;
import edu.practice.d07.model.Course;
import edu.practice.d07.repo.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * The business rules for courses. Controllers (d09) will call these methods and do nothing else.
 */
@Service
public class CourseService {

  /**
   * The biggest lecture theatre holds 300 students.
   */
  public static final int MAX_CAPACITY = 300;

  private final CourseRepository repo;

  public CourseService(CourseRepository repo) {   // constructor injection: Spring passes the repository in
    this.repo = repo;
  }

  /**
   * Given: every course, lowest id first.
   */
  public List<Course> all() {
    return repo.all().stream().sorted(Comparator.comparingLong(Course::getId)).toList();
  }

  /**
   * The course with this id.
   */
  public Course get(long id) {
    // TODO d07-1: ask the repository for the course.
    //             If it isn't there (null), throw new CourseNotFoundException(id). Never return null.
    Course course = repo.get(id);
    if (course == null) {
      throw new CourseNotFoundException(id);
    }
    return course;
  }

  /**
   * Checks the new course, stores it, and returns the stored course.
   */
  public Course add(NewCourse body) {
    // TODO d07-2: CHECK first. One guard clause per rule; each throws
    //             new IllegalArgumentException("<what is wrong>"):
    //               - id must be positive
    //               - code must not be null or blank
    //               - title must not be null or blank
    //               - capacity must be 1..MAX_CAPACITY
    //               - the id must not be used already (repo.contains)
    //             THEN build a Course with code and title trimmed, save it, and return it.
    if (body.id() <= 0) {
      throw new IllegalArgumentException("ID must be positive" + body.id());
    }
    if (body.code() == null || body.code().isBlank()) {
      throw new IllegalArgumentException("Code should not be blank");
    }
    if (body.title() == null || body.title().isBlank()) {
      throw new IllegalArgumentException("Title should not be blank");
    }
    if (body.capacity() <= 0 || body.capacity() > MAX_CAPACITY) {
      throw new IllegalArgumentException("Capacity limit exceeded");
    }
    if (repo.contains(body.id())) {
      throw new IllegalArgumentException("ID already exits");
    }
    Course course = new Course(body.id(), body.code().trim(), body.title().trim(), body.capacity());
    repo.save(course);
    return course;
  }
}
