package com.example.livechat.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Le username est obligatoire")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse) {
}
