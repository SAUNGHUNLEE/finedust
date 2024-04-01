package com.finedust.project.finedust.persistence;


import com.finedust.project.finedust.model.AlarmIssued;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AlarmIssuedRepository extends JpaRepository<AlarmIssued, Integer> {

    Optional<AlarmIssued> findByMeasurementNameAndTime(String measurementName, LocalDateTime time);

    @Query(value = "SELECT * FROM alarm_issued ORDER BY time asc",nativeQuery = true)
    List<AlarmIssued> findAllOrderByTime();
}



