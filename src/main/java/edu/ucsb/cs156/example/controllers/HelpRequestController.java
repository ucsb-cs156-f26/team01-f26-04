package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for HelpRequests */
@Tag(name = "HelpRequest")
@RequestMapping("/api/HelpRequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /**
   * List all help requests.
   *
   * @return an iterable of HelpRequest
   */
  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    return helpRequestRepository.findAll();
  }

  /**
   * Get a single help request by id
   *
   * @param id the id of the help request
   * @return a HelpRequest
   */
  @Operation(summary = "Get a single help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest helpRequest =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    return helpRequest;
  }

  /**
   * Update a single help request
   *
   * @param id id of the help request to update
   * @param incoming the new help request values
   * @return the updated help request
   */
  @Operation(summary = "Update a single help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public HelpRequest updateHelpRequest(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid HelpRequest incoming) {

    HelpRequest helpRequest =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    helpRequest.setRequesterEmail(incoming.getRequesterEmail());
    helpRequest.setTeamId(incoming.getTeamId());
    helpRequest.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
    helpRequest.setRequestTime(incoming.getRequestTime());
    helpRequest.setExplanation(incoming.getExplanation());
    helpRequest.setSolved(incoming.getSolved());

    helpRequestRepository.save(helpRequest);

    return helpRequest;
  }

  /**
   * Delete a single help request.
   *
   * @param id the ID of the help request to delete
   * @return a message indicating whether the record was deleted
   */
  @Operation(summary = "Delete a HelpRequest by ID")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public ResponseEntity<Object> deleteHelpRequest(@Parameter(name = "id") @RequestParam Long id) {

    Optional<HelpRequest> result = helpRequestRepository.findById(id);

    if (result.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(genericMessage("record %s not found".formatted(id)));
    }

    helpRequestRepository.delete(result.get());
    return ResponseEntity.ok(genericMessage("record %s deleted".formatted(id)));
  }

  /**
   * Create a new help request.
   *
   * @param requesterEmail email of the person requesting help
   * @param teamId identifier of the team
   * @param tableOrBreakoutRoom table or breakout room where help is needed
   * @param requestTime time the help was requested, in ISO date-time format
   * @param explanation description of the help needed
   * @param solved whether the request has been solved
   * @return the saved help request
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(
              name = "requestTime",
              description = "Time of request in ISO format, e.g. 2026-10-17T14:30:00")
          @RequestParam("requestTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime requestTime,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved) {

    log.info("requestTime={}", requestTime);

    HelpRequest helpRequest = new HelpRequest();
    helpRequest.setRequesterEmail(requesterEmail);
    helpRequest.setTeamId(teamId);
    helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    helpRequest.setRequestTime(requestTime);
    helpRequest.setExplanation(explanation);
    helpRequest.setSolved(solved);

    return helpRequestRepository.save(helpRequest);
  }
}
