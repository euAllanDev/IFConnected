package com.ifconnected.model.JDBC;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String profileImageUrl;

    // --- CONSTRUTOR 1: Vazio (Obrigatório) ---
    public User() {
    }

    // --- CONSTRUTOR 2: Com argumentos (Causa do seu erro se estiver duplicado) ---
    public User(Long id, String username, String email, String bio, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    // --- Adicione os Getters e Setters para Bio e ProfileImageUrl ---
    public String getBio() { return bio; }

    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }

    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }


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
}