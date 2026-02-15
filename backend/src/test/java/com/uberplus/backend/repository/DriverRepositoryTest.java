package com.uberplus.backend.repository;

import com.uberplus.backend.model.Driver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DriverRepositoryTest {
    @Autowired
    private DriverRepository driverRepository;

    @Test
    @Sql("classpath:driver-findbyactiveunblocked-test.sql")
    public void findByActiveUnblockedTest() {
        List<Driver> drivers = driverRepository.findByActiveTrueAndBlockedFalse();
        Assertions.assertThat(drivers).hasSize(1);
    }

    @Test
    public void findByActiveUnblockedEmptyTest() {
        List<Driver> drivers = driverRepository.findByActiveTrueAndBlockedFalse();
        Assertions.assertThat(drivers).isEmpty();
    }
}
