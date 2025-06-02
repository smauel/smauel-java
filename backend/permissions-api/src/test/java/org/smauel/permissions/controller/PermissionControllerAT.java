package org.smauel.permissions.controller;

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
import org.smauel.permissions.dto.request.CreatePermissionRequest;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Permission Controller Tests")
class PermissionControllerAT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Clean slate for each test
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create permission when valid params")
    @Description("POST /api/v1/permissions")
    void shouldCreatePermissionSuccessfully() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setName("READ_USER");
        request.setDescription("Read user data");
        request.setType(PermissionType.RESOURCE);
        request.setResource("user");
        request.setAction(Action.READ);

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("READ_USER"));
    }

    @Test
    @DisplayName("Should return permission by id when found")
    @Description("GET /api/v1/permissions/{id}")
    void shouldReturnPermissionById() throws Exception {
        Permission permission = Permission.builder()
                .name("UPDATE_USER")
                .description("Update user data")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.UPDATE)
                .build();
        permission = permissionRepository.save(permission);

        mockMvc.perform(get("/api/v1/permissions/" + permission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UPDATE_USER"));
    }

    @Test
    @DisplayName("Should return all permissions")
    @Description("GET /api/v1/permissions")
    void shouldReturnAllPermissions() throws Exception {
        Permission permission1 = Permission.builder()
                .name("CREATE_USER")
                .description("Create new user")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.CREATE)
                .build();
        Permission permission2 = Permission.builder()
                .name("DELETE_USER")
                .description("Delete user")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.DELETE)
                .build();
        permissionRepository.saveAll(List.of(permission1, permission2));

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
