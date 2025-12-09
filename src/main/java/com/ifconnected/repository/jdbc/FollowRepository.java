package com.ifconnected.repository.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FollowRepository {

    private final JdbcTemplate jdbc;

    public FollowRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void followUser(Long followerId, Long followedId) {
        jdbc.update("SELECT follow_user(?, ?)", followerId, followedId);
    }

    public void unfollowUser(Long followerId, Long followedId) {
        jdbc.update("SELECT unfollow_user(?, ?)", followerId, followedId);
    }

    public List<Long> getFollowingIds(Long userId) {
        return jdbc.queryForList(
                "SELECT followed_id FROM follows WHERE follower_id = ?",
                Long.class,
                userId
        );
    }
}