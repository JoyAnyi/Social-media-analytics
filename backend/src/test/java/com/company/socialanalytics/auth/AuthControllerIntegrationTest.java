package com.company.socialanalytics.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.socialanalytics.user.RoleName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndReadProfile() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "analyst@example.com",
                "analyst",
                "Analytics Lead",
                "Correct-horse-battery-1"
        );

        String registerBody = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse registerResponse = objectMapper.readValue(registerBody, AuthResponse.class);
        assertThat(registerResponse.accessToken()).isNotBlank();
        assertThat(registerResponse.refreshToken()).isNotBlank();
        assertThat(registerResponse.user().roles()).contains(RoleName.ROLE_ADMIN, RoleName.ROLE_USER);

        LoginRequest loginRequest = new LoginRequest("analyst@example.com", "Correct-horse-battery-1");
        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse loginResponse = objectMapper.readValue(loginBody, AuthResponse.class);
        assertThat(loginResponse.user().roles()).contains(RoleName.ROLE_ADMIN, RoleName.ROLE_USER);
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + loginResponse.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void regularRegisteredUsersDoNotReceiveAdminRole() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "regular@example.com",
                "regularuser",
                "Regular User",
                "Correct-horse-battery-1"
        );

        String registerBody = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse registerResponse = objectMapper.readValue(registerBody, AuthResponse.class);
        assertThat(registerResponse.user().roles()).contains(RoleName.ROLE_USER);
        assertThat(registerResponse.user().roles()).doesNotContain(RoleName.ROLE_ADMIN);
    }

    @Test
    void rejectsWeakRegistrationPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "weak-password@example.com",
                "weakpassword",
                "Weak Password",
                "password"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
