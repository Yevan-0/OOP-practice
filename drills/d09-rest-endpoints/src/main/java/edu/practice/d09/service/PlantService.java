package edu.practice.d09.service;

import edu.practice.d09.dto.NewPlant;
import edu.practice.d09.dto.PriceQuote;
import edu.practice.d09.error.PlantNotFoundException;
import edu.practice.d09.model.Plant;
import edu.practice.d09.repo.PlantRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Given and finished: ALL the thinking lives here (validation, maths, filtering, sorting).
 * Your controller only has to call these five methods.
 */
@Service
public class PlantService {

  private final PlantRepository repo;

  public PlantService(PlantRepository repo) {
    this.repo = repo;
  }

  /**
   * Every plant, lowest id first.
   */
  public List<Plant> all() {
    return repo.all().stream().sorted(Comparator.comparingLong(Plant::getId)).toList();
  }

  /**
   * One plant, or PlantNotFoundException (404).
   */
  public Plant get(long id) {
    Plant plant = repo.get(id);
    if (plant == null) throw new PlantNotFoundException(id);
    return plant;
  }

  /**
   * The price of `qty` of one plant. qty must be positive (else 400).
   */
  public PriceQuote price(long id, int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty must be positive");
    Plant plant = get(id);
    try {
      return new PriceQuote(plant.getId(), plant.getName(), qty, Math.multiplyExact(qty, plant.getPriceCents()));
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("qty is too large", e);
    }
  }

  /**
   * Plants costing at most maxPriceCents, cheapest first (then lowest id). Optionally only plants in stock.
   */
  public List<Plant> search(int maxPriceCents, boolean inStockOnly) {
    if (maxPriceCents <= 0) throw new IllegalArgumentException("maxPriceCents must be positive");
    return repo.all().stream()
      .filter(p -> p.getPriceCents() <= maxPriceCents)
      .filter(p -> !inStockOnly || p.getStock() > 0)
      .sorted(Comparator.comparingInt(Plant::getPriceCents).thenComparingLong(Plant::getId))
      .toList();
  }

  /**
   * Validates, stores and returns a new plant. Bad input -> IllegalArgumentException (400), nothing stored.
   */
  public Plant add(NewPlant body) {
    if (body == null) {
      throw new IllegalArgumentException("plant details are required");
    }
    if (body.id() <= 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    if (body.name() == null || body.name().isBlank()) {
      throw new IllegalArgumentException("name is required");
    }
    if (body.priceCents() <= 0) {
      throw new IllegalArgumentException("priceCents must be positive");
    }
    if (body.stock() < 0) {
      throw new IllegalArgumentException("stock cannot be negative");
    }
    if (repo.contains(body.id())) {
      throw new IllegalArgumentException("plant " + body.id() + " already exists");
    }
    Plant plant = new Plant(body.id(), body.name().trim(), body.priceCents(), body.stock());
    repo.save(plant);
    return plant;
  }
}
