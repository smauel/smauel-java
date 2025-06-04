package org.smauel.users.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.smauel.users.dto.UserDto;
import org.smauel.users.dto.request.CreateUserRequest;
import org.smauel.users.dto.request.UpdateUserRequest;
import org.smauel.users.mapper.UserMapper;
import org.smauel.users.model.User;
import org.smauel.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service methods for interacting with users
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Create a user
     *
     * @param request The user creation request
     * @return The created user response
     */
    public UserDto createUser(CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    /**
     * Update a user
     *
     * @param id The id of the user to update
     * @param request The update request
     * @return The updated user response
     */
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (!request.getEmail().isEmpty() && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (!request.getFullName().isEmpty() && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    /**
     * Retrieve a user by id
     *
     * @param id The id of the user to retrieve
     * @return The user response if found
     * @throws EntityNotFoundException if user not found
     */
    public UserDto getUserById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    /**
     * Retrieve a user by username
     *
     * @param username The username to search for
     * @return The user response if found
     * @throws EntityNotFoundException if user not found
     */
    public UserDto getUserByUsername(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    /**
     * Get all users from the db
     *
     * @return A list of all user responses
     */
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Delete a user by id
     *
     * @param id The id of the user to delete
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
