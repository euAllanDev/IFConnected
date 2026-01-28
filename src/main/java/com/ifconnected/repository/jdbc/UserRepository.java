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

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 🔑 RowMapper CORRETO (Mapeia tudo, incluindo Role e Password)
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setBio(rs.getString("bio"));
        user.setProfileImageUrl(rs.getString("profile_image_url"));

        // Trata campus_id para não vir 0 se for nulo
        long campusId = rs.getLong("campus_id");
        if (!rs.wasNull()) {
            user.setCampusId(campusId);
        }

        // Mapeia o papel (ADMIN ou STUDENT)
        try {
            String role = rs.getString("role");
            if (role != null) user.setRole(role);
        } catch (Exception e) {
            // Ignora se a coluna não existir em queries antigas
        }

        return user; // Retorno único (Corrigido erro de duplicação)
    };

    // --- BUSCAS ---

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
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

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users", userRowMapper);
    }

    // --- SAVE ---

    // --- SAVE ---

    public User save(User user) {
        // CORREÇÃO: Usamos INSERT direto com 7 campos (username, email, password, bio, image, campus, role)
        // Isso substitui a chamada antiga "SELECT create_user..." que tinha menos campos.
        String sql = """
            INSERT INTO users (username, email, password, bio, profile_image_url, campus_id, role)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try {
            Long id = jdbc.queryForObject(
                    sql,
                    Long.class,
                    user.getUsername(),      // 1
                    user.getEmail(),         // 2
                    user.getPassword(),      // 3
                    user.getBio(),           // 4
                    user.getProfileImageUrl(),// 5
                    user.getCampusId(),      // 6
                    user.getRole()           // 7
            );

            user.setId(id);

            // Garante que o objeto retornado tenha o role preenchido
            if (user.getRole() == null) user.setRole("STUDENT");

            return user;

        } catch (DuplicateKeyException e) {
            throw new RuntimeException(
                    "Erro: O e-mail '" + user.getEmail() + "' já está cadastrado."
            );
        }
    }

    // --- UPDATE ---

    public User update(User user) {
        String sql = """
            UPDATE users
            SET username = ?, bio = ?, profile_image_url = ?, campus_id = ?
            WHERE id = ?
        """;

        jdbc.update(
                sql,
                user.getUsername(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId(),
                user.getId()
        );

        return user;
    }

    public void updateCampus(Long userId, Long campusId) {
        jdbc.update(
                "UPDATE users SET campus_id = ? WHERE id = ?",
                campusId,
                userId
        );
    }

    public void updateProfileImage(Long userId, String imageUrl) {
        jdbc.update("UPDATE users SET profile_image_url = ? WHERE id = ?", imageUrl, userId);
    }

    // --- SUGESTÕES & UTILITÁRIOS ---

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
            AND id NOT IN (
                SELECT followed_id FROM follows WHERE follower_id = ?
            )
            LIMIT 10
        """, inSql);

        List<Object> args = new ArrayList<>(nearbyCampusIds);
        args.add(myId);
        args.add(myId);

        return jdbc.query(sql, userRowMapper, args.toArray());
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, username));
    }

    public boolean existsByUsernameAndIdNot(String username, Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE username = ? AND id <> ?)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, username, id));
    }
}