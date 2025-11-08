package org.smauel.users.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smauel.users.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(showSql = false)
@DisplayName("User Repository")
class UserRepositoryAT {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean slate before each test

        user1 = User.builder()
                .username("johndoe")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .build();

        user2 = User.builder()
                .username("janedoe")
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .build();
    }

    @Test
    @DisplayName("save user should persist the user and assign an ID")
    void whenSaveUser_thenUserIsPersistedWithId() {
        User savedUser = userRepository.save(user1);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull().isPositive();
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");

        Optional<User> foundUserOpt = userRepository.findById(savedUser.getId());
        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("save user with existing username should throw DataIntegrityViolationException")
    void whenSaveUserWithExistingUsername_thenThrowsException() {
        userRepository.save(user1); // Save the first user

        User duplicateUser = User.builder()
                .username("johndoe") // Same username
                .fullName("John Another Doe")
                .email("john.another@example.com")
                .build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    userRepository.saveAndFlush(
                            duplicateUser); // Use saveAndFlush to trigger constraint violation immediately
                },
                "Saving a user with a duplicate username should throw DataIntegrityViolationException");
    }

    @Test
    @DisplayName("save user with null username should throw Exception")
    void whenSaveUserWithNullUsername_thenThrowsException() {
        User userWithNullUsername = User.builder()
                .username(null) // Null username (violates nullable = false)
                .fullName("Null User")
                .email("null.user@example.com")
                .build();

        assertThrows(
                ConstraintViolationException.class,
                () -> {
                    userRepository.saveAndFlush(userWithNullUsername);
                },
                "Saving a user with a null username should throw DataIntegrityViolationException");
    }

    @Test
    @DisplayName("save user with empty username should throw Exception")
    void whenSaveUserWithEmptyUsername_thenThrowsException() {
        User userWithNullUsername = User.builder()
                .username("")
                .fullName("Null User")
                .email("null.user@example.com")
                .build();

        assertThrows(
                ConstraintViolationException.class,
                () -> {
                    userRepository.saveAndFlush(userWithNullUsername);
                },
                "Saving a user with an empty username should throw DataIntegrityViolationException");
    }

    @Test
    @DisplayName("save user with invalid email should throw Exception")
    void whenSaveUserWithInvalidEmail_thenThrowsException() {
        User userWithNullUsername = User.builder()
                .username("johndoe")
                .fullName("John Doe")
                .email("john.doe")
                .build();

        assertThrows(
                ConstraintViolationException.class,
                () -> {
                    userRepository.saveAndFlush(userWithNullUsername);
                },
                "Saving a user with an invalid email should throw DataIntegrityViolationException");
    }

    @Test
    @DisplayName("findById should return user when user exists")
    void whenFindByIdWithExistingUser_thenReturnUser() {
        User savedUser = userRepository.save(user1);

        Optional<User> foundUserOpt = userRepository.findById(savedUser.getId());

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("findById should return empty optional when user does not exist")
    void whenFindByIdWithNonExistingUser_thenReturnEmptyOptional() {
        Optional<User> foundUserOpt = userRepository.findById(999L); // A non-existent ID
        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("findAll should return all persisted users")
    void whenFindAll_thenReturnAllPersistedUsers() {
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("johndoe", "janedoe");
    }

    @Test
    @DisplayName("findAll should return empty list when no users exist")
    void whenFindAllWithNoUsers_thenReturnEmptyList() {
        List<User> users = userRepository.findAll();
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("deleteById should remove the specified user")
    void whenDeleteById_thenUserIsRemovedFromDatabase() {
        User savedUser = userRepository.save(user1);
        Long userId = savedUser.getId();
        assertThat(userRepository.existsById(userId)).isTrue();

        userRepository.deleteById(userId);
        userRepository.flush(); // Ensure delete is committed for the subsequent check

        Optional<User> foundUserOpt = userRepository.findById(userId);
        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("deleteById for a non-existent user should not throw an error")
    void whenDeleteByIdForNonExistentUser_thenNoOperationAndNoError() {
        long nonExistentId = 888L;
        assertThat(userRepository.existsById(nonExistentId)).isFalse();
        // JpaRepository.deleteById does not throw an exception if the ID doesn't exist.
        userRepository.deleteById(nonExistentId);
        // No explicit assertion for error, test passes if no exception is thrown
        assertThat(userRepository.existsById(nonExistentId)).isFalse();
    }

    @Test
    @DisplayName("findByUsername should return user when user with given username exists")
    void whenFindByUsernameWithExistingUser_thenReturnUser() {
        userRepository.save(user1); // username "johndoe"

        Optional<User> foundUserOpt = userRepository.findByUsername("johndoe");

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getFullName()).isEqualTo("John Doe");
        assertThat(foundUserOpt.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("findByUsername should return empty optional when user with given username does not exist")
    void whenFindByUsernameWithNonExistingUser_thenReturnEmptyOptional() {
        userRepository.save(user1); // Save some user to ensure DB isn't empty

        Optional<User> foundUserOpt = userRepository.findByUsername("nonexistentuser");
        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("findByUsername should be case-sensitive")
    void whenFindByUsername_thenIsCaseSensitive() {
        userRepository.save(user1); // username is "johndoe"

        Optional<User> foundUserOpt = userRepository.findByUsername("JohnDoe"); // Different case
        assertThat(foundUserOpt).isNotPresent();

        Optional<User> correctCaseFoundUserOpt = userRepository.findByUsername("johndoe");
        assertThat(correctCaseFoundUserOpt).isPresent();
    }
}
