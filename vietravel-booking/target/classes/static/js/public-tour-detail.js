document.addEventListener("DOMContentLoaded", () => {
     const authEl = document.getElementById("authState");
     const isAuthed = authEl && authEl.getAttribute("data-auth") === "true";
     const loginModal = document.getElementById("loginModal");
     const openLoginModal = () => {
          if (!loginModal) return;
          loginModal.classList.add("is-open");
          loginModal.setAttribute("aria-hidden", "false");
     };
     const closeLoginModal = () => {
          if (!loginModal) return;
          loginModal.classList.remove("is-open");
          loginModal.setAttribute("aria-hidden", "true");
     };

     document.querySelectorAll("[data-close-modal]").forEach(btn => {
          btn.addEventListener("click", closeLoginModal);
     });

     document.addEventListener("keydown", (e) => {
          if (e.key === "Escape") closeLoginModal();
     });

     let selectedDate = "";

     document.querySelectorAll(".js-book-now").forEach(btn => {
          btn.addEventListener("click", (e) => {
               if (!isAuthed) {
                    e.preventDefault();
                    openLoginModal();
                    return;
               }

               const bookUrl = btn.getAttribute("data-book-url") || "";
               if (!selectedDate) {
                    const calendarSection = document.querySelector(".tour-calendar");
                    if (calendarSection) calendarSection.scrollIntoView({ behavior: "smooth", block: "start" });
                    return;
               }

               if (bookUrl) {
                    const params = new URLSearchParams();
                    params.set("date", selectedDate);
                    if (selectedAdultPrice) params.set("adultPrice", selectedAdultPrice);
                    if (selectedChildPrice) params.set("childPrice", selectedChildPrice);
                    window.location.href = `${bookUrl}?${params.toString()}`;
               }
          });
     });

     const mainImg = document.getElementById("tourMainImage");
     document.querySelectorAll(".tour-detail-thumb").forEach(btn => {
          btn.addEventListener("click", () => {
               const src = btn.getAttribute("data-src");
               if (mainImg && src) mainImg.src = src;
          });
     });

     const bookNowBtn = document.getElementById("bookNowBtn");
     const calendarInfo = document.getElementById("calendarInfo");
     const calendarInfoDate = document.getElementById("calendarInfoDate");
     const calendarInfoStart = document.getElementById("calendarInfoStart");
     const calendarInfoEnd = document.getElementById("calendarInfoEnd");
     const calendarInfoDuration = document.getElementById("calendarInfoDuration");
     const calendarInfoPriceAdult = document.getElementById("calendarInfoPriceAdult");
     const calendarInfoPriceChild = document.getElementById("calendarInfoPriceChild");

     const monthItems = Array.from(document.querySelectorAll(".calendar-month-item"));
     const panels = Array.from(document.querySelectorAll(".calendar-panel"));

     const activateMonth = (index) => {
          if (index < 0 || index >= panels.length) return;
          monthItems.forEach((item, i) => item.classList.toggle("is-active", i === index));
          panels.forEach((panel, i) => panel.classList.toggle("is-active", i === index));
     };

     monthItems.forEach((item, index) => {
          item.addEventListener("click", () => activateMonth(index));
     });

     document.querySelectorAll(".calendar-nav.prev").forEach(btn => {
          btn.addEventListener("click", () => {
               const activeIndex = panels.findIndex(p => p.classList.contains("is-active"));
               activateMonth(activeIndex - 1);
          });
     });

     document.querySelectorAll(".calendar-nav.next").forEach(btn => {
          btn.addEventListener("click", () => {
               const activeIndex = panels.findIndex(p => p.classList.contains("is-active"));
               activateMonth(activeIndex + 1);
          });
     });

     let selectedAdultPrice = "";
     let selectedChildPrice = "";

     document.querySelectorAll(".calendar-day.is-available .calendar-day-btn").forEach(btn => {
          btn.addEventListener("click", () => {
               document.querySelectorAll(".calendar-day.is-available").forEach(d => d.classList.remove("is-active"));
               const day = btn.closest(".calendar-day");
               if (day) day.classList.add("is-active");
               const date = btn.getAttribute("data-date") || "";
               const priceAdultLabel = btn.getAttribute("data-price-adult-label");
               const priceChildLabel = btn.getAttribute("data-price-child-label");
               selectedAdultPrice = btn.getAttribute("data-price-adult") || "";
               selectedChildPrice = btn.getAttribute("data-price-child") || "";
               selectedDate = date;
               if (calendarInfo && date) {
                    const parts = date.split("-");
                    calendarInfoDate.textContent = `${parts[2]}/${parts[1]}/${parts[0]}`;
                    calendarInfoStart.textContent = calendarInfo.getAttribute("data-start") || "--";
                    calendarInfoEnd.textContent = calendarInfo.getAttribute("data-end") || "--";
                    calendarInfoDuration.textContent = calendarInfo.getAttribute("data-duration") || "--";
                    calendarInfoPriceAdult.textContent = priceAdultLabel || "--";
                    calendarInfoPriceChild.textContent = priceChildLabel || "--";
                    calendarInfo.classList.add("is-open");
                    calendarInfo.setAttribute("aria-hidden", "false");
               }
               if (bookNowBtn) {
                    bookNowBtn.textContent = "Đặt ngay";
               }
          });
     });

});
