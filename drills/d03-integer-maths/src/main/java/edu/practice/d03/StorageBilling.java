package edu.practice.d03;

/**
 * Cloud storage is sold in BLOCKS of 100 GB. Any started block is charged in full.
 * <p>
 * 1-100 GB  -> 1 block,   101-200 GB -> 2 blocks,   ...
 * <p>
 * Price: the first 10 blocks cost `centsPerBlock` each.
 * Every block AFTER the 10th costs half that, rounded UP to a whole cent.
 * <p>
 * Everything is in whole cents (int). Bad input or a result too big for an int
 * must throw IllegalArgumentException, never return a wrong (wrapped-around) number.
 */
public final class StorageBilling {

  public static final int GB_PER_BLOCK = 100;
  public static final int FULL_PRICE_BLOCKS = 10;

  private StorageBilling() {
  }

  /**
   * How many 100 GB blocks are needed to hold `gb` gigabytes.
   */
  public static int blocksFor(int gb) {
    // TODO d03-1: reject gb <= 0; ceiling division WITHOUT overflow (gb can be Integer.MAX_VALUE).
    if (gb <= 0) {
      throw new IllegalArgumentException("GB must be positive!");
    }
    return (gb - 1) / GB_PER_BLOCK + 1;
  }

  /**
   * Half of `cents`, rounded up: 999 -> 500, 1000 -> 500, 1 -> 1.
   */
  public static int halfRoundedUp(int cents) {
    // TODO d03-2: reject cents <= 0; round up.
    if (cents <= 0) {
      throw new IllegalArgumentException("Cents must be positive");
    }
    return (cents - 1) / 2 + 1;
  }

  /**
   * Monthly price in cents for storing `gb` gigabytes.
   */
  public static int priceCents(int gb, int centsPerBlock) {
    // TODO d03-3: reject centsPerBlock <= 0.
    //             blocks = blocksFor(gb); first 10 at full price, the rest at halfRoundedUp(centsPerBlock).
    //             Use Math.multiplyExact / Math.addExact and turn their ArithmeticException
    //             into an IllegalArgumentException.
    if (centsPerBlock <= 0) {
      throw new IllegalArgumentException("Cents per Block should be positive");
    }
    int blocks = blocksFor(gb);
    int full = Math.min(blocks, FULL_PRICE_BLOCKS);
    int rest = blocks - full;
    try {
      int halfCost = halfRoundedUp(centsPerBlock);
      int fullCost = Math.multiplyExact(full, centsPerBlock);
      int restCost = Math.multiplyExact(rest, halfCost);
      return Math.addExact(fullCost, restCost);
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("Far too large:" + e);
    }
  }
}
