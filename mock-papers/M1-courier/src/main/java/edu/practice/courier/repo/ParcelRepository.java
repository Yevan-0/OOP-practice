package edu.practice.courier.repo;

import edu.practice.courier.model.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ParcelRepository {
  private final Map<Long, Parcel> parcels = new ConcurrentHashMap<>();

  public ParcelRepository() {
    parcels.put(1L, new StandardParcel(1, "CP-1001", 1200, 180));
    parcels.put(2L, new StandardParcel(2, "CP-1002", 500, 250));
    // TODO after Task 2: add new ExpressParcel(3,"CP-1003",900,400,true) and new ExpressParcel(4,"CP-1004",300,350,false).
    parcels.put(3L, new ExpressParcel(3, "CP-1003", 900, 400, true));
    parcels.put(4L, new ExpressParcel(4, "CP-1004", 300, 350, false));
  }

  public Collection<Parcel> all() {
    return List.copyOf(parcels.values());
  }

  public Parcel find(long id) {
    return parcels.get(id);
  }

  public boolean exists(long id) {
    return parcels.containsKey(id);
  }

  public void save(Parcel parcel) {
    parcels.put(parcel.getId(), parcel);
  }
}
