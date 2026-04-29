(function () {
     "use strict";

     function qs(sel, root) {
          return (root || document).querySelector(sel);
     }

     function qsa(sel, root) {
          return Array.prototype.slice.call((root || document).querySelectorAll(sel));
     }

     function initConfirmForms() {
          qsa("form.js-confirm-form").forEach(function (form) {
               form.addEventListener("submit", function (e) {
                    var msg = form.getAttribute("data-confirm-message") || "Xác nhận thao tác?";
                    if (!window.confirm(msg)) {
                         e.preventDefault();
                    }
               });
          });
     }

     function initRejectModal() {
          var modal = qs("#rejectModal");
          if (!modal) return;

          var closeBtns = qsa("[data-reject-modal-close]", modal);
          var reasonInput = qs("#rejectReason", modal);
          var errorText = qs("#rejectReasonError", modal);
          var submitBtn = qs("#rejectReasonSubmit", modal);

          var activeForm = null;

          function openModal(form, bookingCode) {
               activeForm = form;
               if (errorText) errorText.textContent = "";
               if (reasonInput) reasonInput.value = "";

               var title = qs("#rejectModalTitle", modal);
               if (title) {
                    title.textContent = bookingCode ? "Từ chối - " + bookingCode : "Từ chối yêu cầu hủy";
               }

               modal.classList.add("show");
          }

          function closeModal() {
               modal.classList.remove("show");
               activeForm = null;
          }

          qsa(".js-reject-btn").forEach(function (btn) {
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

          if (submitBtn) {
               submitBtn.addEventListener("click", function () {
                    if (!activeForm) return;
                    var reasonValue = reasonInput ? reasonInput.value.trim() : "";
                    if (!reasonValue) {
                         if (errorText) errorText.textContent = "Vui lòng nhập lý do từ chối.";
                         return;
                    }
                    var reasonInputHidden = qs("input[name='reason']", activeForm);
                    if (reasonInputHidden) reasonInputHidden.value = reasonValue;
                    activeForm.submit();
               });
          }
     }

     document.addEventListener("DOMContentLoaded", function () {
          initConfirmForms();
          initRejectModal();
     });
})();
