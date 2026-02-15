package com.uberplus.backend.controllers;

import com.uberplus.backend.config.SecurityConfig;
import com.uberplus.backend.controller.RideController;
import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.security.TokenAuthenticationFilter;
import com.uberplus.backend.service.JwtService;
import com.uberplus.backend.service.PricingService;
import com.uberplus.backend.service.RideHistoryService;
import com.uberplus.backend.service.RideService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RideController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class RideControllerCompleteRideTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;
    @MockitoBean
    private RideHistoryService rideHistoryService;
    @MockitoBean
    private PricingService pricingService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @BeforeEach
    void setupFilterForwarding() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(
                    (HttpServletRequest) invocation.getArgument(0),
                    (HttpServletResponse) invocation.getArgument(1)
            );
            return null;
        }).when(tokenAuthenticationFilter).doFilter(any(), any(), any());
    }

    // POZITIVNI TESTOVI
    @Test
    void completeRideSuccess_driver() throws Exception {
        RideDTO dto = new RideDTO();
        dto.setId(100);
        dto.setStatus(RideStatus.COMPLETED);
        dto.setDriverEmail("driver@test.com");

        when(rideService.completeRide(100, "driver@test.com"))
                .thenReturn(dto);

        mockMvc.perform(put("/api/rides/100/complete")
                        .with(user("driver@test.com").roles("DRIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.driverEmail").value("driver@test.com"));

        verify(rideService).completeRide(100, "driver@test.com");
    }

    // NEGATIVNI TESTOVI
    @Test
    void completeRideForbidden_noAuth() throws Exception {
        mockMvc.perform(put("/api/rides/100/complete"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rideService);
    }

    @Test
    void completeRideForbidden_wrongRole() throws Exception {
        mockMvc.perform(put("/api/rides/100/complete")
                        .with(user("passenger@test.com").roles("PASSENGER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rideService);
    }

    @Test
    void completeRideAdminNotAllowed() throws Exception {
        mockMvc.perform(put("/api/rides/100/complete")
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rideService);
    }

    @Test
    void completeRideNotFound() throws Exception {
        when(rideService.completeRide(999, "driver@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/rides/999/complete")
                        .with(user("driver@test.com").roles("DRIVER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void completeRideForbidden_notDriverOfRide() throws Exception {
        when(rideService.completeRide(100, "wrong@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(put("/api/rides/100/complete")
                        .with(user("wrong@test.com").roles("DRIVER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void completeRideBadRequest_notInProgress() throws Exception {
        when(rideService.completeRide(100, "driver@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        mockMvc.perform(put("/api/rides/100/complete")
                        .with(user("driver@test.com").roles("DRIVER")))
                .andExpect(status().isBadRequest());
    }

    // GRANICNI SLUCAJEVI
    @Test
    void completeRideInvalidIdFormat() throws Exception {
        mockMvc.perform(put("/api/rides/invalid/complete")
                        .with(user("driver@test.com").roles("DRIVER")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rideService);
    }

    // IZUZETNI SLUCAJEVI
    @Test
    void completeRideWrongHttpMethod() throws Exception {
        mockMvc.perform(post("/api/rides/100/complete")
                        .with(user("driver@test.com").roles("DRIVER")))
                .andExpect(status().isMethodNotAllowed());
    }
}
