package com.ifconnected.config;

import com.ifconnected.model.JDBC.Campus;
import com.ifconnected.repository.jdbc.CampusRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(CampusRepository repository) {
        return args -> {
            // Verifica se o banco está vazio antes de inserir
            if (repository.count() == 0) {
                // Factory para criar pontos geográficos (4326 = Padrão GPS WGS84)
                GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

                // --- LITORAL ---

                // IFPB - Campus João Pessoa (Jaguaribe)
                // Coordinate(Longitude X, Latitude Y)
                Point pJP = factory.createPoint(new Coordinate(-34.8753, -7.1356));
                repository.save(new Campus("IFPB - Campus João Pessoa", pJP));

                // IFPB - Campus Cabedelo
                Point pCabedelo = factory.createPoint(new Coordinate(-34.8465, -6.9818));
                repository.save(new Campus("IFPB - Campus Cabedelo", pCabedelo));

                // --- AGRESTE / BORBOREMA ---

                // IFPB - Campus Campina Grande
                Point pCG = factory.createPoint(new Coordinate(-35.9064, -7.2366));
                repository.save(new Campus("IFPB - Campus Campina Grande", pCG));

                // IFPB - Campus Guarabira
                Point pGuarabira = factory.createPoint(new Coordinate(-35.4950, -6.8524));
                repository.save(new Campus("IFPB - Campus Guarabira", pGuarabira));

                // --- SERTÃO ---

                // IFPB - Campus Patos
                Point pPatos = factory.createPoint(new Coordinate(-37.2778, -7.0242));
                repository.save(new Campus("IFPB - Campus Patos", pPatos));

                // IFPB - Campus Cajazeiras (Alto Sertão)
                Point pCajazeiras = factory.createPoint(new Coordinate(-38.5539, -6.8892));
                repository.save(new Campus("IFPB - Campus Cajazeiras", pCajazeiras));

                // IFPB - Campus Sousa
                Point pSousa = factory.createPoint(new Coordinate(-38.2255, -6.7561));
                repository.save(new Campus("IFPB - Campus Sousa", pSousa));

                System.out.println("✅ Campi do IFPB inseridos no PostGIS com sucesso!");

                //testeeeeeee
                //teste2
            }
        };
    }
}