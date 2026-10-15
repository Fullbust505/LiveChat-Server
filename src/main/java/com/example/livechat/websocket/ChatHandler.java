package com.example.livechat.websocket;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.livechat.model.Message;
import com.example.livechat.model.User;
import com.example.livechat.repository.MessageRepository;
import com.example.livechat.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Handler du canal temps réel /ws/messages.
 * À la réception d'un message : (1) persistance en base, (2) retransmission
 * immédiate au destinataire s'il est connecté sur le canal.
 */
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PresenceRegistry presenceRegistry;

    public ChatHandler(MessageRepository messageRepository,
                       UserRepository userRepository,
                       PresenceRegistry presenceRegistry) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.presenceRegistry = presenceRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        presenceRegistry.connecter(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            presenceRegistry.deconnecter(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long expediteurId = (Long) session.getAttributes().get("userId");

        JsonNode payload;
        try {
            payload = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            envoyerErreur(session, "JSON invalide");
            return;
        }

        Long destinataireId = entier(payload, "destinataireId");
        String contenu = payload.path("contenu").asString().trim();
        if (destinataireId == null || contenu.isEmpty()) {
            envoyerErreur(session, "Champs obligatoires manquants : destinataireId, contenu");
            return;
        }

        User destinataire = userRepository.findById(destinataireId).orElse(null);
        if (destinataire == null || destinataireId.equals(expediteurId)) {
            envoyerErreur(session, "Destinataire invalide");
            return;
        }

        // (1) Persistance — horodatage serveur, pas client ; le message n'est jamais perdu,
        // il restera dans l'historique même si le destinataire est hors ligne
        Message messageSauve = new Message();
        messageSauve.setExpediteurId(expediteurId);
        messageSauve.setDestinataireId(destinataireId);
        messageSauve.setContenu(contenu);
        messageSauve.setDateEnvoi(LocalDateTime.now());
        messageRepository.save(messageSauve);

        // (2) Retransmission immédiate au destinataire s'il est connecté
        WebSocketSession sessionDestinataire = presenceRegistry.getSession(destinataireId);
        if (sessionDestinataire != null) {
            sessionDestinataire.sendMessage(new TextMessage(versJson(messageSauve)));
        }

        // Accusé de réception à l'expéditeur : le message est enregistré
        ObjectNode accusé = objectMapper.createObjectNode();
        accusé.put("type", "accuse");
        accusé.put("messageId", messageSauve.getId());
        accusé.put("livre", sessionDestinataire != null);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(accusé)));
    }

    /**
     * Format imposé par l'énoncé :
     * {"type":"message","expediteurId":1,"destinataireId":2,"contenu":"Bonjour !",
     *  "dateEnvoi":"2026-09-21T10:15:30"}
     */
    private String versJson(Message m) throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "message");
        node.put("expediteurId", m.getExpediteurId());
        node.put("destinataireId", m.getDestinataireId());
        node.put("contenu", m.getContenu());
        node.put("dateEnvoi", m.getDateEnvoi().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return objectMapper.writeValueAsString(node);
    }

    private void envoyerErreur(WebSocketSession session, String messageErreur) throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "erreur");
        node.put("message", messageErreur);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(node)));
    }

    private Long entier(JsonNode node, String champ) {
        JsonNode valeur = node.path(champ);
        if (!valeur.isNumber()) {
            return null;
        }
        return valeur.asLong();
    }
}
