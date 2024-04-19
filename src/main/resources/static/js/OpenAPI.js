
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
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
        .then(data => {
            console.log(data);
            document.getElementById('dustData').innerText = `현재 위치의 미세먼지 수치: ${data}`;
            alert('위치 정보가 성공적으로 서버에 전송되었습니다.');
        })
        .catch(error => {
            console.error('Failed to fetch:', error);
            alert('위치 정보를 불러오는 데 실패하였습니다.');
        });
}, error, options);