package edu.practice.courier.model;

public class ExpressParcel extends Parcel {
  private final int basePence;
  private final boolean signatureRequired;

  public ExpressParcel(long id, String trackingCode, int weightGrams, int basePence, boolean signatureRequired) {
    super(id, trackingCode, weightGrams);
    if (weightGrams <= 0) {
      throw new IllegalArgumentException("Weight must be positive");
    }
    if (basePence <= 0) {
      throw new IllegalArgumentException("Base pence must be positive");
    }
    this.basePence = basePence;
    this.signatureRequired = signatureRequired;
  }

  public int getBasePence() {
    return basePence;
  }

  public boolean isSignatureRequired() {
    return signatureRequired;
  }

  @Override
  public int pricePence() {
    int blocks = ((getWeightGrams() - 1) / 250 + 1);
    long totalPrice = (getBasePence() + 90) * (long) blocks;
    if (isSignatureRequired()) {
      return Math.toIntExact(totalPrice + 300);
    }
    if(totalPrice > Integer.MAX_VALUE){
      throw new IllegalArgumentException("Price exceeded limit");
    }
    return Math.toIntExact(totalPrice);
  }

  @Override
  public int priority() {
    if (isSignatureRequired()) {
      return 3;
    } else {
      return 2;
    }
  }
}
