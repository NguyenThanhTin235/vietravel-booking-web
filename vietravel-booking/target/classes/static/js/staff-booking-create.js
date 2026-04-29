(function () {
     "use strict";

     function qs(sel, root) {
          return (root || document).querySelector(sel);
     }

     function qsa(sel, root) {
          return Array.prototype.slice.call((root || document).querySelectorAll(sel));
     }

     function fmtMoney(v) {
          if (v === null || v === undefined) return "--";
          var n = Number(v);
          if (Number.isNaN(n)) return "--";
          return n.toLocaleString("vi-VN") + " ₫";
     }

     function pad(n) {
          return n < 10 ? "0" + n : "" + n;
     }

     function toDateStr(d) {
          return d.getFullYear() + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate());
     }

     function monthLabel(year, month) {
          return "Tháng " + month + "/" + year;
     }

     function buildMonthGrid(year, month, departures, selectedDate) {
          var first = new Date(year, month - 1, 1);
          var startDay = (first.getDay() + 6) % 7; // Monday = 0
          var gridStart = new Date(year, month - 1, 1 - startDay);

          var map = {};
          departures.forEach(function (d) {
               if (!d || !d.startDate) return;
               if (!map[d.startDate]) map[d.startDate] = [];
               map[d.startDate].push(d);
          });

          var cells = [];
          for (var i = 0; i < 42; i++) {
               var cur = new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + i);
               var dateStr = toDateStr(cur);
               var inMonth = cur.getMonth() === (month - 1);
               var day = cur.getDate();
               var dayItems = map[dateStr] || [];
               var best = null;
               if (dayItems.length > 0) {
                    best = dayItems.slice().sort(function (a, b) {
                         return (b.available || 0) - (a.available || 0);
                    })[0];
               }

               cells.push({
                    dateStr: dateStr,
                    inMonth: inMonth,
                    day: day,
                    departure: best,
                    selected: selectedDate === dateStr
               });
          }
          return cells;
     }

     function init() {
          var tourSelect = qs("#tourSelect");
          var tourInfo = qs("#tourInfo");
          var tourThumb = qs("#tourThumb");
          var tourTitle = qs("#tourTitle");
          var tourCode = qs("#tourCode");
          var tourLine = qs("#tourLine");
          var tourTransport = qs("#tourTransport");
          var tourDuration = qs("#tourDuration");
          var tourStart = qs("#tourStart");
          var tourBasePrice = qs("#tourBasePrice");

          var calendarSection = qs("#calendarSection");
          var calendarNav = qs("#calendarNav");
          var calendarGrid = qs("#calendarGrid");

          var btnContinue = qs("#btnContinue");
          var step2 = qs("#bookingStep2");

          var summaryTour = qs("#summaryTour");
          var summaryPrice = qs("#summaryPrice");
          var summaryDate = qs("#summaryDate");
          var summarySeats = qs("#summarySeats");
          var summaryTotal = qs("#summaryTotal");
          var summaryPaymentMethod = qs("#summaryPaymentMethod");
          var summaryPaymentStatus = qs("#summaryPaymentStatus");
          var summaryTotal = qs("#summaryTotal");
          var summaryPaymentMethod = qs("#summaryPaymentMethod");
          var summaryPaymentStatus = qs("#summaryPaymentStatus");

          var hiddenSlug = qs("#selectedTourSlug");
          var hiddenDate = qs("#selectedDepartureDate");
          var hiddenDeparture = qs("#selectedDepartureId");
          var selectedDatePreview = qs("#selectedDatePreview");

          var passengerList = qs("#passengerList");
          var btnAddPassenger = qs("#btnAddPassenger");
          var bookingForm = qs("#bookingCreateForm");

          var state = {
               tours: [],
               selectedTourId: null,
               selectedTour: null,
               selectedTourDetail: null,
               selectedDeparture: null,
               selectedDate: null,
               months: [],
               monthIndex: 0,
               departuresCache: {}
          };

          function renderPassengerRow() {
               var row = document.createElement("div");
               row.className = "passenger-row";
               row.innerHTML =
                    '<input class="input" type="text" placeholder="Họ tên hành khách">' +
                    '<input class="input" type="date" placeholder="Ngày sinh">' +
                    '<select class="input">' +
                    '<option value="ADULT">Người lớn</option>' +
                    '<option value="CHILD">Trẻ em</option>' +
                    "</select>" +
                    '<button class="btn btn-ghost" type="button">Xóa</button>';
               qs("button", row).addEventListener("click", function () {
                    row.remove();
                    setSummary();
               });
               passengerList.appendChild(row);
               setSummary();
          }

          function ensurePassengers() {
               if (!passengerList) return;
               passengerList.innerHTML = "";
               renderPassengerRow();
          }

          function setSummary() {
               if (!state.selectedTour) {
                    summaryTour.textContent = "Chưa chọn";
                    summaryPrice.textContent = "--";
               } else {
                    summaryTour.textContent = state.selectedTour.title || "Tour";
                    var price = state.selectedDeparture && state.selectedDeparture.priceAdult != null
                         ? state.selectedDeparture.priceAdult
                         : state.selectedTour.basePrice;
                    summaryPrice.textContent = fmtMoney(price);
               }

               if (!state.selectedDate) {
                    summaryDate.textContent = "Chưa chọn";
                    summarySeats.textContent = "Còn: --";
               } else {
                    summaryDate.textContent = state.selectedDate;
                    var seats = state.selectedDeparture && state.selectedDeparture.available != null
                         ? state.selectedDeparture.available
                         : "--";
                    summarySeats.textContent = "Còn: " + seats;
               }

               if (summaryPaymentMethod) summaryPaymentMethod.textContent = "Tại quầy";
               if (summaryPaymentStatus) summaryPaymentStatus.textContent = "Thành công";

               if (summaryTotal) {
                    var total = computeTotalAmount();
                    summaryTotal.textContent = total > 0 ? fmtMoney(total) : "--";
               }
          }

          function computeTotalAmount() {
               if (!state.selectedDeparture) return 0;
               var priceAdult = state.selectedDeparture.priceAdult || 0;
               var priceChild = state.selectedDeparture.priceChild || 0;
               var counts = getPassengerCounts();
               return priceAdult * counts.adult + priceChild * counts.child;
          }

          function getPassengerCounts() {
               var rows = qsa(".passenger-row", passengerList);
               var adult = 0;
               var child = 0;
               rows.forEach(function (row) {
                    var nameInput = qs("input[type='text']", row);
                    var nameValue = nameInput ? nameInput.value.trim() : "";
                    if (!nameValue) return;
                    var select = qs("select", row);
                    var type = select ? select.value : "ADULT";
                    if (type === "CHILD") child += 1;
                    else adult += 1;
               });
               return { adult: adult, child: child };
          }

          function collectPassengers() {
               var rows = qsa(".passenger-row", passengerList);
               var items = [];
               rows.forEach(function (row) {
                    var nameInput = qs("input[type='text']", row);
                    var dobInput = qs("input[type='date']", row);
                    var select = qs("select", row);
                    var fullName = nameInput ? nameInput.value.trim() : "";
                    if (!fullName) return;
                    items.push({
                         fullName: fullName,
                         dob: dobInput && dobInput.value ? dobInput.value : null,
                         type: select ? select.value : "ADULT"
                    });
               });
               return items;
          }

          function showToast(msg, type) {
               if (window.AdminUI && typeof window.AdminUI.toast === "function") {
                    window.AdminUI.toast(msg, type || "info");
               }
          }

          function updateSelectedDate(dateStr, departure) {
               state.selectedDate = dateStr;
               state.selectedDeparture = departure;
               if (hiddenDate) hiddenDate.value = dateStr || "";
               if (hiddenDeparture) hiddenDeparture.value = departure ? departure.id : "";
               if (selectedDatePreview) selectedDatePreview.value = dateStr || "";
               btnContinue.disabled = !state.selectedTour || !state.selectedDate;
               setSummary();
               renderCalendar();
          }

          function renderCalendar() {
               if (!calendarGrid || !state.months.length) return;
               var active = state.months[state.monthIndex];
               if (!active) return;
               var key = active.year + "-" + active.month;
               var departures = state.departuresCache[key] || [];
               var cells = buildMonthGrid(active.year, active.month, departures, state.selectedDate);

               var header =
                    '<div class="calendar-week">T2</div>' +
                    '<div class="calendar-week">T3</div>' +
                    '<div class="calendar-week">T4</div>' +
                    '<div class="calendar-week">T5</div>' +
                    '<div class="calendar-week">T6</div>' +
                    '<div class="calendar-week">T7</div>' +
                    '<div class="calendar-week">CN</div>';

               var body = cells
                    .map(function (c) {
                         var dep = c.departure;
                         var available = dep && dep.available != null ? dep.available : null;
                         var enabled = dep && available > 0 && dep.status === "OPEN" && !dep.completed;
                         var price = dep ? fmtMoney(dep.priceAdult) : "";
                         var seatLabel = dep ? "Còn: " + (available != null ? available : "--") : "";
                         var cls = "calendar-cell" +
                              (c.inMonth ? "" : " out") +
                              (enabled ? " available" : "") +
                              (c.selected ? " selected" : "");

                         return (
                              '<button class="' + cls + '" type="button" data-date="' + c.dateStr + '" ' +
                              (enabled ? "" : "disabled") + '>' +
                              '<span class="cal-day">' + c.day + "</span>" +
                              '<span class="cal-price">' + price + "</span>" +
                              '<span class="cal-seat">' + seatLabel + "</span>" +
                              "</button>"
                         );
                    })
                    .join("");

               calendarGrid.innerHTML = header + body;
               qsa(".calendar-cell.available", calendarGrid).forEach(function (btn) {
                    btn.addEventListener("click", function () {
                         var dateStr = btn.getAttribute("data-date");
                         var dayItems = (state.departuresCache[key] || []).filter(function (d) {
                              return d.startDate === dateStr;
                         });
                         var dep = dayItems.length
                              ? dayItems.slice().sort(function (a, b) { return (b.available || 0) - (a.available || 0); })[0]
                              : null;
                         updateSelectedDate(dateStr, dep);
                    });
               });
          }

          function renderMonthNav() {
               if (!calendarNav) return;
               calendarNav.innerHTML = "";
               state.months.forEach(function (m, idx) {
                    var btn = document.createElement("button");
                    btn.type = "button";
                    btn.className = "cal-nav-btn" + (idx === state.monthIndex ? " active" : "");
                    btn.textContent = monthLabel(m.year, m.month);
                    btn.addEventListener("click", function () {
                         state.monthIndex = idx;
                         renderMonthNav();
                         renderCalendar();
                    });
                    calendarNav.appendChild(btn);
               });
          }

          function loadDepartures(tourId) {
               state.departuresCache = {};
               state.monthIndex = 0;
               var now = new Date();
               state.months = [];
               for (var i = 0; i < 6; i++) {
                    var date = new Date(now.getFullYear(), now.getMonth() + i, 1);
                    state.months.push({ year: date.getFullYear(), month: date.getMonth() + 1 });
               }

               var requests = state.months.map(function (m) {
                    var key = m.year + "-" + m.month;
                    return fetch("/api/staff/departures?tourId=" + tourId + "&year=" + m.year + "&month=" + m.month)
                         .then(function (res) {
                              if (!res.ok) throw new Error("Không tải được lịch khởi hành");
                              return res.json();
                         })
                         .then(function (data) { state.departuresCache[key] = data || []; })
                         .catch(function () { state.departuresCache[key] = []; });
               });

               Promise.all(requests).then(function () {
                    renderMonthNav();
                    renderCalendar();
               });
          }

          function applyTourInfo(tour) {
               if (!tour) {
                    tourInfo.style.display = "none";
                    calendarSection.style.display = "none";
                    btnContinue.disabled = true;
                    return;
               }
               tourInfo.style.display = "block";
               calendarSection.style.display = "block";

               var thumb = tour.thumbnailUrl || (tour.images && tour.images[0] && tour.images[0].url);
               tourThumb.src = thumb || "/images/logo.png";
               tourTitle.textContent = tour.title || "Tour";
               tourCode.textContent = "Mã tour: " + (tour.code || "--");
               tourLine.textContent = tour.tourLineName || "--";
               tourTransport.textContent = tour.transportModeName || "--";
               var duration = tour.durationDays != null && tour.durationNights != null
                    ? tour.durationDays + "N" + tour.durationNights + "Đ"
                    : "--";
               tourDuration.textContent = duration;
               tourStart.textContent = tour.startLocationName || "--";
               tourBasePrice.textContent = fmtMoney(tour.basePrice);

               setSummary();
          }

          function handleTourSelect() {
               var id = tourSelect.value;
               if (!id) {
                    state.selectedTour = null;
                    state.selectedTourDetail = null;
                    state.selectedDate = null;
                    state.selectedDeparture = null;
                    if (hiddenSlug) hiddenSlug.value = "";
                    if (hiddenDate) hiddenDate.value = "";
                    if (hiddenDeparture) hiddenDeparture.value = "";
                    if (selectedDatePreview) selectedDatePreview.value = "";
                    applyTourInfo(null);
                    setSummary();
                    return;
               }

               state.selectedTour = state.tours.find(function (t) { return String(t.id) === String(id); }) || null;
               applyTourInfo(state.selectedTour);

               fetch("/api/staff/tours/" + id)
                    .then(function (res) {
                         if (!res.ok) throw new Error("Không tải được tour");
                         return res.json();
                    })
                    .then(function (detail) {
                         state.selectedTourDetail = detail;
                         if (hiddenSlug) hiddenSlug.value = detail.slug || "";
                    })
                    .catch(function () { state.selectedTourDetail = null; });

               state.selectedDate = null;
               state.selectedDeparture = null;
               if (hiddenDate) hiddenDate.value = "";
               if (hiddenDeparture) hiddenDeparture.value = "";
               if (selectedDatePreview) selectedDatePreview.value = "";
               btnContinue.disabled = true;
               loadDepartures(id);
               setSummary();
          }

          function loadTours() {
               fetch("/api/staff/tours?active=true")
                    .then(function (res) {
                         if (!res.ok) throw new Error("Không tải được danh sách tour");
                         return res.json();
                    })
                    .then(function (data) {
                         state.tours = data || [];
                         state.tours.forEach(function (t) {
                              var opt = document.createElement("option");
                              opt.value = t.id;
                              opt.textContent = (t.code ? t.code + " - " : "") + t.title;
                              tourSelect.appendChild(opt);
                         });
                    })
                    .catch(function () {
                         if (window.AdminUI && typeof window.AdminUI.toast === "function") {
                              window.AdminUI.toast("Không tải được danh sách tour", "error");
                         }
                    });
          }

          btnContinue.addEventListener("click", function () {
               if (btnContinue.disabled) return;
               step2.style.display = "block";
               step2.scrollIntoView({ behavior: "smooth", block: "start" });
               ensurePassengers();
          });

          btnAddPassenger.addEventListener("click", function () {
               renderPassengerRow();
          });

          if (passengerList) {
               passengerList.addEventListener("change", function () {
                    setSummary();
               });
               passengerList.addEventListener("input", function () {
                    setSummary();
               });
          }

          if (bookingForm) {
               bookingForm.addEventListener("submit", function (e) {
                    e.preventDefault();

                    var slug = hiddenSlug ? hiddenSlug.value.trim() : "";
                    var date = hiddenDate ? hiddenDate.value.trim() : "";
                    if (!slug || !date) {
                         showToast("Vui lòng chọn tour và ngày khởi hành", "error");
                         return;
                    }

                    var contactName = qs("[name='contactName']", bookingForm);
                    var contactEmail = qs("[name='contactEmail']", bookingForm);
                    var contactPhone = qs("[name='contactPhone']", bookingForm);
                    var note = qs("[name='note']", bookingForm);

                    var nameVal = contactName ? contactName.value.trim() : "";
                    var emailVal = contactEmail ? contactEmail.value.trim() : "";
                    var phoneVal = contactPhone ? contactPhone.value.trim() : "";
                    var noteVal = note ? note.value.trim() : "";

                    if (!nameVal || !emailVal || !phoneVal) {
                         showToast("Vui lòng nhập đầy đủ thông tin liên hệ", "error");
                         return;
                    }

                    var passengers = collectPassengers();
                    if (passengers.length === 0) {
                         showToast("Vui lòng nhập ít nhất 1 hành khách", "error");
                         return;
                    }

                    var counts = getPassengerCounts();
                    var payload = {
                         slug: slug,
                         date: date,
                         contactName: nameVal,
                         contactEmail: emailVal,
                         contactPhone: phoneVal,
                         note: noteVal,
                         totalAdult: counts.adult,
                         totalChild: counts.child,
                         passengers: passengers
                    };

                    fetch("/api/staff/bookings", {
                         method: "POST",
                         headers: { "Content-Type": "application/json" },
                         body: JSON.stringify(payload)
                    })
                         .then(function (res) {
                              if (!res.ok) return res.text().then(function (t) {
                                   throw new Error(t || "Không thể tạo booking");
                              });
                              return res.json();
                         })
                         .then(function () {
                              showToast("Đã tạo booking", "success");
                              setTimeout(function () {
                                   window.location.href = "/staff/bookings/process?toast=create-success";
                              }, 400);
                         })
                         .catch(function (err) {
                              showToast(err.message || "Không thể tạo booking", "error");
                         });
               });
          }

          tourSelect.addEventListener("change", handleTourSelect);
          loadTours();
     }

     document.addEventListener("DOMContentLoaded", init);
})();
