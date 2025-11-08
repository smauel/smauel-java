package org.smauel.permissions.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

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

    private static final String NON_EXISTENT_ID = "9999";
    private static final String NON_EXISTENT_NAME = "NON_EXISTENT_PERMISSION_NAME";

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
    @DisplayName("Should return 400 Bad Request when creating permission with null name")
    @Description("POST /api/v1/permissions - Invalid Data")
    void shouldReturnBadRequestForCreatePermissionWhenNameIsNull() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest();
        // Name is null, which should be invalid based on @Valid and potential annotations in CreatePermissionRequest
        request.setDescription("Read user data");
        request.setType(PermissionType.RESOURCE);
        request.setResource("user");
        request.setAction(Action.READ);

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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
    @DisplayName("Should return 404 Not Found when getting permission by non-existent id")
    @Description("GET /api/v1/permissions/{id} - Not Found")
    void shouldReturnNotFoundForGetPermissionByIdWhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/" + NON_EXISTENT_ID)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return permission by name when found")
    @Description("GET /api/v1/permissions/name/{name}")
    void shouldReturnPermissionByNameWhenFound() throws Exception {
        String permissionName = "VIEW_DASHBOARD";
        Permission permission = Permission.builder()
                .name(permissionName)
                .description("View dashboard analytics")
                .type(PermissionType.RESOURCE)
                .resource("dashboard")
                .action(Action.READ)
                .build();
        permissionRepository.save(permission);

        mockMvc.perform(get("/api/v1/permissions/name/" + permissionName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(permissionName));
    }

    @Test
    @DisplayName("Should return 404 Not Found when getting permission by non-existent name")
    @Description("GET /api/v1/permissions/name/{name} - Not Found")
    void shouldReturnNotFoundForGetPermissionByNameWhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/name/" + NON_EXISTENT_NAME)).andExpect(status().isNotFound());
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

    @Test
    @DisplayName("Should return permissions by resource")
    @Description("GET /api/v1/permissions/resource/{resource}")
    void shouldReturnPermissionsByResource() throws Exception {
        String targetResource = "product";
        Permission permission1 = Permission.builder()
                .name("VIEW_PRODUCT")
                .description("View product details")
                .type(PermissionType.RESOURCE)
                .resource(targetResource)
                .action(Action.READ)
                .build();
        Permission permission2 = Permission.builder()
                .name("EDIT_PRODUCT")
                .description("Edit product details")
                .type(PermissionType.RESOURCE)
                .resource(targetResource)
                .action(Action.UPDATE)
                .build();
        Permission permission3 = Permission.builder() // Different resource
                .name("VIEW_ORDER")
                .description("View order details")
                .type(PermissionType.RESOURCE)
                .resource("order")
                .action(Action.READ)
                .build();
        permissionRepository.saveAll(List.of(permission1, permission2, permission3));

        mockMvc.perform(get("/api/v1/permissions/resource/" + targetResource))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].resource").value(targetResource))
                .andExpect(jsonPath("$[1].resource").value(targetResource));
    }

    @Test
    @DisplayName("Should return empty list when getting permissions by non-existent resource")
    @Description("GET /api/v1/permissions/resource/{resource} - Empty List")
    void shouldReturnEmptyListForPermissionsByNonExistentResource() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/resource/non_existent_resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return permissions by type and resource")
    @Description("GET /api/v1/permissions/type/{type}/resource/{resource}")
    void shouldReturnPermissionsByTypeAndResource() throws Exception {
        PermissionType targetType = PermissionType.RESOURCE;
        String targetResource = "inventory";

        Permission permission1 = Permission.builder()
                .name("READ_INVENTORY")
                .description("Read inventory stock")
                .type(targetType)
                .resource(targetResource)
                .action(Action.READ)
                .build();
        Permission permission2 = Permission.builder() // Different type
                .name("ACCESS_INVENTORY_UI")
                .description("Access inventory UI")
                .type(PermissionType.FEATURE)
                .resource(targetResource)
                .action(Action.READ)
                .build();
        Permission permission3 = Permission.builder() // Different resource
                .name("READ_PRODUCT")
                .description("Read product data")
                .type(targetType)
                .resource("product")
                .action(Action.READ)
                .build();
        Permission permission4 = Permission.builder() // Matching
                .name("UPDATE_INVENTORY")
                .description("Update inventory stock")
                .type(targetType)
                .resource(targetResource)
                .action(Action.UPDATE)
                .build();
        permissionRepository.saveAll(List.of(permission1, permission2, permission3, permission4));

        mockMvc.perform(get("/api/v1/permissions/type/" + targetType + "/resource/" + targetResource))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value(targetType.toString()))
                .andExpect(jsonPath("$[0].resource").value(targetResource))
                .andExpect(jsonPath("$[1].type").value(targetType.toString()))
                .andExpect(jsonPath("$[1].resource").value(targetResource));
    }

    @Test
    @DisplayName("Should return empty list for permissions by type and resource when no matches")
    @Description("GET /api/v1/permissions/type/{type}/resource/{resource} - Empty List")
    void shouldReturnEmptyListForPermissionsByTypeAndResourceWhenNoMatches() throws Exception {
        PermissionType targetType = PermissionType.RESOURCE;
        String targetResource = "non_existent_resource_for_type";

        Permission permission1 = Permission.builder()
                .name("READ_INVENTORY")
                .type(PermissionType.RESOURCE)
                .resource("inventory")
                .action(Action.READ)
                .build();
        permissionRepository.save(permission1);

        mockMvc.perform(get("/api/v1/permissions/type/" + targetType + "/resource/" + targetResource))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should delete permission successfully")
    @Description("DELETE /api/v1/permissions/{id}")
    void shouldDeletePermissionSuccessfully() throws Exception {
        Permission permission = Permission.builder()
                .name("MANAGE_SETTINGS")
                .description("Manage system settings")
                .type(PermissionType.SYSTEM)
                .resource("system")
                .action(Action.UPDATE)
                .build();
        permission = permissionRepository.save(permission);

        mockMvc.perform(delete("/api/v1/permissions/" + permission.getId())).andExpect(status().isNoContent());

        // Verify it's actually deleted
        mockMvc.perform(get("/api/v1/permissions/" + permission.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existent permission")
    @Description("DELETE /api/v1/permissions/{id} - Not Found")
    void shouldReturnNotFoundForDeletePermissionWhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/permissions/" + NON_EXISTENT_ID)).andExpect(status().isNotFound());
    }
}
