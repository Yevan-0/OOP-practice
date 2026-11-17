package edu.practice.d04.repo;

import edu.practice.d04.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage, the same shape as the LBP starters' repositories. Given.
 */
public class VehicleRepository {

  private final Map<Long, Vehicle> vehicles = new ConcurrentHashMap<>();

  public VehicleRepository() {
    vehicles.put(1L, new Van(1, "VAN-01", 30));                 // 30 c per km
    vehicles.put(2L, new CargoBike(2, "BIKE-02", 80));          // 80 c per started 25 km
    vehicles.put(3L, new Truck(3, "TRUCK-03", 1_500, 20));      // 1500 c base + 20 c per km
    vehicles.put(4L, new Van(4, "VAN-04", 22));                 // 22 c per km
  }

  /**
   * A copy of every vehicle. The order is NOT guaranteed: sort it yourself if order matters.
   */
  public Collection<Vehicle> all() {
    return List.copyOf(vehicles.values());
  }

  public Vehicle get(long id) {
    return vehicles.get(id);
  }

  public boolean contains(long id) {
    return vehicles.containsKey(id);
  }

  public void save(Vehicle vehicle) {
    vehicles.put(vehicle.getId(), vehicle);
  }
}
