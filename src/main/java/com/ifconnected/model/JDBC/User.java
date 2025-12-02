package com.ifconnected.model.JDBC;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String email;

    // --- CONSTRUTOR 1: Vazio (Obrigatório) ---
    public User() {
    }

    // --- CONSTRUTOR 2: Com argumentos (Causa do seu erro se estiver duplicado) ---
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
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
}