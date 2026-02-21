package com.ifconnected.controller;

import com.ifconnected.model.JPA.Event;
import com.ifconnected.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        event.setId(id);
        return eventService.createEvent(event); // O seu EventService usa createEvent tanto para salvar quanto atualizar no código atual
    }

    @GetMapping("/events/campus/{campusId}")
    public List<Event> listEventsByCampus(@PathVariable Long campusId) {
        return eventService.getEventsByCampus(campusId);
    }

    @PostMapping("/events/{id}/join")
    public void joinEvent(@PathVariable Long id, @RequestParam Long userId) {
        eventService.toggleParticipation(id, userId, true);
    }

    @PostMapping("/events/{id}/leave")
    public void leaveEvent(@PathVariable Long id, @RequestParam Long userId) {
        eventService.toggleParticipation(id, userId, false);
    }
}