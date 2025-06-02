package org.smauel.permissions.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.smauel.permissions.dto.RoleDto;
import org.smauel.permissions.dto.request.CreateRoleRequest;
import org.smauel.permissions.exception.PermissionNotFoundException;
import org.smauel.permissions.exception.RoleNotFoundException;
import org.smauel.permissions.mapper.RoleMapper;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    public RoleDto createRole(CreateRoleRequest request) {
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(new HashSet<>())
                .build();

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = request.getPermissionIds().stream()
                    .map(id -> permissionRepository.findById(id).orElseThrow(() -> new PermissionNotFoundException(id)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id).orElseThrow(() -> new RoleNotFoundException(id));
        return roleMapper.toDto(role);
    }

    public RoleDto getRoleByName(String name) {
        Role role = roleRepository.findByNameWithPermissions(name).orElseThrow(() -> new RoleNotFoundException(name));
        return roleMapper.toDto(role);
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toDto).collect(Collectors.toList());
    }

    public RoleDto addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        Permission permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));

        role.addPermission(permission);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    public RoleDto removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        Permission permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));

        role.removePermission(permission);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RoleNotFoundException(id);
        }
        roleRepository.deleteById(id);
    }
}
