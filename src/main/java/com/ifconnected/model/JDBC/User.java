package com.ifconnected.model.JDBC;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String password; // Adicionado password
    private String bio;
    private String profileImageUrl;
    private Long campusId;
    private String role = "STUDENT"; // Adicionado role com padrão

    // --- Construtores ---
    public User() {
    }

    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Construtor completo
    public User(Long id, String username, String email, String password, String bio, String profileImageUrl, Long campusId, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.campusId = campusId;
        this.role = role;
    }

    // --- Getters e Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Método auxiliar para verificar se é admin
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
}