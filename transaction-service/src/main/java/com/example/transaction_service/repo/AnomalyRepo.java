package com.example.transaction_service.repo;

import com.example.transaction_service.model.anomaly.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnomalyRepo extends JpaRepository<Anomaly, String> {

    List<Anomaly> findByUserIdOrderByDetectedAtDesc(String userId);

    void deleteByUserIdAndDetectedAtBefore(String userId, LocalDateTime cutoff);

    Optional<Anomaly> findTopByUserIdAndCategoryOrderByDetectedAtDesc(String userId, String category);
}

