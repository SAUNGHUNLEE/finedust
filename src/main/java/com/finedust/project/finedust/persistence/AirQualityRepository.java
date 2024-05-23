package com.finedust.project.finedust.persistence;


import com.finedust.project.finedust.model.AirQuality;
import com.finedust.project.finedust.model.AlarmIssued;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AirQualityRepository extends JpaRepository<AirQuality, Integer> {


   @Query(value = "SELECT * FROM air_quality WHERE station_name =:stationName AND sido_name =:sidoName ",nativeQuery = true)
   Optional<AirQuality> findByStationNameAndSidoName(@Param("stationName") String stationName,@Param("sidoName") String sidoName);


   @Query(value = "SELECT * FROM air_quality WHERE station_name =:stationName",nativeQuery = true)
   Optional<AirQuality> findByRegionName(@Param("stationName") String stationName);


}



