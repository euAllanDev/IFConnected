package com.ifconnected.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifconnected.model.JDBC.Campus;
import com.ifconnected.repository.jdbc.CampusRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class DataSeeder {

    // Classe interna simples para mapear o JSON (DTO temporário)
    // Precisa ser static para o Jackson instanciar
    static class CampusJson {
        public String name;
        public double lat;
        public double lon;
    }

    @Bean
    CommandLineRunner initDatabase(CampusRepository repository) {
        return args -> {
            // Só roda se o banco estiver vazio para não duplicar
            if (repository.count() == 0) {
                System.out.println("📦 Iniciando carga de dados dos Campi via JSON...");

                try {
                    // 1. Ler o arquivo campuses.json da pasta resources
                    ObjectMapper mapper = new ObjectMapper();
                    TypeReference<List<CampusJson>> typeReference = new TypeReference<>() {};
                    InputStream inputStream = TypeReference.class.getResourceAsStream("/campuses.json");

                    if (inputStream == null) {
                        System.out.println("⚠️ Arquivo campuses.json não encontrado!");
                        return;
                    }

                    List<CampusJson> campusList = mapper.readValue(inputStream, typeReference);

                    // 2. Preparar a fábrica de geometria (SRID 4326 = GPS)
                    GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

                    // 3. Iterar e Salvar no Banco
                    for (CampusJson c : campusList) {
                        // Atenção: Point usa (Longitude, Latitude) -> (X, Y)
                        Point p = factory.createPoint(new Coordinate(c.lon, c.lat));

                        Campus campus = new Campus(c.name, p);
                        repository.save(campus);
                    }

                    System.out.println("✅ " + campusList.size() + " Campi inseridos com sucesso!");

                } catch (IOException e) {
                    System.out.println("❌ Erro ao ler JSON: " + e.getMessage());
                }
            } else {
                System.out.println("⚡ Banco de Campi já populado. Pulando carga inicial.");
            }
        };
    }
}