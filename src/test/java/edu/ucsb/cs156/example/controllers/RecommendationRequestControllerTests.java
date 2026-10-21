package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().isOk());
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest").param("id", "7"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "requester@example.com")
                .param("professorEmail", "professor@example.com")
                .param("dateRequested", "2026-10-20T10:00:00")
                .param("dateNeeded", "2026-11-20T10:00:00")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "requester@example.com")
                .param("professorEmail", "professor@example.com")
                .param("dateRequested", "2026-10-20T10:00:00")
                .param("dateNeeded", "2026-11-20T10:00:00")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_recommendation_request_by_id() throws Exception {
    RecommendationRequest request =
        RecommendationRequest.builder()
            .id(7L)
            .requesterEmail("requester@example.com")
            .professorEmail("professor@example.com")
            .explanation("Please write a recommendation.")
            .dateRequested(LocalDateTime.parse("2026-10-20T10:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-11-20T10:00:00"))
            .done(false)
            .build();
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(request));

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(7L);
    assertEquals(mapper.writeValueAsString(request), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_not_found_for_missing_recommendation_request() throws Exception {
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(7L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendation_requests() throws Exception {
    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .requesterEmail("one@example.com")
            .professorEmail("professor@example.com")
            .dateRequested(LocalDateTime.parse("2026-10-20T10:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-11-20T10:00:00"))
            .done(false)
            .build();
    RecommendationRequest request2 =
        RecommendationRequest.builder()
            .requesterEmail("two@example.com")
            .professorEmail("professor@example.com")
            .dateRequested(LocalDateTime.parse("2026-10-21T10:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-11-21T10:00:00"))
            .done(false)
            .build();
    ArrayList<RecommendationRequest> expectedRequests =
        new ArrayList<>(Arrays.asList(request1, request2));
    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();
    assertEquals(
        mapper.writeValueAsString(expectedRequests), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_a_recommendation_request() throws Exception {
    RecommendationRequest expectedRequest =
        RecommendationRequest.builder()
            .requesterEmail("requester@example.com")
            .professorEmail("professor@example.com")
            .dateRequested(LocalDateTime.parse("2026-10-20T10:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-11-20T10:00:00"))
            .done(false)
            .build();
    when(recommendationRequestRepository.save(eq(expectedRequest))).thenReturn(expectedRequest);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "requester@example.com")
                    .param("professorEmail", "professor@example.com")
                    .param("dateRequested", "2026-10-20T10:00:00")
                    .param("dateNeeded", "2026-11-20T10:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(expectedRequest);
    assertEquals(
        mapper.writeValueAsString(expectedRequest), response.getResponse().getContentAsString());
  }
}
