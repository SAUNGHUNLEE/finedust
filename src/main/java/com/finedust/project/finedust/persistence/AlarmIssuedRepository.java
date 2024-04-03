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

    @Query(value = "SELECT * FROM alarm_issued WHERE measurement_name = :measurementName AND time = :time", nativeQuery = true)
    Optional<AlarmIssued> findByMeasurementName(@Param("measurementName") String measurementName,@Param("time") LocalDateTime time);

    @Query(value = "SELECT * FROM alarm_issued ORDER BY time",nativeQuery = true)
    List<AlarmIssued> findAllOrderByTime();



}



