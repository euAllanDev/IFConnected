package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    // --- CORREÇÃO: Removida a vírgula extra no construtor ---
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("bio"),
                rs.getString("profile_image_url") // <--- AQUI: Fechado parêntese sem vírgula extra
        );

        // Mapear o campus_id (se existir na consulta)
        try {
            long campusId = rs.getLong("campus_id");
            if (!rs.wasNull()) {
                user.setCampusId(campusId);
            }
        } catch (Exception e) {
            // Ignora se a coluna não vier no select
        }
        return user;
    };

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User save(User user) {
        // Atenção: Certifique-se que sua procedure SQL 'create_user' suporta campus_id se for salvar
        // Por enquanto mantive o padrão anterior
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
        return jdbc.queryForObject(sql, userRowMapper, id);
    }

    public User update(User user) {
        // Se você alterou a procedure update_user no banco, adicione o campus_id aqui
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

    // Atualiza apenas o Campus do usuário (Método extra útil)
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
}