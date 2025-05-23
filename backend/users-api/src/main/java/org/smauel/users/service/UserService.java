package org.smauel.users.service;

import java.util.List;
import java.util.Optional;
import org.smauel.users.model.User;
import org.smauel.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service methods for interacting with users
 */
@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    /**
     * Create a user
     *
     * @param user The User to create
     * @return The created user
     */
    public User createUser(User user) {
        return repository.save(user);
    }

    /**
     * Retrieve a user by id
     *
     * @param id Retrieve a user by id
     * @return The user if found, else an empty optional
     */
    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }

    /**
     * Get all users from the db
     *
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        return repository.findAll();
    }
}
