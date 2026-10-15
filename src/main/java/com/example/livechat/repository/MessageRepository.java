package com.example.livechat.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.livechat.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Historique complet d'une conversation, dans les deux sens, trié par date d'envoi.
     */
    @Query("""
            select m from Message m
            where (m.expediteurId = :a and m.destinataireId = :b)
               or (m.expediteurId = :b and m.destinataireId = :a)
            order by m.dateEnvoi asc
            """)
    List<Message> findConversation(@Param("a") Long utilisateurA, @Param("b") Long utilisateurB);
}
