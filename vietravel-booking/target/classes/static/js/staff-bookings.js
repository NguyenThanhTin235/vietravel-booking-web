(function () {
     "use strict";

     function qs(sel, root) {
          return (root || document).querySelector(sel);
     }

     function qsa(sel, root) {
          return Array.prototype.slice.call((root || document).querySelectorAll(sel));
     }

     function showToastFromQuery() {
          var params = new URLSearchParams(window.location.search);
          var toastKey = params.get("toast");
          if (!toastKey || !window.AdminUI || typeof window.AdminUI.toast !== "function") return;

          var map = {
               "cancel-success": { msg: "Đã hủy tour", type: "success" },
               "cancel-failed": { msg: "Không thể hủy tour", type: "error" },
               "confirm-success": { msg: "Đã xác nhận booking", type: "success" },
               "confirm-failed": { msg: "Không thể xác nhận booking", type: "error" },
               "create-success": { msg: "Đã tạo booking", type: "success" }
          };

          var entry = map[toastKey];
          if (!entry) return;
          window.AdminUI.toast(entry.msg, entry.type);

          params.delete("toast");
          var newQuery = params.toString();
          var newUrl = window.location.pathname + (newQuery ? "?" + newQuery : "") + window.location.hash;
          if (window.history && typeof window.history.replaceState === "function") {
               window.history.replaceState({}, document.title, newUrl);
          }
     }

     function initConfirmForms() {
          qsa("form.js-confirm-form").forEach(function (form) {
               form.addEventListener("submit", function (e) {
                    var msg = form.getAttribute("data-confirm-message") || "Bạn chắc chắn muốn xác nhận booking này?";
                    if (!window.confirm(msg)) {
                         e.preventDefault();
                    }
               });
          });
     }

     function initCancelModal() {
          var modal = qs("#cancelModal");
          if (!modal) return;

          var closeBtns = qsa("[data-cancel-modal-close]", modal);
          var reasonSelect = qs("#cancelReasonSelect", modal);
          var otherWrap = qs("#cancelReasonOtherWrap", modal);
          var otherInput = qs("#cancelReasonOther", modal);
          var errorText = qs("#cancelReasonError", modal);
          var submitBtn = qs("#cancelReasonSubmit", modal);

          var activeForm = null;

          function openModal(form, bookingCode) {
               activeForm = form;
               if (errorText) errorText.textContent = "";
               if (reasonSelect) reasonSelect.value = "";
               if (otherInput) otherInput.value = "";
               if (otherWrap) otherWrap.style.display = "none";
               if (submitBtn) submitBtn.disabled = false;

               var title = qs("#cancelModalTitle", modal);
               if (title) {
                    title.textContent = bookingCode ? "Hủy tour - " + bookingCode : "Hủy tour";
               }

               modal.classList.add("show");
          }

          function closeModal() {
               modal.classList.remove("show");
               activeForm = null;
          }

          qsa(".js-cancel-btn").forEach(function (btn) {
               btn.addEventListener("click", function () {
                    var form = btn.closest("form");
                    if (!form) return;
                    var bookingCode = btn.getAttribute("data-booking-code") || "";
                    openModal(form, bookingCode);
               });
          });

          closeBtns.forEach(function (btn) {
               btn.addEventListener("click", closeModal);
          });

          modal.addEventListener("click", function (e) {
               if (e.target === modal) {
                    closeModal();
               }
          });

          if (reasonSelect) {
               reasonSelect.addEventListener("change", function () {
                    var isOther = reasonSelect.value === "OTHER";
                    if (otherWrap) otherWrap.style.display = isOther ? "block" : "none";
                    if (!isOther && errorText) errorText.textContent = "";
               });
          }

          if (submitBtn) {
               submitBtn.addEventListener("click", function () {
                    if (!activeForm) return;
                    var reasonValue = reasonSelect ? reasonSelect.value : "";
                    var otherValue = otherInput ? otherInput.value.trim() : "";

                    if (!reasonValue) {
                         if (errorText) errorText.textContent = "Vui lòng chọn lý do hủy.";
                         return;
                    }
                    if (reasonValue === "OTHER" && !otherValue) {
                         if (errorText) errorText.textContent = "Vui lòng nhập lý do khác.";
                         return;
                    }

                    var reasonInput = qs("input[name='cancelReason']", activeForm);
                    var otherInputHidden = qs("input[name='cancelReasonOther']", activeForm);
                    if (reasonInput) reasonInput.value = reasonValue;
                    if (otherInputHidden) otherInputHidden.value = otherValue;

                    activeForm.submit();
               });
          }
     }

     document.addEventListener("DOMContentLoaded", function () {
          showToastFromQuery();
          initConfirmForms();
          initCancelModal();
     });
})();
