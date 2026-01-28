package com.ifconnected.dao;

import com.ifconnected.model.JPA.LostFoundItem;
import java.util.List;


public interface LostFoundItemDAO {
    LostFoundItem save(LostFoundItem item);
    LostFoundItem findById(Long id);
    List<LostFoundItem> findAll();
    List<LostFoundItem> findByStatus(String status);
    List<LostFoundItem> search(String q);
    void delete(Long id);
    List<LostFoundItem> findByOwnerId(Long ownerId);

}
