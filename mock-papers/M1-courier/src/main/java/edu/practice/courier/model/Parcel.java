package edu.practice.courier.model;

public abstract class Parcel {
  private final long id;
  private final String trackingCode;
  private final int weightGrams;

  protected Parcel(long id, String trackingCode, int weightGrams) {
    this.id = id;
    this.trackingCode = trackingCode;
    this.weightGrams = weightGrams;
  }

  public long getId() {
    return id;
  }

  public String getTrackingCode() {
    return trackingCode;
  }

  public int getWeightGrams() {
    return weightGrams;
  }

  public abstract int pricePence();

  public abstract int priority();
}
