package com.app.e_commerce.repository;

import com.app.e_commerce.entity.Traffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
@Repository
public interface TrafficRepo extends JpaRepository<Traffic, Long> {
    Optional<Traffic> findByDate(LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO traffic (date, count, page_views, bounce_count, total_session_duration, session_count, traffic_sources)
            VALUES (:date, 1, 0, 0, 0, 0, NULL)
            ON CONFLICT (date) DO UPDATE SET count = traffic.count + 1
            """, nativeQuery = true)
    void incrementCountUpsert(@Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO traffic (date, count, page_views, bounce_count, total_session_duration, session_count, traffic_sources)
            VALUES (:date, 0, 0, 0, 0, 0, NULL)
            ON CONFLICT (date) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(@Param("date") LocalDate date);
}
