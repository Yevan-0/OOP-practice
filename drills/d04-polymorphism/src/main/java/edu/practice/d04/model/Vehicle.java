package edu.practice.d04.model;

/**
 * Parent of every delivery vehicle. Given: don't change it.
 * <p>
 * It is abstract: there is no such thing as a plain "Vehicle", only a Van, a Truck, a CargoBike...
 * Every subclass MUST answer two questions in its own way: kind() and costCents(km).
 */
public abstract class Vehicle {

  private final long id;
  private final String plate;

  protected Vehicle(long id, String plate) {
    if (id <= 0) throw new IllegalArgumentException("id must be positive");
    if (plate == null || plate.isBlank()) throw new IllegalArgumentException("plate is required");
    this.id = id;
    this.plate = plate.trim();
  }

  public long getId() {
    return id;
  }

  public String getPlate() {
    return plate;
  }

  /**
   * A short label for this kind of vehicle, e.g. "VAN".
   */
  public abstract String kind();

  /**
   * Fuel (or charging) cost in whole cents for driving `km` kilometres. Rejects km <= 0.
   */
  public abstract int costCents(int km);
}
