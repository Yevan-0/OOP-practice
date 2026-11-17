package edu.practice.d06.dto;

/**
 * What the API sends back for one room: "room 301, a SUITE, 3 nights, costs 37,000".
 * As JSON it must look like this:
 * {"roomId":3,"number":"301","type":"SUITE","nights":3,"totalCents":37000}
 * <p>
 * The file is here (empty) so that QuoteService compiles. Filling it in is your job.
 */
// TODO d06-1: put the five components in the brackets, in this order and with these exact names:
//             roomId (long), number (String), type (String), nights (int), totalCents (int).
public record RoomQuote(long roomId, String number, String type, int nights, int totalCents) {

  // TODO d06-2: add a COMPACT constructor (look at StayRequest for the shape) that throws
  //             IllegalArgumentException when number is null/blank, nights <= 0 or totalCents < 0.
  public RoomQuote {
    if (number == null || number.isBlank()) {
      throw new IllegalArgumentException("Number cannot be left blank");
    }
    if (nights <= 0) {
      throw new IllegalArgumentException("Nights must be positive");
    }
    if (totalCents < 0) {
      throw new IllegalArgumentException("Total Cents cannot be 0");
    }
  }
}
