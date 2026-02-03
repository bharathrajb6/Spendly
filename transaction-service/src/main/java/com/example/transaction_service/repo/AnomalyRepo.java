package com.example.transaction_service.repo;

import com.example.transaction_service.model.anomaly.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnomalyRepo extends JpaRepository<Anomaly, String> {

    /**
     * Retrieves a list of anomalies for the given user, ordered by the date and
     * time they were detected in descending order.
     *
     * @param userId The ID of the user for whom to retrieve the anomalies.
     * @return A list of anomalies for the given user, ordered by the date and time
     *         they were detected in descending order.
     */
    List<Anomaly> findByUserIdOrderByDetectedAtDesc(String userId);

    /**
     * Deletes anomalies for the given user that were detected before the specified
     * cutoff date and time.
     *
     * @param userId The ID of the user for whom to delete the anomalies.
     * @param cutoff The cutoff date and time before which anomalies should be
     *               deleted.
     */
    void deleteByUserIdAndDetectedAtBefore(String userId, LocalDateTime cutoff);

    /**
     * Retrieves the most recent anomaly for the given user and category.
     *
     * @param userId   The ID of the user for whom to retrieve the anomaly.
     * @param category The category of the anomaly to retrieve.
     * @return An optional containing the most recent anomaly for the given user and
     *         category, or an empty optional if no such anomaly is found.
     */
    Optional<Anomaly> findTopByUserIdAndCategoryOrderByDetectedAtDesc(String userId, String category);
}
