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
        // Garante que a tabela tem as colunas certas
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(255), email VARCHAR(255), bio TEXT, profile_image_url VARCHAR(500))");
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("bio"),
            rs.getString("profile_image_url")
    );

    public User save(User user) {
        String sql = "INSERT INTO users (username, email, bio, profile_image_url) VALUES (?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, user.getUsername(), user.getEmail(), user.getBio(), user.getProfileImageUrl());
        user.setId(id);
        return user;
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public void followUser(Long followerId, Long followedId) {
        String sql = "INSERT INTO follows (follower_id, followed_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, followerId, followedId);
    }

    // --- A CORREÇÃO ESTÁ AQUI 👇 ---
    public User update(User user) {
        // Agora o SQL inclui bio e profile_image_url
        String sql = "UPDATE users SET username = ?, email = ?, bio = ?, profile_image_url = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getBio(),             // <--- Adicionado
                user.getProfileImageUrl(), // <--- Adicionado
                user.getId()
        );

        return user;
    }
}