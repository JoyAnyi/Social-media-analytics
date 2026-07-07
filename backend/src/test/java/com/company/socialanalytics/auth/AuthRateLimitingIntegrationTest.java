package com.company.socialanalytics.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.security.registration-rate-limit=1",
        "app.security.rate-limit-window=1m"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRateLimitingIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rateLimitsRegistrationAttemptsByClient() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "first-rate-limit@example.com",
                                "ratelimitone",
                                "Rate Limit One",
                                "Correct-horse-battery-1"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "second-rate-limit@example.com",
                                "ratelimittwo",
                                "Rate Limit Two",
                                "Correct-horse-battery-2"
                        ))))
                .andExpect(status().isTooManyRequests());
    }
}
