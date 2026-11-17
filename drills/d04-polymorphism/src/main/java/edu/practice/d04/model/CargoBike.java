package edu.practice.d04.model;

/**
 * Given. An electric cargo bike pays for battery charges.
 * One charge lasts 25 km, and any started charge is paid in full:
 * 1-25 km -> 1 charge,  26-50 km -> 2 charges,  ...
 */
public class CargoBike extends Vehicle {

  public static final int KM_PER_CHARGE = 25;

  private final int centsPerCharge;

  public CargoBike(long id, String plate, int centsPerCharge) {
    super(id, plate);
    if (centsPerCharge <= 0) throw new IllegalArgumentException("centsPerCharge must be positive");
    this.centsPerCharge = centsPerCharge;
  }

  public int getCentsPerCharge() {
    return centsPerCharge;
  }

  @Override
  public String kind() {
    return "CARGO_BIKE";
  }

  /**
   * charges x centsPerCharge. Example: 10 km at 80 c/charge = 1 charge = 80.
   */
  @Override
  public int costCents(int km) {
    if (km <= 0) throw new IllegalArgumentException("km must be positive");
    int charges = (km - 1) / KM_PER_CHARGE + 1;      // ceiling division (d03)
    try {
      return Math.multiplyExact(charges, centsPerCharge);
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("cost too large", e);
    }
  }
}
