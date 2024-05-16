let startId = 11140;
let endId = 11740;
let i = null;
// 모달을 가져오기
var modal = document.getElementById("myModal");
// 모달을 여는 버튼 가져오기
var btn = document.getElementById("modalBtn");

// 모달의 닫기 요소(스팬) 가져오기
var span = document.getElementsByClassName("close")[0];

// 버튼 클릭 시 모달 열기
btn.onclick = function() {
    modal.style.display = "block";
}

// 닫기(×) 버튼을 클릭하면 모달 닫기
span.onclick = function() {
    modal.style.display = "none";
}

// 모달 바깥 영역 클릭 시 모달 닫기
window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}


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
                <form id="dateForm" action="/main/info" method = "GET">
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
    // 이벤트 리스너를 폼에 연결
    document.getElementById('dateForm').addEventListener('submit', submitForm);
}
function submitForm(event) {
    event.preventDefault(); // 기본 폼 제출 동작 방지
    const formData = new FormData(event.target);
    const measurementName = formData.get('measurementName');
    const date = formData.get('date');
    const time = formData.get('time');
    const dateTime = `${date}T${time}`;

    const url = `/main/info?measurementName=${encodeURIComponent(measurementName)}&date=${encodeURIComponent(dateTime)}`;
    console.log(url); // 디버깅을 위한 URL 로그 출력

    if (!measurementName || !dateTime) {
        alert("구, 날짜 및 시간을 모두 선택해주세요.");
    } else {
        window.location.href = url; // 올바르게 조합된 URL로 페이지 이동
    }
}



