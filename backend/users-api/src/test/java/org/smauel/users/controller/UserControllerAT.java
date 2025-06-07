package org.smauel.users.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.smauel.users.dto.request.CreateUserRequest;
import org.smauel.users.dto.request.UpdateUserRequest;
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
        CreateUserRequest user = new CreateUserRequest();
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
    @DisplayName("Should return 400 Bad Request when creating user with invalid params")
    @Description("POST /api/v1/users")
    void shouldReturnBadRequestWhenCreatingUserWithInvalidParams() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(""); // Username is required
        createUserRequest.setFullName("John Doe");
        createUserRequest.setEmail("john@email.com");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest());
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
    @DisplayName("Should return 404 when getting user by non-existent id")
    @Description("GET /api/v1/users/{id}")
    void shouldReturnNotFoundWhenGettingUserWithNonExistentId() throws Exception {
        long nonExistentId = 9999L;
        mockMvc.perform(get("/api/v1/users/" + nonExistentId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return user by username when found")
    @Description("GET /api/v1/users/username/{name}")
    void shouldReturnUserByName() throws Exception {
        User user = new User();
        user.setUsername("janedoe");
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user = userRepository.save(user);

        mockMvc.perform(get("/api/v1/users/username/" + user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"));
    }

    @Test
    @DisplayName("Should return 404 when getting user by non-existent username")
    @Description("GET /api/v1/users/username/{username}")
    void shouldReturnNotFoundWhenGettingUserByNonExistentUsername() throws Exception {
        String nonExistentUsername = "nonexistentuser";
        mockMvc.perform(get("/api/v1/users/username/" + nonExistentUsername)).andExpect(status().isNotFound());
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

    @Test
    @DisplayName("Should update user when valid params")
    @Description("PUT /api/v1/users/{id}")
    void shouldUpdateUserSuccessfully() throws Exception {
        User existingUser = User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();
        existingUser = userRepository.save(existingUser);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFullName("Updated Test User");
        updateUserRequest.setEmail("updated.test@example.com");

        mockMvc.perform(put("/api/v1/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser")) // Username should not change
                .andExpect(jsonPath("$.fullName").value("Updated Test User"))
                .andExpect(jsonPath("$.email").value("updated.test@example.com"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent user")
    @Description("PUT /api/v1/users/{id}")
    void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
        long nonExistentId = 9998L;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFullName("Non Existent User Update");
        updateUserRequest.setEmail("nonexistent.update@example.com");

        mockMvc.perform(put("/api/v1/users/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when updating user with invalid email")
    @Description("PUT /api/v1/users/{id}")
    void shouldReturnBadRequestWhenUpdatingUserWithInvalidEmail() throws Exception {
        User existingUser = User.builder()
                .username("validuser")
                .fullName("Valid User")
                .email("valid@example.com")
                .build();
        existingUser = userRepository.save(existingUser);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFullName("Updated Name");
        updateUserRequest.setEmail("invalid-email-format"); // Invalid email

        mockMvc.perform(put("/api/v1/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete user successfully")
    @Description("DELETE /api/v1/users/{id}")
    void shouldDeleteUserSuccessfully() throws Exception {
        User userToDelete = User.builder()
                .username("deleteuser")
                .fullName("Delete User")
                .email("delete@example.com")
                .build();
        userToDelete = userRepository.save(userToDelete);

        mockMvc.perform(delete("/api/v1/users/" + userToDelete.getId())).andExpect(status().isNoContent());

        // Verify the user is actually deleted
        mockMvc.perform(get("/api/v1/users/" + userToDelete.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    @Description("DELETE /api/v1/users/{id}")
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
        long nonExistentId = 9997L;
        mockMvc.perform(delete("/api/v1/users/" + nonExistentId)).andExpect(status().isNotFound());
    }
}
