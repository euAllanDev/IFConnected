package com.ifconnected.model.JDBC;

public class Follow {
    private Long followerId;
    private Long followedId;

    // --- CONSTRUTOR 1: Vazio ---
    public Follow() {
    }

    // --- CONSTRUTOR 2: Com argumentos ---
    public Follow(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    // --- Getters e Setters ---
    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Long getFollowedId() {
        return followedId;
    }

    public void setFollowedId(Long followedId) {
        this.followedId = followedId;
    }
}