const inputField = document.getElementById("stationInput");
const suggestionBox = document.getElementById("suggestions");

// 디바운스 처리: 입력 후 일정 시간 대기 후 요청
let debounceTimeout;

inputField.addEventListener("input", () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => {
        const query = inputField.value.trim();
        if (query.length > 0) {
            fetchSuggestions(query);
        } else {
            suggestionBox.innerHTML = ""; // 입력이 없으면 초기화
        }
    }, 300); // 300ms 대기
});

// 서버로 자동완성 요청
async function fetchSuggestions(query) {
    try {
        const response = await fetch(`/subway/search/autocomplete?query=${query}`);
        const data = await response.json();

        displaySuggestions(data.data.stations);
    } catch (error) {
        console.error("자동완성 요청 중 에러:", error);
    }
}

// 자동완성 결과 표시
function displaySuggestions(stations) {
    suggestionBox.innerHTML = ""; // 이전 결과 초기화

    if (stations.length === 0) {
        suggestionBox.innerHTML = "<p>결과가 없습니다.</p>";
        return;
    }

    stations.forEach(station => {
        const suggestionItem = document.createElement("div");
        suggestionItem.textContent = station;
        suggestionItem.style.cursor = "pointer";

        // 클릭 이벤트: 클릭한 역 이름을 입력란에 설정
        suggestionItem.addEventListener("click", () => {
            inputField.value = station;
            suggestionBox.innerHTML = ""; // 선택 후 목록 초기화
        });

        suggestionBox.appendChild(suggestionItem);
    });
}
