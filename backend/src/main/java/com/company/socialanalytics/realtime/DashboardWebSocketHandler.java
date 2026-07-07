package com.company.socialanalytics.realtime;

import com.company.socialanalytics.security.JwtAuthenticationException;
import com.company.socialanalytics.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler implements RealtimeEventPublisher {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final Clock clock;

    public DashboardWebSocketHandler(ObjectMapper objectMapper, JwtService jwtService, Clock clock) {
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            jwtService.parse(accessToken(session));
            sessions.add(session);
            send(session, new RealtimeMessage(
                    RealtimeChannel.DASHBOARD_UPDATES.value(),
                    Map.of("status", "connected"),
                    Instant.now(clock)
            ));
        } catch (JwtAuthenticationException ex) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session);
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void publish(RealtimeChannel channel, Object payload) {
        RealtimeMessage message = new RealtimeMessage(channel.value(), payload, Instant.now(clock));
        sessions.removeIf(session -> !session.isOpen());
        sessions.forEach(session -> {
            try {
                send(session, message);
            } catch (IOException ex) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ignored) {
                    // Session cleanup happens on the next publish or close callback.
                }
            }
        });
    }

    private void send(WebSocketSession session, RealtimeMessage message) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    private String accessToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            throw new JwtAuthenticationException("JWT is missing");
        }
        for (String part : uri.getQuery().split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals("token") && !pair[1].isBlank()) {
                return pair[1];
            }
        }
        throw new JwtAuthenticationException("JWT is missing");
    }
}
