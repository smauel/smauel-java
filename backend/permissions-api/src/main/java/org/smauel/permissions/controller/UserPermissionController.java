package org.smauel.permissions.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smauel.permissions.dto.UserPermissionDto;
import org.smauel.permissions.dto.request.GrantPermissionRequest;
import org.smauel.permissions.dto.request.GrantRoleRequest;
import org.smauel.permissions.service.UserPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-permissions")
@RequiredArgsConstructor
@Validated
public class UserPermissionController {
    private final UserPermissionService userPermissionService;

    @PostMapping("/users/{userId}/permissions")
    public ResponseEntity<UserPermissionDto> grantPermission(
            @PathVariable Long userId, @Valid @RequestBody GrantPermissionRequest request) {
        UserPermissionDto userPermission = userPermissionService.grantPermissionToUser(userId, request);
        return ResponseEntity.ok(userPermission);
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserPermissionDto>> grantRole(
            @PathVariable Long userId, @Valid @RequestBody GrantRoleRequest request) {
        List<UserPermissionDto> permissions = userPermissionService.grantRoleToUser(userId, request);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<List<UserPermissionDto>> getUserPermissions(@PathVariable Long userId) {
        List<UserPermissionDto> permissions = userPermissionService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/users/{userId}/permissions/{permissionName}/check")
    public ResponseEntity<Boolean> checkPermission(@PathVariable Long userId, @PathVariable String permissionName) {
        boolean hasPermission = userPermissionService.hasPermission(userId, permissionName);
        return ResponseEntity.ok(hasPermission);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokePermission(@PathVariable Long id) {
        userPermissionService.revokePermission(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> revokeAllUserPermissions(@PathVariable Long userId) {
        userPermissionService.revokeAllUserPermissions(userId);
        return ResponseEntity.noContent().build();
    }
}
