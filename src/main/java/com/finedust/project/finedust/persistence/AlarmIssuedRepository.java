package com.finedust.project.finedust.persistence;


import com.finedust.project.finedust.model.AlarmIssued;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface AlarmIssuedRepository extends JpaRepository<AlarmIssued, Integer> {

    Optional<AlarmIssued> findByMeasurementNameAndTime(String measurementName, LocalDateTime time);

}



