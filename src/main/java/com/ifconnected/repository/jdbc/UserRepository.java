package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User save(User user) {
        Long id = jdbc.queryForObject(
                "SELECT create_user(?, ?, ?, ?)",
                Long.class,
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl()
        );
        user.setId(id);
        return user;
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        return jdbc.queryForObject(sql, (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("bio"),
                rs.getString("profile_image_url")
        ), id);
    }

    public User update(User user) {
        jdbc.update(
                "SELECT update_user(?, ?, ?, ?, ?)",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl()
        );

        return user;
    }
}