package edu.practice.courier.dto;

import java.util.List;

public record VanLoad(int capacityGrams, int weightUsed, int weightRemaining, int totalPence, List<Quote> loaded,
                      List<Long> skipped) {
}
