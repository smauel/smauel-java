package org.smauel.users.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.smauel.users.model.User;
import org.smauel.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("User Controller")
class UserControllerAT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean slate for each test
    }

    @Test
    @DisplayName("Should be able to create a user when valid params")
    @Description("POST /api/v1/users")
    void shouldCreateUserSuccessfully() throws Exception {
        User user = new User();
        user.setUsername("johndoe");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    @DisplayName("Should return user by id when found")
    @Description("GET /api/v1/users/{id}")
    void shouldReturnUserById() throws Exception {
        User user = new User();
        user.setUsername("janedoe");
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user = userRepository.save(user);

        mockMvc.perform(get("/api/v1/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"));
    }

    @Test
    @DisplayName("Should return all users")
    @Description("GET /api/v1/users")
    void shouldReturnAllUsers() throws Exception {
        User user1 = User.builder()
                .username("user1")
                .fullName("User One")
                .email("user1@example.com")
                .build();
        User user2 = User.builder()
                .username("user2")
                .fullName("User Two")
                .email("user2@example.com")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
