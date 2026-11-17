package edu.practice.d09.model;

/**
 * A plant for sale. Given. Jackson turns the getters into JSON: {"id":1,"name":"Lavender","priceCents":450,"stock":12}.
 */
public class Plant {

  private final long id;
  private final String name;
  private final int priceCents;
  private final int stock;

  public Plant(long id, String name, int priceCents, int stock) {
    this.id = id;
    this.name = name;
    this.priceCents = priceCents;
    this.stock = stock;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getPriceCents() {
    return priceCents;
  }

  public int getStock() {
    return stock;
  }
}
