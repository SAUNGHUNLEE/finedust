/*
package com.finedust.project.finedust;

import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.dto.FineDustDTO;
import com.finedust.project.finedust.model.FineDust;
import com.finedust.project.finedust.persistence.AlarmIssuedRepository;
import com.finedust.project.finedust.persistence.FineDustRepository;
import com.finedust.project.finedust.service.MainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MainServiceTest {

    @Mock
    //가짜 객체임을 나타내는 어노테이션
    private FineDustRepository fineDustRepository;

    @Mock
    private AlarmIssuedRepository alarmIssuedRepository;

    @InjectMocks
    private MainService mainService;

    private int id;
    private LocalDateTime date;
    private String measurementName;
    private String measurementCode;
    private int pm10;
    private int pm2_5;


    @Test
    public void testPmWaring(){

        // 날짜를 포함하여 FineDustDTO 객체를 적절히 초기화합니다.
        LocalDateTime testDate = LocalDateTime.of(2023, 3, 5, 22, 0);
        FineDustDTO fineDustDTO = new FineDustDTO(3, testDate, "노원구", "111311", 150, 75);


        List<FineDust> expectedFineDustList = Arrays.asList(
                new FineDust(3,fineDustDTO.getDate(),fineDustDTO.getMeasurementName(),fineDustDTO.getMeasurementCode(), fineDustDTO.getPm10(),fineDustDTO.getPm2_5()),
                new FineDust(4,fineDustDTO.getDate().plusHours(1),fineDustDTO.getMeasurementName(),fineDustDTO.getMeasurementCode(), fineDustDTO.getPm10(),fineDustDTO.getPm2_5()),
                new FineDust(5,fineDustDTO.getDate().plusHours(2),fineDustDTO.getMeasurementName(),fineDustDTO.getMeasurementCode(), fineDustDTO.getPm10(),fineDustDTO.getPm2_5())
                // 필요한 만큼 FineDust 객체를 추가
        );


        // stub -> 행동지시
        // Mockito 스텁 설정을 실제 메소드 호출과 일치하도록 조정
        when(fineDustRepository.findFineDustByPmChoice(
                eq("노원구"),
                eq(testDate.minusHours(2)), // 2시간 전
                eq(testDate) // 선택한 시간
        )).thenReturn(expectedFineDustList);

        AlarmIssuedDTO result = mainService.getPmWarning(fineDustDTO);
        // then
        assertNotNull(result);
        System.out.println("Result: " + result);

        assertEquals("성공", result.getWarningLevel(), "경보 레벨이 예상과 일치하지 않습니다.");

    }


}
*/
