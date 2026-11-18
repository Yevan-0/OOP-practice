package edu.practice.d08.controller;

import edu.practice.d08.dto.BookingRequest;
import edu.practice.d08.dto.NewTicket;
import edu.practice.d08.model.Ticket;
import edu.practice.d08.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Given and finished. Notice: no try/catch anywhere. When the service throws,
 * the exception flies straight out of these methods, and Spring goes looking
 * for an @ExceptionHandler that matches it.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) { this.service = service; }

    @GetMapping
    public List<Ticket> all() { return service.all(); }

    @GetMapping("/{id}")
    public Ticket get(@PathVariable long id) { return service.get(id); }

    @PostMapping("/{id}/book")
    public Ticket book(@PathVariable long id, @RequestBody BookingRequest body) {
        return service.book(id, body.customer());
    }

    @PostMapping
    public ResponseEntity<Ticket> add(@RequestBody NewTicket body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
    }


}
