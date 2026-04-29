document.addEventListener("DOMContentLoaded", () => {
    const els = [...document.querySelectorAll(".reveal")];
    if (els.length) {
        const io = new IntersectionObserver((entries) => {
            entries.forEach(e => {
                if (e.isIntersecting) e.target.classList.add("is-in");
            });
        }, { threshold: 0.12 });

        els.forEach(el => io.observe(el));
    }

    const form = document.getElementById("heroSearch");
    if (form) {
        form.addEventListener("submit", () => {
            form.classList.add("is-submitting");
            setTimeout(() => form.classList.remove("is-submitting"), 500);
        });
    }

    const tourLineSelect = document.getElementById("tourLineSelect");
    if (tourLineSelect) {
        const params = new URLSearchParams(window.location.search || "");
        const selected = params.get("tourLineId") || tourLineSelect.getAttribute("data-selected") || "";
        const loadTourLines = async () => {
            try {
                const res = await fetch("/api/tour-lines?active=true");
                const items = await res.json().catch(() => []);
                const opts = (items || [])
                    .filter(x => x && x.isActive !== false)
                    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
                    .map(x => {
                        const isSelected = selected && String(selected) === String(x.id);
                        return `<option value="${x.id}" ${isSelected ? "selected" : ""}>${x.name || "Dòng tour"}</option>`;
                    })
                    .join("");

                tourLineSelect.innerHTML = `<option value="">Tất cả dòng tour</option>` + opts;
            } catch (_) {
                // keep default option if load fails
            }
        };

        loadTourLines();
    }
});
