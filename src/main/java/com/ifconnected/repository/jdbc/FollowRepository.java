package com.ifconnected.repository.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FollowRepository {

    private final JdbcTemplate jdbcTemplate;

    public FollowRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // Cria a tabela automaticamente se não existir
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS follows (" +
                "follower_id BIGINT NOT NULL, " +
                "followed_id BIGINT NOT NULL, " +
                "PRIMARY KEY (follower_id, followed_id))");
    }

    // Seguir alguém
    public void followUser(Long followerId, Long followedId) {
        // Verifica se já segue para evitar erro de chave duplicada
        if (!isFollowing(followerId, followedId)) {
            String sql = "INSERT INTO follows (follower_id, followed_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, followerId, followedId);
        }
    }

    // Deixar de seguir
    public void unfollowUser(Long followerId, Long followedId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND followed_id = ?";
        jdbcTemplate.update(sql, followerId, followedId);
    }

    // Verificar se já segue
    public boolean isFollowing(Long followerId, Long followedId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followed_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, followerId, followedId);
        return count != null && count > 0;
    }

    // Listar quem um usuário segue (retorna lista de IDs)
    public List<Long> getFollowingIds(Long userId) {
        String sql = "SELECT followed_id FROM follows WHERE follower_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }
}