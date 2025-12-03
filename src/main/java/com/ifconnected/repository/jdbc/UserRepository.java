
package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // Garante que a tabela existe (os alters você roda manualmente ou via flyway)
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(255), email VARCHAR(255), bio TEXT, profile_image_url VARCHAR(500))");
    }

    // RowMapper atualizado
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("bio"),              // Novo
            rs.getString("profile_image_url") // Novo
    );

    public User save(User user) {
        String sql = "INSERT INTO users (username, email, bio, profile_image_url) VALUES (?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, user.getUsername(), user.getEmail(), user.getBio(), user.getProfileImageUrl());
        user.setId(id);
        return user;
    }

    // Novo método para atualizar Bio e Foto
    public void updateProfile(Long userId, String bio, String profileImageUrl) {
        String sql = "UPDATE users SET bio = ?, profile_image_url = ? WHERE id = ?";
        jdbcTemplate.update(sql, bio, profileImageUrl, userId);
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    // Segunda entidade JDBC: Seguir usuário
    public void followUser(Long followerId, Long followedId) {
        String sql = "INSERT INTO follows (follower_id, followed_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, followerId, followedId);
    }
}