<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>충전소 탐색</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/station/station_map.css">

<script type="text/javascript"
        src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJavascriptKey}&libraries=services&autoload=false">
</script>
</head>
<body>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="map-page">

    <!-- 왼쪽 패널 -->
    <aside class="map-left-panel">

        <section class="panel-section">
            <div class="panel-title-row">
                <h3>출발지 등록</h3>
                <button type="button" id="openLocationModalBtn">+ 위치 추가</button>
            </div>

            <div id="savedLocationList" class="saved-location-list">
                <div class="empty-location">주소를 추가하세요.</div>
            </div>
        </section>

        <section class="panel-section">
            <div class="panel-title-row">
                <h3>즐겨찾기</h3>
                <button type="button">전체 보기</button>
            </div>

            <div id="favoriteList" class="favorite-list">
                <div class="favorite-item">
                    <span>⚡</span>
                    <div>
                        <strong>부산 사상 EV 충전소</strong>
                        <p>1.2km</p>
                    </div>
                </div>

                <div class="favorite-item">
                    <span>⚡</span>
                    <div>
                        <strong>서면 EV 스테이션</strong>
                        <p>2.3km</p>
                    </div>
                </div>
            </div>
        </section>

    </aside>

    <!-- 가운데 지도 영역 -->
    <main class="map-center">

        <div class="map-toolbar">
            <div class="search-box">
                <input type="text" id="stationSearchInput" placeholder="지역, 충전소명 검색">
                <button type="button" id="stationSearchBtn">🔍</button>
            </div>
        </div>

        <div class="map-wrapper">
            <div id="map"></div>

            <a href="${pageContext.request.contextPath}/station/list" class="list-view-btn">
                목록 보기
            </a>
        </div>

    </main>

    <!-- 오른쪽 상세 패널 -->
    <aside class="map-right-panel" id="stationDetailPanel">
        <div class="detail-empty">
            지도에서 충전소를 선택하세요.
        </div>
    </aside>

</div>

<!-- 위치 추가 모달 -->
<div id="locationModal" class="location-modal">
    <div class="location-modal-content">
        <div class="location-modal-header">
            <h3>위치 추가</h3>
            <button type="button" id="closeLocationModalBtn">×</button>
        </div>

        <div class="location-form-group">
            <label for="locationNameInput">위치 이름</label>
            <input type="text" id="locationNameInput" placeholder="예: 집, 회사, 학교">
        </div>

        <div class="location-form-group">
            <label for="locationTypeInput">위치 유형</label>
            <select id="locationTypeInput">
                <option value="HOME">집</option>
                <option value="WORK">회사</option>
                <option value="SCHOOL">학교</option>
                <option value="FAVORITE">즐겨찾기</option>
                <option value="CUSTOM">직접 입력</option>
            </select>
        </div>

        <div class="location-form-group">
            <label for="locationAddressInput">주소</label>
            <input type="text" id="locationAddressInput" placeholder="예: 부산 부산진구 중앙대로 627">
        </div>

        <label class="default-check">
            <input type="checkbox" id="locationDefaultInput">
            기본 출발 위치로 설정
        </label>

        <div class="location-modal-actions">
            <button type="button" class="cancel-location-btn" id="cancelLocationBtn">취소</button>
            <button type="button" class="save-location-btn" id="saveLocationBtn">저장</button>
        </div>
    </div>
</div>

<script>
    var contextPath = "${pageContext.request.contextPath}";

    var urlParams = new URLSearchParams(window.location.search);
    var selectedStationId = urlParams.get("stationId");

    var defaultLat = 35.1631;
    var defaultLng = 128.9842;

    var map = null;

    /*
     * 충전소 마커 목록
     */
    var markers = [];

    /*
     * 주소 등록 위치 마커 목록
     */
    var locationMarkers = [];

    /*
     * 길찾기 경로선
     */
    var routeLine = null;

    var infoWindow = null;
    var geocoder = null;

    /*
     * 길찾기 출발지로 사용할 주소 등록 위치
     */
    var selectedStartLocation = null;

    /*
     * 현재 오른쪽 상세 패널에 표시 중인 충전소
     * 길찾기 도착점으로 사용한다.
     */
    var currentStation = null;

    /*
     * 카카오맵 SDK 로딩이 끝난 뒤 지도 생성
     */
    kakao.maps.load(function() {
        console.log("kakao.maps.load 실행됨");

        initMap();
        bindSearchEvents();
        bindLocationModalEvents();

        loadSavedLocations();
        loadStations(null);
    });

    /*
     * 카카오맵 생성
     */
    function initMap() {
        console.log("initMap 실행됨");

        var mapContainer = document.getElementById("map");

        var mapOption = {
            center: new kakao.maps.LatLng(defaultLat, defaultLng),
            level: 5
        };

        map = new kakao.maps.Map(mapContainer, mapOption);
        infoWindow = new kakao.maps.InfoWindow();

        /*
         * 주소 → 좌표 변환용
         * SDK에 libraries=services가 반드시 필요하다.
         */
        geocoder = new kakao.maps.services.Geocoder();

        console.log("지도 생성 완료");
    }

    /*
     * 검색 버튼 / 엔터 이벤트 연결
     */
    function bindSearchEvents() {
        var searchInput = document.getElementById("stationSearchInput");
        var searchBtn = document.getElementById("stationSearchBtn");

        searchBtn.addEventListener("click", function() {
            searchStations();
        });

        searchInput.addEventListener("keydown", function(e) {
            if (e.key === "Enter") {
                searchStations();
            }
        });
    }

    /*
     * 검색 실행
     */
    function searchStations() {
        var keyword = document.getElementById("stationSearchInput").value;

        /*
         * 검색 후에는 목록 화면에서 넘어온 stationId 기준 이동을 더 이상 적용하지 않음
         */
        selectedStationId = null;

        loadStations(keyword);
    }

    /*
     * DB 충전소 목록 조회
     *
     * keyword가 있으면:
     * GET /station/map-data?keyword=검색어
     *
     * keyword가 없으면:
     * GET /station/map-data
     */
    function loadStations(keyword) {
        var url = contextPath + "/station/map-data";

        if (keyword && keyword.trim() !== "") {
            url += "?keyword=" + encodeURIComponent(keyword.trim());
        }

        fetch(url)
            .then(function(response) {
                return response.json();
            })
            .then(function(stations) {
                console.log("stations => ", stations);

                drawMarkers(stations);

                if (!stations || stations.length === 0) {
                    renderEmptySearchResult(keyword);
                    return;
                }

                /*
                 * 목록 화면에서 stationId를 들고 넘어온 경우
                 * 해당 충전소를 찾아서 지도 중심 이동 + 상세 패널 출력
                 */
                if (selectedStationId) {
                    var selectedStation = null;

                    for (var i = 0; i < stations.length; i++) {
                        if (String(stations[i].stationId) === String(selectedStationId)) {
                            selectedStation = stations[i];
                            break;
                        }
                    }

                    if (selectedStation) {
                        moveToStation(selectedStation);
                        renderStationDetail(selectedStation);
                        return;
                    }
                }

                /*
                 * 검색 결과 또는 전체 조회 결과의 첫 번째 충전소를 기본 표시
                 */
                renderStationDetail(stations[0]);
            })
            .catch(function(error) {
                console.log("map-data error => ", error);
                renderEmptySearchResult(keyword);
            });
    }

    /*
     * 기존 충전소 마커 제거
     */
    function clearMarkers() {
        for (var i = 0; i < markers.length; i++) {
            markers[i].setMap(null);
        }

        markers = [];
    }

    /*
     * 지도에 충전소 마커 표시
     */
    function drawMarkers(stations) {
        clearMarkers();

        if (!stations || stations.length === 0) {
            console.log("충전소 데이터 없음");
            return;
        }

        var bounds = new kakao.maps.LatLngBounds();

        for (var i = 0; i < stations.length; i++) {
            (function(station) {
                var position = new kakao.maps.LatLng(station.latitude, station.longitude);

                var marker = new kakao.maps.Marker({
                    map: map,
                    position: position
                });

                bounds.extend(position);

                kakao.maps.event.addListener(marker, "click", function() {
                    map.setLevel(4);
                    map.panTo(position);
                    renderStationDetail(station);
                });

                markers.push(marker);
            })(stations[i]);
        }

        /*
         * 특정 충전소 선택 없이 들어왔을 때는 전체 검색 결과가 보이게 조정
         * 검색 결과가 1개면 해당 위치로 확대
         */
        if (!selectedStationId) {
            if (stations.length === 1) {
                moveToStation(stations[0]);
            } else {
                map.setBounds(bounds);
            }
        }
    }

    /*
     * 특정 충전소 위치로 이동
     */
    function moveToStation(station) {
        var position = new kakao.maps.LatLng(station.latitude, station.longitude);

        map.setLevel(4);
        map.panTo(position);
    }

    /*
     * 검색 결과 없음 표시
     */
    function renderEmptySearchResult(keyword) {
        var panel = document.getElementById("stationDetailPanel");

        var text = keyword && keyword.trim() !== "" ? keyword.trim() : "전체";

        var html = "";

        html += "<div class='detail-empty'>";
        html += "   <div>";
        html += "       <strong>'" + text + "' 검색 결과가 없습니다.</strong>";
        html += "       <p>충전소명, 주소, 운영사를 다시 입력해 주세요.</p>";
        html += "   </div>";
        html += "</div>";

        panel.innerHTML = html;
    }

    /*
     * 저장 위치 목록 조회
     *
     * 서버 API:
     * GET /station/saved-locations
     */
    function loadSavedLocations() {
        fetch(contextPath + "/station/saved-locations")
            .then(function(response) {
                return response.json();
            })
            .then(function(locations) {
                console.log("savedLocations => ", locations);

                renderSavedLocations(locations);
            })
            .catch(function(error) {
                console.log("saved location error => ", error);

                var list = document.getElementById("savedLocationList");
                list.innerHTML = "<div class='empty-location'>위치를 불러오지 못했습니다.</div>";
            });
    }

    /*
     * 저장 위치 목록 렌더링
     *
     * 1. 좌측 주소 등록 목록 출력
     * 2. 저장 위치 클릭 이벤트 연결
     * 3. 삭제 버튼 이벤트 연결
     * 4. 지도 위에 저장 위치 마커 표시
     */
    function renderSavedLocations(locations) {
        var list = document.getElementById("savedLocationList");

        if (!locations || locations.length === 0) {
            list.innerHTML = "<div class='empty-location'>저장된 위치가 없습니다.</div>";
            selectedStartLocation = null;

            clearLocationMarkers();

            return;
        }

        var html = "";

        for (var i = 0; i < locations.length; i++) {
            var location = locations[i];

            var activeClass = location.isDefault ? " active" : "";

            html += "<div class='saved-place" + activeClass + "' ";
            html += "data-location-id='" + location.locationId + "' ";
            html += "data-name='" + location.locationName + "' ";
            html += "data-type='" + location.locationType + "' ";
            html += "data-address='" + location.address + "' ";
            html += "data-lat='" + location.latitude + "' ";
            html += "data-lng='" + location.longitude + "'>";

            html += "   <div class='saved-place-info'>";
            html += "       <strong>" + location.locationName + "</strong>";
            html += "       <p>" + location.address + "</p>";
            html += "   </div>";

            html += "   <button type='button' class='delete-location-btn' ";
            html += "data-location-id='" + location.locationId + "'>";
            html += "삭제";
            html += "   </button>";

            html += "</div>";
        }

        list.innerHTML = html;

        /*
         * innerHTML로 새로 그렸기 때문에 이벤트를 다시 연결해야 한다.
         */
        bindSavedLocationEvents();
        bindDeleteLocationEvents();

        /*
         * 기본 위치가 있으면 기본 위치를 출발지로 설정.
         * 없으면 첫 번째 위치를 출발지로 설정.
         */
        var defaultLocation = locations[0];

        for (var j = 0; j < locations.length; j++) {
            if (locations[j].isDefault) {
                defaultLocation = locations[j];
                break;
            }
        }

        selectedStartLocation = {
            locationId: defaultLocation.locationId,
            name: defaultLocation.locationName,
            type: defaultLocation.locationType,
            address: defaultLocation.address,
            lat: defaultLocation.latitude,
            lng: defaultLocation.longitude
        };

        console.log("selectedStartLocation => ", selectedStartLocation);

        /*
         * 지도 위에 저장 위치 마커 표시
         */
        drawSavedLocationMarkers(locations);
    }

    /*
     * 기존 저장 위치 마커 제거
     */
    function clearLocationMarkers() {
        for (var i = 0; i < locationMarkers.length; i++) {
            locationMarkers[i].setMap(null);
        }

        locationMarkers = [];
    }

    /*
     * 지도 위에 저장 위치 마커 표시
     *
     * 충전소 마커와 구분되도록 CustomOverlay를 사용한다.
     */
    function drawSavedLocationMarkers(locations) {
        clearLocationMarkers();

        if (!locations || locations.length === 0) {
            return;
        }

        for (var i = 0; i < locations.length; i++) {
            (function(location) {
                var position = new kakao.maps.LatLng(
                    location.latitude,
                    location.longitude
                );

                var markerContent = document.createElement("div");
                markerContent.className = "saved-location-marker";
                markerContent.innerHTML =
                    "<span class='saved-location-dot'></span>" +
                    "<span class='saved-location-label'>" + location.locationName + "</span>";

                /*
                 * 저장 위치 마커 클릭 시 해당 위치를 출발지로 선택
                 */
                markerContent.addEventListener("click", function() {
                    selectSavedLocation(location);
                });

                var overlay = new kakao.maps.CustomOverlay({
                    map: map,
                    position: position,
                    content: markerContent,
                    yAnchor: 1
                });

                locationMarkers.push(overlay);
            })(locations[i]);
        }
    }

    /*
     * 저장 위치 선택
     *
     * 좌측 목록 클릭 또는 지도 위 저장 위치 마커 클릭 시 실행된다.
     */
    function selectSavedLocation(location) {
        selectedStartLocation = {
            locationId: location.locationId,
            name: location.locationName,
            type: location.locationType,
            address: location.address,
            lat: location.latitude,
            lng: location.longitude
        };

        console.log("selectedStartLocation => ", selectedStartLocation);

        /*
         * 좌측 목록 active 처리
         */
        var savedPlaces = document.querySelectorAll(".saved-place");

        savedPlaces.forEach(function(place) {
            place.classList.remove("active");

            if (String(place.dataset.locationId) === String(location.locationId)) {
                place.classList.add("active");
            }
        });

        moveToStartLocation();

        /*
         * 충전소 상세 패널이 이미 열려 있으면 출발지 문구 갱신
         */
        if (currentStation) {
            renderStationDetail(currentStation);
        }
    }

    /*
     * 저장 위치 클릭 이벤트
     */
    function bindSavedLocationEvents() {
        var savedPlaces = document.querySelectorAll(".saved-place");

        savedPlaces.forEach(function(place) {
            place.addEventListener("click", function() {
                var location = {
                    locationId: place.dataset.locationId,
                    locationName: place.dataset.name,
                    locationType: place.dataset.type,
                    address: place.dataset.address,
                    latitude: parseFloat(place.dataset.lat),
                    longitude: parseFloat(place.dataset.lng)
                };

                selectSavedLocation(location);
            });
        });
    }

    /*
     * 저장 위치 삭제 버튼 이벤트
     */
    function bindDeleteLocationEvents() {
        var deleteButtons = document.querySelectorAll(".delete-location-btn");

        deleteButtons.forEach(function(button) {
            button.addEventListener("click", function(e) {
                /*
                 * 삭제 버튼을 눌렀을 때 부모 saved-place 클릭 이벤트가
                 * 같이 실행되지 않도록 막는다.
                 */
                e.stopPropagation();

                var locationId = button.dataset.locationId;

                if (!confirm("위치를 삭제하시겠습니까?")) {
                    return;
                }

                deleteSavedLocation(locationId);
            });
        });
    }

    /*
     * 저장 위치 삭제
     *
     * 삭제 버튼 클릭
     * → /station/saved-locations/delete POST 요청
     * → DB 삭제
     * → 좌측 위치 목록 다시 조회
     */
    function deleteSavedLocation(locationId) {
        var formData = new URLSearchParams();
        formData.append("locationId", locationId);

        fetch(contextPath + "/station/saved-locations/delete", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        })
        .then(function(response) {
            return response.text();
        })
        .then(function(resultText) {
            if (resultText === "success") {
                alert("위치가 삭제되었습니다.");

                clearRouteLine();
                loadSavedLocations();
            } else if (resultText === "login_required") {
                alert("로그인 후 이용할 수 있습니다.");
                location.href = contextPath + "/login";
            } else {
                alert("위치 삭제에 실패했습니다.");
            }
        })
        .catch(function(error) {
            console.log("delete location error => ", error);
            alert("위치 삭제 중 오류가 발생했습니다.");
        });
    }

    /*
     * 선택한 저장 위치로 지도 이동
     */
    function moveToStartLocation() {
        if (!selectedStartLocation) {
            return;
        }

        var position = new kakao.maps.LatLng(
            selectedStartLocation.lat,
            selectedStartLocation.lng
        );

        map.setLevel(5);
        map.panTo(position);
    }

    /*
     * 위치 추가 모달 이벤트
     */
    function bindLocationModalEvents() {
        var modal = document.getElementById("locationModal");

        document.getElementById("openLocationModalBtn").addEventListener("click", function() {
            modal.classList.add("active");
        });

        document.getElementById("closeLocationModalBtn").addEventListener("click", function() {
            closeLocationModal();
        });

        document.getElementById("cancelLocationBtn").addEventListener("click", function() {
            closeLocationModal();
        });

        document.getElementById("saveLocationBtn").addEventListener("click", function() {
            saveSavedLocation();
        });
    }

    /*
     * 위치 추가 모달 닫기
     */
    function closeLocationModal() {
        document.getElementById("locationModal").classList.remove("active");

        document.getElementById("locationNameInput").value = "";
        document.getElementById("locationTypeInput").value = "HOME";
        document.getElementById("locationAddressInput").value = "";
        document.getElementById("locationDefaultInput").checked = false;
    }

    /*
     * 위치 등록
     *
     * 1. 주소 입력
     * 2. 카카오 Geocoder로 주소 → 좌표 변환
     * 3. /station/saved-locations POST
     * 4. DB 저장 성공 시 좌측 위치 목록 다시 조회
     */
    function saveSavedLocation() {
        var locationName = document.getElementById("locationNameInput").value.trim();
        var locationType = document.getElementById("locationTypeInput").value;
        var address = document.getElementById("locationAddressInput").value.trim();
        var isDefault = document.getElementById("locationDefaultInput").checked;

        if (locationName === "") {
            alert("위치 이름을 입력하세요.");
            return;
        }

        if (address === "") {
            alert("주소를 입력하세요.");
            return;
        }

        geocoder.addressSearch(address, function(result, status) {
            if (status !== kakao.maps.services.Status.OK) {
                alert("주소를 찾을 수 없습니다. 정확한 주소를 입력하세요.");
                return;
            }

            var latitude = parseFloat(result[0].y);
            var longitude = parseFloat(result[0].x);

            var formData = new URLSearchParams();
            formData.append("locationName", locationName);
            formData.append("locationType", locationType);
            formData.append("address", address);
            formData.append("latitude", latitude);
            formData.append("longitude", longitude);
            formData.append("isDefault", isDefault);

            fetch(contextPath + "/station/saved-locations", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData.toString()
            })
            .then(function(response) {
                return response.text();
            })
            .then(function(resultText) {
                if (resultText === "success") {
                    alert("위치가 추가되었습니다.");

                    closeLocationModal();
                    loadSavedLocations();

                    var position = new kakao.maps.LatLng(latitude, longitude);
                    map.setLevel(5);
                    map.panTo(position);
                } else if (resultText === "login_required") {
                    alert("로그인 후 위치를 추가할 수 있습니다.");
                    location.href = contextPath + "/login";
                } else {
                    alert("위치 추가에 실패했습니다. 응답값: " + resultText);
                }
            })
            .catch(function(error) {
                console.log("save location error => ", error);
                alert("위치 추가 중 오류가 발생했습니다.");
            });
        });
    }

    /*
     * 오른쪽 상세 패널 출력
     */
    function renderStationDetail(station) {
        /*
         * 현재 선택된 충전소를 저장한다.
         * 길찾기에서 도착점으로 사용한다.
         */
        currentStation = station;

        var panel = document.getElementById("stationDetailPanel");

        var chargerCount = station.chargerCount || 0;
        var availableCount = station.availableChargerCount || 0;

        var html = "";

        html += "<div class='detail-header'>";
        html += "   <div>";
        html += "       <h2>" + station.stationName + " <span>☆</span></h2>";
        html += "       <p>" + station.address + "</p>";

        if (selectedStartLocation) {
            html += "       <p class='start-location-text'>출발지: "
                + selectedStartLocation.name + " · "
                + selectedStartLocation.address
                + "</p>";
        }

        html += "       <div class='detail-meta'>";
        html += "           <span>24시간 운영</span>";
        html += "           <span class='status'>" + station.stationStatus + "</span>";
        html += "       </div>";
        html += "   </div>";
        html += "</div>";

        html += "<div class='charger-tags'>";
        html += "   <span>충전기 " + chargerCount + "대</span>";
        html += "   <span>사용 가능 " + availableCount + "대</span>";
        html += "   <span>주차 가능</span>";
        html += "</div>";

        html += "<div class='detail-actions'>";
        html += "   <button type='button' class='route-btn' onclick='requestRouteSimulation()'>길찾기</button>";
        html += "   <button type='button' class='reserve-btn' onclick='goReservation("
            + station.stationId + ")'>예약하기</button>";
        html += "</div>";

        html += "<div class='detail-tabs'>";
        html += "   <button type='button' class='active'>충전기 정보</button>";
        html += "   <button type='button'>이용 안내</button>";
        html += "</div>";

        html += "<div class='charger-list'>";
        html += "   <div class='charger-row'>";
        html += "       <span class='charger-no'>1</span>";
        html += "       <div>";
        html += "           <strong>충전기 정보</strong>";
        html += "           <p>등록된 충전기 " + chargerCount + "대 / 사용 가능 " + availableCount + "대</p>";
        html += "       </div>";
        html += "       <em>사용 가능</em>";
        html += "   </div>";
        html += "</div>";

        html += "<a class='detail-link' href='" + contextPath + "/station/detail?stationId="
            + station.stationId + "'>상세보기</a>";

        panel.innerHTML = html;
    }

    /*
     * 길찾기 요청
     *
     * 출발지:
     * 좌측 주소 등록에서 선택한 위치
     *
     * 도착지:
     * 현재 오른쪽 상세 패널에 표시 중인 충전소
     */
    function requestRouteSimulation() {
        if (!selectedStartLocation) {
            alert("좌측 주소 등록에서 출발지를 먼저 선택하세요.");
            return;
        }

        if (!currentStation) {
            alert("도착할 충전소를 먼저 선택하세요.");
            return;
        }

        var url = contextPath + "/route/simulation"
            + "?startLat=" + selectedStartLocation.lat
            + "&startLng=" + selectedStartLocation.lng
            + "&endLat=" + currentStation.latitude
            + "&endLng=" + currentStation.longitude;

        console.log("route simulation url => ", url);

        fetch(url)
            .then(function(response) {
                return response.json();
            })
            .then(function(route) {
                console.log("route => ", route);

                if (!route.path || route.path.length === 0) {
                    alert("길찾기 경로를 찾지 못했습니다.");
                    return;
                }

                drawRouteLine(route);
                renderRouteSummary(route);
            })
            .catch(function(error) {
                console.log("route simulation error => ", error);
                alert("길찾기 요청 중 오류가 발생했습니다.");
            });
    }

    /*
     * 지도에 길찾기 경로선 그리기
     */
    function drawRouteLine(route) {
        clearRouteLine();

        var linePath = [];

        for (var i = 0; i < route.path.length; i++) {
            linePath.push(new kakao.maps.LatLng(
                route.path[i].lat,
                route.path[i].lng
            ));
        }

        routeLine = new kakao.maps.Polyline({
            path: linePath,
            strokeWeight: 6,
            strokeColor: "#f05a00",
            strokeOpacity: 0.9,
            strokeStyle: "solid"
        });

        routeLine.setMap(map);

        var bounds = new kakao.maps.LatLngBounds();

        for (var j = 0; j < linePath.length; j++) {
            bounds.extend(linePath[j]);
        }

        map.setBounds(bounds);
    }

    /*
     * 기존 길찾기 경로선 제거
     */
    function clearRouteLine() {
        if (routeLine) {
            routeLine.setMap(null);
            routeLine = null;
        }
    }
    
    /*
     * 길찾기 결과 전체 제거
     *
     * 1. 지도 위 경로선 제거
     * 2. 오른쪽 패널의 길찾기 결과 박스 제거
     */
    function clearRouteResult() {
        clearRouteLine();

        var oldSummary = document.querySelector(".route-summary-box");

        if (oldSummary) {
            oldSummary.remove();
        }
    }

    /*
     * 오른쪽 상세 패널에 길찾기 결과 표시
     */
     /*
      * 오른쪽 상세 패널에 길찾기 결과 표시
      */
     function renderRouteSummary(route) {
         var panel = document.getElementById("stationDetailPanel");

         /*
          * 기존 길찾기 결과가 있으면 제거
          * 같은 충전소에서 길찾기를 여러 번 눌렀을 때 중복 방지
          */
         var oldSummary = document.querySelector(".route-summary-box");

         if (oldSummary) {
             oldSummary.remove();
         }

         var summaryHtml = "";

         summaryHtml += "<div class='route-summary-box'>";
         summaryHtml += "   <div class='route-summary-header'>";
         summaryHtml += "       <strong>길찾기 결과</strong>";
         summaryHtml += "       <button type='button' onclick='clearRouteResult()'>경로 지우기</button>";
         summaryHtml += "   </div>";

         summaryHtml += "   <p>출발지: " + selectedStartLocation.name + "</p>";
         summaryHtml += "   <p>도착지: " + currentStation.stationName + "</p>";

         summaryHtml += "   <div class='route-summary-info'>";
         summaryHtml += "       <span>거리 " + route.distanceText + "</span>";
         summaryHtml += "       <span>예상 " + route.durationText + "</span>";
         summaryHtml += "   </div>";

         summaryHtml += "</div>";

         panel.insertAdjacentHTML("afterbegin", summaryHtml);
     }
    
     /*
      * 예약하기
      *
      * 1. 선택한 충전소의 충전기 목록 조회
      * 2. 사용 가능한 충전기 선택
      * 3. 예약 폼으로 이동
      */
     function goReservation(stationId) {
         fetch(contextPath + "/station/chargers?stationId=" + stationId)
             .then(function(response) {
                 return response.json();
             })
             .then(function(chargers) {
                 console.log("chargers => ", chargers);

                 if (!chargers || chargers.length === 0) {
                     alert("등록된 충전기가 없습니다.");
                     return;
                 }

                 var selectedCharger = null;

                 for (var i = 0; i < chargers.length; i++) {
                     if (chargers[i].status === "사용가능") {
                         selectedCharger = chargers[i];
                         break;
                     }
                 }

                 if (!selectedCharger) {
                     alert("사용 가능한 충전기가 없습니다.");
                     return;
                 }

                 location.href = contextPath + "/reservation/form?chargerId="
                     + selectedCharger.chargerId;
             })
             .catch(function(error) {
                 console.log("charger list error => ", error);
                 alert("충전기 정보를 불러오지 못했습니다.");
             });
     }
</script>

</body>
</html>