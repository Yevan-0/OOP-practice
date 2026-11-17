package edu.practice.d05.model;

/**
 * One team's record in a five-a-side tournament. Given: don't change it.
 * <p>
 * A win is worth 3 points and a draw 1 point. A loss is worth 0.
 * Goal difference = goals scored - goals conceded (it can be negative).
 */
public class Team {

  private final String name;
  private final int won;
  private final int drawn;
  private final int lost;
  private final int goalsFor;
  private final int goalsAgainst;

  public Team(String name, int won, int drawn, int lost, int goalsFor, int goalsAgainst) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
    if (won < 0 || drawn < 0 || lost < 0) throw new IllegalArgumentException("results cannot be negative");
    if (goalsFor < 0 || goalsAgainst < 0) throw new IllegalArgumentException("goals cannot be negative");
    this.name = name.trim();
    this.won = won;
    this.drawn = drawn;
    this.lost = lost;
    this.goalsFor = goalsFor;
    this.goalsAgainst = goalsAgainst;
  }

  public String getName() {
    return name;
  }

  public int getWon() {
    return won;
  }

  public int getDrawn() {
    return drawn;
  }

  public int getLost() {
    return lost;
  }

  public int getGoalsFor() {
    return goalsFor;
  }

  public int getGoalsAgainst() {
    return goalsAgainst;
  }

  public int getPlayed() {
    return won + drawn + lost;
  }

  /**
   * 3 per win + 1 per draw.
   */
  public int getPoints() {
    return 3 * won + drawn;
  }

  /**
   * goalsFor - goalsAgainst, e.g. 14 - 6 = +8, 7 - 15 = -8.
   */
  public int getGoalDifference() {
    return goalsFor - goalsAgainst;
  }

  /**
   * Makes test failure messages readable: [Rovers, City] instead of [Team@1b6d3586, ...].
   */
  @Override
  public String toString() {
    return name;
  }
}
