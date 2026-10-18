package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockitoBean HelpRequestRepository helpRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isOk());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {
    LocalDateTime requestTime1 = LocalDateTime.parse("2026-10-17T14:30:00");
    LocalDateTime requestTime2 = LocalDateTime.parse("2026-10-17T15:00:00");

    HelpRequest request1 =
        HelpRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 5")
            .requestTime(requestTime1)
            .explanation("Need help with Java")
            .solved(false)
            .build();

    HelpRequest request2 =
        HelpRequest.builder()
            .requesterEmail("student2@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("Breakout Room 2")
            .requestTime(requestTime2)
            .explanation("Need help with testing")
            .solved(true)
            .build();

    ArrayList<HelpRequest> expectedRequests = new ArrayList<>(Arrays.asList(request1, request2));

    when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/HelpRequest/post")
                .param("requesterEmail", "student1@ucsb.edu")
                .param("teamId", "team01")
                .param("tableOrBreakoutRoom", "Table 5")
                .param("requestTime", "2026-10-17T14:30:00")
                .param("explanation", "Need help with Java")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/HelpRequest/post")
                .param("requesterEmail", "student1@ucsb.edu")
                .param("teamId", "team01")
                .param("tableOrBreakoutRoom", "Table 5")
                .param("requestTime", "2026-10-17T14:30:00")
                .param("explanation", "Need help with Java")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_help_request() throws Exception {
    LocalDateTime requestTime = LocalDateTime.parse("2026-10-17T14:30:00");

    HelpRequest expectedRequest =
        HelpRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 5")
            .requestTime(requestTime)
            .explanation("Need help with Java")
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(expectedRequest))).thenReturn(expectedRequest);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/HelpRequest/post")
                    .param("requesterEmail", "student1@ucsb.edu")
                    .param("teamId", "team01")
                    .param("tableOrBreakoutRoom", "Table 5")
                    .param("requestTime", "2026-10-17T14:30:00")
                    .param("explanation", "Need help with Java")
                    .param("solved", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).save(argThat(request -> request.getSolved()));
    String expectedJson = mapper.writeValueAsString(expectedRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
