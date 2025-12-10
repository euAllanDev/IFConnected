package com.ifconnected.model.JDBC;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String profileImageUrl;
    private Long campusId;

    // --- CONSTRUTOR 1: Vazio (Obrigatório) ---
    public User() {
    }

    // --- CONSTRUTOR 2: Básico ---
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // --- CONSTRUTOR 3: O QUE ESTAVA FALTANDO (5 Argumentos) ---
    // Esse é o que o UserRepository está chamando
    public User(Long id, String username, String email, String bio, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    // --- CONSTRUTOR 4: Completo (Com Campus) ---
    public User(Long id, String username, String email, String bio, String profileImageUrl, Long campusId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.campusId = campusId;
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
}