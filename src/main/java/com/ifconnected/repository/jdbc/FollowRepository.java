package com.ifconnected.repository.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FollowRepository {

    private final JdbcTemplate jdbc;

    public FollowRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        initializeTable(); // Garante que a tabela existe
    }

    private void initializeTable() {
        // Cria tabela de relacionamentos (N:N)
        // A Chave Primária Composta (follower + followed) impede duplicatas
        String sql = """
            CREATE TABLE IF NOT EXISTS follows (
                follower_id BIGINT NOT NULL,
                followed_id BIGINT NOT NULL,
                PRIMARY KEY (follower_id, followed_id)
            )
        """;
        jdbc.execute(sql);
    }

    // Seguir alguém
    public void followUser(Long followerId, Long followedId) {
        // ON CONFLICT DO NOTHING: Funcionalidade do Postgres.
        // Se já estiver seguindo (chave duplicada), não dá erro, apenas ignora.
        String sql = """
            INSERT INTO follows (follower_id, followed_id) 
            VALUES (?, ?) 
            ON CONFLICT DO NOTHING
        """;
        jdbc.update(sql, followerId, followedId);
    }

    // Deixar de seguir
    public void unfollowUser(Long followerId, Long followedId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND followed_id = ?";
        jdbc.update(sql, followerId, followedId);
    }

    // Listar IDs de quem eu sigo
    public List<Long> getFollowingIds(Long userId) {
        String sql = "SELECT followed_id FROM follows WHERE follower_id = ?";
        return jdbc.queryForList(sql, Long.class, userId);
    }

    // --- MÉTODOS AUXILIARES PARA O PERFIL (UserProfileDTO) ---

    // Quantos seguidores eu tenho?
    public int countFollowers(Long userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE followed_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // Quantas pessoas eu sigo?
    public int countFollowing(Long userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // Verifico se A segue B (Útil para o botão mudar de cor no Front)
    public boolean isFollowing(Long followerId, Long followedId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followed_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, followerId, followedId);
        return count != null && count > 0;
    }
}