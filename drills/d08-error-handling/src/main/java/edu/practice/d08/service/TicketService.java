package edu.practice.d08.service;

import edu.practice.d08.dto.NewTicket;
import edu.practice.d08.error.TicketConflictException;
import edu.practice.d08.error.TicketNotFoundException;
import edu.practice.d08.model.Ticket;
import edu.practice.d08.repo.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Given and finished. Read it to see WHICH exception is thrown WHEN.
 * Nothing here knows about HTTP: the service only throws. Turning exceptions
 * into status codes is the job of ApiExceptionHandler (your task).
 */
@Service
public class TicketService {

  private final TicketRepository repo;

  public TicketService(TicketRepository repo) {
    this.repo = repo;
  }

  public List<Ticket> all() {
    return repo.all().stream().sorted(Comparator.comparingLong(Ticket::getId)).toList();
  }

  public Ticket get(long id) {
    Ticket ticket = repo.get(id);
    if (ticket == null) throw new TicketNotFoundException(id);                 // -> should become 404
    return ticket;
  }

  public Ticket book(long id, String customer) {
    if (customer == null || customer.isBlank()) {
      throw new IllegalArgumentException("customer is required");              // -> should become 400
    }
    Ticket ticket = get(id);                                                      // may throw -> 404
    if (ticket.isBooked()) throw new TicketConflictException(id);                // -> should become 409
    ticket.bookFor(customer.trim());
    return ticket;
  }

  public Ticket add(NewTicket body) {
    if (body == null) {
      throw new IllegalArgumentException("ticket details are required");
    }
    if (body.id() <= 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    if (body.event() == null || body.event().isBlank()) {
      throw new IllegalArgumentException("event is required");
    }
    if (body.seat() == null || body.seat().isBlank()) {
      throw new IllegalArgumentException("seat is required");
    }
    if (body.priceCents() <= 0) {
      throw new IllegalArgumentException("priceCents must be positive");
    }
    if (repo.contains(body.id())) {
      throw new IllegalArgumentException("ticket " + body.id() + " already exists");
    }
    Ticket ticket = new Ticket(body.id(), body.event().trim(), body.seat().trim(), body.priceCents());
    repo.save(ticket);
    return ticket;
  }
}
