<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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

<!-- Daum/Kakao 주소 검색 API -->
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
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

<c:if test="${not empty errorMsg}">
    <script>
        alert("${errorMsg}");
    </script>
</c:if>

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

            <div class="address-search-row">
                <input type="text"
                       id="locationAddressInput"
                       placeholder="주소 검색 버튼을 눌러주세요"
                       readonly>

                <button type="button"
                        id="addressSearchBtn"
                        class="address-search-btn">
                    주소 검색
                </button>
            </div>
        </div>

        <div class="location-form-group">
            <label for="locationDetailAddressInput">상세주소</label>
            <input type="text"
                   id="locationDetailAddressInput"
                   placeholder="예: 101동 1201호">
        </div>

        <input type="hidden" id="locationPostcodeInput">
        <input type="hidden" id="locationRoadAddressInput">
        <input type="hidden" id="locationJibunAddressInput">
        <input type="hidden" id="locationLatitudeInput">
        <input type="hidden" id="locationLongitudeInput">

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
    
    /*
     * 길찾기 차량 이동 시뮬레이션용 마커
     */
    var carOverlay = null;
    var carMoveTimer = null;

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

            html += "   <div class='saved-place-actions'>";

            if (location.isDefault) {
                html += "       <span class='default-location-badge'>기본 위치</span>";
            } else {
                html += "       <button type='button' class='default-location-btn' ";
                html += "data-location-id='" + location.locationId + "'>";
                html += "기본 설정";
                html += "       </button>";
            }

            html += "       <button type='button' class='delete-location-btn' ";
            html += "data-location-id='" + location.locationId + "'>";
            html += "삭제";
            html += "       </button>";
            html += "   </div>";

            html += "</div>";
        }

        list.innerHTML = html;

        bindSavedLocationEvents();
        bindDefaultLocationEvents();
        bindDeleteLocationEvents();

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

        drawSavedLocationMarkers(locations);
    }

    /*
     * 기본 위치 설정 버튼 이벤트
     */
    function bindDefaultLocationEvents() {
        var defaultButtons = document.querySelectorAll(".default-location-btn");

        defaultButtons.forEach(function(button) {
            button.addEventListener("click", function(e) {
                e.stopPropagation();

                var locationId = button.dataset.locationId;

                setDefaultLocation(locationId);
            });
        });
    }

    /*
     * 기본 위치 설정 요청
     */
    function setDefaultLocation(locationId) {
        var formData = new URLSearchParams();
        formData.append("locationId", locationId);

        fetch(contextPath + "/station/saved-locations/default", {
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
                alert("기본 출발 위치가 변경되었습니다.");

                clearRouteLine();
                loadSavedLocations();

            } else if (resultText === "login_required") {
                alert("로그인 후 이용할 수 있습니다.");
                location.href = contextPath + "/login";

            } else {
                alert("기본 위치 설정에 실패했습니다.");
            }
        })
        .catch(function(error) {
            console.log("set default location error => ", error);
            alert("기본 위치 설정 중 오류가 발생했습니다.");
        });
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

        var savedPlaces = document.querySelectorAll(".saved-place");

        savedPlaces.forEach(function(place) {
            place.classList.remove("active");

            if (String(place.dataset.locationId) === String(location.locationId)) {
                place.classList.add("active");
            }
        });

        moveToStartLocation();

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

        document.getElementById("addressSearchBtn").addEventListener("click", function() {
            openDaumAddressSearch();
        });

        document.getElementById("saveLocationBtn").addEventListener("click", function() {
            saveSavedLocation();
        });
    }

    /*
     * Daum/Kakao 주소 검색
     */
    function openDaumAddressSearch() {
        if (typeof daum === "undefined" || !daum.Postcode) {
            alert("주소 검색 API를 불러오지 못했습니다.");
            return;
        }

        new daum.Postcode({
            oncomplete: function(data) {
                var selectedAddress = "";

                if (data.userSelectedType === "R") {
                    selectedAddress = data.roadAddress;
                } else {
                    selectedAddress = data.jibunAddress;
                }

                document.getElementById("locationAddressInput").value = selectedAddress;
                document.getElementById("locationPostcodeInput").value = data.zonecode || "";
                document.getElementById("locationRoadAddressInput").value = data.roadAddress || "";
                document.getElementById("locationJibunAddressInput").value = data.jibunAddress || "";

                document.getElementById("locationLatitudeInput").value = "";
                document.getElementById("locationLongitudeInput").value = "";

                setLocationCoordinateByAddress(selectedAddress);

                document.getElementById("locationDetailAddressInput").focus();
            }
        }).open();
    }

    /*
     * 선택한 주소를 위도/경도로 변환
     */
    function setLocationCoordinateByAddress(address) {
        if (!geocoder || !address) {
            return;
        }

        geocoder.addressSearch(address, function(result, status) {
            if (status !== kakao.maps.services.Status.OK || !result || result.length === 0) {
                console.log("주소 좌표 변환 실패 => ", address);
                return;
            }

            document.getElementById("locationLatitudeInput").value = result[0].y;
            document.getElementById("locationLongitudeInput").value = result[0].x;
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
        document.getElementById("locationDetailAddressInput").value = "";
        document.getElementById("locationPostcodeInput").value = "";
        document.getElementById("locationRoadAddressInput").value = "";
        document.getElementById("locationJibunAddressInput").value = "";
        document.getElementById("locationLatitudeInput").value = "";
        document.getElementById("locationLongitudeInput").value = "";
        document.getElementById("locationDefaultInput").checked = false;
    }

    /*
     * 위치 등록
     */
    function saveSavedLocation() {
        var locationName = document.getElementById("locationNameInput").value.trim();
        var locationType = document.getElementById("locationTypeInput").value;
        var address = document.getElementById("locationAddressInput").value.trim();
        var detailAddress = document.getElementById("locationDetailAddressInput").value.trim();
        var latitudeValue = document.getElementById("locationLatitudeInput").value;
        var longitudeValue = document.getElementById("locationLongitudeInput").value;
        var isDefault = document.getElementById("locationDefaultInput").checked;

        if (locationName === "") {
            alert("위치 이름을 입력하세요.");
            return;
        }

        if (address === "") {
            alert("주소 검색 버튼을 눌러 주소를 선택하세요.");
            return;
        }

        var fullAddress = address;

        if (detailAddress !== "") {
            fullAddress += " " + detailAddress;
        }

        if (latitudeValue !== "" && longitudeValue !== "") {
            requestSaveLocation(
                locationName,
                locationType,
                fullAddress,
                parseFloat(latitudeValue),
                parseFloat(longitudeValue),
                isDefault
            );

            return;
        }

        geocoder.addressSearch(address, function(result, status) {
            if (status !== kakao.maps.services.Status.OK || !result || result.length === 0) {
                alert("주소를 찾을 수 없습니다. 주소 검색으로 다시 선택해 주세요.");
                return;
            }

            requestSaveLocation(
                locationName,
                locationType,
                fullAddress,
                parseFloat(result[0].y),
                parseFloat(result[0].x),
                isDefault
            );
        });
    }

    /*
     * 저장 위치 등록 요청
     */
    function requestSaveLocation(locationName, locationType, address, latitude, longitude, isDefault) {
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
        html += "</div>";

        html += "<div class='charger-list'>";
        html += "   <div class='charger-row'>";
        html += "       <div>";
        html += "           <strong>충전기 정보</strong>";
        html += "           <p>등록된 충전기 " + chargerCount + "대 / 사용 가능 " + availableCount + "대</p>";
        html += "       </div>";

        if (availableCount > 0) {
            html += "       <em>사용 가능</em>";
        } else {
            html += "       <em>사용 불가</em>";
        }

        html += "   </div>";
        html += "</div>";
        
        html += "<a class='detail-link' href='" + contextPath + "/station/detail?stationId="
            + station.stationId + "'>상세보기</a>";

        panel.innerHTML = html;
    }

    /*
     * 길찾기 요청
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
                startCarMoveSimulation(route);
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
     * 경로를 따라 차량이 이동하는 시뮬레이션
     *
     * route.path 배열을 기준으로 차량 마커를 순서대로 이동시킨다.
     */
    function startCarMoveSimulation(route) {
        clearCarSimulation();

        if (!route.path || route.path.length === 0) {
            return;
        }

        /*
         * route.path 점 간격이 넓으면 차량이 툭툭 끊겨 보이므로
         * 중간 좌표를 만들어 부드럽게 이동시킨다.
         */
        var smoothPath = buildSmoothPath(route.path, 12);

        if (smoothPath.length === 0) {
            return;
        }

        var firstPosition = new kakao.maps.LatLng(
            smoothPath[0].lat,
            smoothPath[0].lng
        );

        carOverlay = new kakao.maps.CustomOverlay({
            position: firstPosition,
            content: "<div class='car-simulation-marker'></div>",
            xAnchor: 0.5,
            yAnchor: 0.5,
            zIndex: 999
        });

        carOverlay.setMap(map);

        var index = 0;

        carMoveTimer = setInterval(function() {
            index++;

            if (index >= smoothPath.length) {
                clearInterval(carMoveTimer);
                carMoveTimer = null;

                showCarArrivedMarker(smoothPath[smoothPath.length - 1]);
                return;
            }

            var nextPosition = new kakao.maps.LatLng(
                smoothPath[index].lat,
                smoothPath[index].lng
            );

            carOverlay.setPosition(nextPosition);

        }, 70);
    }
    
    /*
     * 경로 좌표 사이를 잘게 나눠서 부드러운 이동 경로를 만든다.
     *
     * path:
     * - 서버에서 받은 route.path
     *
     * divideCount:
     * - 두 좌표 사이를 몇 등분할지
     */
    function buildSmoothPath(path, divideCount) {
        var smoothPath = [];

        for (var i = 0; i < path.length - 1; i++) {
            var start = path[i];
            var end = path[i + 1];

            for (var j = 0; j < divideCount; j++) {
                var ratio = j / divideCount;

                smoothPath.push({
                    lat: start.lat + (end.lat - start.lat) * ratio,
                    lng: start.lng + (end.lng - start.lng) * ratio
                });
            }
        }

        smoothPath.push(path[path.length - 1]);

        return smoothPath;
    }
    
    /*
     * 차량이 도착했을 때 마커 모양 변경
     */
    function showCarArrivedMarker(position) {
        if (!carOverlay) {
            return;
        }

        carOverlay.setContent("<div class='car-simulation-marker arrived'>도착</div>");

        carOverlay.setPosition(new kakao.maps.LatLng(
            position.lat,
            position.lng
        ));
    }
    
    /*
     * 기존 차량 이동 시뮬레이션 제거
     */
    function clearCarSimulation() {
        if (carMoveTimer) {
            clearInterval(carMoveTimer);
            carMoveTimer = null;
        }

        if (carOverlay) {
            carOverlay.setMap(null);
            carOverlay = null;
        }
    }

    /*
     * 기존 길찾기 경로선 제거
     */
     function clearRouteLine() {
    	    clearCarSimulation();

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
    function renderRouteSummary(route) {
        var panel = document.getElementById("stationDetailPanel");

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
     * 기존 방식:
     * - /station/chargers 조회
     * - 첫 번째 사용가능 충전기 선택
     *
     * 변경 방식:
     * - 충전소 ID만 서버로 전달
     * - 서버에서 사용가능 + Redis Lock 가능한 충전기를 찾음
     * - 예약 가능한 충전기가 있으면 /reservation/form?chargerId=... 로 이동
     * - 없으면 /station/map으로 돌아오며 errorMsg 표시
     */
    function goReservation(stationId) {
        location.href = contextPath + "/reservation/form/station?stationId=" + stationId;
    }
</script>

</body>
</html>