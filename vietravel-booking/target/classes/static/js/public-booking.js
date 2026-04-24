document.addEventListener("DOMContentLoaded", () => {
     const bookingRoot = document.getElementById("bookingRoot");
     const adultAmountEl = document.getElementById("adultAmount");
     const childAmountEl = document.getElementById("childAmount");
     const discountAmountEl = document.getElementById("discountAmount");
     const subtotalAmountEl = document.getElementById("subtotalAmount");
     const totalAmountEl = document.getElementById("totalAmount");
     const couponInput = document.querySelector(".coupon-input input");
     const payBtn = document.getElementById("payBtn");

     const parsePrice = (v) => {
          if (v == null || v === "") return 0;
          const n = Number(v);
          return Number.isNaN(n) ? 0 : n;
     };

     const fmt = (v) => new Intl.NumberFormat("vi-VN").format(v) + " đ";
     const fmtLine = (price, qty) => `${fmt(price)} x ${qty}`;
     const adultList = document.querySelector("[data-pax-list='adult']");
     const childList = document.querySelector("[data-pax-list='child']");
     const adultTpl = document.getElementById("adultPassengerTpl");
     const childTpl = document.getElementById("childPassengerTpl");

     const renderList = (listEl, tpl, count) => {
          if (!listEl || !tpl) return;
          listEl.innerHTML = "";
          for (let i = 0; i < count; i += 1) {
               const node = tpl.content.cloneNode(true);
               listEl.appendChild(node);
          }
     };

     const updatePassengers = () => {
          const adultVal = Number(document.querySelector("[data-pax='adult'] [data-value]")?.textContent || 0);
          const childVal = Number(document.querySelector("[data-pax='child'] [data-value]")?.textContent || 0);
          renderList(adultList, adultTpl, adultVal);
          renderList(childList, childTpl, childVal);
          updatePricing(adultVal, childVal);
     };

     const updatePricing = (adultVal, childVal) => {
          const adultPrice = parsePrice(bookingRoot?.getAttribute("data-adult-price"));
          const childPrice = parsePrice(bookingRoot?.getAttribute("data-child-price"));
          const adultTotal = adultVal * adultPrice;
          const childTotal = childVal * childPrice;

          if (adultAmountEl) adultAmountEl.textContent = fmtLine(adultPrice, adultVal);
          if (childAmountEl) childAmountEl.textContent = fmtLine(childPrice, childVal);

          if (discountAmountEl) discountAmountEl.textContent = "0 đ";
          if (couponInput) couponInput.value = "";

          const subtotal = adultTotal + childTotal;
          if (subtotalAmountEl) subtotalAmountEl.textContent = fmt(subtotal);
          if (totalAmountEl) totalAmountEl.textContent = fmt(subtotal);
     };

     const collectPassengers = () => {
          const passengers = [];
          document.querySelectorAll(".passenger-item").forEach(item => {
               const type = item.getAttribute("data-passenger-type");
               const name = item.querySelector("[data-field='name']")?.value?.trim();
               const dob = item.querySelector("[data-field='dob']")?.value || null;
               if (!type || !name) return;
               passengers.push({ type, fullName: name, dob });
          });
          return passengers;
     };

     const submitBooking = async () => {
          if (!bookingRoot) return;
          const slug = bookingRoot.getAttribute("data-slug") || "";
          const date = bookingRoot.getAttribute("data-date") || "";
          const contactName = document.getElementById("contactName")?.value?.trim() || "";
          const contactPhone = document.getElementById("contactPhone")?.value?.trim() || "";
          const contactEmail = document.getElementById("contactEmail")?.value?.trim() || "";
          const note = document.getElementById("contactNote")?.value?.trim() || "";
          const totalAdult = Number(document.querySelector("[data-pax='adult'] [data-value]")?.textContent || 0);
          const totalChild = Number(document.querySelector("[data-pax='child'] [data-value]")?.textContent || 0);

          if (!slug || !date) {
               alert("Vui lòng chọn ngày khởi hành trước khi thanh toán.");
               return;
          }
          if (!contactName || !contactPhone || !contactEmail) {
               alert("Vui lòng nhập đầy đủ thông tin liên lạc.");
               return;
          }

          const payload = {
               slug,
               date,
               contactName,
               contactPhone,
               contactEmail,
               note,
               totalAdult,
               totalChild,
               passengers: collectPassengers()
          };

          try {
               if (payBtn) payBtn.disabled = true;
               const res = await fetch("/api/customer/bookings", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
               });

               const data = await res.json().catch(() => ({}));
               if (!res.ok) {
                    throw new Error(data.message || "Không thể tạo booking");
               }
               alert(`Đặt tour thành công. Mã booking: ${data.bookingCode || ""}`);
          } catch (e) {
               alert(e.message || "Không thể tạo booking");
          } finally {
               if (payBtn) payBtn.disabled = false;
          }
     };

     document.querySelectorAll(".pax-control").forEach(control => {
          const valueEl = control.querySelector("[data-value]");
          const getValue = () => Number(valueEl?.textContent || 0);
          const setValue = (v) => {
               if (!valueEl) return;
               valueEl.textContent = String(Math.max(0, v));
               updatePassengers();
          };

          control.querySelectorAll(".pax-btn").forEach(btn => {
               btn.addEventListener("click", () => {
                    const action = btn.getAttribute("data-action");
                    const current = getValue();
                    if (action === "minus") {
                         setValue(current - 1);
                    } else {
                         setValue(current + 1);
                    }
               });
          });
     });

     updatePassengers();

     if (payBtn) {
          payBtn.addEventListener("click", submitBooking);
     }
});
