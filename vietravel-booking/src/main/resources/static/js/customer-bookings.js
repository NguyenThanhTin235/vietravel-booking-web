(function () {
     "use strict";

     function qs(sel, root) {
          return (root || document).querySelector(sel);
     }

     function qsa(sel, root) {
          return Array.prototype.slice.call((root || document).querySelectorAll(sel));
     }

     function initCancelRequestModal() {
          var modal = qs("#cancelRequestModal");
          if (!modal) return;

          var closeBtns = qsa("[data-cancel-request-close]", modal);
          var reasonSelect = qs("#cancelRequestSelect", modal);
          var otherWrap = qs("#cancelRequestOtherWrap", modal);
          var otherInput = qs("#cancelRequestOther", modal);
          var errorText = qs("#cancelRequestError", modal);
          var submitBtn = qs("#cancelRequestSubmit", modal);

          var activeForm = null;

          function openModal(form) {
               activeForm = form;
               if (errorText) errorText.textContent = "";
               if (reasonSelect) reasonSelect.value = "";
               if (otherInput) otherInput.value = "";
               if (otherWrap) otherWrap.style.display = "none";
               modal.classList.add("is-open");
          }

          function closeModal() {
               modal.classList.remove("is-open");
               activeForm = null;
          }

          qsa(".js-cancel-request-btn").forEach(function (btn) {
               btn.addEventListener("click", function () {
                    var form = btn.closest("form");
                    if (!form) return;
                    openModal(form);
               });
          });

          closeBtns.forEach(function (btn) {
               btn.addEventListener("click", closeModal);
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
          initCancelRequestModal();
     });
})();
