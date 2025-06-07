package org.smauel.permissions.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.dto.UserRoleAssignmentDto;
import org.smauel.permissions.dto.request.GrantRoleRequest;
import org.smauel.permissions.exception.RoleAlreadyAssignedException;
import org.smauel.permissions.exception.RoleNotFoundException;
import org.smauel.permissions.mapper.PermissionMapper;
import org.smauel.permissions.mapper.UserRoleAssignmentMapper;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.UserRoleAssignment;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.smauel.permissions.repository.UserRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleAssignmentMapper userRoleAssignmentMapper;
    private final PermissionMapper permissionMapper;

    public UserRoleAssignmentDto assignRoleToUser(Long userId, GrantRoleRequest request) {
        Role role = roleRepository
                .findByNameWithPermissions(request.getRoleName())
                .orElseThrow(() -> new RoleNotFoundException(request.getRoleName()));

        // Check if the user already has this role
        if (userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(userId, role.getId(), LocalDateTime.now())) {
            throw new RoleAlreadyAssignedException(userId, role.getName());
        }

        UserRoleAssignment assignment = UserRoleAssignment.builder()
                .userId(userId)
                .role(role)
                .assignedBy(request.getGrantedBy())
                .expiresAt(request.getExpiresAt())
                .build();

        UserRoleAssignment saved = userRoleAssignmentRepository.save(assignment);
        return userRoleAssignmentMapper.toDto(saved);
    }

    public boolean hasPermission(Long userId, String permissionName) {
        return userRoleAssignmentRepository.hasPermissionThroughRoles(userId, permissionName);
    }

    public List<PermissionDto> getUserPermissions(Long userId) {
        List<UserRoleAssignment> activeRoles =
                userRoleAssignmentRepository.findActiveRoleAssignmentsByUserId(userId, LocalDateTime.now());

        // TODO: do this in the db
        // Extract all unique permissions from the user's roles
        Set<Permission> allPermissions = new HashSet<>();
        for (UserRoleAssignment assignment : activeRoles) {
            Role role = assignment.getRole();
            // If role.getPermissions() is lazy-loaded, ensure it's fetched
            Role roleWithPermissions = roleRepository
                    .findByIdWithPermissions(role.getId())
                    .orElse(role); // Fallback to the already loaded role if for some reason it can't be found
            allPermissions.addAll(roleWithPermissions.getPermissions());
        }

        return allPermissions.stream().map(permissionMapper::toDto).collect(Collectors.toList());
    }

    public List<UserRoleAssignmentDto> getUserRoles(Long userId) {
        List<UserRoleAssignment> assignments =
                userRoleAssignmentRepository.findActiveRoleAssignmentsByUserId(userId, LocalDateTime.now());
        return assignments.stream().map(userRoleAssignmentMapper::toDto).collect(Collectors.toList());
    }

    public void revokeRole(Long userId, Long roleId) {
        userRoleAssignmentRepository.deleteByUserIdAndRole_Id(userId, roleId);
    }

    public void revokeAllRoles(Long userId) {
        userRoleAssignmentRepository.deleteByUserId(userId);
    }
}
