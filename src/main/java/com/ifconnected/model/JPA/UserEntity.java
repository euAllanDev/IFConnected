package com.ifconnected.model.JPA;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    // --- Achados e Perdidos (bidirecional) ---
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LostFoundItem> lostFoundItems = new HashSet<>();

    // --- Eventos (bidirecional ManyToMany) ---
    @JsonIgnore
    @ManyToMany(mappedBy = "participants")
    private Set<Event> joinedEvents = new HashSet<>();

    public Set<Event> getJoinedEvents() {
        return joinedEvents;
    }

    public void setJoinedEvents(Set<Event> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    public Set<LostFoundItem> getLostFoundItems() {
        return lostFoundItems;
    }

    public void setLostFoundItems(Set<LostFoundItem> lostFoundItems) {
        this.lostFoundItems = lostFoundItems;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

}
