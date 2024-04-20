// 옵션 설정
const options = {
    enableHighAccuracy: true, // 더 정확한 위치
    timeout: 5000,           // 최대 대기 시간 (밀리초)
    maximumAge: 0            // 캐시된 위치 정보의 최대 허용 연령
};

// 에러 처리 함수
function error(err) {
    console.warn(`ERROR(${err.code}): ${err.message}`);
    alert('위치 정보 접근 권한이 거부되었거나, 위치를 찾을 수 없습니다.');
}

navigator.geolocation.getCurrentPosition(function (position) {
    fetch('/api/detail/getRegionData', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            longitude: position.coords.longitude,
            latitude: position.coords.latitude
        })
    }).then(response => {
        if (!response.ok) {
            throw new Error('서버 응답이 올바르지 않습니다: ' + response.status);
        }
        return response.json();
    }).then(data => {
        console.log('서버로부터 받은 데이터:', JSON.stringify(data, null, 2));
        const aq = data; // 미세먼지 데이터 객체
        let displayText = `시 이름: ${aq.sidoName} / 구 이름: ${aq.stationName} / 측정시간: ${aq.dataTime} / PM10 수치: ${aq.pm10Value} / PM10 등급: ${aq.pm10Grade} / PM2.5 수치: ${aq.pm25Value} / PM2.5 등급: ${aq.pm25Grade}`;
        document.getElementById('dustData').innerHTML = displayText;
        alert('위치 정보가 성공적으로 서버에 전송되었습니다.');
    })
        .catch(error => {
            console.error('Failed to fetch:', error);
            alert('위치 정보를 불러오는 데 실패하였습니다.');
        });
}, error, options);