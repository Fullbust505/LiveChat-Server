package com.example.livechat.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.livechat.dto.MessageResponse;
import com.example.livechat.model.Message;
import com.example.livechat.model.User;
import com.example.livechat.repository.MessageRepository;
import com.example.livechat.repository.UserRepository;
import com.example.livechat.ressource.NotFoundException;

/**
 * GET /api/messages/{utilisateurId} — historique des messages avec un utilisateur donné
 * (protégé par JWT, lecture via REST : ressource identifiable par une URL).
 */
@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessagesController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{utilisateurId}")
    public List<MessageResponse> historique(@PathVariable Long utilisateurId, Authentication auth) {
        // L'utilisateur courant est identifié par le JWT (subject = username)
        User moi = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NotFoundException("Utilisateur courant introuvable"));

        if (utilisateurId.equals(moi.getId()) || !userRepository.existsById(utilisateurId)) {
            throw new NotFoundException("Utilisateur " + utilisateurId + " introuvable");
        }

        List<Message> conversation = messageRepository.findConversation(moi.getId(), utilisateurId);
        return conversation.stream().map(MessageResponse::from).toList();
    }
}
