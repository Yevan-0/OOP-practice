package edu.practice.courier.model;

public class StandardParcel extends Parcel {
  // TODO Task 1: declare a private final int ratePerBlockPence field and a public getter getRatePerBlockPence().
  private final int ratePerBlockPence;

  public StandardParcel(long id, String trackingCode, int weightGrams, int ratePerBlockPence) {
    super(id, trackingCode, weightGrams);
    // TODO Task 1: reject weightGrams <= 0 or ratePerBlockPence <= 0 (IllegalArgumentException), then store the rate.
    if(weightGrams <=0){
      throw new IllegalArgumentException("Weight in grams must be positive");
    }
    if (ratePerBlockPence <= 0) {
      throw new IllegalArgumentException("Rate in grams must be positive");
    }
    this.ratePerBlockPence = ratePerBlockPence;
  }

  public int getRatePerBlockPence() {
    return ratePerBlockPence;
  }

  @Override
  public int pricePence() {
    // TODO Task 1: charge ratePerBlockPence for every started 500 g block; reject a price that does not fit in an int.
    int blocks = ((getWeightGrams() - 1) / 500) + 1;
    long totalPrice = getRatePerBlockPence() * (long) blocks;
    if (totalPrice > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Calculated price exceeds maximum value");
    }
    return (int) totalPrice;
  }

  @Override
  public int priority() { // TODO Task 1: a standard parcel always has priority 1.
    return 1;
  }
}
