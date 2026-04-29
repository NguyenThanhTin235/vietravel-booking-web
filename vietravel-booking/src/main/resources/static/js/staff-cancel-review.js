(function () {
     "use strict";

     function qs(sel, root) {
          return (root || document).querySelector(sel);
     }

     function qsa(sel, root) {
          return Array.prototype.slice.call((root || document).querySelectorAll(sel));
     }

     function initReviewToggle() {
          var forms = qsa("[data-review-form]");
          if (!forms.length) return;

          function hideAll() {
               forms.forEach(function (el) {
                    el.classList.remove("is-open");
               });
          }

          qsa("[data-review-toggle]").forEach(function (btn) {
               btn.addEventListener("click", function () {
                    var target = btn.getAttribute("data-review-toggle");
                    var form = qs("[data-review-form='" + target + "']");
                    if (!form) return;
                    var willOpen = !form.classList.contains("is-open");
                    hideAll();
                    if (willOpen) {
                         form.classList.add("is-open");
                         form.scrollIntoView({ behavior: "smooth", block: "start" });
                    }
               });
          });
     }

     document.addEventListener("DOMContentLoaded", function () {
          initReviewToggle();
     });
})();
