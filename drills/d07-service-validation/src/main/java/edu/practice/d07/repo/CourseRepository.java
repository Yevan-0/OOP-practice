package edu.practice.d07.repo;

import edu.practice.d07.model.Course;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage, the same shape as the LBP starters' repositories. Given: don't change it.
 */
@Repository
public class CourseRepository {

  private final Map<Long, Course> courses = new ConcurrentHashMap<>();

  public CourseRepository() {
    courses.put(1L, new Course(1, "OOP101", "Object Oriented Programming", 120));
    courses.put(2L, new Course(2, "WEB205", "Web Design Studio", 35));
  }

  public Collection<Course> all() {
    return List.copyOf(courses.values());
  }

  /**
   * The course with this id, or null if there isn't one.
   */
  public Course get(long id) {
    return courses.get(id);
  }

  public boolean contains(long id) {
    return courses.containsKey(id);
  }

  /**
   * Stores the course. If the id is already used, the old course is silently REPLACED.
   */
  public void save(Course course) {
    courses.put(course.getId(), course);
  }
}
