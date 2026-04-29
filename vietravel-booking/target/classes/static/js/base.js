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

    const notiWrap = document.querySelector("[data-noti]");
    if (notiWrap) {
        const toggle = notiWrap.querySelector("[data-noti-toggle]");
        const list = notiWrap.querySelector("[data-noti-list]");
        const badge = notiWrap.querySelector("[data-noti-count]");
        const modal = document.querySelector("[data-noti-modal]");
        const modalCard = modal ? modal.querySelector("[data-noti-modal-card]") : null;
        const modalTitle = modal ? modal.querySelector("[data-noti-modal-title]") : null;
        const modalTime = modal ? modal.querySelector("[data-noti-modal-time]") : null;
        const modalMessage = modal ? modal.querySelector("[data-noti-modal-message]") : null;
        const modalDelete = modal ? modal.querySelector("[data-noti-modal-delete]") : null;
        const modalCloses = modal ? Array.from(modal.querySelectorAll("[data-noti-modal-close]")) : [];
        let currentId = null;

        const formatTime = (v) => {
            if (!v) return "";
            const d = new Date(v);
            if (Number.isNaN(d.getTime())) return v;
            return d.toLocaleString("vi-VN");
        };

        const setBadge = (count) => {
            if (!badge) return;
            if (!count) {
                badge.hidden = true;
                badge.textContent = "";
                return;
            }
            badge.hidden = false;
            badge.textContent = count > 99 ? "99+" : String(count);
        };

        const loadCount = async () => {
            try {
                const res = await fetch("/api/notifications/unread-count", { headers: { "X-Requested-With": "XMLHttpRequest" } });
                const data = await res.json();
                setBadge(data && typeof data.count === "number" ? data.count : 0);
            } catch (_) {
                setBadge(0);
            }
        };

        const renderList = (items) => {
            if (!list) return;
            list.innerHTML = "";
            if (!items || !items.length) {
                const empty = document.createElement("div");
                empty.className = "noti-empty";
                empty.textContent = "Chưa có thông báo";
                list.appendChild(empty);
                return;
            }
            items.forEach((n) => {
                const btn = document.createElement("button");
                btn.type = "button";
                btn.className = "noti-item" + (n.read ? "" : " is-unread");
                btn.setAttribute("data-id", n.id);

                const title = document.createElement("div");
                title.className = "noti-item-title";
                title.textContent = n.title || "Thông báo";

                const msg = document.createElement("div");
                msg.className = "noti-item-msg";
                msg.textContent = n.message || "";

                const time = document.createElement("div");
                time.className = "noti-item-time";
                time.textContent = formatTime(n.createdAt);

                btn.appendChild(title);
                btn.appendChild(msg);
                btn.appendChild(time);
                list.appendChild(btn);
            });
        };

        const loadList = async () => {
            try {
                const res = await fetch("/api/notifications?limit=20", { headers: { "X-Requested-With": "XMLHttpRequest" } });
                const data = await res.json();
                renderList(Array.isArray(data) ? data : []);
            } catch (_) {
                renderList([]);
            }
        };

        const setModalType = (type) => {
            if (!modalCard) return;
            modalCard.classList.remove("info", "success", "warning", "error");
            if (!type) return;
            modalCard.classList.add(String(type).toLowerCase());
        };

        const openDetail = async (id) => {
            if (!id) return;
            currentId = id;
            try {
                const res = await fetch(`/api/notifications/${encodeURIComponent(id)}`, { headers: { "X-Requested-With": "XMLHttpRequest" } });
                const data = await res.json();
                if (modalTitle) modalTitle.textContent = data.title || "Thông báo";
                if (modalTime) modalTime.textContent = formatTime(data.createdAt);
                if (modalMessage) modalMessage.textContent = data.message || "";
                setModalType(data.type || "info");
                if (modal) modal.classList.add("show");
                await fetch(`/api/notifications/${encodeURIComponent(id)}/read`, { method: "POST", headers: { "X-Requested-With": "XMLHttpRequest" } });
                await loadCount();
                await loadList();
            } catch (_) {
            }
        };

        const deleteCurrent = async () => {
            if (!currentId) return;
            try {
                await fetch(`/api/notifications/${encodeURIComponent(currentId)}`, { method: "DELETE", headers: { "X-Requested-With": "XMLHttpRequest" } });
            } catch (_) {
            }
            closeModal();
            await loadCount();
            await loadList();
        };

        const closeModal = () => {
            if (modal) modal.classList.remove("show");
            currentId = null;
        };

        if (toggle) {
            toggle.addEventListener("click", (e) => {
                e.stopPropagation();
                notiWrap.classList.toggle("open");
                if (notiWrap.classList.contains("open")) {
                    loadList();
                    loadCount();
                }
            });
        }

        if (list) {
            list.addEventListener("click", (e) => {
                const btn = e.target.closest("[data-id]");
                if (!btn) return;
                openDetail(btn.getAttribute("data-id"));
            });
        }

        if (modal) {
            modal.addEventListener("click", (e) => {
                if (e.target === modal) closeModal();
            });
        }

        if (modalDelete) {
            modalDelete.addEventListener("click", () => deleteCurrent());
        }

        modalCloses.forEach((btn) => btn.addEventListener("click", () => closeModal()));

        document.addEventListener("click", (e) => {
            if (!notiWrap.contains(e.target)) notiWrap.classList.remove("open");
        });

        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                notiWrap.classList.remove("open");
                closeModal();
            }
        });

        loadCount();
    }
});
