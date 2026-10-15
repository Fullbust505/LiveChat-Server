package com.example.livechat.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.livechat.dto.RegisterRequest;
import com.example.livechat.model.User;
import com.example.livechat.repository.UserRepository;

import jakarta.validation.Valid;

/**
 * POST /api/comptes — création de compte (endpoint public).
 * Le mot de passe est hashé avec BCrypt, jamais stocké en clair.
 */
@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CompteController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> creerCompte(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Le username est déjà utilisé"));
        }

        User user = new User();
        user.setUsername(request.username());
        user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", user.getId(), "username", user.getUsername()));
    }
}
