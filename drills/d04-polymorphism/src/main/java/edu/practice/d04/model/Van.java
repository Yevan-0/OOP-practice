package edu.practice.d04.model;

/**
 * Given. A van burns diesel: every kilometre costs the same.
 */
public class Van extends Vehicle {

  private final int centsPerKm;

  public Van(long id, String plate, int centsPerKm) {
    super(id, plate);
    if (centsPerKm <= 0) throw new IllegalArgumentException("centsPerKm must be positive");
    this.centsPerKm = centsPerKm;
  }

  public int getCentsPerKm() {
    return centsPerKm;
  }

  @Override
  public String kind() {
    return "VAN";
  }

  /**
   * km x centsPerKm. Example: 10 km at 30 c/km = 300.
   */
  @Override
  public int costCents(int km) {
    if (km <= 0) throw new IllegalArgumentException("km must be positive");
    try {
      return Math.multiplyExact(km, centsPerKm);
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("cost too large", e);
    }
  }
}
