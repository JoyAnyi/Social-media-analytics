package com.company.socialanalytics.config;

import com.company.socialanalytics.realtime.DashboardWebSocketHandler;
import com.company.socialanalytics.realtime.WebSocketProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final DashboardWebSocketHandler dashboardWebSocketHandler;
    private final WebSocketProperties webSocketProperties;

    public WebSocketConfig(DashboardWebSocketHandler dashboardWebSocketHandler, WebSocketProperties webSocketProperties) {
        this.dashboardWebSocketHandler = dashboardWebSocketHandler;
        this.webSocketProperties = webSocketProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(dashboardWebSocketHandler, "/ws/dashboard")
                .setAllowedOriginPatterns(webSocketProperties.getAllowedOriginPatterns().toArray(String[]::new));
    }
}
