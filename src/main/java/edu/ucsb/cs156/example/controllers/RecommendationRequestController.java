package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequestController */
@Tag(name = "RecommendationRequests")
@RequestMapping({"/api/recommendationrequests", "/api/RecommendationRequest"})
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all recommendation requests
   *
   * @return an iterable of RecommendationRequest
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> requests = recommendationRequestRepository.findAll();
    return requests;
  }

  /**
   * Get a single recommendation request by id
   *
   * @param id the id of the recommendation request
   * @return a RecommendationRequest
   */
  @Operation(summary = "Get a single recommendation request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam("id") Long id) {
    return recommendationRequestRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("id %s not found".formatted(id)));
  }

  /**
   * Update a recommendation request.
   *
   * @param id the id of the request to update
   * @param incoming the new request field values
   * @return the updated recommendation request
   */
  @Operation(summary = "Update a recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id") @RequestParam("id") Long id,
      @RequestBody @Valid RecommendationRequest incoming) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("id %s not found".formatted(id)));

    recommendationRequest.setRequesterEmail(incoming.getRequesterEmail());
    recommendationRequest.setProfessorEmail(incoming.getProfessorEmail());
    recommendationRequest.setExplanation(incoming.getExplanation());
    recommendationRequest.setDateRequested(incoming.getDateRequested());
    recommendationRequest.setDateNeeded(incoming.getDateNeeded());
    recommendationRequest.setDone(incoming.getDone());

    return recommendationRequestRepository.save(recommendationRequest);
  }

  /**
   * Delete a recommendation request.
   *
   * @param id the id of the request to delete
   * @return a message indicating whether the request was deleted
   */
  @Operation(summary = "Delete a recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam("id") Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("record %s not found".formatted(id)));

    recommendationRequestRepository.delete(recommendationRequest);
    return genericMessage("record %s deleted".formatted(id));
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail the email of the person requesting the recommendation
   * @param professorEmail the email of the professor for whom the recommendation is requested
   * @param explanation the reason for requesting the recommendation
   * @param dateRequested the date the recommendation is requested
   * @param dateNeeded the date the recommendation is needed
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam("requesterEmail") String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam("professorEmail") String professorEmail,
      @Parameter(name = "explanation") @RequestParam(name = "explanation", required = false)
          String explanation,
      @Parameter(name = "done") @RequestParam(name = "done", defaultValue = "false") boolean done,
      @Parameter(
              name = "dateRequested",
              description =
                  "date requested (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(
              name = "dateNeeded",
              description =
                  "date needed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded) {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateRequested={}", dateRequested);
    log.info("dateNeeded={}", dateNeeded);

    RecommendationRequest recommendationRequest = new RecommendationRequest();
    recommendationRequest.setRequesterEmail(requesterEmail);
    recommendationRequest.setProfessorEmail(professorEmail);
    recommendationRequest.setExplanation(explanation);
    recommendationRequest.setDone(done);
    recommendationRequest.setDateRequested(dateRequested);
    recommendationRequest.setDateNeeded(dateNeeded);

    RecommendationRequest savedRecommendationRequest =
        recommendationRequestRepository.save(recommendationRequest);

    return savedRecommendationRequest;
  }
}
