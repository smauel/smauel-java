package org.smauel.permissions.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.dto.UserRoleAssignmentDto;
import org.smauel.permissions.dto.request.GrantRoleRequest;
import org.smauel.permissions.service.UserRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-roles")
@RequiredArgsConstructor
@Validated
public class UserRoleController {
    private final UserRoleService userRoleService;

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<UserRoleAssignmentDto> assignRole(
            @PathVariable Long userId, @Valid @RequestBody GrantRoleRequest request) {
        UserRoleAssignmentDto assignment = userRoleService.assignRoleToUser(userId, request);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<List<PermissionDto>> getUserPermissions(@PathVariable Long userId) {
        List<PermissionDto> permissions = userRoleService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserRoleAssignmentDto>> getUserRoles(@PathVariable Long userId) {
        List<UserRoleAssignmentDto> roles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/users/{userId}/permissions/{permissionName}/check")
    public ResponseEntity<Boolean> checkPermission(@PathVariable Long userId, @PathVariable String permissionName) {
        boolean hasPermission = userRoleService.hasPermission(userId, permissionName);
        return ResponseEntity.ok(hasPermission);
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> revokeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleService.revokeRole(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> revokeAllRoles(@PathVariable Long userId) {
        userRoleService.revokeAllRoles(userId);
        return ResponseEntity.noContent().build();
    }
}
