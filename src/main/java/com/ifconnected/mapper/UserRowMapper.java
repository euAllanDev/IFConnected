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
        user.setBio(rs.getString("bio"));

        // Mapeia o snake_case do banco para o camelCase do Java
        user.setProfileImageUrl(rs.getString("profile_image_url"));

        // getObject é seguro para campos que podem ser nulos (como campus_id)
        user.setCampusId(rs.getObject("campus_id", Long.class));

        return user;
    }
}