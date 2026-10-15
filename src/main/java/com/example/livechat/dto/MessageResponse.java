package com.example.livechat.dto;

import java.time.LocalDateTime;
import com.example.livechat.model.Message;

public record MessageResponse(
        Long id,
        Long expediteurId,
        Long destinataireId,
        String contenu,
        LocalDateTime dateEnvoi) {

    public static MessageResponse from(Message m) {
        return new MessageResponse(m.getId(), m.getExpediteurId(), m.getDestinataireId(),
                m.getContenu(), m.getDateEnvoi());
    }
}
