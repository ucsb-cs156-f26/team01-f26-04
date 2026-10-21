package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository repository;

  @Operation(summary = "List all UCSB Dining Commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allMenuItems() {
    return repository.findAll();
  }

  @Operation(summary = "Create a new UCSB Dining Commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postMenuItem(
      @Parameter(description = "Dining commons code, e.g. ortega") @RequestParam
          String diningCommonsCode,
      @Parameter(description = "Name of the menu item") @RequestParam String name,
      @Parameter(description = "Station serving the item") @RequestParam String station) {
    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();

    item.setDiningCommonsCode(diningCommonsCode);
    item.setName(name);
    item.setStation(station);

    return repository.save(item);
  }

  @Operation(summary = "Get a single UCSB Dining Commons menu item by ID")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getMenuItem(
      @Parameter(description = "ID of the menu item") @RequestParam long id) {

    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));
  }

  @Operation(summary = "Update a single UCSB Dining Commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem item =
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    item.setDiningCommonsCode(incoming.getDiningCommonsCode());
    item.setName(incoming.getName());
    item.setStation(incoming.getStation());

    repository.save(item);

    return item;
  }
}
