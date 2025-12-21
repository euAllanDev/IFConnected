package com.ifconnected.service;

import com.ifconnected.model.JDBC.Campus;
import com.ifconnected.model.DTO.CampusDTO;
import com.ifconnected.repository.jdbc.CampusRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampusService {

    private final CampusRepository campusRepository;
    private final GeometryFactory geometryFactory;

    public CampusService(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    // Método já existente...
    public List<CampusDTO> findNearestCampuses(Double lat, Double lon) {
        Point userLocation = geometryFactory.createPoint(new Coordinate(lon, lat));
        List<Campus> campuses = campusRepository.findNearest(userLocation);
        return convertToDTOs(campuses);
    }

    // --- NOVO MÉTODO: Listar Todos (Para o Registro) ---
    public List<CampusDTO> getAll() {
        List<Campus> campuses = campusRepository.findAll();
        return convertToDTOs(campuses);
    }

    // Auxiliar para converter lista de Entidade -> DTO
    private List<CampusDTO> convertToDTOs(List<Campus> campuses) {
        return campuses.stream()
                .map(c -> new CampusDTO(
                        c.getId(),
                        c.getName(),
                        c.getLocation().getY(), // Latitude (Y)
                        c.getLocation().getX()  // Longitude (X)
                ))
                .collect(Collectors.toList());
    }
}