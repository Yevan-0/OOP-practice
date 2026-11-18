package edu.practice.courier.dto;

public record Quote(long id, String trackingCode, int weightGrams, int priority, int pricePence) {
}
