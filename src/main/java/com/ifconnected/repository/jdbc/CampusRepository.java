package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.Campus;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CampusRepository {

    private final JdbcTemplate jdbcTemplate;

    public CampusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Campus> campusRowMapper = (rs, rowNum) -> {
        Campus campus = new Campus();
        campus.setId(rs.getLong("id"));
        campus.setName(rs.getString("name"));

        byte[] wkb = rs.getBytes("location_bytes");

        if (wkb != null) {
            try {
                Point point = (Point) new WKBReader().read(wkb);
                campus.setLocation(point);
            } catch (ParseException e) {
                throw new RuntimeException("Erro ao converter geometria do banco", e);
            }
        }
        return campus;
    };

    public void save(Campus campus) {
        String sql = "INSERT INTO campus (name, location) VALUES (?, ST_SetSRID(ST_MakePoint(?, ?), 4326))";

        // Point: X = Longitude, Y = Latitude
        jdbcTemplate.update(sql, campus.getName(), campus.getLocation().getX(), campus.getLocation().getY());
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM campus", Long.class);
    }

    public List<Campus> findNearest(Point userLocation) {
        String sql = """
            SELECT id, name, ST_AsBinary(location) as location_bytes 
            FROM campus 
            ORDER BY location <-> ST_SetSRID(ST_MakePoint(?, ?), 4326) 
            LIMIT 10
        """;

        return jdbcTemplate.query(sql,
                campusRowMapper,
                userLocation.getX(), // Longitude
                userLocation.getY()  // Latitude
        );
    }

    public List<Long> findIdsWithinRadius(Point center, double radiusInMeters) {
        String sql = """
            SELECT id FROM campus 
            WHERE ST_DWithin(
                location::geography, 
                ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, 
                ?
            )
        """;

        return jdbcTemplate.queryForList(sql,
                Long.class,
                center.getX(), // Longitude
                center.getY(), // Latitude
                radiusInMeters // Metros
        );
    }

    public Point getLocationById(Long campusId) {
        String sql = "SELECT ST_AsBinary(location) FROM campus WHERE id = ?";
        try {
            byte[] wkb = jdbcTemplate.queryForObject(sql, byte[].class, campusId);
            return (Point) new org.locationtech.jts.io.WKBReader().read(wkb);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Campus ID " + campusId + " não encontrado. Atualize seu perfil.");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler geometria", e);
        }
    }

    public List<Campus> findAll() {
        // ST_AsBinary é crucial para converter a geometria do PostGIS
        String sql = "SELECT id, name, ST_AsBinary(location) as location_bytes FROM campus";
        return jdbcTemplate.query(sql, campusRowMapper);
    }
}