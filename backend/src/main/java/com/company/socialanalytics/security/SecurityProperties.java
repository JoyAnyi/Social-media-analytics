package com.company.socialanalytics.security;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private List<String> corsAllowedOrigins = List.of("http://localhost:5173", "http://localhost:8080");
    private Duration rateLimitWindow = Duration.ofMinutes(1);
    private int loginRateLimit = 10;
    private int registrationRateLimit = 5;
    private int passwordResetRateLimit = 5;
    private int reportGenerationRateLimit = 20;
    private boolean publicDocsEnabled;

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public Duration getRateLimitWindow() {
        return rateLimitWindow;
    }

    public void setRateLimitWindow(Duration rateLimitWindow) {
        this.rateLimitWindow = rateLimitWindow;
    }

    public int getLoginRateLimit() {
        return loginRateLimit;
    }

    public void setLoginRateLimit(int loginRateLimit) {
        this.loginRateLimit = loginRateLimit;
    }

    public int getRegistrationRateLimit() {
        return registrationRateLimit;
    }

    public void setRegistrationRateLimit(int registrationRateLimit) {
        this.registrationRateLimit = registrationRateLimit;
    }

    public int getPasswordResetRateLimit() {
        return passwordResetRateLimit;
    }

    public void setPasswordResetRateLimit(int passwordResetRateLimit) {
        this.passwordResetRateLimit = passwordResetRateLimit;
    }

    public int getReportGenerationRateLimit() {
        return reportGenerationRateLimit;
    }

    public void setReportGenerationRateLimit(int reportGenerationRateLimit) {
        this.reportGenerationRateLimit = reportGenerationRateLimit;
    }

    public boolean isPublicDocsEnabled() {
        return publicDocsEnabled;
    }

    public void setPublicDocsEnabled(boolean publicDocsEnabled) {
        this.publicDocsEnabled = publicDocsEnabled;
    }
}
