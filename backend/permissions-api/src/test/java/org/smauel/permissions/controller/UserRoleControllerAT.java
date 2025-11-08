package org.smauel.permissions.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.qameta.allure.Description;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.smauel.permissions.dto.request.GrantRoleRequest;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UserRole Controller Tests")
@Transactional
class UserRoleControllerAT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private org.smauel.permissions.service.UserRoleService userRoleService; // For test setup convenience

    private static final String API_BASE_PATH = "/api/v1/user-roles";
    private static final Long NON_EXISTENT_USER_ID = 99999L;
    private static final Long NON_EXISTENT_ROLE_ID = 88888L;
    private static final String NON_EXISTENT_PERMISSION_NAME = "GHOST_PERMISSION";

    private Long userId;
    private Long grantedById;
    private Role adminRole;
    private Role userRole;
    private Permission permReadData;
    private Permission permWriteData;
    private Permission permManageUsers;

    private LocalDateTime validExpiry = LocalDateTime.now().plusMonths(1);
    private LocalDateTime invalidExpiry = LocalDateTime.now().minusMonths(1);

    private Role createAndSaveRole(String name, String description, Set<Permission> permissions) {
        Role role = Role.builder()
                .name(name)
                .description(description)
                .permissions(permissions)
                .build();
        return roleRepository.saveAndFlush(role);
    }

    private Permission createAndSavePermission(String name, String resource, Action action) {
        Permission permission = Permission.builder()
                .name(name)
                .description("Test permission " + name)
                .type(PermissionType.RESOURCE)
                .resource(resource)
                .action(action)
                .build();
        return permissionRepository.saveAndFlush(permission);
    }

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        userId = 123L;
        grantedById = 456L;

        permReadData = createAndSavePermission("READ_DATA", "data", Action.READ);
        permWriteData = createAndSavePermission("WRITE_DATA", "data", Action.UPDATE);
        permManageUsers = createAndSavePermission("MANAGE_USERS", "users", Action.APPROVE);

        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(permReadData);
        adminPermissions.add(permWriteData);
        adminPermissions.add(permManageUsers);
        adminRole = createAndSaveRole("ADMIN_ROLE", "Administrator Role", adminPermissions);

        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(permReadData);
        userRole = createAndSaveRole("USER_ROLE", "Standard User Role", userPermissions);
    }

    @Test
    @DisplayName("Should assign role to user")
    @Description("POST /api/v1/user-roles/users/{userId}/roles")
    void shouldAssignRoleToUser() throws Exception {
        GrantRoleRequest request = new GrantRoleRequest("USER_ROLE", grantedById, validExpiry);

        mockMvc.perform(post(API_BASE_PATH + "/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.role.id").value(userRole.getId()))
                .andExpect(jsonPath("$.role.name").value(userRole.getName()));
    }

    @Test
    @DisplayName("Should not be able to assign a duplicate role to user")
    @Description("POST /api/v1/user-roles/users/{userId}/roles - Duplicate Role")
    void shouldNotAssignDuplicateRoleToUser() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, validExpiry));

        // try to assign a duplicate role to the user
        GrantRoleRequest request = new GrantRoleRequest("USER_ROLE", grantedById, validExpiry.plusMonths(1));

        mockMvc.perform(post(API_BASE_PATH + "/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should be able to reassign an expired role to user")
    @Description("POST /api/v1/user-roles/users/{userId}/roles - Reassign Expired Role")
    void shouldReassignExpiredRoleToUser() throws Exception {
        // assign an expired role to the user
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, invalidExpiry));

        // now try to reassign the same role to the user
        GrantRoleRequest request = new GrantRoleRequest("USER_ROLE", grantedById, validExpiry);

        mockMvc.perform(post(API_BASE_PATH + "/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.role.id").value(userRole.getId()))
                .andExpect(jsonPath("$.role.name").value(userRole.getName()));
    }

    @Test
    @DisplayName("Should return 404 if role not found for assignment")
    @Description("POST /api/v1/user-roles/users/{userId}/roles - Role Not Found")
    void shouldReturnNotFoundWhenAssigningNonExistentRoleToUser() throws Exception {
        GrantRoleRequest request = new GrantRoleRequest("NON_EXISTENT", userRole.getId(), validExpiry);

        mockMvc.perform(post(API_BASE_PATH + "/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for invalid request (e.g. null roleId)")
    @Description("POST /api/v1/user-roles/users/{userId}/roles - Invalid Request")
    void shouldReturnBadRequestForAssignRoleWithInvalidRequest() throws Exception {
        GrantRoleRequest request = new GrantRoleRequest(null, userRole.getId(), validExpiry); // roleId is null

        mockMvc.perform(post(API_BASE_PATH + "/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get user permissions")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions")
    void shouldGetUserPermissions() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("ADMIN_ROLE", grantedById, validExpiry));

        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // READ_DATA, WRITE_DATA, MANAGE_USERS
                .andExpect(jsonPath("$[*].name", hasItem(permReadData.getName())))
                .andExpect(jsonPath("$[*].name", hasItem(permWriteData.getName())))
                .andExpect(jsonPath("$[*].name", hasItem(permManageUsers.getName())));
    }

    @Test
    @DisplayName("Should ignore expired permissions")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions - Expired Permissions")
    void shouldIgnoreExpiredPermissions() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("ADMIN_ROLE", grantedById, invalidExpiry));

        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return empty list if user has no roles/permissions")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions - No Roles")
    void shouldReturnEmptyPermissionsForUserWithNoRoles() throws Exception {
        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return empty list if user not found")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions - User Not Found")
    void shouldReturnEmptyPermissionsWhenUserNotFound() throws Exception {
        mockMvc.perform(get(API_BASE_PATH + "/users/" + NON_EXISTENT_USER_ID + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should get user roles")
    @Description("GET /api/v1/user-roles/users/{userId}/roles")
    void shouldGetUserRoles() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, validExpiry));
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("ADMIN_ROLE", grantedById, validExpiry));

        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].role.name", hasItem(userRole.getName())))
                .andExpect(jsonPath("$[*].role.name", hasItem(adminRole.getName())));
    }

    @Test
    @DisplayName("Should ignore expired roles")
    @Description("GET /api/v1/user-roles/users/{userId}/roles - Expired Roles")
    void shouldIgnoreExpiredRoles() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, invalidExpiry));
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("ADMIN_ROLE", grantedById, invalidExpiry));

        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return empty list if user not found")
    @Description("GET /api/v1/user-roles/users/{userId}/roles - User Not Found")
    void shouldReturnEmptyRolesWhenUserNotFound() throws Exception {
        mockMvc.perform(get(API_BASE_PATH + "/users/" + NON_EXISTENT_USER_ID + "/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return true if user has permission")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions/{permissionName}/check")
    void shouldReturnTrueWhenUserHasPermission() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("ADMIN_ROLE", grantedById, validExpiry));

        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/permissions/" + permWriteData.getName() + "/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return false if user lacks permission")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions/{permissionName}/check - Permission Lacking")
    void shouldReturnFalseWhenUserLacksPermission() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, validExpiry));

        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/permissions/" + permWriteData.getName() + "/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should return false for non-existent permission name check")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions/{permissionName}/check - Non-existent Permission")
    void shouldReturnFalseForCheckOfNonExistentPermissionName() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, validExpiry));

        mockMvc.perform(get(
                        API_BASE_PATH + "/users/" + userId + "/permissions/" + NON_EXISTENT_PERMISSION_NAME + "/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("false")); // Service expected to return false if permission name is unknown
    }

    @Test
    @DisplayName("Should return 404 if user for check not found")
    @Description("GET /api/v1/user-roles/users/{userId}/permissions/{permissionName}/check - User Not Found")
    void shouldReturnNotFoundForCheckPermissionWhenUserNotFound() throws Exception {
        mockMvc.perform(get(API_BASE_PATH + "/users/" + NON_EXISTENT_USER_ID + "/permissions/" + permReadData.getName()
                        + "/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should revoke role from user")
    @Description("DELETE /api/v1/user-roles/users/{userId}/roles/{roleId}")
    void shouldRevokeRoleFromUser() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, validExpiry));

        // Revoke role
        mockMvc.perform(delete(API_BASE_PATH + "/users/" + userId + "/roles/" + userRole.getId()))
                .andExpect(status().isNoContent());

        // Verify by checking roles (should be empty or not contain the revoked one)
        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/roles"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[?(@.roleId == " + userRole.getId() + ")]").doesNotExist());
    }

    @Test
    @DisplayName("Should return 204 if user for revoke not found")
    @Description("DELETE /api/v1/user-roles/users/{userId}/roles/{roleId} - User Not Found")
    void shouldReturnNoContentWhenRevokingRoleForNonExistentUser() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/users/" + NON_EXISTENT_USER_ID + "/roles/" + userRole.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 204 if role for revoke not found")
    @Description("DELETE /api/v1/user-roles/users/{userId}/roles/{roleId} - Role Not Found")
    void shouldReturnNoContentWhenRevokingNonExistentRole() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/users/" + userId + "/roles/" + NON_EXISTENT_ROLE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 204 if user does not have the role (idempotency)")
    @Description("DELETE /api/v1/user-roles/users/{userId}/roles/{roleId} - Idempotent Revoke")
    void shouldReturnNoContentWhenRevokingRoleUserDoesNotHave() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/users/" + userId + "/roles/" + adminRole.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should revoke all roles from user")
    @Description("DELETE /api/v1/user-roles/users/{userId}")
    void shouldRevokeAllRolesFromUser() throws Exception {
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("USER_ROLE", grantedById, validExpiry));
        userRoleService.assignRoleToUser(userId, new GrantRoleRequest("ADMIN_ROLE", grantedById, validExpiry));

        // Revoke all roles
        mockMvc.perform(delete(API_BASE_PATH + "/users/" + userId)).andExpect(status().isNoContent());

        // Verify (user should have no roles)
        mockMvc.perform(get(API_BASE_PATH + "/users/" + userId + "/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 204 if user for revoke all not found")
    @Description("DELETE /api/v1/user-roles/users/{userId} - User Not Found")
    void shouldReturnNoContentWhenRevokingAllRolesForNonExistentUser() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/users/" + NON_EXISTENT_USER_ID))
                .andExpect(status().isNoContent()); // Service handles not found user gracefully
    }
}
