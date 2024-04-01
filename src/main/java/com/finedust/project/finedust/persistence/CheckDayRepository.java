package com.finedust.project.finedust.persistence;



import com.finedust.project.finedust.model.AlarmIssued;
import com.finedust.project.finedust.model.CheckDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface CheckDayRepository extends JpaRepository<CheckDay, Integer> {

    Optional<CheckDay> findByMeasurementNameAndCheckTime(String measurementName, LocalDateTime checkTime);


}



