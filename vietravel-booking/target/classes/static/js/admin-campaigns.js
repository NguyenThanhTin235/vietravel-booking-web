function qs(id) { return document.getElementById(id) }
function qsa(sel, root) { return Array.from((root || document).querySelectorAll(sel)) }

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

async function apiForm(url, formData, opts) {
     const res = await fetch(url, Object.assign({ method: "POST", body: formData }, opts || {}))
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
     const wrap = qs("tcToastWrap")
     if (!wrap) return
     const el = document.createElement("div")
     el.className = "tc-toast " + (type === "err" ? "err" : "ok")
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
     const m = qs("tcModal")
     const t = qs("tcModalTitle")
     const b = qs("tcModalBody")
     const ok = qs("tcModalOk")
     const cancel = qs("tcModalCancel")
     const close = qs("tcModalClose")
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

function toSlug(v) {
     if (!v) return ""
     return v.trim().toLowerCase()
          .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
          .replace(/đ/g, "d").replace(/Đ/g, "d")
          .replace(/[^a-z0-9]+/g, "-")
          .replace(/^-+|-+$/g, "")
          .replace(/-+/g, "-")
}

const state = { page: 1, pageSize: 8, all: [], filtered: [] }

function renderPager(total) {
     const pageInfo = qs("pageInfo")
     const pageNums = qs("pageNums")
     const btnPrev = qs("btnPrev")
     const btnNext = qs("btnNext")

     const size = state.pageSize
     const totalPages = Math.max(1, Math.ceil(total / size))
     if (state.page > totalPages) state.page = totalPages

     const start = total === 0 ? 0 : ((state.page - 1) * size + 1)
     const end = Math.min(total, state.page * size)

     if (pageInfo) pageInfo.textContent = total ? `${start}-${end} / ${total}` : "0 kết quả"

     if (btnPrev) {
          btnPrev.disabled = state.page <= 1
          btnPrev.onclick = () => { if (state.page > 1) { state.page--; renderListPage() } }
     }
     if (btnNext) {
          btnNext.disabled = state.page >= totalPages
          btnNext.onclick = () => { if (state.page < totalPages) { state.page++; renderListPage() } }
     }

     if (pageNums) {
          pageNums.innerHTML = ""
          const maxBtns = 7
          let from = Math.max(1, state.page - 3)
          let to = Math.min(totalPages, from + maxBtns - 1)
          from = Math.max(1, to - maxBtns + 1)

          for (let p = from; p <= to; p++) {
               const b = document.createElement("button")
               b.type = "button"
               b.className = "tc-btn sm tc-pageNum" + (p === state.page ? " active" : "")
               b.textContent = String(p)
               b.addEventListener("click", () => { state.page = p; renderListPage() })
               pageNums.appendChild(b)
          }
     }
}

function formatDateTime(raw) {
     if (!raw) return "--"
     const d = new Date(raw)
     if (Number.isNaN(d.getTime())) return "--"
     const pad = n => String(n).padStart(2, "0")
     return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function renderListPage() {
     const rows = qs("rows")
     if (!rows) return
     const total = state.filtered.length
     const size = state.pageSize
     const startIdx = (state.page - 1) * size
     const pageItems = state.filtered.slice(startIdx, startIdx + size)

     renderPager(total)

     if (!pageItems.length) {
          rows.innerHTML = `<tr><td colspan="8" class="muted">Không có dữ liệu</td></tr>`
          return
     }

     rows.innerHTML = pageItems.map(x => {
          const statusBadge = {
               SCHEDULE: "badge badge-pending",
               ACTIVE: "badge badge-confirmed",
               EXPIRED: "badge badge-canceled",
               DISABLED: "badge badge-canceled"
          }[x.status] || "badge badge-confirmed"
          const statusLabel = {
               SCHEDULE: "Chờ chạy",
               ACTIVE: "Đang chạy",
               EXPIRED: "Hết hạn",
               DISABLED: "Tắt"
          }[x.status] || "--"
          const timeLabel = `${formatDateTime(x.startAt)} → ${formatDateTime(x.endAt)}`
          const discountLabel = x.discountType === "PERCENT"
               ? `${x.discountValue || 0}%`
               : `${Number(x.discountValue || 0).toLocaleString("vi-VN")} ₫`
          const usageLabel = x.usageLimit && x.usageLimit > 0
               ? `${x.usedCount || 0}/${x.usageLimit}`
               : `${x.usedCount || 0}/∞`
          return `
            <tr>
                <td>${x.id}</td>
                <td>${x.name || ""}</td>
                <td><span class="tc-code">${x.code || ""}</span></td>
                <td><span class="${statusBadge}">${statusLabel}</span></td>
                <td>${timeLabel}</td>
                <td>${discountLabel}</td>
                <td>${usageLabel}</td>
                <td>
                    <div class="table-actions process-actions-row">
                        <a class="btn btn-ghost" href="/admin/campaigns/edit?id=${x.id}">Sửa</a>
                    </div>
                </td>
            </tr>
        `
     }).join("")
}

function applyFilters() {
     const q = (qs("searchInput")?.value || "").trim().toLowerCase()
     const status = (qs("statusFilter")?.value || "").trim()
     state.filtered = state.all.filter(x => {
          const hit = !q || ((x.name || "").toLowerCase().includes(q) || (x.code || "").toLowerCase().includes(q))
          const okStatus = !status || x.status === status
          return hit && okStatus
     })
     state.page = 1
     renderListPage()
}

async function loadList() {
     const rows = qs("rows")
     if (rows) rows.innerHTML = `<tr><td colspan="8" class="muted">Đang tải...</td></tr>`
     const items = await api("/api/admin/campaigns", { method: "GET" })
     state.all = Array.isArray(items) ? items : []
     applyFilters()
}

function bindList() {
     const searchInput = qs("searchInput")
     const statusFilter = qs("statusFilter")
     if (searchInput) searchInput.addEventListener("input", applyFilters)
     if (statusFilter) statusFilter.addEventListener("change", applyFilters)
}

function renderScopeOptions(items, wrap) {
     if (!wrap) return
     wrap.innerHTML = items.map(x => {
          return `
            <label class="tc-scope-item">
                <input type="checkbox" value="${x.id}">
                <span>${x.name}</span>
            </label>
        `
     }).join("")
}

function buildScopePayload(scopeType) {
     if (scopeType === "ALL") return [{ scopeType: "ALL" }]
     if (scopeType === "CATEGORY") {
          const ids = qsa("#scopeCategories input:checked").map(i => Number(i.value))
          return ids.map(id => ({ scopeType: "CATEGORY", refId: id }))
     }
     if (scopeType === "TOUR") {
          const ids = qsa("#scopeTours input:checked").map(i => Number(i.value))
          return ids.map(id => ({ scopeType: "TOUR", refId: id }))
     }
     return [{ scopeType: "ALL" }]
}

function setSelectedScopes(scopes) {
     if (!scopes || !scopes.length) return
     const hasAll = scopes.some(s => s.scopeType === "ALL")
     const scopeType = hasAll ? "ALL" : (scopes.some(s => s.scopeType === "CATEGORY") ? "CATEGORY" : "TOUR")
     const scopeTypeEl = qs("scopeType")
     if (scopeTypeEl) scopeTypeEl.value = scopeType
     toggleScopeSections()

     if (scopeType === "CATEGORY") {
          const ids = scopes.filter(s => s.scopeType === "CATEGORY").map(s => String(s.refId))
          qsa("#scopeCategories input").forEach(i => { i.checked = ids.includes(i.value) })
     }
     if (scopeType === "TOUR") {
          const ids = scopes.filter(s => s.scopeType === "TOUR").map(s => String(s.refId))
          qsa("#scopeTours input").forEach(i => { i.checked = ids.includes(i.value) })
     }
}

function toggleScopeSections() {
     const scopeType = qs("scopeType")?.value || "ALL"
     const catWrap = qs("scopeCategoryWrap")
     const tourWrap = qs("scopeTourWrap")
     if (catWrap) catWrap.hidden = scopeType !== "CATEGORY"
     if (tourWrap) tourWrap.hidden = scopeType !== "TOUR"
}

async function loadScopeData() {
     const categories = await api("/api/admin/tour-categories", { method: "GET" })
     const tours = await api("/api/admin/tours", { method: "GET" })
     renderScopeOptions((categories || []).map(c => ({ id: c.id, name: c.name })), qs("scopeCategories"))
     renderScopeOptions((tours || []).map(t => ({ id: t.id, name: t.title })), qs("scopeTours"))
}

async function bindForm() {
     const id = qs("id")?.value
     const form = qs("form")
     const err = qs("err")
     const delBtn = qs("btnDelete")
     const name = qs("name")
     const slug = qs("slug")
     const code = qs("code")
     const status = qs("status")
     const startAt = qs("startAt")
     const endAt = qs("endAt")
     const discountType = qs("discountType")
     const discountValue = qs("discountValue")
     const minOrder = qs("minOrder")
     const maxDiscount = qs("maxDiscount")
     const usageLimit = qs("usageLimit")
     const perUserLimit = qs("perUserLimit")
     const bannerUrl = qs("bannerUrl")
     const bannerFile = qs("bannerFile")
     const bannerPreview = qs("bannerPreview")
     const bannerPreviewWrap = qs("bannerPreviewWrap")
     const btnChooseBanner = qs("btnChooseBanner")
     const btnRemoveBanner = qs("btnRemoveBanner")
     const description = qs("description")
     const scopeTypeEl = qs("scopeType")

     await loadScopeData()
     toggleScopeSections()
     if (scopeTypeEl) scopeTypeEl.addEventListener("change", toggleScopeSections)

     const setBanner = (url) => {
          if (bannerUrl) bannerUrl.value = url || ""
          if (bannerPreview) {
               bannerPreview.src = url || ""
               bannerPreview.style.display = url ? "block" : "none"
          }
          if (bannerPreviewWrap) {
               const empty = bannerPreviewWrap.querySelector(".tc-banner-empty")
               if (empty) empty.style.display = url ? "none" : "block"
          }
     }

     if (bannerUrl) {
          bannerUrl.addEventListener("input", () => setBanner(bannerUrl.value.trim()))
     }

     if (btnChooseBanner && bannerFile) {
          btnChooseBanner.addEventListener("click", () => bannerFile.click())
          bannerFile.addEventListener("change", async () => {
               if (!bannerFile.files || !bannerFile.files[0]) return
               try {
                    btnChooseBanner.disabled = true
                    if (btnRemoveBanner) btnRemoveBanner.disabled = true
                    const fd = new FormData()
                    fd.append("file", bannerFile.files[0])
                    if (name && name.value) fd.append("name", name.value.trim())
                    const res = await apiForm("/api/admin/uploads/campaigns/banner", fd)
                    setBanner(res ? res.url : "")
                    toast("ok", "Đã tải ảnh", "Banner đã cập nhật")
               } catch (e) {
                    toast("err", "Lỗi", e.message || "Upload thất bại")
               } finally {
                    btnChooseBanner.disabled = false
                    if (btnRemoveBanner) btnRemoveBanner.disabled = false
               }
          })
     }

     if (btnRemoveBanner) {
          btnRemoveBanner.addEventListener("click", () => setBanner(""))
     }

     if (name && slug) {
          name.addEventListener("blur", () => {
               if (!slug.value.trim()) slug.value = toSlug(name.value)
          })
     }
     if (code) {
          code.addEventListener("blur", () => { code.value = code.value.trim().toUpperCase() })
     }

     if (id) {
          try {
               const data = await api(`/api/admin/campaigns/${id}`, { method: "GET" })
               name.value = data.name || ""
               slug.value = data.slug || ""
               code.value = data.code || ""
               status.value = data.status || "SCHEDULE"
               startAt.value = data.startAt ? data.startAt.slice(0, 16) : ""
               endAt.value = data.endAt ? data.endAt.slice(0, 16) : ""
               discountType.value = data.discountType || "PERCENT"
               discountValue.value = data.discountValue || 0
               minOrder.value = data.minOrder || 0
               maxDiscount.value = data.maxDiscount || ""
               usageLimit.value = data.usageLimit || 0
               perUserLimit.value = data.perUserLimit || 1
               setBanner(data.bannerUrl || "")
               description.value = data.description || ""
               setSelectedScopes(data.scopes)
               if (delBtn) delBtn.style.display = "inline-flex"
          } catch (e) {
               if (err) { err.style.display = "block"; err.textContent = e.message || "Không thể tải dữ liệu" }
          }
     }

     if (delBtn) {
          delBtn.addEventListener("click", async () => {
               const ok = await modalConfirm("Xóa chiến dịch", "Bạn chắc chắn muốn xóa chiến dịch này?", "Xóa")
               if (!ok) return
               try {
                    await api(`/api/admin/campaigns/${id}`, { method: "DELETE" })
                    toast("ok", "Đã xóa", "Chiến dịch đã được xóa")
                    setTimeout(() => { window.location.href = "/admin/campaigns" }, 600)
               } catch (e) {
                    toast("err", "Xóa thất bại", e.message || "Không thể xóa")
               }
          })
     }

     if (form) {
          form.addEventListener("submit", async (e) => {
               e.preventDefault()
               if (err) { err.style.display = "none"; err.textContent = "" }
               const scopeType = scopeTypeEl ? scopeTypeEl.value : "ALL"
               if (scopeType === "CATEGORY" && qsa("#scopeCategories input:checked").length === 0) {
                    if (err) { err.style.display = "block"; err.textContent = "Vui lòng chọn ít nhất một danh mục" }
                    return
               }
               if (scopeType === "TOUR" && qsa("#scopeTours input:checked").length === 0) {
                    if (err) { err.style.display = "block"; err.textContent = "Vui lòng chọn ít nhất một tour" }
                    return
               }

               const payload = {
                    name: name.value.trim(),
                    slug: slug.value.trim(),
                    code: code.value.trim(),
                    status: status.value,
                    startAt: startAt.value ? `${startAt.value}:00` : null,
                    endAt: endAt.value ? `${endAt.value}:00` : null,
                    discountType: discountType.value,
                    discountValue: Number(discountValue.value || 0),
                    minOrder: Number(minOrder.value || 0),
                    maxDiscount: maxDiscount.value ? Number(maxDiscount.value) : null,
                    usageLimit: Number(usageLimit.value || 0),
                    perUserLimit: Number(perUserLimit.value || 1),
                    bannerUrl: bannerUrl.value.trim() || null,
                    description: description.value.trim() || null,
                    scopes: buildScopePayload(scopeType)
               }
               try {
                    if (id) {
                         await api(`/api/admin/campaigns/${id}`, { method: "PUT", body: JSON.stringify(payload) })
                         toast("ok", "Đã lưu", "Cập nhật chiến dịch thành công")
                    } else {
                         await api(`/api/admin/campaigns`, { method: "POST", body: JSON.stringify(payload) })
                         toast("ok", "Đã tạo", "Chiến dịch đã được tạo")
                    }
                    setTimeout(() => { window.location.href = "/admin/campaigns" }, 600)
               } catch (e2) {
                    if (err) { err.style.display = "block"; err.textContent = e2.message || "Không thể lưu" }
               }
          })
     }
}

document.addEventListener("DOMContentLoaded", () => {
     const page = getPage()
     if (page === "campaigns-list") {
          bindList()
          loadList().catch(e => toast("err", "Lỗi tải", e.message || ""))
     }
     if (page === "campaigns-form") {
          bindForm()
     }
})
