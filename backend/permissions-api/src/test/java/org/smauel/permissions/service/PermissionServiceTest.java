package org.smauel.permissions.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.dto.request.CreatePermissionRequest;
import org.smauel.permissions.exception.PermissionNotFoundException;
import org.smauel.permissions.mapper.PermissionMapper;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.smauel.permissions.repository.PermissionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Tests")
class PermissionServiceTest implements WithAssertions {

    @Mock
    private PermissionRepository permissionRepository;

    private final PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(permissionRepository, permissionMapper);
    }

    @Nested
    @DisplayName("Create Permission")
    class CreatePermissionTests {

        @Test
        @DisplayName("should create and return permission DTO")
        void shouldCreatePermission() {
            // Given
            var request = new CreatePermissionRequest(
                    "test.create", "description", PermissionType.RESOURCE, "/resource", Action.CREATE);
            var savedPermission = Permission.builder()
                    .id(1L)
                    .name(request.getName())
                    .description(request.getDescription())
                    .type(request.getType())
                    .resource(request.getResource())
                    .action(request.getAction())
                    .build();
            var expectedDto = permissionMapper.toDto(savedPermission);

            when(permissionRepository.save(any(Permission.class))).thenReturn(savedPermission);

            // When
            PermissionDto result = permissionService.createPermission(request);

            // Then
            assertThat(result).isEqualTo(expectedDto);
            verify(permissionRepository).save(any(Permission.class));
        }
    }

    @Nested
    @DisplayName("Get Permission By ID")
    class GetPermissionByIdTests {

        @Test
        @DisplayName("should return permission DTO when found by ID")
        void shouldReturnPermissionWhenFoundById() {
            // Given
            long id = 1L;
            var permission = Permission.builder()
                    .id(id)
                    .name("test.read")
                    .description("description")
                    .type(PermissionType.RESOURCE)
                    .resource("/resource")
                    .action(Action.READ)
                    .build();
            var expectedDto = permissionMapper.toDto(permission);
            when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));

            // When
            PermissionDto result = permissionService.getPermissionById(id);

            // Then
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("should throw PermissionNotFoundException when not found by ID")
        void shouldThrowPermissionNotFoundExceptionWhenNotFoundById() {
            // Given
            long id = 1L;
            when(permissionRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(PermissionNotFoundException.class)
                    .isThrownBy(() -> permissionService.getPermissionById(id))
                    .withMessage("Permission not found with id: 1");
        }
    }

    @Nested
    @DisplayName("Get Permission By Name")
    class GetPermissionByNameTests {

        @Test
        @DisplayName("should return permission DTO when found by name")
        void shouldReturnPermissionWhenFoundByName() {
            // Given
            String name = "test.read";
            var permission = Permission.builder()
                    .id(1L)
                    .name(name)
                    .description("description")
                    .type(PermissionType.RESOURCE)
                    .resource("/resource")
                    .action(Action.READ)
                    .build();
            var expectedDto = permissionMapper.toDto(permission);
            when(permissionRepository.findByName(name)).thenReturn(Optional.of(permission));

            // When
            PermissionDto result = permissionService.getPermissionByName(name);

            // Then
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("should throw PermissionNotFoundException when not found by name")
        void shouldThrowPermissionNotFoundExceptionWhenNotFoundByName() {
            // Given
            String name = "test.read";
            when(permissionRepository.findByName(name)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(PermissionNotFoundException.class)
                    .isThrownBy(() -> permissionService.getPermissionByName(name))
                    .withMessage("Permission not found: test.read");
        }
    }

    @Nested
    @DisplayName("Get All Permissions")
    class GetAllPermissionsTests {

        @Test
        @DisplayName("should return a list of all permission DTOs")
        void shouldReturnAllPermissions() {
            // Given
            var permission1 = Permission.builder()
                    .id(1L)
                    .name("p1")
                    .description("d1")
                    .type(PermissionType.RESOURCE)
                    .resource("r1")
                    .action(Action.APPROVE)
                    .build();
            var permission2 = Permission.builder()
                    .id(2L)
                    .name("p2")
                    .description("d2")
                    .type(PermissionType.FEATURE)
                    .resource("r2")
                    .action(Action.DELETE)
                    .build();
            var expectedDto1 = permissionMapper.toDto(permission1);
            var expectedDto2 = permissionMapper.toDto(permission2);

            when(permissionRepository.findAll()).thenReturn(List.of(permission1, permission2));

            // When
            List<PermissionDto> result = permissionService.getAllPermissions();

            // Then
            assertThat(result).containsExactlyInAnyOrder(expectedDto1, expectedDto2);
        }

        @Test
        @DisplayName("should return an empty list when no permissions exist")
        void shouldReturnEmptyListWhenNoPermissionsExist() {
            // Given
            when(permissionRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<PermissionDto> result = permissionService.getAllPermissions();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Permissions By Resource")
    class GetPermissionsByResourceTests {

        @Test
        @DisplayName("should return permission DTOs for a given resource")
        void shouldReturnPermissionsByResource() {
            // Given
            String resource = "/test";
            var permission = Permission.builder()
                    .id(1L)
                    .name("p1")
                    .description("d1")
                    .type(PermissionType.FEATURE)
                    .resource(resource)
                    .action(Action.REJECT)
                    .build();
            var expectedDto = permissionMapper.toDto(permission);

            when(permissionRepository.findByResource(resource)).thenReturn(List.of(permission));

            // When
            List<PermissionDto> result = permissionService.getPermissionsByResource(resource);

            // Then
            assertThat(result).containsExactly(expectedDto);
        }

        @Test
        @DisplayName("should return an empty list when no permissions for a resource exist")
        void shouldReturnEmptyListWhenNoPermissionsForResource() {
            // Given
            String resource = "/test";
            when(permissionRepository.findByResource(resource)).thenReturn(Collections.emptyList());

            // When
            List<PermissionDto> result = permissionService.getPermissionsByResource(resource);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Permissions By Type And Resource")
    class GetPermissionsByTypeAndResourceTests {

        @Test
        @DisplayName("should return permission DTOs for a given type and resource")
        void shouldReturnPermissionsByTypeAndResource() {
            // Given
            String resource = "/test";
            var type = PermissionType.FEATURE;
            var permission = Permission.builder()
                    .id(1L)
                    .name("p1")
                    .description("d1")
                    .type(type)
                    .resource(resource)
                    .action(Action.UPDATE)
                    .build();
            var expectedDto = permissionMapper.toDto(permission);

            when(permissionRepository.findByTypeAndResource(type, resource)).thenReturn(List.of(permission));

            // When
            List<PermissionDto> result = permissionService.getPermissionsByTypeAndResource(type, resource);

            // Then
            assertThat(result).containsExactly(expectedDto);
        }

        @Test
        @DisplayName("should return an empty list when no permissions for a type and resource exist")
        void shouldReturnEmptyListWhenNoPermissionsForTypeAndResource() {
            // Given
            String resource = "/test";
            var type = PermissionType.FEATURE;
            when(permissionRepository.findByTypeAndResource(type, resource)).thenReturn(Collections.emptyList());

            // When
            List<PermissionDto> result = permissionService.getPermissionsByTypeAndResource(type, resource);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Delete Permission")
    class DeletePermissionTests {

        @Test
        @DisplayName("should delete permission when it exists")
        void shouldDeletePermissionWhenExists() {
            // Given
            long id = 1L;
            when(permissionRepository.existsById(id)).thenReturn(true);

            // When
            permissionService.deletePermission(id);

            // Then
            verify(permissionRepository).deleteById(id);
        }

        @Test
        @DisplayName("should throw PermissionNotFoundException when deleting a non-existent permission")
        void shouldThrowPermissionNotFoundExceptionWhenDeletingNonExistentPermission() {
            // Given
            long id = 1L;
            when(permissionRepository.existsById(id)).thenReturn(false);

            // When & Then
            assertThatExceptionOfType(PermissionNotFoundException.class)
                    .isThrownBy(() -> permissionService.deletePermission(id))
                    .withMessage("Permission not found with id: 1");

            verify(permissionRepository, never()).deleteById(id);
        }
    }
}
