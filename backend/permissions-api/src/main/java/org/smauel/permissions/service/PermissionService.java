package org.smauel.permissions.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.dto.request.CreatePermissionRequest;
import org.smauel.permissions.exception.PermissionNotFoundException;
import org.smauel.permissions.mapper.PermissionMapper;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.enums.PermissionType;
import org.smauel.permissions.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionDto createPermission(CreatePermissionRequest request) {
        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .resource(request.getResource())
                .action(request.getAction())
                .build();

        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toDto(saved);
    }

    public PermissionDto getPermissionById(Long id) {
        Permission permission =
                permissionRepository.findById(id).orElseThrow(() -> new PermissionNotFoundException(id));
        return permissionMapper.toDto(permission);
    }

    public PermissionDto getPermissionByName(String name) {
        Permission permission =
                permissionRepository.findByName(name).orElseThrow(() -> new PermissionNotFoundException(name));
        return permissionMapper.toDto(permission);
    }

    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PermissionDto> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource).stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PermissionDto> getPermissionsByTypeAndResource(PermissionType type, String resource) {
        return permissionRepository.findByTypeAndResource(type, resource).stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new PermissionNotFoundException(id);
        }
        permissionRepository.deleteById(id);
    }
}
