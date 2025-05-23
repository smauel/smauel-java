package org.smauel.users.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void gettersAndSetters() {
        User u = User.builder()
                .id(1L)
                .username("sam")
                .fullName("Sam Morrison")
                .email("sam@sam.com")
                .build();

        assertThat(u.getId()).isEqualTo(1L);
        assertThat(u.getUsername()).isEqualTo("sam");
        assertThat(u.getFullName()).isEqualTo("Sam Morrison");
        assertThat(u.getEmail()).isEqualTo("sam@sam.com");
    }
}
