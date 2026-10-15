package com.example.livechat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.livechat.dto.UtilisateurResponse;
import com.example.livechat.model.User;
import com.example.livechat.repository.UserRepository;
import com.example.livechat.websocket.PresenceRegistry;

/**
 * GET /api/utilisateurs — liste des utilisateurs et de leur statut (protégé par JWT).
 */
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UserRepository userRepository;
    private final PresenceRegistry presenceRegistry;

    public UtilisateurController(UserRepository userRepository, PresenceRegistry presenceRegistry) {
        this.userRepository = userRepository;
        this.presenceRegistry = presenceRegistry;
    }

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return userRepository.findAll().stream()
                .map(this::versReponse)
                .toList();
    }

    private UtilisateurResponse versReponse(User user) {
        String statut = presenceRegistry.estConnecte(user.getId()) ? "connecté" : "hors ligne";
        return new UtilisateurResponse(user.getId(), user.getUsername(), statut);
    }
}
