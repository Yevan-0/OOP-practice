package edu.practice.courier.service;

import edu.practice.courier.model.*;
import edu.practice.courier.dto.*;
import edu.practice.courier.repo.*;
import edu.practice.courier.error.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ParcelService {
  private final ParcelRepository repo;

  public ParcelService(ParcelRepository repo) {
    this.repo = repo;
  }

  public Collection<Parcel> all() {
    return repo.all();
  }

  public Quote quote(long id) {
    // TODO Task 3: find the parcel or throw ParcelNotFoundException; build the Quote from pricePence() and priority().
    Parcel quote = repo.find(id);
    if (quote == null) {
      throw new ParcelNotFoundException(id);
    }
    return new Quote(id, quote.getTrackingCode(), quote.getWeightGrams(), quote.priority(), quote.pricePence());
  }

  public Parcel addStandard(NewStandardParcel body) {
    // TODO Task 3: validate ID, tracking code, weight and rate (IllegalArgumentException); trim the tracking code;
    // a duplicate ID or tracking code -> DuplicateParcelException; save and return the new StandardParcel.
    if (body.id() <= 0) {
      throw new IllegalArgumentException("ID  must be positive" + body.id());
    }
    if (body.trackingCode() == null || body.trackingCode().isBlank()) {
      throw new IllegalArgumentException("Tracking code cannot be empty");
    }
    if (body.weightGrams() <= 0 || body.weightGrams() > 30000) {
      throw new IllegalArgumentException("Weight must be in the range 1 to 30,000");
    }
    if (body.ratePerBlockPence() <= 0 || body.ratePerBlockPence() > 10000) {
      throw new IllegalArgumentException("Rate must be in the range 1 to 10,000");
    }
    if (repo.all().contains(body.id())) {
      throw new DuplicateParcelException("ID already exists");
    }
    if (repo.all().contains(body.trackingCode().trim())) {
      throw new DuplicateParcelException("Tracking code already exists");
    }
    StandardParcel parcel = new StandardParcel(body.id(), body.trackingCode().trim(), body.weightGrams(), body.ratePerBlockPence());
    repo.save(parcel);
    return parcel;
  }

  public VanLoad loadVan(int capacityGrams) {
    // TODO Task 2: sort priority DESC, pricePence ASC, ID ASC; load each parcel whose weight still fits;
    // record skipped IDs in the order considered. No concrete type checks. (Task 3: reject capacityGrams <= 0.)
    if (capacityGrams <= 0) {
      throw new IllegalArgumentException("Capacity must be positive");
    }
    List<Parcel> sortedParcels = repo.all().stream()
      .sorted(Comparator.comparing(Parcel::priority).reversed()
        .thenComparingInt(Parcel::pricePence)
        .thenComparingLong(Parcel::getId))
      .toList();

    List<Quote> loaded = new ArrayList<>();
    List<Long> skippedIds = new ArrayList<>();
    int remainingCapacity = capacityGrams;
    long totalPrice = 0;
    for (Parcel parcel : sortedParcels) {
      if (parcel.getWeightGrams() <= remainingCapacity) {
        loaded.add(toQuote(parcel));
        remainingCapacity -= parcel.getWeightGrams();
        totalPrice += parcel.pricePence();
      } else {
        skippedIds.add(parcel.getId());
      }
    }
    int weightUsed = capacityGrams - remainingCapacity;

    return new VanLoad(capacityGrams, weightUsed, remainingCapacity, (int) totalPrice, loaded, skippedIds);
  }

  // You may add a private helper method for building Quote objects.
  private Quote toQuote(Parcel parcel) {
    return new Quote(
      parcel.getId(),
      parcel.getTrackingCode(),
      parcel.getWeightGrams(),
      parcel.priority(),
      parcel.pricePence()
    );
  }


}
