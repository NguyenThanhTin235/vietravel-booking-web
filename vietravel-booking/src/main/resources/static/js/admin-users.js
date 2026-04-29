function qs(id) { return document.getElementById(id) }
function qsa(sel, root) { return Array.from((root || document).querySelectorAll(sel)) }

function debounce(fn, ms) {
    let t
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms) }
}

async function api(url, opts) {
    const res = await fetch(url, Object.assign({ headers: { "Content-Type": "application/json" } }, opts || {}))
    if (res.status === 204) return null
    const ct = res.headers.get("content-type") || ""
    let data = null
    let text = ""
    if (ct.includes("application/json")) data = await res.json().catch(() => null)
    else text = await res.text().catch(() => "")
    if (!res.ok) {
        const msg = (data && data.message) ? data.message : (text ? text : ("HTTP " + res.status))
        throw new Error(msg)
    }
    return data
}

function toast(type, title, desc) {
    const wrap = qs("tlToastWrap")
    if (!wrap) return
    const el = document.createElement("div")
    el.className = "tl-toast " + (type === "err" ? "err" : "ok")
    el.innerHTML = `
        <div class="dot"></div>
        <div>
            <div class="t">${title || ""}</div>
            <div class="d">${desc || ""}</div>
        </div>
        <button class="x">×</button>
    `
    wrap.appendChild(el)
    const kill = () => { el.remove() }
    el.querySelector(".x").addEventListener("click", kill)
    setTimeout(kill, 3200)
}

function modalConfirm(title, body, okText) {
    const m = qs("tlModal")
    const t = qs("tlModalTitle")
    const b = qs("tlModalBody")
    const ok = qs("tlModalOk")
    const cancel = qs("tlModalCancel")
    const close = qs("tlModalClose")
    if (!m || !t || !b || !ok || !cancel || !close) return Promise.resolve(false)

    t.textContent = title || "Xác nhận"
    b.textContent = body || ""
    ok.textContent = okText || "Đồng ý"

    return new Promise(resolve => {
        const end = (v) => {
            m.classList.remove("show")
            ok.onclick = null
            cancel.onclick = null
            close.onclick = null
            m.onclick = null
            resolve(v)
        }
        ok.onclick = () => end(true)
        cancel.onclick = () => end(false)
        close.onclick = () => end(false)
        m.onclick = (e) => { if (e.target === m) end(false) }
        m.classList.add("show")
    })
}

function getPage() {
    const el = document.querySelector("[data-page]")
    return el ? el.getAttribute("data-page") : ""
}

function getMode() {
    const el = document.querySelector("[data-mode]")
    return el ? el.getAttribute("data-mode") : ""
}

function fmtDateTime(v) {
    if (!v) return ""
    const d = new Date(v)
    if (Number.isNaN(d.getTime())) return String(v)
    return d.toLocaleString("vi-VN")
}

/* =========================
   PAGINATION STATE
========================= */
const uState = { page: 1, pageSize: 5, filtered: [] }

function renderPager(total) {
    const pageInfo = qs("pageInfo")
    const pageNums = qs("pageNums")
    const btnPrev = qs("btnPrev")
    const btnNext = qs("btnNext")

    const size = uState.pageSize
    const totalPages = Math.max(1, Math.ceil(total / size))
    if (uState.page > totalPages) uState.page = totalPages

    const start = total === 0 ? 0 : ((uState.page - 1) * size + 1)
    const end = Math.min(total, uState.page * size)

    if (pageInfo) pageInfo.textContent = total ? `Hiển thị ${start}-${end} / ${total}` : "0 kết quả"

    if (btnPrev) {
        btnPrev.disabled = uState.page <= 1
        btnPrev.onclick = () => { if (uState.page > 1) { uState.page--; renderListPage() } }
    }
    if (btnNext) {
        btnNext.disabled = uState.page >= totalPages
        btnNext.onclick = () => { if (uState.page < totalPages) { uState.page++; renderListPage() } }
    }

    if (pageNums) {
        pageNums.innerHTML = ""
        const maxBtns = 7
        let from = Math.max(1, uState.page - 3)
        let to = Math.min(totalPages, from + maxBtns - 1)
        from = Math.max(1, to - maxBtns + 1)

        for (let p = from; p <= to; p++) {
            const b = document.createElement("button")
            b.type = "button"
            b.className = "tl-btn sm tl-pageNum" + (p === uState.page ? " active" : "")
            b.textContent = String(p)
            b.addEventListener("click", () => { uState.page = p; renderListPage() })
            pageNums.appendChild(b)
        }
    }
}

function bindRowActions(rows) {
    qsa("[data-toggle]", rows).forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.getAttribute("data-toggle")
            const name = btn.getAttribute("data-name") || ""
            const next = btn.getAttribute("data-next") || ""
            const ok = await modalConfirm("Đổi trạng thái", `Bạn muốn ${next} tài khoản "${name}"?`, "Đồng ý")
            if (!ok) return
            try {
                await api(`/api/admin/users/${id}/toggle-lock`, { method: "PATCH" })
                toast("ok", "Thành công", "Đã cập nhật trạng thái")
                uState.page = 1
                loadList()
            } catch (e) {
                toast("err", "Thất bại", e.message || "Không thể cập nhật")
            }
        })
    })
}

function renderListPage() {
    const rows = qs("rows")
    const msg = qs("msg")
    if (!rows) return

    const total = uState.filtered.length
    const size = uState.pageSize
    const startIdx = (uState.page - 1) * size
    const pageItems = uState.filtered.slice(startIdx, startIdx + size)

    renderPager(total)

    if (!pageItems.length) {
        rows.innerHTML = `
            <tr>
                <td colspan="4" style="color:var(--muted);font-weight:900;padding:16px">Không có dữ liệu</td>
            </tr>`
        if (msg) msg.textContent = "0 kết quả"
        return
    }

    rows.innerHTML = pageItems.map(x => {
        const toggleNext = (x.status === "LOCKED") ? "mở khóa" : "khóa"
        const toggleText = (x.status === "LOCKED") ? "Mở khóa" : "Khóa"
        const toggleBtnClass = (x.status === "LOCKED") ? "primary" : "danger"

        const statusBadge = x.status === "ACTIVE"
            ? `<span class="tl-badge on"><i></i>ACTIVE</span>`
            : `<span class="tl-badge off"><i></i>${x.status || ""}</span>`

        return `
            <tr>
                <td>${x.id}</td>
                <td style="font-weight:900">${x.email || ""}</td>
                <td>${statusBadge}</td>
                <td class="tl-center">
                   <span class="tl-btnRow">
                        <a class="tl-btn sm" href="/admin/users/view?id=${x.id}">Xem</a>
                        <a class="tl-btn sm" href="/admin/users/edit?id=${x.id}">Sửa</a>
                        <button
                            class="tl-btn sm ${toggleBtnClass}"
                            data-toggle="${x.id}"
                            data-next="${toggleNext}"
                            data-name="${(x.email || "").replace(/"/g, "&quot;")}"
                        >${toggleText}</button>
                    </span>

                </td>
            </tr>
        `
    }).join("")

    bindRowActions(rows)
    if (msg) msg.textContent = `${total} kết quả`
}

/* =========================
   LIST LOAD (server filter)
========================= */
async function loadList() {
    const rows = qs("rows")
    const msg = qs("msg")
    if (!rows) return

    rows.innerHTML = `
        <tr class="tl-skeleton"><td colspan="4"><div class="tl-skel" style="height:14px;width:55%"></div></td></tr>
        <tr class="tl-skeleton"><td colspan="4"><div class="tl-skel" style="height:14px;width:75%"></div></td></tr>
        <tr class="tl-skeleton"><td colspan="4"><div class="tl-skel" style="height:14px;width:65%"></div></td></tr>
    `
    if (msg) msg.textContent = ""

    try {
        const onlyActive = qs("onlyActive")
        const role = qs("role")
        const status = qs("status")
        const q = qs("q")

        const params = []
        if (onlyActive && onlyActive.checked) params.push("active=true")

        const roleVal = (role && role.value) ? role.value.trim() : ""
        const statusVal = (status && status.value) ? status.value.trim() : ""
        const qVal = (q && q.value) ? q.value.trim() : ""

        if (roleVal) params.push("role=" + encodeURIComponent(roleVal))
        if (statusVal) params.push("status=" + encodeURIComponent(statusVal))
        if (qVal) params.push("q=" + encodeURIComponent(qVal))

        const url = "/api/admin/users" + (params.length ? ("?" + params.join("&")) : "")
        const items = await api(url, { method: "GET" })

        uState.filtered = items || []
        if (uState.page < 1) uState.page = 1
        renderListPage()
    } catch (e) {
        rows.innerHTML = `<tr><td colspan="4" style="color:#b42318;font-weight:950;padding:16px">${e.message || "Lỗi tải dữ liệu"}</td></tr>`
        toast("err", "Lỗi", e.message || "Không tải được dữ liệu")
    }
}

/* =========================
   AVATAR UPLOAD (Cloudinary)
========================= */
function setAvatar(url) {
    const img = qs("avatarImg")
    const fb = qs("avatarFallback")

    if (!img || !fb) return

    if (url) {
        img.src = url
        img.style.display = "block"
        fb.style.display = "none"
    } else {
        img.src = ""
        img.style.display = "none"
        fb.style.display = "flex"
    }

    const avatarInp = qs("avatar")
    if (avatarInp) avatarInp.value = url || ""
}


function setAvatarHint(text, isErr) {
    const h = qs("avatarHint")
    if (!h) return
    h.textContent = text || ""
    h.style.color = isErr ? "#b42318" : "var(--muted)"
}

async function uploadAvatar(userId, file) {
    if (!userId) throw new Error("Chưa có userId để upload avatar")
    if (!file) throw new Error("Chưa chọn ảnh")

    const fd = new FormData()
    fd.append("file", file)

    const res = await fetch(`/api/admin/uploads/users/${encodeURIComponent(userId)}/avatar`, {
        method: "POST",
        body: fd
    })

    if (!res.ok) {
        const ct = res.headers.get("content-type") || ""
        let msg = "Upload thất bại"
        if (ct.includes("application/json")) {
            const data = await res.json().catch(() => null)
            msg = (data && data.message) ? data.message : msg
        } else {
            const t = await res.text().catch(() => "")
            msg = t || msg
        }
        throw new Error(msg)
    }

    const data = await res.json().catch(() => null)
    const url = (data && (data.secureUrl || data.url)) ? (data.secureUrl || data.url) : ""
    if (!url) throw new Error("Cloudinary không trả URL")
    return url
}

function bindAvatarUploader(userId, isCreateStaff) {
    const fileInp = qs("avatarFile")
    const btnClear = qs("btnClearAvatar")
    const img = qs("avatarImg")
    const fb = qs("avatarFallback")

    if (isCreateStaff) return
    if (!fileInp) return

    let selectedFile = null

    fileInp.addEventListener("change", async () => {
        selectedFile = fileInp.files && fileInp.files[0] ? fileInp.files[0] : null
        if (!selectedFile) {
            setAvatarHint("Chọn ảnh .jpg/.png để tải lên", false)
            return
        }
        if (!selectedFile.type || !selectedFile.type.startsWith("image/")) {
            selectedFile = null
            fileInp.value = ""
            setAvatarHint("File không hợp lệ (chỉ cho phép ảnh)", true)
            return
        }

        const previewUrl = URL.createObjectURL(selectedFile)
        if (img && fb) {
            img.src = previewUrl
            img.style.display = ""
            fb.style.display = "none"
        }

        if (!userId) {
            setAvatarHint("Bạn cần lưu user trước (có ID) rồi mới upload avatar", true)
            return
        }

        try {
            fileInp.disabled = true
            setAvatarHint("Đang tải ảnh lên Cloudinary...", false)

            const url = await uploadAvatar(userId, selectedFile)
            setAvatar(url)

            selectedFile = null
            fileInp.value = ""
            setAvatarHint("Đã tải ảnh lên thành công", false)
            toast("ok", "Upload thành công", "Avatar đã được cập nhật (nhớ bấm Lưu nếu cần cập nhật DB)")

        } catch (e) {
            setAvatarHint(e.message || "Upload thất bại", true)
            toast("err", "Upload thất bại", e.message || "Không thể upload")
        } finally {
            fileInp.disabled = false
        }
    })

    if (btnClear) {
        btnClear.addEventListener("click", async () => {
            const ok = await modalConfirm("Xóa avatar", "Bạn muốn xóa avatar hiện tại?", "Đồng ý")
            if (!ok) return
            setAvatar("")
            setAvatarHint("Đã xóa avatar (nhớ bấm Lưu để cập nhật DB)", false)
            toast("ok", "Đã xóa", "Bấm Lưu để cập nhật")
        })
    }
}

/* =========================
   FORM
========================= */
async function loadForm() {
    const form = qs("form")
    if (!form) return

    const mode = getMode()
    const idHidden = qs("id")
    const idParam = (idHidden && idHidden.value) ? idHidden.value : ""
    const isCreateStaff = (mode === "createStaff")
    const isEdit = !!idParam && !isCreateStaff

    const err = qs("err")
    const btnToggleLock = qs("btnToggleLock")

    const setErr = (m) => { if (!err) return; err.style.display = "block"; err.textContent = m || "" }
    const clearErr = () => { if (!err) return; err.style.display = "none"; err.textContent = "" }

    if (btnToggleLock) btnToggleLock.style.display = (isEdit ? "" : "none")

    if (isEdit) {
        try {
            const data = await api(`/api/admin/users/${idParam}`, { method: "GET" })
            qs("email").value = data.email || ""
            qs("email").readOnly = true

            const roleSel = qs("role")
            if (roleSel) roleSel.value = data.role || "CUSTOMER"

            qs("fullName").value = data.fullName || ""
            qs("phone").value = data.phone || ""
            if (qs("gender")) qs("gender").value = data.gender || ""
            if (qs("dob")) qs("dob").value = data.dob || ""
            if (qs("address")) qs("address").value = data.address || ""
            setAvatar(data.avatar || "")
            if (qs("status")) qs("status").value = data.status || "ACTIVE"

        } catch (e) {
            setErr(e.message || "Không tải được dữ liệu")
            toast("err", "Lỗi", e.message || "Không tải được dữ liệu")
        }
    } else {
        const email = qs("email")
        if (email) email.readOnly = false
    }

    bindAvatarUploader(idParam, isCreateStaff)

    if (btnToggleLock && isEdit) {
        btnToggleLock.addEventListener("click", async () => {
            const name = qs("fullName").value || qs("email").value || ""
            const ok = await modalConfirm("Khóa/Mở khóa", `Bạn muốn đổi trạng thái tài khoản "${name}"?`, "Đồng ý")
            if (!ok) return
            clearErr()
            try {
                await api(`/api/admin/users/${idParam}/toggle-lock`, { method: "PATCH" })
                toast("ok", "Thành công", "Đã cập nhật trạng thái")
                setTimeout(() => { window.location.reload() }, 350)
            } catch (e) {
                setErr(e.message || "Không thể cập nhật")
                toast("err", "Thất bại", e.message || "Không thể cập nhật")
            }
        })
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault()
        clearErr()

        try {
            if (isCreateStaff) {
                const payload = {
                    email: qs("email").value.trim(),
                    password: qs("password").value.trim(),
                    fullName: qs("fullName").value.trim(),
                    phone: qs("phone").value.trim()
                }
                if (!payload.email) { setErr("Email không được rỗng"); return }
                if (!payload.password) { setErr("Mật khẩu không được rỗng"); return }
                if (!payload.fullName) { setErr("Họ tên không được rỗng"); return }

                await api(`/api/admin/users/staff`, { method: "POST", body: JSON.stringify(payload) })
                toast("ok", "Đã tạo", "Nhân viên đã được tạo")
                setTimeout(() => { window.location.href = "/admin/users" }, 450)
                return
            }

            const payload = {
                fullName: qs("fullName").value.trim(),
                phone: qs("phone").value.trim(),
                gender: qs("gender") ? qs("gender").value : "",
                dob: qs("dob") ? (qs("dob").value || null) : null,
                address: qs("address") ? qs("address").value.trim() : "",
                avatar: qs("avatar") ? qs("avatar").value.trim() : "",
                status: qs("status") ? qs("status").value : null
            }

            if (!payload.fullName) { setErr("Họ tên không được rỗng"); return }

            await api(`/api/admin/users/${idParam}`, { method: "PUT", body: JSON.stringify(payload) })
            toast("ok", "Đã cập nhật", "Người dùng đã được lưu")
            setTimeout(() => { window.location.href = "/admin/users" }, 450)

        } catch (ex) {
            setErr(ex.message || "Lưu thất bại")
            toast("err", "Lưu thất bại", ex.message || "Không thể lưu")
        }
    })
}

async function loadView() {
    const wrap = document.querySelector("[data-page='admin-users-view']")
    if (!wrap) return

    const idHidden = qs("id")
    const idParam = (idHidden && idHidden.value) ? idHidden.value : ""

    const err = qs("err")
    const btnToggleLock = qs("btnToggleLock")
    const btnGoEdit = qs("btnGoEdit")

    const setErr = (m) => { if (!err) return; err.style.display = "block"; err.textContent = m || "" }
    const clearErr = () => { if (!err) return; err.style.display = "none"; err.textContent = "" }

    if (!idParam) { setErr("Thiếu userId"); return }

    if (btnGoEdit) btnGoEdit.href = `/admin/users/edit?id=${encodeURIComponent(idParam)}`

    const fill = (data) => {
        qs("email").value = data.email || ""
        const roleSel = qs("role")
        if (roleSel) roleSel.value = data.role || "CUSTOMER"

        qs("fullName").value = data.fullName || ""
        qs("phone").value = data.phone || ""
        if (qs("gender")) qs("gender").value = data.gender || ""
        if (qs("dob")) qs("dob").value = data.dob || ""
        if (qs("address")) qs("address").value = data.address || ""
        setAvatar(data.avatar || "")
        if (qs("status")) qs("status").value = data.status || "ACTIVE"
    }

    const reload = async () => {
        const data = await api(`/api/admin/users/${idParam}`, { method: "GET" })
        fill(data)
        return data
    }

    try {
        clearErr()
        await reload()
    } catch (e) {
        setErr(e.message || "Không tải được dữ liệu")
        toast("err", "Lỗi", e.message || "Không tải được dữ liệu")
    }

    if (btnToggleLock) {
        btnToggleLock.addEventListener("click", async () => {
            const name = qs("fullName").value || qs("email").value || ""
            const ok = await modalConfirm("Khóa/Mở khóa", `Bạn muốn đổi trạng thái tài khoản "${name}"?`, "Đồng ý")
            if (!ok) return
            clearErr()
            try {
                await api(`/api/admin/users/${idParam}/toggle-lock`, { method: "PATCH" })
                toast("ok", "Thành công", "Đã cập nhật trạng thái")
                setTimeout(() => { window.location.reload() }, 350)
            } catch (e) {
                setErr(e.message || "Không thể cập nhật")
                toast("err", "Thất bại", e.message || "Không thể cập nhật")
            }
        })
    }
}
document.addEventListener("DOMContentLoaded", () => {
    const page = getPage()

    if (page === "admin-users-list") {
        uState.page = 1
        loadList()

        const q = qs("q")
        const onlyActive = qs("onlyActive")
        const btnReload = qs("btnReload")
        const role = qs("role")
        const status = qs("status")

        if (btnReload) btnReload.addEventListener("click", () => { uState.page = 1; loadList() })
        if (onlyActive) onlyActive.addEventListener("change", () => { uState.page = 1; loadList() })
        if (role) role.addEventListener("change", () => { uState.page = 1; loadList() })
        if (status) status.addEventListener("change", () => { uState.page = 1; loadList() })
        if (q) q.addEventListener("input", debounce(() => { uState.page = 1; loadList() }, 260))
    }

    if (page === "admin-users-form") {
        loadForm()
    }

    if (page === "admin-users-view") {
        loadView()
    }
})
