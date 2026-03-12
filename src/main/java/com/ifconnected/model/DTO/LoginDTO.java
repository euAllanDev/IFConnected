package com.ifconnected.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginDTO {
    private String email;
    private String password;

    // Construtor vazio (obrigatório para o Jackson)
    public LoginDTO() {}

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}