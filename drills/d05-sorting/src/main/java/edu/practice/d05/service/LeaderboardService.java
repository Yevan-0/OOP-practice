package edu.practice.d05.service;

import edu.practice.d05.dto.Standing;
import edu.practice.d05.model.Team;

import java.util.Comparator;
import java.util.List;

/**
 * Builds the tournament leaderboard.
 * <p>
 * Leaderboard order:
 * 1. more points first                         (points DESC)
 * 2. equal points -> bigger goal difference first (goal difference DESC)
 * 3. still equal  -> name alphabetically, A to Z  (name ASC)
 * <p>
 * None of these methods may change the list they are given.
 */
public class LeaderboardService {

  /**
   * The leaderboard order as ONE Comparator.
   */
  public Comparator<Team> leaderboardOrder() {
    // TODO d05-1: build the comparator from three keys: Team::getPoints, Team::getGoalDifference, Team::getName.
    //             Careful: .reversed() flips EVERYTHING chained before it. See LESSON.md before you use it.
    return Comparator.comparingInt(Team::getPoints).reversed()
      .thenComparing(Team::getGoalDifference, Comparator.reverseOrder())
      .thenComparing(Team::getName);
  }

  /**
   * A NEW list with the teams in leaderboard order. `teams` itself is left untouched.
   */
  public List<Team> sorted(List<Team> teams) {
    // TODO d05-2: return the teams sorted with leaderboardOrder(), as a NEW list.
    //             Don't call teams.sort(...): that changes the caller's list (and crashes on List.of(...)).
    return teams.stream().sorted(leaderboardOrder()).toList();
  }

  /**
   * The published table: only teams with at least `minPoints` points, in leaderboard order,
   * each turned into a Standing. minPoints < 0 -> IllegalArgumentException.
   */
  public List<Standing> table(List<Team> teams, int minPoints) {
    // TODO d05-3: reject minPoints < 0.
    //             Then: keep teams with points >= minPoints, sort with leaderboardOrder(),
    //             turn each Team into new Standing(name, points, goalDifference), and return the list.
    if (minPoints < 0) {
      throw new IllegalArgumentException("Minimum points must be positive");
    }
    return teams.stream()
      .filter(team -> team.getPoints() >= minPoints)
      .sorted(leaderboardOrder())
      .map(team -> new Standing(team.getName(), team.getPoints(), team.getGoalDifference()))
      .toList();
  }
}
