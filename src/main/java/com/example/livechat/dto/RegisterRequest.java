package com.example.livechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le username est obligatoire")
        @Size(min = 3, max = 50, message = "Le username doit faire entre 3 et 50 caractères")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 4, max = 100, message = "Le mot de passe doit faire entre 4 et 100 caractères")
        String motDePasse) {
}
