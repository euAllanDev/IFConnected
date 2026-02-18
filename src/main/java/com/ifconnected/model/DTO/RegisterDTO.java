package com.ifconnected.model.DTO;

public class RegisterDTO {
    private String username;
    private String email;
    private String password;
    private String bio;
    private String profileImageUrl;
    private Long campusId;
    private String role;

    // Getters e Setters
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
}