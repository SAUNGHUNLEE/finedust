package com.finedust.project.finedust.persistence;


import com.finedust.project.finedust.model.FineDust;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface FineDustRepository extends JpaRepository<FineDust, Integer> {

    @Query(value ="SELECT * FROM fine_dust WHERE measurement_name =:measurementName AND date =:date",nativeQuery = true)
    Optional<FineDust> findFineDust(@Param("measurementName")String measurementName, @Param("date") LocalDateTime date);

    //경보발령
    @Query(value = "SELECT * FROM fine_dust WHERE measurement_name = :measurementName AND ((pm10 >= 150 OR pm2_5 >= 75) OR (pm10 >= 300 OR pm2_5 >= 150)) AND date >= :beforeDate AND date <= :currentDate", nativeQuery = true)
    List<FineDust> findFineDustByPmChoice(@Param("measurementName") String measurementName,@Param("beforeDate") LocalDateTime beforeDate,@Param("currentDate") LocalDateTime currentDate);

    //pm10,pm2.5가 모두 0인경우
    @Query(value = "SELECT * FROM fine_dust WHERE measurement_name = :measurementName AND pm10 = 0 AND pm2_5 = 0 AND date= :dateTime",nativeQuery = true)
    Optional<FineDust> findCheckDay(@Param("measurementName") String measurementName,@Param("dateTime") LocalDateTime dateTime);

}



