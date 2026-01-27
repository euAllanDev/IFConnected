package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.mapper.UserRowMapper;
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
        // chama função do banco (PL/pgSQL)
        jdbc.queryForObject("SELECT follow_user(?, ?)", Object.class, followerId, followedId);
    }

    public void unfollowUser(Long followerId, Long followedId) {
        // chama função do banco (PL/pgSQL)
        jdbc.queryForObject("SELECT unfollow_user(?, ?)", Object.class, followerId, followedId);
    }

    public boolean isFollowing(Long followerId, Long followedId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followed_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, followerId, followedId);
        return count != null && count > 0;
    }

    public List<Long> getFollowingIds(Long userId) {
        String sql = "SELECT followed_id FROM follows WHERE follower_id = ?";
        return jdbc.queryForList(sql, Long.class, userId);
    }

    public int countFollowers(Long userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE followed_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public int countFollowing(Long userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public List<User> getFollowersList(Long userId) {
        String sql = """
            SELECT u.*
            FROM users u
            INNER JOIN follows f ON u.id = f.follower_id
            WHERE f.followed_id = ?
        """;
        return jdbc.query(sql, new UserRowMapper(), userId);
    }

    public List<User> getFollowingList(Long userId) {
        String sql = """
            SELECT u.*
            FROM users u
            INNER JOIN follows f ON u.id = f.followed_id
            WHERE f.follower_id = ?
        """;
        return jdbc.query(sql, new UserRowMapper(), userId);
    }
}
