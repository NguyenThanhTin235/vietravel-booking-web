document.addEventListener("DOMContentLoaded", () => {
    const burger = document.getElementById("burgerBtn");
    const mobileNav = document.getElementById("mobileNav");
    if (burger && mobileNav) {
        burger.addEventListener("click", () => {
            const open = mobileNav.style.display === "block";
            mobileNav.style.display = open ? "none" : "block";
        });
    }

    const destTrigger = document.querySelector("[data-destination-trigger]");
    const destMenu = document.getElementById("destinationMenu");
    if (destTrigger && destMenu) {
        let loaded = false;
        const categoryEndpoint = destMenu.getAttribute("data-category-endpoint") || "/api/admin/tour-categories";
        const destinationEndpoint = destMenu.getAttribute("data-destination-endpoint") || "/api/admin/destinations";
        const groupWrap = destMenu.querySelector(".nav-mega-groups");
        const groupEls = {
            "trong-nuoc": destMenu.querySelector("[data-mega-group='trong-nuoc']"),
            "nuoc-ngoai": destMenu.querySelector("[data-mega-group='nuoc-ngoai']")
        };
        const tabs = Array.from(destMenu.querySelectorAll("[data-mega-tab]"));

        const setActiveGroup = (key) => {
            Object.keys(groupEls).forEach(k => {
                if (groupEls[k]) groupEls[k].style.display = (k === key) ? "" : "none";
            });
            tabs.forEach(t => t.classList.toggle("is-active", t.getAttribute("data-mega-tab") === key));
        };

        tabs.forEach(t => t.addEventListener("click", () => setActiveGroup(t.getAttribute("data-mega-tab"))));

        const closeMenu = () => {
            destMenu.classList.remove("open");
            destTrigger.setAttribute("aria-expanded", "false");
        };

        const buildMenu = (categories, destinations) => {
            if (!groupWrap) return;

            const norm = (s) => (s || "")
                .toLowerCase()
                .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
                .replace(/\s+/g, " ")
                .trim();

            const parents = [
                { label: "Trong nước", key: "trong nuoc", group: "trong-nuoc" },
                { label: "Nước ngoài", key: "nuoc ngoai", group: "nuoc-ngoai" }
            ];

            const parentMap = new Map(
                (categories || [])
                    .filter(c => !c || c.parentId == null)
                    .map(c => [norm(c.name), c])
            );

            parents.forEach(parent => {
                const groupEl = groupEls[parent.group];
                if (!groupEl) return;
                groupEl.innerHTML = "";

                const parentCat = parentMap.get(parent.key) || null;
                const groupCats = (categories || []).filter(c => {
                    if (!c || c.isActive === false) return false;
                    if (parentCat && String(c.parentId) === String(parentCat.id)) return true;
                    const p = norm(c.parentName || "");
                    return p === parent.key;
                });

                if (!groupCats.length) {
                    groupEl.innerHTML = "<div class=\"nav-mega-empty\">Chưa có danh mục</div>";
                    return;
                }

                groupCats.forEach(cat => {
                    const col = document.createElement("div");
                    col.className = "nav-mega-col";

                    const title = document.createElement("div");
                    title.className = "nav-mega-title-sm";
                    title.textContent = cat.name || "Danh mục";

                    const wrap = document.createElement("div");
                    wrap.className = "nav-mega-list";

                    const list = (destinations || [])
                        .filter(d => d && d.isActive !== false && String(d.categoryId) === String(cat.id))
                        .slice(0, 5);

                    if (!list.length) {
                        const empty = document.createElement("div");
                        empty.className = "nav-mega-empty";
                        empty.textContent = "Chưa có điểm đến";
                        wrap.appendChild(empty);
                    } else {
                        list.forEach(d => {
                            const a = document.createElement("a");
                            a.href = `/tour/tim-kiem?destinationId=${encodeURIComponent(d.id)}`;
                            a.textContent = d.name || "Điểm đến";
                            wrap.appendChild(a);
                        });
                    }

                    const more = document.createElement("a");
                    more.className = "nav-mega-more";
                    more.href = `/tour/tim-kiem?categoryId=${encodeURIComponent(cat.id)}`;
                    more.textContent = "Xem tất cả →";
                    wrap.appendChild(more);

                    col.appendChild(title);
                    col.appendChild(wrap);
                    groupEl.appendChild(col);
                });
            });

            setActiveGroup("nuoc-ngoai");
        };

        const loadMenu = async () => {
            if (loaded) return;
            try {
                const [cRes, dRes] = await Promise.all([
                    fetch(categoryEndpoint),
                    fetch(destinationEndpoint)
                ]);
                const categories = await cRes.json().catch(() => []);
                const destinations = await dRes.json().catch(() => []);
                buildMenu(categories, destinations);
                loaded = true;
            } catch (_) {
                if (groupWrap) groupWrap.innerHTML = "<div class=\"nav-mega-empty\">Không thể tải danh mục</div>";
            }
        };

        destTrigger.addEventListener("click", (e) => {
            e.preventDefault();
            const isOpen = destMenu.classList.toggle("open");
            destTrigger.setAttribute("aria-expanded", String(isOpen));
            if (isOpen) loadMenu();
        });

        document.addEventListener("click", (e) => {
            if (!destMenu.classList.contains("open")) return;
            if (destMenu.contains(e.target) || destTrigger.contains(e.target)) return;
            closeMenu();
        });

        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") closeMenu();
        });

        const closeBtn = destMenu.querySelector("[data-mega-close]");
        if (closeBtn) closeBtn.addEventListener("click", closeMenu);
    }

    const blockedFrames = document.querySelectorAll("iframe[src*='wps.com']");
    blockedFrames.forEach(frame => {
        const url = frame.getAttribute("src") || "";
        const link = document.createElement("a");
        link.href = url;
        link.target = "_blank";
        link.rel = "noopener";
        link.textContent = "Mở nội dung trên WPS";
        frame.replaceWith(link);
    });
});
