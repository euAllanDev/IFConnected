package com.ifconnected.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CampusDTO", description = "Representa um campus do IF com coordenadas geográficas.")
public class CampusDTO {

    @Schema(description = "ID do campus", example = "1")
    private Long id;

    @Schema(description = "Nome do campus", example = "IFPB - Campus João Pessoa")
    private String name;

    @Schema(description = "Latitude do campus (WGS84)", example = "-7.1195")
    private double latitude;

    @Schema(description = "Longitude do campus (WGS84)", example = "-34.8450")
    private double longitude;

    public CampusDTO() {}

    public CampusDTO(Long id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}