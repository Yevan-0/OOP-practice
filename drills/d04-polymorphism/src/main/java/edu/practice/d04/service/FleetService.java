package edu.practice.d04.service;

import edu.practice.d04.dto.CostQuote;
import edu.practice.d04.model.Vehicle;
import edu.practice.d04.repo.VehicleRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic for the delivery fleet.
 * <p>
 * RULE FOR THIS WHOLE FILE: only ever talk to vehicles as `Vehicle`.
 * No type checks, no casts, no mentioning the subclasses by name.
 * Let each object run its own costCents(...) rule (dynamic dispatch).
 * (A test reads this file and fails if it finds instanceof, getClass() or a subclass name.)
 */
public class FleetService {

  private final VehicleRepository repo;

  public FleetService(VehicleRepository repo) {
    this.repo = repo;
  }

  /**
   * Total cost of sending EVERY vehicle in `convoy` the same distance `km`.
   * An empty convoy costs 0. km <= 0 or a total too big for an int -> IllegalArgumentException.
   */
  public int convoyCostCents(List<Vehicle> convoy, int km) {
    // TODO d04-1: reject km <= 0 FIRST (an empty convoy with km 0 is still an error).
    //             Loop over the convoy and add up v.costCents(km) for every vehicle v.
    //             Add with Math.addExact and turn its ArithmeticException into an IllegalArgumentException.
    if (km <= 0) {
      throw new IllegalArgumentException("KM should be positive");
    }
    int total = 0;
    for (Vehicle v : convoy) {
      try {
        total = Math.addExact(total, v.costCents(km));
      } catch (ArithmeticException e) {
        throw new IllegalArgumentException("Convoy too large");
      }
    }
    return total;
  }

  /**
   * One CostQuote for every vehicle in the repository, for a trip of `km` kilometres,
   * sorted cheapest first; equal costs -> lower vehicle id first. km <= 0 -> IllegalArgumentException.
   */
  public List<CostQuote> quotes(int km) {
    // TODO d04-2: reject km <= 0. For every vehicle in repo.all(), build
    //             new CostQuote(id, plate, kind(), km, costCents(km))  (a private helper is neat for this).
    if (km <= 0) {
      throw new IllegalArgumentException("Km should be positive");
    }
    // TODO d04-3: sort by costCents ascending, then vehicleId ascending, and return the list.
    return repo.all().stream()
      .map(v -> toCost(v, km))
      .sorted(Comparator.comparing(CostQuote::costCents)
        .thenComparing(CostQuote::vehicleId))
      .toList();
  }

  // You may add a private helper here, e.g. private CostQuote toQuote(Vehicle v, int km) { ... }
  private CostQuote toCost(Vehicle v, int km) {
    return new CostQuote(
      v.getId(),
      v.getPlate(),
      v.kind(),
      km,
      v.costCents(km)
    );
  }
}
