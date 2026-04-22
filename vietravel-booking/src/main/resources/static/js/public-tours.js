document.addEventListener("DOMContentLoaded", () => {
     const form = document.getElementById("tourFilterForm");
     if (!form) return;

     const minInput = document.getElementById("minPrice");
     const maxInput = document.getElementById("maxPrice");
     const sortSelect = document.getElementById("sortSelect");
     const sortInput = document.getElementById("sortInput");

     const budgetButtons = Array.from(document.querySelectorAll("[data-budget]"));
     budgetButtons.forEach(btn => {
          btn.addEventListener("click", () => {
               const min = btn.getAttribute("data-min") || "";
               const max = btn.getAttribute("data-max") || "";
               if (minInput) minInput.value = min;
               if (maxInput) maxInput.value = max;
               budgetButtons.forEach(b => b.classList.remove("is-active"));
               btn.classList.add("is-active");
          });
     });

     const tourLineInput = document.getElementById("tourLineId");
     const tourLineButtons = Array.from(document.querySelectorAll("[data-tour-line]"));
     tourLineButtons.forEach(btn => {
          btn.addEventListener("click", () => {
               const id = btn.getAttribute("data-tour-line") || "";
               if (tourLineInput) tourLineInput.value = id;
               tourLineButtons.forEach(b => b.classList.remove("is-active"));
               btn.classList.add("is-active");
          });
     });

     const transportInput = document.getElementById("transportModeId");
     const transportButtons = Array.from(document.querySelectorAll("[data-transport]"));
     transportButtons.forEach(btn => {
          btn.addEventListener("click", () => {
               const id = btn.getAttribute("data-transport") || "";
               if (transportInput) transportInput.value = id;
               transportButtons.forEach(b => b.classList.remove("is-active"));
               btn.classList.add("is-active");
          });
     });

     if (sortSelect) {
          sortSelect.addEventListener("change", () => {
               if (sortInput) sortInput.value = sortSelect.value;
               form.submit();
          });
     }

     const dateLists = Array.from(document.querySelectorAll(".tour-date-list"));
     dateLists.forEach(list => {
          const dates = Array.from(list.querySelectorAll(".tour-date"));
          let start = 0;
          const prevBtn = list.querySelector(".tour-date-prev");
          const nextBtn = list.querySelector(".tour-date-next");

          const render = () => {
               dates.forEach((el, idx) => {
                    el.classList.toggle("is-hidden", idx < start || idx >= start + 5);
               });
               if (prevBtn) prevBtn.classList.toggle("is-disabled", start <= 0);
               if (nextBtn) nextBtn.classList.toggle("is-disabled", start + 5 >= dates.length);
          };

          const flash = (dir) => {
               list.classList.remove("is-next", "is-prev");
               list.classList.add(dir === "next" ? "is-next" : "is-prev");
               setTimeout(() => list.classList.remove("is-next", "is-prev"), 180);
          };

          if (prevBtn) {
               prevBtn.addEventListener("click", () => {
                    if (start <= 0) return;
                    start = Math.max(0, start - 5);
                    flash("prev");
                    render();
               });
          }

          if (nextBtn) {
               nextBtn.addEventListener("click", () => {
                    if (start + 5 >= dates.length) return;
                    start = Math.min(dates.length - 5, start + 5);
                    flash("next");
                    render();
               });
          }

          render();
     });
});
