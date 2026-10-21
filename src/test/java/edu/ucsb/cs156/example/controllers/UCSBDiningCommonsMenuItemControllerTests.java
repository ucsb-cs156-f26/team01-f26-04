package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository repository;

  @MockitoBean UserRepository userRepository;

  // GET /all tests

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    UCSBDiningCommonsMenuItem item2 =
        UCSBDiningCommonsMenuItem.builder()
            .id(2L)
            .diningCommonsCode("carrillo")
            .name("Pizza")
            .station("Pizza Station")
            .build();

    List<UCSBDiningCommonsMenuItem> expected = List.of(item1, item2);

    when(repository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(repository).findAll();

    assertEquals(mapper.writeValueAsString(expected), response.getResponse().getContentAsString());
  }

  // POST /post tests

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Alfredo")
                .param("station", "Entrees")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Alfredo")
                .param("station", "Entrees")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_menu_item() throws Exception {
    UCSBDiningCommonsMenuItem incoming =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    UCSBDiningCommonsMenuItem saved =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    when(repository.save(eq(incoming))).thenReturn(saved);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItem/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Chicken Alfredo")
                    .param("station", "Entrees")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(repository).save(eq(incoming));

    assertEquals(mapper.writeValueAsString(saved), response.getResponse().getContentAsString());
  }

  @Test
  public void user_can_get_menu_item_by_id() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(42L)
            .diningCommonsCode("ortega")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    when(repository.findById(42L)).thenReturn(Optional.of(item));

    mockMvc
        .perform(
            get("/api/UCSBDiningCommonsMenuItem")
                .param("id", "42")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.diningCommonsCode").value("ortega"))
        .andExpect(jsonPath("$.name").value("Chicken Alfredo"))
        .andExpect(jsonPath("$.station").value("Entrees"));

    verify(repository, times(1)).findById(42L);
  }

  @Test
  public void user_gets_404_for_nonexistent_menu_item() throws Exception {
    when(repository.findById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/UCSBDiningCommonsMenuItem")
                .param("id", "999")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isNotFound())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id 999 not found")));

    verify(repository, times(1)).findById(999L);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_menu_item() throws Exception {
    // arrange
    UCSBDiningCommonsMenuItem original =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("ortega")
            .name("Pizza")
            .station("Pizza Station")
            .build();

    UCSBDiningCommonsMenuItem incoming =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("carrillo")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    UCSBDiningCommonsMenuItem expected =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("carrillo")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    String requestBody = mapper.writeValueAsString(incoming);

    when(repository.findById(eq(67L))).thenReturn(Optional.of(original));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(67L);
    verify(repository, times(1)).save(expected);
    assertEquals(mapper.writeValueAsString(expected), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_nonexistent_menu_item() throws Exception {
    // arrange
    UCSBDiningCommonsMenuItem incoming =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    String requestBody = mapper.writeValueAsString(incoming);

    when(repository.findById(eq(999L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(999L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 999 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_user_cannot_edit_menu_item() throws Exception {
    UCSBDiningCommonsMenuItem incoming =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Chicken Alfredo")
            .station("Entrees")
            .build();

    mockMvc
        .perform(
            put("/api/UCSBDiningCommonsMenuItem")
                .param("id", "67")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }
}
