package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.Campus;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CampusRepository {

    private final JdbcTemplate jdbcTemplate;

    public CampusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // Cria a tabela se não existir (garantindo que a extensão postgis esteja ativa)
        this.jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS postgis");
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS campus (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "location GEOMETRY(Point, 4326))");
    }

    // --- Mapper: Converte a linha do Banco (SQL) para Objeto Java ---
    private final RowMapper<Campus> campusRowMapper = (rs, rowNum) -> {
        Campus campus = new Campus();
        campus.setId(rs.getLong("id"));
        campus.setName(rs.getString("name"));

        // O PostGIS retorna a geometria como bytes (WKB - Well Known Binary)
        byte[] wkb = rs.getBytes("location_bytes");

        if (wkb != null) {
            try {
                // WKBReader converte os bytes de volta para o objeto Point do Java
                Point point = (Point) new WKBReader().read(wkb);
                campus.setLocation(point);
            } catch (ParseException e) {
                throw new RuntimeException("Erro ao converter geometria do banco", e);
            }
        }
        return campus;
    };

    // --- Método: Salvar (INSERT) ---
    public void save(Campus campus) {
        // ST_SetSRID(ST_MakePoint(x, y), 4326) cria o ponto geográfico no SQL
        String sql = "INSERT INTO campus (name, location) VALUES (?, ST_SetSRID(ST_MakePoint(?, ?), 4326))";

        // Point: X = Longitude, Y = Latitude
        jdbcTemplate.update(sql, campus.getName(), campus.getLocation().getX(), campus.getLocation().getY());
    }

    // Método auxiliar para contar registros (usado no DataSeeder)
    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM campus", Long.class);
    }

    // --- Método: Buscar Próximos (Lógica Espacial) ---
    public List<Campus> findNearest(Point userLocation) {
        // ST_AsBinary: Garante que o banco devolva em formato padrão para lermos no Java
        // <-> : Operador nativo do PostGIS para distância (mais rápido que ST_Distance em ordenação)
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
        // ST_DWithin: Retorna TRUE se a distância for menor que o raio
        // use_spheroid=false para ser mais rápido (plano), true para precisão (globo)
        String sql = """
            SELECT id FROM campus 
            WHERE ST_DWithin(
                location, 
                ST_SetSRID(ST_MakePoint(?, ?), 4326), 
                ?, 
                true
            )
        """;

        return jdbcTemplate.queryForList(sql,
                Long.class,
                center.getX(), // Longitude
                center.getY(), // Latitude
                radiusInMeters
        );
    }

    // Método auxiliar para pegar a localização de um campus específico pelo ID
    public Point getLocationById(Long campusId) {
        String sql = "SELECT ST_AsBinary(location) FROM campus WHERE id = ?";
        byte[] wkb = jdbcTemplate.queryForObject(sql, byte[].class, campusId);
        try {
            return (Point) new org.locationtech.jts.io.WKBReader().read(wkb);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler geometria", e);
        }
    }

}