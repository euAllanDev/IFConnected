package com.ifconnected.mapper;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));

        // 🔥 OBRIGATÓRIO: Ler a senha do banco
        user.setPassword(rs.getString("password"));

        user.setBio(rs.getString("bio"));
        user.setProfileImageUrl(rs.getString("profile_image_url"));

        long campusId = rs.getLong("campus_id");
        if (!rs.wasNull()) {
            user.setCampusId(campusId);
        }

        try {
            String role = rs.getString("role");
            // Se vier nulo do banco, assume STUDENT
            user.setRole(role != null ? role : "STUDENT");
        } catch (SQLException e) {
            user.setRole("STUDENT");
        }

        return user;
    }
}