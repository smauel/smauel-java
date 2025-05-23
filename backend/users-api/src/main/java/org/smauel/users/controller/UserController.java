package org.smauel.users.controller;

import java.util.List;
import org.smauel.users.model.User;
import org.smauel.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API of users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService service;

    /**
     * Create a new user
     *
     * @param user The user to be created
     * @return The created user
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(service.createUser(user), HttpStatus.CREATED);
    }

    /**
     * Retrieve a user given an id
     *
     * @param id The id of the user to retrieve
     * @return The user, if found, else 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return service.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Return all users
     *
     * @return All users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }
}
