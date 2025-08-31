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
    @Query("UPDATE Traffic t SET t.count = t.count + 1 WHERE t.date = :date")
    int incrementCount(@Param("date") LocalDate date);
}
