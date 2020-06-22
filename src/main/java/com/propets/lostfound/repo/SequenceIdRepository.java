package com.propets.lostfound.repo;

import com.propets.lostfound.model.SequenceId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceIdRepository extends MongoRepository<SequenceId,Integer> {
}
