(function () {
    function qs(s, root) { return (root || document).querySelector(s); }
    function qsa(s, root) { return Array.from((root || document).querySelectorAll(s)); }

    function clamp(n, min, max) { return Math.max(min, Math.min(max, n)); }
    function easeOutCubic(t) { return 1 - Math.pow(1 - t, 3); }

    function animateNumber(el, to, duration) {
        var from = parseFloat((el.getAttribute("data-from") || "0").replace(/[^\d.-]/g, "")) || 0;
        var start = performance.now();
        var isCurrency = /₫/.test(el.textContent) || el.hasAttribute("data-currency");
        var decimals = parseInt(el.getAttribute("data-decimals") || "0", 10);
        function fmt(v) {
            if (isCurrency) {
                var n = Math.round(v);
                return n.toLocaleString("vi-VN") + " ₫";
            }
            if (decimals > 0) return v.toFixed(decimals);
            return Math.round(v).toString();
        }
        function frame(now) {
            var p = clamp((now - start) / duration, 0, 1);
            var v = from + (to - from) * easeOutCubic(p);
            el.textContent = fmt(v);
            if (p < 1) requestAnimationFrame(frame);
        }
        requestAnimationFrame(frame);
    }

    function closeAllDropdown() {
        qsa("[data-dropdown]").forEach(function (box) { box.classList.remove("open"); });
    }

    function initDropdowns() {
        document.addEventListener("click", function (e) {
            var btn = e.target.closest("[data-dropdown-btn]");
            if (btn) {
                var box = btn.closest("[data-dropdown]");
                if (!box) return;
                var isOpen = box.classList.contains("open");
                closeAllDropdown();
                if (!isOpen) box.classList.add("open");
                return;
            }
            if (!e.target.closest("[data-dropdown]")) closeAllDropdown();
        });
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") closeAllDropdown();
        });
    }

    function initSidebarToggle() {
        qsa("[data-sidebar-toggle]").forEach(function (btn) {
            btn.addEventListener("click", function () {
                document.body.classList.toggle("sidebar-collapsed");
            });
        });
    }

    function toast(msg, type) {
        var wrap = qs(".toast-wrap");
        if (!wrap) {
            wrap = document.createElement("div");
            wrap.className = "toast-wrap";
            document.body.appendChild(wrap);
        }
        var t = document.createElement("div");
        t.className = "toast " + (type ? ("toast-" + type) : "");
        t.innerHTML = '<div class="toast-dot"></div><div class="toast-msg"></div><button class="toast-x" type="button">×</button>';
        qs(".toast-msg", t).textContent = msg;
        qs(".toast-x", t).addEventListener("click", function () { t.classList.add("out"); setTimeout(function () { t.remove(); }, 220); });
        wrap.appendChild(t);
        setTimeout(function () { t.classList.add("show"); }, 10);
        setTimeout(function () { t.classList.add("out"); setTimeout(function () { t.remove(); }, 220); }, 2600);
    }

    function initPageTransitions() {
        var shell = qs(".admin-shell");
        if (!shell) return;

        document.documentElement.classList.add("js");
        document.body.classList.add("page-enter");
        requestAnimationFrame(function () { document.body.classList.add("page-enter-active"); });
        setTimeout(function () {
            document.body.classList.remove("page-enter");
            document.body.classList.remove("page-enter-active");
        }, 280);

        document.addEventListener("click", function (e) {
            var a = e.target.closest("a");
            if (!a) return;

            var href = a.getAttribute("href") || "";
            var target = a.getAttribute("target");
            if (target === "_blank") return;
            if (href.startsWith("#") || href.startsWith("mailto:") || href.startsWith("tel:")) return;
            if (href.startsWith("javascript:")) return;
            if (a.hasAttribute("download")) return;

            var origin = location.origin;
            try {
                var u = new URL(href, location.href);
                if (u.origin !== origin) return;
            } catch (_) {
                return;
            }

            if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;

            e.preventDefault();
            document.body.classList.add("page-exit");
            setTimeout(function () { location.href = href; }, 160);
        });
    }

    function initRevealOnScroll() {
        var els = qsa(".card,.kpi,.list-item,.table tbody tr");
        if (els.length === 0) return;

        els.forEach(function (el) { el.classList.add("reveal"); });

        var io = new IntersectionObserver(function (entries) {
            entries.forEach(function (en) {
                if (en.isIntersecting) {
                    en.target.classList.add("reveal-in");
                    io.unobserve(en.target);
                }
            });
        }, { threshold: 0.12 });

        els.forEach(function (el) { io.observe(el); });
    }

    function initKPICounters() {
        var els = qsa("[data-counter]");
        els.forEach(function (el) {
            var to = parseFloat((el.getAttribute("data-to") || "0").replace(/[^\d.-]/g, "")) || 0;
            var duration = parseInt(el.getAttribute("data-duration") || "900", 10);
            var once = false;

            var io = new IntersectionObserver(function (entries) {
                entries.forEach(function (en) {
                    if (en.isIntersecting && !once) {
                        once = true;
                        animateNumber(el, to, duration);
                        io.disconnect();
                    }
                });
            }, { threshold: 0.35 });

            io.observe(el);
        });
    }

    function initTableSearch() {
        var input = qs("[data-table-search]");
        var table = qs("[data-table]");
        if (!input || !table) return;

        var rows = qsa("tbody tr", table);
        function norm(s) { return (s || "").toLowerCase().trim(); }
        input.addEventListener("input", function () {
            var q = norm(input.value);
            var shown = 0;
            rows.forEach(function (r) {
                var text = norm(r.textContent);
                var ok = !q || text.indexOf(q) > -1;
                r.style.display = ok ? "" : "none";
                if (ok) shown++;
            });
            var c = qs("[data-table-count]");
            if (c) c.textContent = shown.toString();
        });
    }

    function initSkeletonLoading() {
        var btn = qs("[data-demo-loading]");
        if (!btn) return;

        btn.addEventListener("click", function () {
            var blocks = qsa("[data-skeleton]");
            blocks.forEach(function (b) { b.classList.add("skeleton"); });
            toast("Đang tải dữ liệu...", "info");
            setTimeout(function () {
                blocks.forEach(function (b) { b.classList.remove("skeleton"); });
                toast("Tải dữ liệu thành công", "success");
            }, 900);
        });
    }

    function initActiveNavInk() {
        var active = qs(".nav-item.active");
        if (!active) return;
        active.classList.add("pulse-active");
        setTimeout(function () { active.classList.remove("pulse-active"); }, 900);
    }

    function initSidebarAutoScroll() {
        var sidebar = qs(".admin-sidebar");
        var nav = qs(".sidebar-nav", sidebar);
        if (!sidebar || !nav) return;

        var target = qs(".nav-sub-item.active", nav) || qs(".nav-item.active", nav);
        if (!target) return;

        var group = target.closest(".nav-group");
        if (group) group.classList.add("open");

        requestAnimationFrame(function () {
            try {
                target.scrollIntoView({ block: "center", inline: "nearest", behavior: "instant" });
            } catch (_) {
                target.scrollIntoView();
            }
        });
    }

    function initNotifications() {
        var wrap = qs("[data-noti]");
        if (!wrap) return;

        var toggle = qs("[data-noti-toggle]", wrap);
        var dropdown = qs("[data-noti-dropdown]", wrap);
        var list = qs("[data-noti-list]", wrap);
        var badge = qs("[data-noti-count]", wrap);
        var modal = qs("[data-noti-modal]");
        var modalCard = qs("[data-noti-modal-card]", modal);
        var modalTitle = qs("[data-noti-modal-title]", modal);
        var modalTime = qs("[data-noti-modal-time]", modal);
        var modalMessage = qs("[data-noti-modal-message]", modal);
        var modalDelete = qs("[data-noti-modal-delete]", modal);
        var modalCloses = qsa("[data-noti-modal-close]", modal);
        var currentId = null;

        function formatTime(v) {
            if (!v) return "";
            var d = new Date(v);
            if (isNaN(d.getTime())) return v;
            return d.toLocaleString("vi-VN");
        }

        function setBadge(count) {
            if (!badge) return;
            if (!count) {
                badge.hidden = true;
                badge.textContent = "";
                return;
            }
            badge.hidden = false;
            badge.textContent = count > 99 ? "99+" : String(count);
        }

        async function loadCount() {
            try {
                var res = await fetch("/api/notifications/unread-count", { headers: { "X-Requested-With": "XMLHttpRequest" } });
                var data = await res.json();
                setBadge(data && typeof data.count === "number" ? data.count : 0);
            } catch (_) {
                setBadge(0);
            }
        }

        function renderList(items) {
            if (!list) return;
            list.innerHTML = "";
            if (!items || !items.length) {
                var empty = document.createElement("div");
                empty.className = "noti-empty";
                empty.textContent = "Chưa có thông báo";
                list.appendChild(empty);
                return;
            }
            items.forEach(function (n) {
                var btn = document.createElement("button");
                btn.type = "button";
                btn.className = "noti-item" + (n.read ? "" : " is-unread");
                btn.setAttribute("data-id", n.id);

                var title = document.createElement("div");
                title.className = "noti-item-title";
                title.textContent = n.title || "Thông báo";

                var msg = document.createElement("div");
                msg.className = "noti-item-msg";
                msg.textContent = n.message || "";

                var time = document.createElement("div");
                time.className = "noti-item-time";
                time.textContent = formatTime(n.createdAt);

                btn.appendChild(title);
                btn.appendChild(msg);
                btn.appendChild(time);
                list.appendChild(btn);
            });
        }

        async function loadList() {
            try {
                var res = await fetch("/api/notifications?limit=20", { headers: { "X-Requested-With": "XMLHttpRequest" } });
                var data = await res.json();
                renderList(Array.isArray(data) ? data : []);
            } catch (_) {
                renderList([]);
            }
        }

        function setModalType(type) {
            if (!modalCard) return;
            modalCard.classList.remove("info", "success", "warning", "error");
            if (!type) return;
            modalCard.classList.add(String(type).toLowerCase());
        }

        async function openDetail(id) {
            if (!id) return;
            currentId = id;
            try {
                var res = await fetch("/api/notifications/" + encodeURIComponent(id), { headers: { "X-Requested-With": "XMLHttpRequest" } });
                var data = await res.json();
                if (modalTitle) modalTitle.textContent = data.title || "Thông báo";
                if (modalTime) modalTime.textContent = formatTime(data.createdAt);
                if (modalMessage) modalMessage.textContent = data.message || "";
                setModalType(data.type || "info");
                if (modal) modal.classList.add("show");
                await fetch("/api/notifications/" + encodeURIComponent(id) + "/read", { method: "POST", headers: { "X-Requested-With": "XMLHttpRequest" } });
                await loadCount();
                await loadList();
            } catch (_) {
            }
        }

        async function deleteCurrent() {
            if (!currentId) return;
            try {
                await fetch("/api/notifications/" + encodeURIComponent(currentId), { method: "DELETE", headers: { "X-Requested-With": "XMLHttpRequest" } });
            } catch (_) {
            }
            closeModal();
            await loadCount();
            await loadList();
        }

        function closeModal() {
            if (modal) modal.classList.remove("show");
            currentId = null;
        }

        if (toggle) {
            toggle.addEventListener("click", function (e) {
                e.stopPropagation();
                wrap.classList.toggle("open");
                if (wrap.classList.contains("open")) {
                    loadList();
                    loadCount();
                }
            });
        }

        if (list) {
            list.addEventListener("click", function (e) {
                var btn = e.target.closest("[data-id]");
                if (!btn) return;
                openDetail(btn.getAttribute("data-id"));
            });
        }

        if (modal) {
            modal.addEventListener("click", function (e) {
                if (e.target === modal) closeModal();
            });
        }

        if (modalDelete) {
            modalDelete.addEventListener("click", function () { deleteCurrent(); });
        }

        modalCloses.forEach(function (btn) {
            btn.addEventListener("click", function () { closeModal(); });
        });

        document.addEventListener("click", function (e) {
            if (!wrap.contains(e.target)) wrap.classList.remove("open");
        });

        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") {
                wrap.classList.remove("open");
                closeModal();
            }
        });

        loadCount();
    }

    initDropdowns();
    initSidebarToggle();
    initPageTransitions();
    initRevealOnScroll();
    initKPICounters();
    initTableSearch();
    initSkeletonLoading();
    initActiveNavInk();
    initSidebarAutoScroll();
    initNotifications();

    window.AdminUI = { toast: toast };
})();
