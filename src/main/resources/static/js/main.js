function selectDateAndTime(event, districtName) {
    event.stopPropagation();

    var container = document.getElementById('datePicker');
    container.innerHTML = `
                <h3>${districtName} - 날짜 및 시간 선택</h3>
                <form id="dateForm">
                    <input type="hidden" name="measurementName" value="${districtName}">
                    <select name="date" required>
                        <option value="2023-03-01">2023년 3월 1일</option>
                        ${Array.from({length: 31}, (_, i) => `<option value="2023-03-${(i + 1).toString().padStart(2, '0')}">2023년 3월 ${(i + 1).toString().padStart(2, '0')}일</option>`).join('')}
                        <option value="2023-04-01">2023년 4월 1일</option>
                    </select>
                    <select name="time" required>
                        ${Array.from({length: 24}, (_, i) => `<option value="${i.toString().padStart(2, '0')}:00:00">${i.toString().padStart(2, '0')}시</option>`).join('')}
                    </select>
                    <button type="submit">확인</button>
                </form>
            `;
    document.getElementById('dateForm').onsubmit = submitForm;
}

async function submitForm(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    const districtName = formData.get('measurementName');
    const date = formData.get('date');
    const time = formData.get('time');
    const dateTime = date + 'T' + time;

    try {
        const response = await fetch('/main/info', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded',},
            body: `measurementName=${encodeURIComponent(districtName)}&date=${encodeURIComponent(dateTime)}`,
        });
        if (response.ok) {
            window.location.href = '/main/info';
        } else {
            throw new Error('서버 응답 에러');
        }
    } catch (error) {
        console.error('에러:', error);
    }


}
let stompClient = null;


function connect(){
    let socket = new SockJS('/ws/alarm');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame){
        console.log('연결 :' + frame);

        //서버->클라
        //alarm -> 서비스 getAlarmView 메서드에 alarmIssuedDTO
        stompClient.subscribe('/topic/alarm',function(alarm){
            let alarmMessage = JSON.parse(alarm.body);
            if (Array.isArray(alarmMessages)) {
                alarmMessages.forEach(alarmMessage => {
                    displayAlarmMessage(alarmMessage.measurementName, alarmMessage.message, alarmMessage.time);
                });
            } else {
                // 서버로부터 받은 메시지가 배열이 아닌 단일 객체인 경우
                displayAlarmMessage(alarmMessages.measurementName, alarmMessages.message, alarmMessages.time);
            }
        })
        // 연결 후 서버에 첫 번째 알람 정보를 요청합니다.
        stompClient.send("/app/connect", {}, {});
    });
}

function displayAlarmMessage(measurementName,alarmMessage,time){
    let alarmList = document.getElementById('alarmNotifications');
    let messageElement = document.createElement('div');
    messageElement.classList.add('alarm-message');
    messageElement.innerText = "경보 알람 메시지 : " + measurementName + ": " + alarmMessage + "  시간은 : " + time;
    alarmList.appendChild(messageElement);
}



// 알람 정보를 로컬 스토리지에 저장합니다.
function saveAlarmToLocalStorage(alarmInfo) {
    // 로컬 스토리지에서 알람 정보 배열을 가져옵니다.
    var alarms = JSON.parse(localStorage.getItem("alarms")) || [];
    alarms.push(alarmInfo);
    localStorage.setItem("alarms", JSON.stringify(alarms));
}




// 페이지 로드 시 로컬 스토리지에서 알람 정보를 불러와서 표시합니다.
function loadAlarmsFromLocalStorage() {
    var alarms = JSON.parse(localStorage.getItem("alarms")) || [];
    alarms.forEach(function(alarmInfo) {
        displayAlarm(alarmInfo);
    });
}



window.onload = function() {
    // DOM 로드 완료 후 WebSocket 연결
    connect();
};


