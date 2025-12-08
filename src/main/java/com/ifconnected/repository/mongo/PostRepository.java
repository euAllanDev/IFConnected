package com.ifconnected.repository.mongo;

import com.ifconnected.model.NOSQL.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserId(Long userId);

    List<Post> findByUserIdIn(List<Long> userIds);

}