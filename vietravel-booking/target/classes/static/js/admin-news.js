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

     t.textContent = title || "Xac nhan"
     b.textContent = body || ""
     ok.textContent = okText || "Dong y"

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

     if (pageInfo) pageInfo.textContent = total ? `${start}-${end} / ${total}` : "0 ket qua"

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

function formatDate(raw) {
     if (!raw) return "--"
     const d = new Date(raw)
     if (Number.isNaN(d.getTime())) return "--"
     const pad = n => String(n).padStart(2, "0")
     return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`
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
          rows.innerHTML = `<tr><td colspan="7" class="muted">Khong co du lieu</td></tr>`
          return
     }

     rows.innerHTML = pageItems.map(x => {
          const statusBadge = {
               PUBLISHED: "badge badge-confirmed",
               DRAFT: "badge badge-pending",
               HIDDEN: "badge badge-canceled"
          }[x.status] || "badge badge-pending"
          const statusLabel = {
               PUBLISHED: "Da dang",
               DRAFT: "Ban nhap",
               HIDDEN: "An"
          }[x.status] || "--"
          const featuredLabel = x.isFeatured ? "Co" : "Khong"
          return `
            <tr>
                <td>${x.id}</td>
                <td>${x.title || ""}</td>
                <td><span class="${statusBadge}">${statusLabel}</span></td>
                <td>${featuredLabel}</td>
                <td>${x.viewCount || 0}</td>
                <td>${formatDate(x.createdAt)}</td>
                <td>
                    <div class="table-actions process-actions-row">
                        <a class="btn btn-ghost" href="/admin/news/edit?id=${x.id}">Sua</a>
                    </div>
                </td>
            </tr>
        `
     }).join("")
}

function applyFilters() {
     const q = (qs("searchInput")?.value || "").trim().toLowerCase()
     const status = (qs("statusFilter")?.value || "").trim()
     const featured = (qs("featuredFilter")?.value || "").trim()

     state.filtered = state.all.filter(x => {
          const hit = !q || ((x.title || "").toLowerCase().includes(q) || (x.slug || "").toLowerCase().includes(q))
          const okStatus = !status || x.status === status
          const okFeatured = !featured || String(!!x.isFeatured) === featured
          return hit && okStatus && okFeatured
     })
     state.page = 1
     renderListPage()
}

async function loadList() {
     const rows = qs("rows")
     if (rows) rows.innerHTML = `<tr><td colspan="7" class="muted">Dang tai...</td></tr>`
     const items = await api("/api/admin/news", { method: "GET" })
     state.all = Array.isArray(items) ? items : []
     applyFilters()
}

function bindList() {
     const searchInput = qs("searchInput")
     const statusFilter = qs("statusFilter")
     const featuredFilter = qs("featuredFilter")
     if (searchInput) searchInput.addEventListener("input", applyFilters)
     if (statusFilter) statusFilter.addEventListener("change", applyFilters)
     if (featuredFilter) featuredFilter.addEventListener("change", applyFilters)
}

function setThumbPreview(url) {
     const img = qs("thumbPreview")
     const empty = qs("thumbPreviewWrap")?.querySelector(".tc-banner-empty")
     if (!img) return
     if (url) {
          img.src = url
          img.style.display = "block"
          if (empty) empty.style.display = "none"
     } else {
          img.src = ""
          img.style.display = "none"
          if (empty) empty.style.display = "block"
     }
}

async function loadForm() {
     const form = qs("form")
     if (!form) return

     const idHidden = qs("id")
     const idParam = (idHidden && idHidden.value) ? idHidden.value : ""
     const isEdit = !!idParam
     const title = qs("title")
     const btnDelete = qs("btnDelete")
     const err = qs("err")

     if (isEdit) {
          try {
               const data = await api(`/api/admin/news/${idParam}`, { method: "GET" })
               if (title) title.textContent = "Cap nhat tin tuc"
               qs("titleInput").value = data.title || ""
               qs("slug").value = data.slug || ""
               qs("status").value = data.status || "PUBLISHED"
               qs("isFeatured").value = String(!!data.isFeatured)
               qs("thumbnail").value = data.thumbnail || ""
               qs("summary").value = data.summary || ""
               qs("contentHtml").value = data.contentHtml || ""
               qs("viewCount").value = data.viewCount || 0
               setThumbPreview(data.thumbnail)
               if (btnDelete) btnDelete.style.display = "inline-flex"
          } catch (e) {
               if (err) {
                    err.textContent = e.message || "Khong the tai du lieu"
                    err.style.display = "block"
               }
          }
     } else {
          qs("viewCount").value = 0
     }

     const titleInput = qs("titleInput")
     const slugInput = qs("slug")
     if (titleInput && slugInput) {
          titleInput.addEventListener("input", () => {
               if (!slugInput.value) slugInput.value = toSlug(titleInput.value)
          })
     }

     const thumbFile = qs("thumbFile")
     const btnChooseThumb = qs("btnChooseThumb")
     const btnRemoveThumb = qs("btnRemoveThumb")
     if (btnChooseThumb && thumbFile) {
          btnChooseThumb.addEventListener("click", () => thumbFile.click())
     }
     if (btnRemoveThumb) {
          btnRemoveThumb.addEventListener("click", () => {
               qs("thumbnail").value = ""
               setThumbPreview("")
          })
     }
     if (thumbFile) {
          thumbFile.addEventListener("change", async () => {
               const file = thumbFile.files && thumbFile.files[0]
               if (!file) return
               const fd = new FormData()
               fd.append("file", file)
               fd.append("name", qs("titleInput")?.value || "news")
               try {
                    const res = await apiForm("/api/admin/uploads/news/thumbnail", fd)
                    if (res && res.url) {
                         qs("thumbnail").value = res.url
                         setThumbPreview(res.url)
                    }
               } catch (e) {
                    toast("err", "Upload that bai", e.message || "Khong the upload")
               }
          })
     }

     if (btnDelete) {
          btnDelete.addEventListener("click", async () => {
               if (!isEdit) return
               const ok = await modalConfirm("Xoa tin tuc", "Ban chac chan muon xoa tin tuc nay?", "Xoa")
               if (!ok) return
               try {
                    await api(`/api/admin/news/${idParam}`, { method: "DELETE" })
                    toast("ok", "Da xoa", "Tin tuc da duoc xoa")
                    setTimeout(() => { window.location.href = "/admin/news" }, 800)
               } catch (e) {
                    toast("err", "Xoa that bai", e.message || "Khong the xoa")
               }
          })
     }

     form.addEventListener("submit", async (ev) => {
          ev.preventDefault()
          if (err) err.style.display = "none"

          const payload = {
               title: qs("titleInput").value.trim(),
               slug: qs("slug").value.trim(),
               thumbnail: qs("thumbnail").value.trim() || null,
               summary: qs("summary").value.trim() || null,
               contentHtml: qs("contentHtml").value.trim(),
               isFeatured: qs("isFeatured").value === "true",
               status: qs("status").value
          }

          try {
               if (isEdit) {
                    await api(`/api/admin/news/${idParam}`, { method: "PUT", body: JSON.stringify(payload) })
               } else {
                    await api("/api/admin/news", { method: "POST", body: JSON.stringify(payload) })
               }
               toast("ok", "Thanh cong", "Da luu tin tuc")
               setTimeout(() => { window.location.href = "/admin/news" }, 800)
          } catch (e) {
               if (err) {
                    err.textContent = e.message || "Khong the luu"
                    err.style.display = "block"
               }
               toast("err", "Loi", e.message || "Khong the luu")
          }
     })
}

document.addEventListener("DOMContentLoaded", () => {
     const page = getPage()
     if (page === "news-list") {
          bindList()
          loadList().catch(() => { })
     }
     if (page === "news-form") {
          loadForm().catch(() => { })
     }
})
