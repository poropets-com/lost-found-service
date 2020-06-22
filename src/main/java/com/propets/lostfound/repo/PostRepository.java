package com.propets.lostfound.repo;

import com.propets.lostfound.model.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<PostEntity,Long> {
    Page<PostEntity> findAllByPostType(String found, Pageable pageable);

    Page<PostEntity> findAllByPostTypeAndTypeAndCoordinatesNear(
            String typePost, String type, Point point, Distance distance, Pageable pageable);
}
