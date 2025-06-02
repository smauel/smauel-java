package org.smauel.permissions.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.smauel.permissions.dto.UserPermissionDto;
import org.smauel.permissions.dto.request.GrantPermissionRequest;
import org.smauel.permissions.dto.request.GrantRoleRequest;
import org.smauel.permissions.exception.PermissionNotFoundException;
import org.smauel.permissions.exception.RoleNotFoundException;
import org.smauel.permissions.mapper.UserPermissionMapper;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.UserPermission;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.smauel.permissions.repository.UserPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPermissionService {
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserPermissionMapper userPermissionMapper;

    public UserPermissionDto grantPermissionToUser(Long userId, GrantPermissionRequest request) {
        Permission permission = permissionRepository
                .findByName(request.getPermissionName())
                .orElseThrow(() -> new PermissionNotFoundException(request.getPermissionName()));

        UserPermission userPermission = UserPermission.builder()
                .userId(userId)
                .permission(permission)
                .grantedBy(request.getGrantedBy())
                .expiresAt(request.getExpiresAt())
                .build();

        UserPermission saved = userPermissionRepository.save(userPermission);
        return userPermissionMapper.toDto(saved);
    }

    public List<UserPermissionDto> grantRoleToUser(Long userId, GrantRoleRequest request) {
        Role role = roleRepository
                .findByNameWithPermissions(request.getRoleName())
                .orElseThrow(() -> new RoleNotFoundException(request.getRoleName()));

        // Create a user permission entry for each permission in the role
        List<UserPermission> permissions = role.getPermissions().stream()
                .map(permission -> UserPermission.builder()
                        .userId(userId)
                        .permission(permission)
                        .role(role) // Link to the role that granted this permission
                        .grantedBy(request.getGrantedBy())
                        .expiresAt(request.getExpiresAt())
                        .build())
                .map(userPermissionRepository::save)
                .collect(Collectors.toList());

        return permissions.stream().map(userPermissionMapper::toDto).collect(Collectors.toList());
    }

    public boolean hasPermission(Long userId, String permissionName) {
        return userPermissionRepository.existsByUserIdAndPermissionName(userId, permissionName);
    }

    public List<UserPermissionDto> getUserPermissions(Long userId) {
        List<UserPermission> permissions =
                userPermissionRepository.findActivePermissionsByUserId(userId, LocalDateTime.now());

        return permissions.stream().map(userPermissionMapper::toDto).collect(Collectors.toList());
    }

    public void revokePermission(Long userPermissionId) {
        userPermissionRepository.deleteById(userPermissionId);
    }

    public void revokeAllUserPermissions(Long userId) {
        List<UserPermission> permissions = userPermissionRepository.findByUserId(userId);
        userPermissionRepository.deleteAll(permissions);
    }
}
