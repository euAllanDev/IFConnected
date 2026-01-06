package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.mapper.UserRowMapper; // Certifique-se que este import está correto no seu projeto
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FollowRepository {

    private final JdbcTemplate jdbc;

    public FollowRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        // initializeTable() REMOVIDO -> O Liquibase cuida disso agora
    }

    public void followUser(Long followerId, Long followedId) {
        String sql = """
            INSERT INTO follows (follower_id, followed_id) 
            VALUES (?, ?) 
            ON CONFLICT DO NOTHING
        """;
        jdbc.update(sql, followerId, followedId);
    }

    public void unfollowUser(Long followerId, Long followedId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND followed_id = ?";
        jdbc.update(sql, followerId, followedId);
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

    // --- MÉTODOS DE LISTAGEM (Unificados) ---

    public List<User> getFollowersList(Long userId) {
        // Quem segue o usuário (follower_id) onde o followed_id é o usuário alvo
        String sql = """
            SELECT u.* 
            FROM users u
            INNER JOIN follows f ON u.id = f.follower_id
            WHERE f.followed_id = ?
        """;
        return jdbc.query(sql, new UserRowMapper(), userId);
    }

    public List<User> getFollowingList(Long userId) {
        // Quem o usuário segue (followed_id) onde o follower_id é o usuário alvo
        String sql = """
            SELECT u.* 
            FROM users u
            INNER JOIN follows f ON u.id = f.followed_id
            WHERE f.follower_id = ?
        """;
        return jdbc.query(sql, new UserRowMapper(), userId);
    }
}