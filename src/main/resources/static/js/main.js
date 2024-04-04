let startId = 11140;
let endId = 11740;
let i = null;

for(i = startId; i <= endId; i++){
    let currentId = 'LCD' + i;
    let element = document.getElementById(currentId);
    if(element){
        element.addEventListener('click', function(event){
            let districtName = event.target.textContent;
            selectDateAndTime(event,districtName);
        });
    }

}

/*document.getElementById('LCD11740').addEventListener('click', function() {
    var districtName = event.target.textContent;
    selectDateAndTime(event,districtName);

});*/

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

function click() {
    var alarmContent = document.getElementById('alarmNotifications');
    alarmContent.style.display = alarmContent.style.display === 'none' ? 'block' : 'none';
}

document.getElementById('alarmHeader').addEventListener('click', click);


/*let stompClient = new StompJs.Client({
    brokerURL : 'ws//localhost:8090/ws/alarm'
});*/
let stompCLient = null;
let displayedAlarms = new Set(); // 화면에 표시된 알람 ID를 저장하는 Set

function connect() {
    let socket = new SockJS('/ws/alarm');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('연결 :' + frame);

        //서버->클라
        //alarm -> 서비스 getAlarmView 메서드에 alarmIssuedDTO
        stompClient.subscribe('/topic/alarm', function (alarm) {
            let alarmMessages = JSON.parse(alarm.body);


            if (!Array.isArray(alarmMessages)) {
                alarmMessages = [alarmMessages];
            }

            alarmMessages.forEach(alarmMessages => {
                // 이미 화면에 표시된 알람이 아닐 경우에만 추가
                if (!displayedAlarms.has(alarmMessages.id)) {
                    disPlayAlarmMessage(alarmMessages.id, alarmMessages.measurementName, alarmMessages.message, alarmMessages.time);
                    displayedAlarms.add(alarmMessages.id); // Set에 알람 ID 추가
                }
            });
        });

        // 최초 페이지 로드 시에만 서버에 초기 알람 목록을 요청
        // 현재는 아래와 같은 방법을 더 선호한다함
            stompClient.send("/app/loadInitialAlarms", {}, {});

       /*     stompClient.publish({
                destination: "/app/ws",
                body:'{}',
                headers:{}
            });*/
    });
}
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect(function() {
            console.log("Disconnected");
        });
    }
}

let alarmArray = [];

function disPlayAlarmMessage(id,measurementName,alarmMessage,time){

    let newAlarm = {id,measurementName,alarmMessage,time};
    alarmArray.push(newAlarm);
    alarmArray.sort((a, b) => new Date(a.time) - new Date(b.time)); //time 순으로 정렬
    updateAlarmMessage();
}


function updateAlarmMessage() {
    let alarmList = document.getElementById('alarmNotifications');
    alarmList.innerText = "";
    // 로컬 스토리지에서 삭제된 알람 목록
    let deletedAlarms = JSON.parse(localStorage.getItem("deletedAlarms")) || [];

    alarmArray.forEach(alarm => {
        // 현재 알람 ID가 삭제된 알람 목록에 없는 경우에만 알람을 화면에 추가
        if (!deletedAlarms.includes(alarm.id)) {

            let messageElement = document.createElement('div');
            messageElement.classList.add('alarm-message');
            messageElement.id = `alarm-${alarm.id}`;
            messageElement.innerText = `경보 알람 메시지: ${alarm.measurementName}: ${alarm.alarmMessage} 시간은: ${alarm.time}`;
            alarmList.appendChild(messageElement);

            let deleteButton = document.createElement('button');
            deleteButton.innerHTML = '삭제';
            deleteButton.onclick = function () {
                deleteAlarmView(id);
            };
            messageElement.appendChild(deleteButton);
        }
    })

}



function deleteAlarmView(id) {
    const alarmElement = document.getElementById(`alarm-${id}`);
    if (alarmElement) {
        alarmElement.remove();
    }
    removeFromLocalStorage(id);
}

function removeFromLocalStorage(id) {
    if (id === null || id === undefined) {
        console.error('유효하지 않은 id값');
        return;
    }

    let deletedAlarms = JSON.parse(localStorage.getItem("deletedAlarms")) || [];
    if (!deletedAlarms.includes(id)) {
        deletedAlarms.push(id);
        localStorage.setItem("deletedAlarms", JSON.stringify(deletedAlarms));
    }
}

window.onload = function () {
    connect();
};
window.onbeforeunload = disconnect;

