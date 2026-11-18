package edu.practice.courier.dto;

public record NewStandardParcel(long id, String trackingCode, int weightGrams, int ratePerBlockPence) {
}
