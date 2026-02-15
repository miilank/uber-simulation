package com.uberplus.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uberplus.backend.controller.RideController;
import com.uberplus.backend.dto.ride.CreateRideRequestDTO;
import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.security.TokenAuthenticationFilter;
import com.uberplus.backend.service.JwtService;
import com.uberplus.backend.service.PricingService;
import com.uberplus.backend.service.RideHistoryService;
import com.uberplus.backend.service.RideService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RideController.class)
@ActiveProfiles("test")
public class RideControllerIntegrationTest {
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
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupFilterForwarding() throws Exception {
        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter((HttpServletRequest) req, (HttpServletResponse) res);
            return null;
        }).when(tokenAuthenticationFilter)
                .doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
        }
    }

    @Test
    void createRideValid() throws Exception {
        // Arrange
        CreateRideRequestDTO request = createValidRequest();
        RideDTO expectedResponse = new RideDTO();
        expectedResponse.setDriverEmail("success@mail.com");

        when(rideService.requestRide(eq("test@example.com"), any(CreateRideRequestDTO.class)))
                .thenReturn(expectedResponse);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")));

        mockMvc.perform(post("/api/rides")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.driverEmail").value(expectedResponse.getDriverEmail()));

        verify(rideService).requestRide(eq("test@example.com"), any(CreateRideRequestDTO.class));
    }

    @Test
    void createRideNoAuthorization() throws Exception {
        CreateRideRequestDTO request = createValidRequest();
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rideService);
    }

    @ParameterizedTest
    @MethodSource("invalidRideRequests")
    void createRideNullRequests(CreateRideRequestDTO request) throws Exception {

        mockMvc.perform(post("/api/rides")
                        .with(user("test@example.com").roles("PASSENGER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    private static Stream<CreateRideRequestDTO> invalidRideRequests() {
        CreateRideRequestDTO nullStart = createValidRequest();
        nullStart.setStartLocation(null);

        CreateRideRequestDTO nullEnd = createValidRequest();
        nullEnd.setEndLocation(null);

        CreateRideRequestDTO nullDistance = createValidRequest();
        nullDistance.setDistanceKm(null);

        CreateRideRequestDTO nullTime = createValidRequest();
        nullTime.setScheduledTime(null);

        return Stream.of(nullStart, nullEnd, nullDistance, nullTime);
    }

    private static CreateRideRequestDTO createValidRequest() {
        CreateRideRequestDTO request = new CreateRideRequestDTO();

        LocationDTO startLocation = new LocationDTO();
        startLocation.setLatitude(40.0000);
        startLocation.setLongitude(-70.0000);
        startLocation.setAddress("Street");

        LocationDTO endLocation = new LocationDTO();
        endLocation.setLatitude(40.000);
        endLocation.setLongitude(-70.100);
        endLocation.setAddress("Street");

        request.setStartLocation(startLocation);
        request.setEndLocation(endLocation);
        request.setVehicleType(VehicleType.STANDARD);
        request.setBabyFriendly(false);
        request.setPetFriendly(false);
        request.setScheduledTime(LocalDateTime.now().plusHours(1));
        request.setEstimatedDurationMinutes(30);
        request.setDistanceKm(5.5);

        return request;
    }

}
