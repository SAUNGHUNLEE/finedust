function selectDateAndTime(districtName) {
    var container = document.getElementById('datePicker');
    container.innerHTML = `
                <h3>${districtName} - 날짜 및 시간 선택</h3>
                <form id="dateForm">
                    <input type="hidden" name="measurementName" value="${districtName}">
                    <select name="date" required>
                        <option value="2023-03-01">2023년 3월 1일</option>
                        ${Array.from({ length: 31 }, (_, i) => `<option value="2023-03-${(i + 1).toString().padStart(2, '0')}">2023년 3월 ${(i + 1).toString().padStart(2, '0')}일</option>`).join('')}
                        <option value="2023-04-01">2023년 4월 1일</option>
                    </select>
                    <select name="time" required>
                        ${Array.from({ length: 24 }, (_, i) => `<option value="${i.toString().padStart(2, '0')}:00:00">${i.toString().padStart(2, '0')}시</option>`).join('')}
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
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', },
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