document.addEventListener("DOMContentLoaded", () => {
     const bookingRoot = document.getElementById("bookingRoot");
     const adultAmountEl = document.getElementById("adultAmount");
     const childAmountEl = document.getElementById("childAmount");
     const discountAmountEl = document.getElementById("discountAmount");
     const subtotalAmountEl = document.getElementById("subtotalAmount");
     const totalAmountEl = document.getElementById("totalAmount");
     const couponInput = document.querySelector(".coupon-input input");
     const couponApplyBtn = document.querySelector(".coupon-input .btn-apply");
     const payBtn = document.getElementById("payBtn");
     const stepInfo = document.querySelector(".booking-step[data-step='info']");
     const stepPay = document.querySelector(".booking-step[data-step='payment']");
     const stepDone = document.querySelector(".booking-step[data-step='done']");
     const resultSuccess = document.getElementById("bookingResultSuccess");
     const resultFailed = document.getElementById("bookingResultFailed");
     const resultSuccessDesc = document.getElementById("bookingResultSuccessDesc");
     const retryPayBtn = document.getElementById("retryPayBtn");
     const bookingContentLeft = document.getElementById("bookingContentLeft");
     const bookingContentRight = document.getElementById("bookingContentRight");
     const bookingGrid = document.getElementById("bookingGrid");

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
     let appliedCoupon = null;
     let appliedDiscount = 0;

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
          appliedCoupon = null;
          appliedDiscount = 0;

          const subtotal = adultTotal + childTotal;
          if (subtotalAmountEl) subtotalAmountEl.textContent = fmt(subtotal);
          if (totalAmountEl) totalAmountEl.textContent = fmt(subtotal);
     };

     const getSubtotal = () => {
          const adultVal = Number(document.querySelector("[data-pax='adult'] [data-value]")?.textContent || 0);
          const childVal = Number(document.querySelector("[data-pax='child'] [data-value]")?.textContent || 0);
          const adultPrice = parsePrice(bookingRoot?.getAttribute("data-adult-price"));
          const childPrice = parsePrice(bookingRoot?.getAttribute("data-child-price"));
          return adultVal * adultPrice + childVal * childPrice;
     };

     const applyCoupon = async () => {
          if (!couponInput) return;
          const code = couponInput.value.trim();
          if (!code) {
               alert("Vui lòng nhập mã giảm giá.");
               return;
          }
          const slug = bookingRoot?.getAttribute("data-slug") || "";
          const subtotal = getSubtotal();
          try {
               const res = await fetch("/api/public/campaigns/apply", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ code, tourSlug: slug, totalAmount: subtotal })
               });
               const data = await res.json().catch(() => ({}));
               if (!data || data.valid !== true) {
                    throw new Error(data.message || "Không thể áp dụng mã giảm giá");
               }
               appliedCoupon = code;
               appliedDiscount = Number(data.discountAmount || 0);
               if (discountAmountEl) discountAmountEl.textContent = fmt(appliedDiscount);
               if (totalAmountEl) totalAmountEl.textContent = fmt(subtotal - appliedDiscount);
               alert(data.message || "Áp dụng thành công");
          } catch (e) {
               appliedCoupon = null;
               appliedDiscount = 0;
               if (discountAmountEl) discountAmountEl.textContent = "0 đ";
               if (totalAmountEl) totalAmountEl.textContent = fmt(subtotal);
               alert(e.message || "Không thể áp dụng mã giảm giá");
          }
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
               passengers: collectPassengers(),
               couponCode: appliedCoupon
          };

          try {
               if (payBtn) payBtn.disabled = true;
               const res = await fetch("/api/customer/bookings/pay", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
               });

               const data = await res.json().catch(() => ({}));
               if (!res.ok) {
                    throw new Error(data.message || "Không thể tạo booking");
               }
               if (data.paymentUrl) {
                    window.location.href = data.paymentUrl;
                    return;
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

     const setStepState = (active, doneList = []) => {
          [stepInfo, stepPay, stepDone].forEach(step => {
               step?.classList.remove("is-active");
               step?.classList.remove("is-done");
          });
          doneList.forEach(step => step?.classList.add("is-done"));
          active?.classList.add("is-active");
     };

     const params = new URLSearchParams(window.location.search);
     const paymentStatus = params.get("payment");
     const bookingCode = params.get("code") || "";
     if (paymentStatus === "success") {
          setStepState(stepDone, [stepInfo, stepPay]);
          if (resultSuccess) resultSuccess.hidden = false;
          if (resultFailed) resultFailed.hidden = true;
          if (bookingContentLeft) bookingContentLeft.hidden = true;
          if (bookingContentRight) bookingContentRight.hidden = true;
          bookingGrid?.classList.add("is-result");
          if (resultSuccessDesc) {
               resultSuccessDesc.textContent = bookingCode
                    ? `Mã booking của bạn: ${bookingCode}. Chúng tôi sẽ liên hệ sớm.`
                    : "Giao dịch đã hoàn tất. Chúng tôi sẽ liên hệ sớm.";
          }
     } else if (paymentStatus === "failed") {
          setStepState(stepPay, [stepInfo]);
          if (resultFailed) resultFailed.hidden = false;
          if (resultSuccess) resultSuccess.hidden = true;
          if (bookingContentLeft) bookingContentLeft.hidden = true;
          if (bookingContentRight) bookingContentRight.hidden = true;
          bookingGrid?.classList.add("is-result");
     } else {
          setStepState(stepInfo);
          if (resultSuccess) resultSuccess.hidden = true;
          if (resultFailed) resultFailed.hidden = true;
          if (bookingContentLeft) bookingContentLeft.hidden = false;
          if (bookingContentRight) bookingContentRight.hidden = false;
          bookingGrid?.classList.remove("is-result");
     }

     if (payBtn) {
          payBtn.addEventListener("click", submitBooking);
     }

     if (couponApplyBtn) {
          couponApplyBtn.addEventListener("click", applyCoupon);
     }

     if (retryPayBtn) {
          retryPayBtn.addEventListener("click", submitBooking);
     }
});
