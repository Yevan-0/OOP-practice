package edu.practice.d06.service;

import edu.practice.d06.dto.RoomQuote;
import edu.practice.d06.dto.StayRequest;
import edu.practice.d06.model.Room;
import edu.practice.d06.repo.RoomRepository;

import java.nio.channels.FileLockInterruptionException;
import java.util.Comparator;
import java.util.List;

/**
 * Turns hotel rooms (model) into price quotes (DTOs).
 */
public class QuoteService {

  private final RoomRepository repo;

  public QuoteService(RoomRepository repo) {
    this.repo = repo;
  }

  /**
   * The quote for one room. Unknown id -> IllegalArgumentException; nights <= 0 -> IllegalArgumentException.
   */
  public RoomQuote quote(long roomId, int nights) {
    // TODO d06-4: look the room up in repo; null -> throw new IllegalArgumentException("...").
    //             Then return toQuote(room, nights). (Room.priceCents already rejects nights <= 0.)
    Room room = repo.get(roomId);
    if (room == null) {
      throw new IllegalArgumentException("Room is missing" + roomId);
    }
    return toQuote(room, nights);
  }

  /**
   * A quote for every room that can sleep `request.guests()` people, for `request.nights()` nights,
   * cheapest first; equal totals -> lower room id first.
   */
  public List<RoomQuote> quotes(StayRequest request) {
    // TODO d06-5: from repo.all(): keep rooms with capacity >= request.guests(),
    //             turn each into a RoomQuote with toQuote(room, request.nights()),
    //             sort by totalCents ascending, then roomId ascending, and return the list.
    //             (StayRequest is a record: its accessors are request.nights() and request.guests().)
    return repo.all().stream()
      .filter(room -> room.getCapacity() >= request.guests())
      .map(room -> toQuote(room, request.nights()))
      .sorted(Comparator.comparingInt(RoomQuote::totalCents)
        .thenComparing(RoomQuote::roomId)
      )
      .toList();
  }

  // TODO d06-3: write the model -> DTO mapping ONCE, in a private helper both methods above can use:
  //             private RoomQuote toQuote(Room room, int nights) { return new RoomQuote(...); }
  private RoomQuote toQuote(Room room, int nights) {
    return new RoomQuote(room.getId(), room.getNumber(), room.type(), nights, room.priceCents(nights));
  }
}
