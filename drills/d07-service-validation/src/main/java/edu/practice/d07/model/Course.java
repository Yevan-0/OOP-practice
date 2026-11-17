package edu.practice.d07.model;

/**
 * A course students can enrol on. Given: don't change it.
 * <p>
 * Notice: this class does NO checking. It trusts whoever creates it.
 * In this drill, keeping bad data out is the SERVICE's job.
 */
public class Course {

  private final long id;
  private final String code;
  private final String title;
  private final int capacity;

  public Course(long id, String code, String title, int capacity) {
    this.id = id;
    this.code = code;
    this.title = title;
    this.capacity = capacity;
  }

  public long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }

  public int getCapacity() {
    return capacity;
  }
}
