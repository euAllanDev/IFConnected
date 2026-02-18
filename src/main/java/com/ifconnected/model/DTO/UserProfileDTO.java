package com.ifconnected.model.DTO;

public class UserProfileDTO {
    // Agora usamos o DTO seguro aqui, não a entidade User
    private UserResponseDTO user;
    private int followersCount;
    private int followingCount;
    private long postCount;

    public UserProfileDTO() {
    }

    // O construtor agora aceita UserResponseDTO
    public UserProfileDTO(UserResponseDTO user, int followersCount, int followingCount, long postCount) {
        this.user = user;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.postCount = postCount;
    }

    // Getters e Setters atualizados

    public UserResponseDTO getUser() {
        return user;
    }

    public void setUser(UserResponseDTO user) {
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