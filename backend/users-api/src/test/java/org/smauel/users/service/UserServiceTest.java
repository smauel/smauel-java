package org.smauel.users.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smauel.users.dto.UserDto;
import org.smauel.users.dto.request.CreateUserRequest;
import org.smauel.users.dto.request.UpdateUserRequest;
import org.smauel.users.exception.UserNotFoundException;
import org.smauel.users.mapper.UserMapper;
import org.smauel.users.model.User;
import org.smauel.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserMapper userMapper; // Real instance, not mocked

    private UserService userService; // Manually instantiated

    private User user; // A general user instance for tests
    private UserDto userDto; // DTO corresponding to 'user'
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class); // Initialize real UserMapper
        userService = new UserService(userRepository, userMapper); // Manually inject dependencies

        user = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        // userDto is now created by the real mapper instance
        userDto = userMapper.toDto(user);

        createUserRequest = new CreateUserRequest("newuser", "New User", "new@example.com");
        updateUserRequest = new UpdateUserRequest("Updated User", "updated@example.com");
    }

    @Test
    void createUser_shouldReturnUserDto() {
        User userToSaveArgument = User.builder()
                .username(createUserRequest.getUsername())
                .fullName(createUserRequest.getFullName())
                .email(createUserRequest.getEmail())
                .build(); // No ID before save

        User savedUserFromRepo = User.builder()
                .id(1L) // ID assigned after save
                .username(createUserRequest.getUsername())
                .fullName(createUserRequest.getFullName())
                .email(createUserRequest.getEmail())
                .build();

        UserDto expectedDto = userMapper.toDto(savedUserFromRepo);

        when(userRepository.save(argThat(u -> u.getUsername().equals(userToSaveArgument.getUsername())
                        && u.getFullName().equals(userToSaveArgument.getFullName())
                        && u.getEmail().equals(userToSaveArgument.getEmail())
                        && u.getId() == null))) // Assert properties of user before save
                .thenReturn(savedUserFromRepo);

        UserDto result = userService.createUser(createUserRequest);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getUsername(), result.getUsername());
        assertEquals(expectedDto.getFullName(), result.getFullName());
        assertEquals(expectedDto.getEmail(), result.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUserDto() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        // This is the state of the user entity after service method applies updates
        User updatedUserEntity = User.builder()
                .id(existingUser.getId())
                .username(existingUser.getUsername()) // Username is not updated
                .fullName(updateUserRequest.getFullName())
                .email(updateUserRequest.getEmail())
                .build();

        UserDto expectedDto = userMapper.toDto(updatedUserEntity);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateUserRequest);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getUsername(), result.getUsername());
        assertEquals(expectedDto.getFullName(), result.getFullName());
        assertEquals(expectedDto.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1))
                .save(argThat(saved -> saved.getId().equals(1L)
                        && saved.getFullName().equals(updateUserRequest.getFullName())
                        && saved.getEmail().equals(updateUserRequest.getEmail())));
    }

    @Test
    void updateUser_whenUserExists_withPartialUpdateFullNameNull_shouldReturnUpdatedUserDto() {
        UpdateUserRequest partialUpdateRequest = new UpdateUserRequest(null, "partialupdate@example.com");
        User existingUser = User.builder()
                .id(1L)
                .username("original")
                .fullName("Original Name")
                .email("original@example.com")
                .build();

        // Expected state after update logic in service
        User userAfterPartialUpdate = User.builder()
                .id(1L)
                .username("original")
                .fullName("Original Name") // FullName should remain unchanged as request.getFullName() is null
                .email("partialupdate@example.com")
                .build();
        UserDto expectedDto = userMapper.toDto(userAfterPartialUpdate);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, partialUpdateRequest);

        assertNotNull(result);
        assertEquals(expectedDto.getEmail(), result.getEmail());
        assertEquals(expectedDto.getFullName(), result.getFullName());
        assertEquals(expectedDto.getUsername(), result.getUsername());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1))
                .save(argThat(u -> u.getEmail().equals("partialupdate@example.com")
                        && u.getFullName().equals("Original Name")));
    }

    @Test
    void updateUser_whenUserExists_withPartialUpdateEmailNull_shouldReturnUpdatedUserDto() {
        UpdateUserRequest partialUpdateRequest = new UpdateUserRequest("New Full Name", null);
        User existingUser = User.builder()
                .id(1L)
                .username("original")
                .fullName("Original Name")
                .email("original@example.com")
                .build();

        User userAfterPartialUpdate = User.builder()
                .id(1L)
                .username("original")
                .fullName("New Full Name")
                .email("original@example.com") // Email should remain unchanged
                .build();
        UserDto expectedDto = userMapper.toDto(userAfterPartialUpdate);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, partialUpdateRequest);

        assertNotNull(result);
        assertEquals(expectedDto.getEmail(), result.getEmail());
        assertEquals(expectedDto.getFullName(), result.getFullName());
        assertEquals(expectedDto.getUsername(), result.getUsername());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1))
                .save(argThat(u ->
                        u.getFullName().equals("New Full Name") && u.getEmail().equals("original@example.com")));
    }

    @Test
    void updateUser_whenUserExists_withBlankEmailAndFullName_shouldNotUpdateEmailAndFullName() {
        UpdateUserRequest blankUpdateRequest = new UpdateUserRequest(" ", "  ");
        User existingUser = User.builder()
                .id(1L)
                .username("original")
                .fullName("Original Name")
                .email("original@example.com")
                .build();
        // Expected DTO is based on the original user as no fields should change
        UserDto expectedDto = userMapper.toDto(existingUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        // Save will be called, but the entity's state for email/fullName shouldn't have changed
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto result = userService.updateUser(1L, blankUpdateRequest);

        assertNotNull(result);
        assertEquals(expectedDto.getEmail(), result.getEmail());
        assertEquals(expectedDto.getFullName(), result.getFullName());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1))
                .save(argThat(u -> u.getEmail().equals("original@example.com")
                        && u.getFullName().equals("Original Name")));
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateUserRequest));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUserDto() {
        // 'user' and 'userDto' from setUp are already configured for this.
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getFullName(), result.getFullName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUserDto() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId()); // userDto is from the global 'user'
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getFullName(), result.getFullName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("nonexistent"));

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void getAllUsers_shouldReturnListOfUserDtos() {
        User anotherUser = User.builder()
                .id(2L)
                .username("another")
                .fullName("Another User")
                .email("another@example.com")
                .build();
        List<User> usersFromRepo = List.of(user, anotherUser);

        // Expected DTO list, mapped by the real mapper
        List<UserDto> expectedDtos =
                usersFromRepo.stream().map(userMapper::toDto).collect(Collectors.toList());

        when(userRepository.findAll()).thenReturn(usersFromRepo);

        List<UserDto> results = userService.getAllUsers();

        assertNotNull(results);
        assertEquals(2, results.size());
        // Simple check, assumes UserDto has equals/hashCode or compare field by field
        assertEquals(expectedDtos, results);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> results = userService.getAllUsers();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }
}
