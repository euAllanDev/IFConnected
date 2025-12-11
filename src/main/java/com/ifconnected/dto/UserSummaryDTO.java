package com.ifconnected.dto;

public class UserSummaryDTO {
    private Long id;
    private String username;
    private String profileImageUrl;
    private String bio;

    public UserSummaryDTO(Long id, String username, String profileImageUrl, String bio) {
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBio() {
        return bio;
    }
}
