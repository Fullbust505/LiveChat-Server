package com.example.livechat.websocket;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.livechat.model.User;
import com.example.livechat.repository.UserRepository;
import com.example.livechat.security.JwtUtil;

/**
 * Interceptor du handshake WebSocket : le JWT doit être transmis en query param
 * (?token=...) — un client non authentifié ne peut pas se connecter au canal.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extraireToken(request.getURI());

        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        User user = userRepository.findByUsername(jwtUtil.extraireUsername(token)).orElse(null);
        if (user == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("userId", user.getId());
        attributes.put("username", user.getUsername());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // rien à faire
    }

    private String extraireToken(URI uri) {
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if ("token".equals(pair[0]) && pair.length == 2) {
                return pair[1];
            }
        }
        return null;
    }
}
