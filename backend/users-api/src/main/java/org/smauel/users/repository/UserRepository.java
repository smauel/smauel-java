package org.smauel.users.repository;

import java.util.Optional;
import org.smauel.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository methods for interacting with the db
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Retrieve a user from the db given a username
     *
     * @param username The username of the User to retrieve from the db
     * @return A user, if found, else an empty optional
     */
    Optional<User> findByUsername(String username);
}
