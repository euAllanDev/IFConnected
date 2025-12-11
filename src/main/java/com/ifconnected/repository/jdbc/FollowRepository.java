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

    public boolean isFollowing(Long followerId, Long followedId) {
        Boolean result = jdbc.queryForObject(
                "SELECT is_following(?, ?)",
                Boolean.class,
                followerId,
                followedId
        );
        return result != null && result;
    }

    public List<Long> getFollowingIds(Long userId) {
        return jdbc.queryForList(
                "SELECT id FROM get_following_ids(?)",
                Long.class,
                userId
        );
    }

    public List<Long> getFollowerIds(Long userId) {
        return jdbc.queryForList(
                "SELECT id FROM get_follower_ids(?)",
                Long.class,
                userId
        );
    }
}
