package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setBio(rs.getString("bio"));
        user.setProfileImageUrl(rs.getString("profile_image_url"));

        long campusId = rs.getLong("campus_id");
        if (!rs.wasNull()) {
            user.setCampusId(campusId);
        }

        try {
            String role = rs.getString("role");
            if (role != null) user.setRole(role);
        } catch (Exception e) {
            user.setRole("STUDENT");
        }

        return user;
    };

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User save(User user) {
        String sql = """
            INSERT INTO users (username, email, password, bio, profile_image_url, campus_id, role)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try {
            Long newId = jdbc.queryForObject(sql, Long.class,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getBio(),
                    user.getProfileImageUrl(),
                    user.getCampusId(),
                    user.getRole()
            );

            user.setId(newId);
            return user;

        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Erro: O e-mail '" + user.getEmail() + "' já está cadastrado no sistema.");
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User update(User user) {
        String sql = """
            UPDATE users 
            SET username = ?, email = ?, bio = ?, profile_image_url = ?, campus_id = ?
            WHERE id = ?
        """;

        jdbc.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId(),
                user.getId()
        );

        return user;
    }

    public void updateCampus(Long userId, Long campusId) {
        String sql = "UPDATE users SET campus_id = ? WHERE id = ?";
        jdbc.update(sql, campusId, userId);
    }

    public List<Long> findUserIdsByCampusIds(List<Long> campusIds) {
        if (campusIds.isEmpty()) return List.of();

        String inSql = String.join(",", Collections.nCopies(campusIds.size(), "?"));
        String sql = String.format("SELECT id FROM users WHERE campus_id IN (%s)", inSql);

        return jdbc.queryForList(sql, Long.class, campusIds.toArray());
    }

    public List<User> findSuggestions(Long myId, List<Long> nearbyCampusIds) {
        if (nearbyCampusIds.isEmpty()) return List.of();

        String inSql = String.join(",", Collections.nCopies(nearbyCampusIds.size(), "?"));

        String sql = String.format("""
            SELECT * FROM users 
            WHERE campus_id IN (%s) 
            AND id != ? 
            AND id NOT IN (SELECT followed_id FROM follows WHERE follower_id = ?)
            LIMIT 10
        """, inSql);

        List<Object> args = new ArrayList<>(nearbyCampusIds);
        args.add(myId);
        args.add(myId);

        return jdbc.query(sql, userRowMapper, args.toArray());
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, userRowMapper);
    }

    public void updateProfileImage(Long userId, String imageUrl) {
        String sql = "UPDATE users SET profile_image_url = ? WHERE id = ?";
        jdbc.update(sql, imageUrl, userId);
    }
}