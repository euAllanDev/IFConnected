package com.ifconnected.repository.jpa;

import com.ifconnected.model.JPA.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCreatorId(Long creatorId);

    // O JPA cria o SQL sozinho baseado no nome do método!
    List<Event> findByCampusIdOrderByEventDateAsc(Long campusId);
}
