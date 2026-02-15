package com.uberplus.backend.repository;

import com.uberplus.backend.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    @Sql("classpath:user-findbyemail-test.sql")
    void findByEmailTest() {
        Optional<User> user = userRepository.findByEmail("correct@example.com");
        Assertions.assertThat(user).isNotEmpty();
        Assertions.assertThat(user.get().getFirstName()).isEqualTo("Correct");
    }

    @Test
    void findByEmailTestEmpty() {
        Optional<User> user = userRepository.findByEmail("wrong@example.com");
        Assertions.assertThat(user).isEmpty();
    }
}
