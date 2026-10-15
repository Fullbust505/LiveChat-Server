package com.example.livechat.ressource;

/**
 * Exception 404 utilisée quand une ressource (utilisateur, conversation) n'existe pas.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
