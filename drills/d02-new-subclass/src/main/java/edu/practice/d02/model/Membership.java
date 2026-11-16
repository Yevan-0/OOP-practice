package edu.practice.d02.model;

/**
 * Parent of every gym membership. Given: don't change it.
 */
public abstract class Membership {

  private final long id;
  private final String member;

  protected Membership(long id, String member) {
    if (id <= 0) throw new IllegalArgumentException("id must be positive");
    if (member == null || member.isBlank()) throw new IllegalArgumentException("member is required");
    this.id = id;
    this.member = member.trim();
  }

  public long getId() {
    return id;
  }

  public String getMember() {
    return member;
  }

  /**
   * What this membership costs for one month in which the member visited `visits` times.
   */
  public abstract int monthlyFeeCents(int visits);
}
