package org.smauel.permissions.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.smauel.permissions.dto.request.CreateRoleRequest;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Role Controller Tests")
@Transactional
class RoleControllerAT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private static final String API_BASE_PATH = "/api/v1/roles";
    private static final String NON_EXISTENT_ID = "99999";
    private static final String NON_EXISTENT_NAME = "NON_EXISTENT_ROLE_NAME";

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    private Role createAndSaveRole(String name, String description) {
        Role role = Role.builder()
                .name(name)
                .description(description)
                .permissions(new HashSet<>())
                .build();
        return roleRepository.save(role);
    }

    private Permission createAndSavePermission(String name, String resource, Action action) {
        Permission permission = Permission.builder()
                .name(name)
                .description("Test permission " + name)
                .type(PermissionType.RESOURCE)
                .resource(resource)
                .action(action)
                .build();
        return permissionRepository.save(permission);
    }

    @Test
    @DisplayName("Should create role successfully")
    @Description("POST /roles")
    void shouldCreateRoleSuccessfully() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("ADMIN_ROLE");
        request.setDescription("Administrator role with full permissions");

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("ADMIN_ROLE"))
                .andExpect(jsonPath("$.description").value("Administrator role with full permissions"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for create role with null name")
    @Description("POST /roles - Invalid Data")
    void shouldReturnBadRequestForCreateRoleWithNullName() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setDescription("Description without a name");

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return role by ID when found")
    @Description("GET /roles/{id}")
    void shouldReturnRoleByIdWhenFound() throws Exception {
        Role role = createAndSaveRole("USER_ROLE", "Standard user role");

        mockMvc.perform(get(API_BASE_PATH + "/" + role.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(role.getId()))
                .andExpect(jsonPath("$.name").value("USER_ROLE"));
    }

    @Test
    @DisplayName("Should return 404 Not Found for non-existent role ID")
    @Description("GET /roles/{id} - Not Found")
    void shouldReturnNotFoundForGetRoleByIdWhenNotFound() throws Exception {
        mockMvc.perform(get(API_BASE_PATH + "/" + NON_EXISTENT_ID)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return role by name when found")
    @Description("GET /roles/name/{name}")
    void shouldReturnRoleByNameWhenFound() throws Exception {
        Role role = createAndSaveRole("GUEST_ROLE", "Guest user role");

        mockMvc.perform(get(API_BASE_PATH + "/name/" + role.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GUEST_ROLE"));
    }

    @Test
    @DisplayName("Should return 404 Not Found for non-existent role name")
    @Description("GET /roles/name/{name} - Not Found")
    void shouldReturnNotFoundForGetRoleByNameWhenNotFound() throws Exception {
        mockMvc.perform(get(API_BASE_PATH + "/name/" + NON_EXISTENT_NAME)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all roles")
    @Description("GET /roles")
    void shouldReturnAllRoles() throws Exception {
        createAndSaveRole("ROLE_ONE", "First role");
        createAndSaveRole("ROLE_TWO", "Second role");

        mockMvc.perform(get(API_BASE_PATH)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should return empty list when no roles exist")
    @Description("GET /roles - Empty List")
    void shouldReturnEmptyListWhenNoRolesExist() throws Exception {
        mockMvc.perform(get(API_BASE_PATH)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should add permission to role")
    @Description("PUT /roles/{roleId}/permissions/{permissionId}")
    void shouldAddPermissionToRoleSuccessfully() throws Exception {
        Role role = createAndSaveRole("EDITOR_ROLE", "Content editor role");
        Permission permission = createAndSavePermission("EDIT_ARTICLE", "article", Action.UPDATE);

        mockMvc.perform(put(API_BASE_PATH + "/" + role.getId() + "/permissions/" + permission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(role.getId()))
                .andExpect(jsonPath("$.permissions", hasSize(1)))
                .andExpect(jsonPath("$.permissions[0].id").value(permission.getId()))
                .andExpect(jsonPath("$.permissions[0].name").value("EDIT_ARTICLE"));
    }

    @Test
    @DisplayName("Should return 404 if role not found")
    @Description("PUT /roles/{roleId}/permissions/{permissionId} - Role Not Found")
    void shouldReturnNotFoundWhenAddingPermissionToNonExistentRole() throws Exception {
        Permission permission = createAndSavePermission("VIEW_CONTENT", "content", Action.READ);

        mockMvc.perform(put(API_BASE_PATH + "/" + NON_EXISTENT_ID + "/permissions/" + permission.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 if permission not found")
    @Description("PUT /roles/{roleId}/permissions/{permissionId} - Permission Not Found")
    void shouldReturnNotFoundWhenAddingNonExistentPermissionToRole() throws Exception {
        Role role = createAndSaveRole("MODERATOR_ROLE", "Moderator role");

        mockMvc.perform(put(API_BASE_PATH + "/" + role.getId() + "/permissions/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should remove permission from role")
    @Description("DELETE /roles/{roleId}/permissions/{permissionId}")
    void shouldRemovePermissionFromRoleSuccessfully() throws Exception {
        Role role = createAndSaveRole("REVIEWER_ROLE", "Document reviewer role");
        Permission p1 = createAndSavePermission("APPROVE_DOC", "document", Action.APPROVE);
        Permission p2 = createAndSavePermission("COMMENT_DOC", "document", Action.CREATE);

        // Add both permissions first
        role.getPermissions().add(p1);
        role.getPermissions().add(p2);
        roleRepository.save(role);

        mockMvc.perform(delete(API_BASE_PATH + "/" + role.getId() + "/permissions/" + p1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(role.getId()))
                .andExpect(jsonPath("$.permissions", hasSize(1)))
                .andExpect(jsonPath("$.permissions[0].id").value(p2.getId())); // p1 should be removed
    }

    @Test
    @DisplayName("Should return 404 if role for removing permission not found")
    @Description("DELETE /roles/{roleId}/permissions/{permissionId} - Role Not Found")
    void shouldReturnNotFoundWhenRemovingPermissionFromNonExistentRole() throws Exception {
        Permission permission = createAndSavePermission("DELETE_ITEM", "item", Action.DELETE);

        mockMvc.perform(delete(API_BASE_PATH + "/" + NON_EXISTENT_ID + "/permissions/" + permission.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return role if permission to remove not found or not assigned (idempotency)")
    @Description("DELETE /roles/{roleId}/permissions/{permissionId} - Permission Not Found")
    void shouldReturnOkEvenIfPermissionToRemoveNotFoundOrNotAssigned() throws Exception {
        Role role = createAndSaveRole("LIMITED_USER", "User with limited permissions");

        mockMvc.perform(delete(API_BASE_PATH + "/" + role.getId() + "/permissions/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete role successfully")
    @Description("DELETE /roles/{id}")
    void shouldDeleteRoleSuccessfully() throws Exception {
        Role role = createAndSaveRole("TEMP_ROLE", "Temporary role to be deleted");

        mockMvc.perform(delete(API_BASE_PATH + "/" + role.getId())).andExpect(status().isNoContent());

        // Verify it's actually deleted
        mockMvc.perform(get(API_BASE_PATH + "/" + role.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found for non-existent role ID")
    @Description("DELETE /roles/{id} - Not Found")
    void shouldReturnNotFoundWhenDeletingNonExistentRole() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/" + NON_EXISTENT_ID)).andExpect(status().isNotFound());
    }
}
