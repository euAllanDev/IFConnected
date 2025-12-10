package com.ifconnected.model.JDBC;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point; // Importante: Use este import do JTS

@Entity
@Table(name = "campus")
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Coluna especial do PostGIS. 4326 é o código para coordenadas GPS (WGS84)
    @Column(columnDefinition = "geometry(Point, 4326")
    private Point location;

    public Campus() {}

    public Campus(String name, Point location) {
        this.name = name;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
