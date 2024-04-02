package com.finedust.project.finedust.persistence;


import com.finedust.project.finedust.model.AlarmIssued;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AlarmIssuedRepository extends JpaRepository<AlarmIssued, Integer> {

    @Query(value = "SELECT * FROM alarm_issued WHERE measurement_name = :measurementName", nativeQuery = true)
    Optional<AlarmIssued> findByMeasurementName(@Param("measurementName") String measurementName);

    @Query(value = "SELECT * FROM alarm_issued ORDER BY time asc",nativeQuery = true)
    List<AlarmIssued> findAllOrderByTime();


    // lastSentIndex보다 큰 id를 가진 AlarmIssued 엔티티를 조회
    @Query("SELECT a FROM AlarmIssued a WHERE a.id > :lastSentIndex ORDER BY a.time ASC")
    List<AlarmIssued> findAlarmsAfter(@Param("lastSentIndex") int lastSentIndex);
}



