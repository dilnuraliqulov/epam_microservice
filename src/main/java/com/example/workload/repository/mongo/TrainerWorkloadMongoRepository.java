package com.example.workload.repository.mongo;

import com.example.workload.entity.mongo.TrainerWorkloadDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("mongo")
public interface TrainerWorkloadMongoRepository extends MongoRepository<TrainerWorkloadDocument, String> {

    Optional<TrainerWorkloadDocument> findByUsername(String username);

    boolean existsByUsername(String username);

    List<TrainerWorkloadDocument> findByFirstNameAndLastName(String firstName, String lastName);
}

