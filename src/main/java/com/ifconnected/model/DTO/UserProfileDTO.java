package com.ifconnected.model.DTO;

import com.ifconnected.model.JDBC.User;

public class UserProfileDTO {
    private User user;
    private int followersCount;
    private int followingCount;
    private long postCount;

    // --- Construtor Vazio (Necessário para serialização JSON) ---
    public UserProfileDTO() {
    }

    // --- Construtor Completo (Usado pelo UserService) ---
    public UserProfileDTO(User user, int followersCount, int followingCount, long postCount) {
        this.user = user;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.postCount = postCount;
    }

    // --- Getters e Setters Manuais (Sem Lombok) ---

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }
}