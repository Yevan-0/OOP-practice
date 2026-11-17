package edu.practice.d05.dto;

/**
 * Given. One row of the published leaderboard: what the API would send back as JSON.
 */
public record Standing(String team, int points, int goalDifference) {
}
