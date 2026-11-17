package edu.practice.d09.controller;

import edu.practice.d09.dto.NewPlant;
import edu.practice.d09.dto.PriceQuote;
import edu.practice.d09.model.Plant;
import edu.practice.d09.service.PlantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

  private final PlantService service;

  public PlantController(PlantService service) {
    this.service = service;
  }

  // Every method below is ONE line: call the matching PlantService method and return its result.
  // The controller does no thinking: no if, no loops, no maths, no repository.
  //
  // TODO d09-1: GET  /api/plants                                  -> service.all()
  // TODO d09-2: GET  /api/plants/{id}                             -> service.get(id)              (@PathVariable)
  // TODO d09-3: GET  /api/plants/{id}/price?qty=3                 -> service.price(id, qty)       (@RequestParam)
  // TODO d09-4: GET  /api/plants/search?maxPriceCents=500&inStockOnly=true
  //                                                               -> service.search(max, inStock)
  //             inStockOnly is OPTIONAL: when it is left out it means false.
  // TODO d09-5: POST /api/plants   JSON body like {"id":6,"name":"Rosemary","priceCents":399,"stock":7}
  //                                                               -> service.add(body)            (@RequestBody NewPlant)
  //             It must answer HTTP 201 Created (not 200) with the created plant.

  @GetMapping
  public List<Plant> all() {
    return service.all();
  }

  @GetMapping("/{id}")
  public Plant get(@PathVariable long id) {
    return service.get(id);
  }

  @GetMapping("/{id}/price")
  public PriceQuote price(@PathVariable long id, @RequestParam int qty) {
    return service.price(id, qty);
  }

  @GetMapping("/search")
  public List<Plant> search(@RequestParam int maxPriceCents, @RequestParam(defaultValue = "false") boolean inStockOnly) {
    return service.search(maxPriceCents, inStockOnly);
  }

  @PostMapping
  public ResponseEntity<Plant> add(@RequestBody NewPlant body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
  }
}
