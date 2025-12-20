package com.ifconnected.service;

import com.ifconnected.model.JPA.Event;
import com.ifconnected.repository.jpa.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <--- IMPORTANTE

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getEventsByCampus(Long campusId) {
        return eventRepository.findByCampusIdOrderByEventDateAsc(campusId);
    }

    // --- CORREÇÃO AQUI ---
    @Transactional // <--- Garante que a lista de participantes possa ser lida e escrita
    public void toggleParticipation(Long eventId, Long userId, boolean join) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // Inicializa a lista se estiver nula (segurança extra)
        if (event.getParticipantIds() == null) {
            event.setParticipantIds(new java.util.HashSet<>());
        }

        if (join) {
            event.getParticipantIds().add(userId);
        } else {
            event.getParticipantIds().remove(userId);
        }

        eventRepository.save(event);
    }
}