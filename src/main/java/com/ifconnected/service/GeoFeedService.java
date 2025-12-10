package com.ifconnected.service;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.NOSQL.Post;
import com.ifconnected.repository.jdbc.CampusRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.repository.mongo.PostRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoFeedService {

    private final CampusRepository campusRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public GeoFeedService(CampusRepository campusRepository, UserRepository userRepository, PostRepository postRepository) {
        this.campusRepository = campusRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // FEED 1: Posts de usuários em Campi Próximos (Raio em Km)
    public List<Post> getNearbyCampusFeed(Long myCampusId, double radiusKm) {
        // 1. Pega a localização do meu campus
        Point myCampusLocation = campusRepository.getLocationById(myCampusId);

        // 2. Acha IDs dos Campi vizinhos (convertendo km para metros)
        List<Long> nearbyCampusIds = campusRepository.findIdsWithinRadius(myCampusLocation, radiusKm * 1000);

        // 3. Acha todos os alunos desses campi
        List<Long> userIds = userRepository.findUserIdsByCampusIds(nearbyCampusIds);

        // 4. Busca os posts desses alunos no Mongo
        return postRepository.findByUserIdIn(userIds);
    }

    // SUGESTÃO: Pessoas que você talvez conheça (Baseado no Raio)
    public List<User> getPeopleYouMightKnow(Long myUserId, Long myCampusId, double radiusKm) {
        Point myCampusLocation = campusRepository.getLocationById(myCampusId);

        // Campi vizinhos
        List<Long> nearbyCampusIds = campusRepository.findIdsWithinRadius(myCampusLocation, radiusKm * 1000);

        // Busca usuários não seguidos nesses campi
        return userRepository.findSuggestions(myUserId, nearbyCampusIds);
    }
}