package com.ifconnected.model.JDBC;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class User implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    // --- NOVOS CAMPOS ---
    private String password;
    // --------------------
    private String bio;
    private String profileImageUrl;
    private Long campusId;

    // --- NOVO CAMPO (Com anotação para forçar leitura do JSON) ---
    @JsonProperty("role")
    private String role = "STUDENT";
    // -------------------------------------------------------------

    // --- CONSTRUTOR 1: Vazio (Obrigatório) ---
    public User() {
    }

    // --- CONSTRUTOR 2: Básico ---
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // --- CONSTRUTOR 3: Intermediário (Sem senha/role/campus) ---
    public User(Long id, String username, String email, String bio, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    // --- CONSTRUTOR 4: COMPLETO (Atualizado com Password e Role) ---
    public User(Long id, String username, String email, String password, String bio, String profileImageUrl, Long campusId, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password; // Novo
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.campusId = campusId;
        this.role = role;         // Novo
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

    // ✅ Getter e Setter da Senha (Essencial para o Login)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    // ✅ Getter e Setter do Role (Essencial para permissões)
    public String getRole() {
        return role;
    }

    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
    }

    // ✅ Método auxiliar para verificar se é admin
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
}