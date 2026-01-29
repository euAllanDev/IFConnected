package com.ifconnected.model.JPA;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    private String locationName;

    // Como User e Campus ainda são JDBC no seu projeto, guardamos apenas o ID aqui.
    @Column(name = "campus_Id", nullable = false)
    private Long campusId;


    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

// criacao do relacionamento "lista todos os id dos usuarios que vao"

    @ElementCollection
    @CollectionTable(
            name = "event_participants",
            joinColumns = @JoinColumn(name = "event_id")
    )
    @Column(name = "user_id")
    private Set<Long> participantIds = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "event_participants",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @org.hibernate.annotations.Immutable
    private Set<UserEntity> participants = new HashSet<>();


    public Event(){}


    public Event(String title, String description,  LocalDateTime eventDate, String locationName, Long campusId, Long creatorId) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.locationName = locationName;
        this.campusId = campusId;
        this.creatorId = creatorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Set<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(Set<Long> participantIds) {
        this.participantIds = participantIds;
    }
}