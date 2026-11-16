package edu.practice.d02.model;

public class Annual extends Membership {
  private final int yearlyCents;

  public Annual(long id, String member, int yearlyCents) {
    super(id, member);
    if (yearlyCents <= 0) {
      throw new IllegalArgumentException("Yearly cents must be positive");
    }
    this.yearlyCents = yearlyCents;
  }

  public int getYearlyCents() {
    return yearlyCents;
  }

  @Override
  public int monthlyFeeCents(int visits) {
    if (visits < 0) {
      throw new IllegalArgumentException("Visits must be a positive value");
    }
    return ((yearlyCents - 1) / 12 + 1);
  }
}
