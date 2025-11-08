package org.smauel.permissions.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(showSql = false)
@DisplayName("Permission Repository Acceptance Tests")
class PermissionRepositoryAT {

    @Autowired
    private PermissionRepository permissionRepository;

    private Permission permission1;
    private Permission permission2;
    private Permission permission3;

    @BeforeEach
    void setUp() {
        permissionRepository.deleteAll();

        permission1 = Permission.builder()
                .name("user:create")
                .description("Create a new user")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.CREATE)
                .build();

        permission2 = Permission.builder()
                .name("user:read")
                .description("Read user details")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.READ)
                .build();

        permission3 = Permission.builder()
                .name("order:read")
                .description("Read order details")
                .type(PermissionType.FEATURE)
                .resource("order")
                .action(Action.READ)
                .build();
    }

    @Test
    @DisplayName("save permission should persist the permission and assign an ID")
    void whenSavePermission_thenPermissionIsPersistedWithId() {
        Permission savedPermission = permissionRepository.save(permission1);

        assertThat(savedPermission).isNotNull();
        assertThat(savedPermission.getId()).isNotNull().isPositive();
        assertThat(savedPermission.getName()).isEqualTo("user:create");

        Optional<Permission> foundPermissionOpt = permissionRepository.findById(savedPermission.getId());
        assertThat(foundPermissionOpt).isPresent();
        assertThat(foundPermissionOpt.get().getName()).isEqualTo("user:create");
    }

    @Test
    @DisplayName("save permission with existing name should throw DataIntegrityViolationException")
    void whenSavePermissionWithExistingName_thenThrowsException() {
        permissionRepository.save(permission1);

        Permission duplicatePermission = Permission.builder()
                .name("user:create") // Same name
                .description("Another description")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.CREATE)
                .build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> permissionRepository.saveAndFlush(duplicatePermission),
                "Saving a permission with a duplicate name should throw DataIntegrityViolationException");
    }

    @Test
    @DisplayName("save permission with null name should throw DataIntegrityViolationException")
    void whenSavePermissionWithNullName_thenThrowsException() {
        Permission permissionWithNullName = Permission.builder()
                .name(null)
                .description("A permission with no name")
                .type(PermissionType.RESOURCE)
                .resource("test")
                .action(Action.READ)
                .build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> permissionRepository.saveAndFlush(permissionWithNullName),
                "Saving a permission with a null name should throw ConstraintViolationException");
    }

    @Test
    @DisplayName("findById should return permission when permission exists")
    void whenFindByIdWithExistingPermission_thenReturnPermission() {
        Permission savedPermission = permissionRepository.save(permission1);

        Optional<Permission> foundPermissionOpt = permissionRepository.findById(savedPermission.getId());

        assertThat(foundPermissionOpt).isPresent();
        assertThat(foundPermissionOpt.get().getName()).isEqualTo("user:create");
    }

    @Test
    @DisplayName("findById should return empty optional when permission does not exist")
    void whenFindByIdWithNonExistingPermission_thenReturnEmptyOptional() {
        Optional<Permission> foundPermissionOpt = permissionRepository.findById(999L); // A non-existent ID
        assertThat(foundPermissionOpt).isNotPresent();
    }

    @Test
    @DisplayName("findAll should return all persisted permissions")
    void whenFindAll_thenReturnAllPersistedPermissions() {
        permissionRepository.save(permission1);
        permissionRepository.save(permission2);

        List<Permission> permissions = permissionRepository.findAll();

        assertThat(permissions).hasSize(2);
        assertThat(permissions).extracting(Permission::getName).containsExactlyInAnyOrder("user:create", "user:read");
    }

    @Test
    @DisplayName("findAll should return empty list when no permissions exist")
    void whenFindAllWithNoPermissions_thenReturnEmptyList() {
        List<Permission> permissions = permissionRepository.findAll();
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("deleteById should remove the specified permission")
    void whenDeleteById_thenPermissionIsRemovedFromDatabase() {
        Permission savedPermission = permissionRepository.save(permission1);
        Long permissionId = savedPermission.getId();
        assertThat(permissionRepository.existsById(permissionId)).isTrue();

        permissionRepository.deleteById(permissionId);
        permissionRepository.flush();

        Optional<Permission> foundPermissionOpt = permissionRepository.findById(permissionId);
        assertThat(foundPermissionOpt).isNotPresent();
    }

    @Test
    @DisplayName("findByName should return permission when permission with given name exists")
    void whenFindByNameWithExistingPermission_thenReturnPermission() {
        permissionRepository.save(permission1);

        Optional<Permission> foundPermissionOpt = permissionRepository.findByName("user:create");

        assertThat(foundPermissionOpt).isPresent();
        assertThat(foundPermissionOpt.get().getDescription()).isEqualTo("Create a new user");
    }

    @Test
    @DisplayName("findByName should return empty optional when permission with given name does not exist")
    void whenFindByNameWithNonExistingPermission_thenReturnEmptyOptional() {
        Optional<Permission> foundPermissionOpt = permissionRepository.findByName("nonexistent:permission");
        assertThat(foundPermissionOpt).isNotPresent();
    }

    @Test
    @DisplayName("findByResource should return permissions for a given resource")
    void whenFindByResource_thenReturnMatchingPermissions() {
        permissionRepository.save(permission1);
        permissionRepository.save(permission2);
        permissionRepository.save(permission3);

        List<Permission> userPermissions = permissionRepository.findByResource("user");
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions)
                .extracting(Permission::getName)
                .containsExactlyInAnyOrder("user:create", "user:read");

        List<Permission> orderPermissions = permissionRepository.findByResource("order");
        assertThat(orderPermissions).hasSize(1);
        assertThat(orderPermissions.getFirst().getName()).isEqualTo("order:read");
    }

    @Test
    @DisplayName("findByResource should return empty list when no permissions for resource exist")
    void whenFindByResourceForNonexistentResource_thenReturnEmptyList() {
        permissionRepository.save(permission1);

        List<Permission> permissions = permissionRepository.findByResource("nonexistent");
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("findByTypeAndResource should return permissions for a given type and resource")
    void whenFindByTypeAndResource_thenReturnMatchingPermissions() {
        permissionRepository.save(permission1); // RESOURCE, user
        permissionRepository.save(permission2); // RESOURCE, user
        permissionRepository.save(permission3); // FEATURE, order

        List<Permission> apiUserPermissions =
                permissionRepository.findByTypeAndResource(PermissionType.RESOURCE, "user");
        assertThat(apiUserPermissions).hasSize(2);
        assertThat(apiUserPermissions)
                .extracting(Permission::getName)
                .containsExactlyInAnyOrder("user:create", "user:read");

        List<Permission> uiOrderPermissions =
                permissionRepository.findByTypeAndResource(PermissionType.FEATURE, "order");
        assertThat(uiOrderPermissions).hasSize(1);
        assertThat(uiOrderPermissions.getFirst().getName()).isEqualTo("order:read");
    }

    @Test
    @DisplayName("findByTypeAndResource should return empty list when no permissions match")
    void whenFindByTypeAndResourceForNonMatchingCriteria_thenReturnEmptyList() {
        permissionRepository.save(permission1);
        permissionRepository.save(permission3);

        List<Permission> permissions = permissionRepository.findByTypeAndResource(PermissionType.FEATURE, "user");
        assertThat(permissions).isEmpty();
    }
}
