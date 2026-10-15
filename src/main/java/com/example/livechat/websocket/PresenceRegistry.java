package com.example.livechat.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Registre des utilisateurs actuellement connectés au canal WebSocket.
 * Sert aussi au statut "connecté / hors ligne" de GET /api/utilisateurs.
 */
@Component
public class PresenceRegistry {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public boolean estConnecte(Long utilisateurId) {
        WebSocketSession session = sessions.get(utilisateurId);
        return session != null && session.isOpen();
    }

    public WebSocketSession getSession(Long utilisateurId) {
        WebSocketSession session = sessions.get(utilisateurId);
        return (session != null && session.isOpen()) ? session : null;
    }

    public void connecter(Long utilisateurId, WebSocketSession session) {
        sessions.put(utilisateurId, session);
    }

    public void deconnecter(Long utilisateurId, WebSocketSession session) {
        sessions.remove(utilisateurId, session);
    }
}
