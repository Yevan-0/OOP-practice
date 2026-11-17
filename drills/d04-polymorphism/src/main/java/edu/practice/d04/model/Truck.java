package edu.practice.d04.model;

/**
 * Given. A truck pays a fixed base fee (tolls, engine start) plus a cheaper rate per kilometre.
 */
public class Truck extends Vehicle {

  private final int baseCents;
  private final int centsPerKm;

  public Truck(long id, String plate, int baseCents, int centsPerKm) {
    super(id, plate);
    if (baseCents < 0) throw new IllegalArgumentException("baseCents cannot be negative");
    if (centsPerKm <= 0) throw new IllegalArgumentException("centsPerKm must be positive");
    this.baseCents = baseCents;
    this.centsPerKm = centsPerKm;
  }

  public int getBaseCents() {
    return baseCents;
  }

  public int getCentsPerKm() {
    return centsPerKm;
  }

  @Override
  public String kind() {
    return "TRUCK";
  }

  /**
   * baseCents + km x centsPerKm. Example: base 1500, 20 c/km, 10 km = 1500 + 200 = 1700.
   */
  @Override
  public int costCents(int km) {
    if (km <= 0) throw new IllegalArgumentException("km must be positive");
    try {
      return Math.addExact(baseCents, Math.multiplyExact(km, centsPerKm));
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("cost too large", e);
    }
  }
}
