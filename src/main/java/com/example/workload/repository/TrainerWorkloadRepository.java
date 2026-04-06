package com.example.workload.repository;

import com.example.workload.entity.TrainerWorkload;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("h2")
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByUsername(String username);

    boolean existsByUsername(String username);
}

