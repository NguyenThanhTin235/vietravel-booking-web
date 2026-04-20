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
     const wrap = qs("tourToastWrap")
     if (!wrap) return
     const el = document.createElement("div")
     el.className = "tour-toast " + (type === "err" ? "err" : "ok")
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

const tourState = {
     list: [],
     map: new Map(),
     selectedId: null
}

function getPage() {
     const el = document.querySelector("[data-page]")
     return el ? el.getAttribute("data-page") : ""
}

function fmtPrice(v) {
     if (v == null || v === "") return "--"
     try { return Number(v).toLocaleString("vi-VN") + " ₫" } catch (_) { return v }
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

async function loadCategories(selectEl, includeEmpty) {
     if (!selectEl) return
     const items = await api("/api/admin/tour-categories", { method: "GET" })
     const opts = items
          .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
          .map(x => `<option value="${x.id}">${x.name}${x.parentName ? " • (" + x.parentName + ")" : ""}</option>`)
          .join("")
     selectEl.innerHTML = (includeEmpty ? "<option value=\"\">Tất cả danh mục</option>" : "") + opts
}

async function loadTourLines(selectEl) {
     if (!selectEl) return
     const items = await api("/api/admin/tour-lines", { method: "GET" })
     const opts = items
          .filter(x => x.isActive !== false)
          .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
          .map(x => `<option value="${x.id}">${x.name}</option>`)
          .join("")
     selectEl.innerHTML = `<option value="">-- Chọn --</option>` + opts
}

async function resolveTourLineByPrice(price) {
     if (price == null || price === "") return null
     try {
          return await api(`/api/admin/tour-lines/resolve?price=${price}`, { method: "GET" })
     } catch (_) {
          return null
     }
}

async function loadTransportModes(selectEl) {
     if (!selectEl) return
     const items = await api("/api/admin/transport-modes", { method: "GET" })
     const opts = items
          .filter(x => x.isActive !== false)
          .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
          .map(x => `<option value="${x.id}">${x.name}</option>`)
          .join("")
     selectEl.innerHTML = `<option value="">-- Chọn --</option>` + opts
}

async function loadDestinations(selectEl) {
     if (!selectEl) return
     const items = await api("/api/admin/destinations", { method: "GET" })
     const opts = items
          .filter(x => x.isActive !== false)
          .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
          .map(x => `<option value="${x.id}">${x.name}</option>`)
          .join("")
     selectEl.innerHTML = opts
}

async function loadStartLocations(selectEl) {
     if (!selectEl) return
     const items = await api("/api/admin/destinations", { method: "GET" })
     const normalize = (v) => {
          if (!v) return ""
          return v.trim().toLowerCase()
               .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
               .replace(/đ/g, "d")
               .replace(/[^a-z0-9]+/g, " ")
               .replace(/\s+/g, " ")
               .trim()
     }

     const cityMatchers = {
          "Hà Nội": new Set(["ha noi", "hn", "hanoi"]),
          "Đà Nẵng": new Set(["da nang", "danang"]),
          "Hồ Chí Minh": new Set([
               "ho chi minh",
               "tp hcm",
               "tp ho chi minh",
               "hcm",
               "sai gon",
               "saigon"
          ])
     }

     const pickByCanonical = (label) => {
          const matchers = cityMatchers[label]
          if (!matchers) return null
          const exact = items.find(x => x && x.name === label)
          if (exact) return exact
          return items.find(x => matchers.has(normalize(x && x.name))) || null
     }

     const selected = [
          pickByCanonical("Đà Nẵng"),
          pickByCanonical("Hà Nội"),
          pickByCanonical("Hồ Chí Minh")
     ].filter(Boolean)

     const seen = new Set()
     const opts = selected
          .filter(x => !seen.has(x.id) && seen.add(x.id))
          .map(x => {
               const label = Object.keys(cityMatchers).find(k => cityMatchers[k].has(normalize(x.name))) || x.name
               return `<option value="${x.id}">${label}</option>`
          })
          .join("")
     selectEl.innerHTML = `<option value="">-- Chọn --</option>` + opts
}

function renderTourCard(t) {
     const cats = (t.categories || []).slice(0, 2).map(c => `<span class="tour-tag">${c}</span>`).join("")
     const status = t.isActive ? "Đang hiển thị" : "Đã ẩn"
     const statusClass = t.isActive ? "badge-confirmed" : "badge-canceled"
     const thumb = t.thumbnailUrl || "/images/auth/placeholder.jpg"
     const duration = `${t.durationDays || 0} ngày / ${t.durationNights || 0} đêm`
     const location = (t.destinationNames && t.destinationNames.length)
          ? t.destinationNames.join(" - ")
          : ""
     return `
        <div class="tour-card-item" data-id="${t.id}">
            <div class="tour-card-thumb"><img src="${thumb}" alt="thumb"></div>
            <div class="tour-card-body">
                    <div class="tour-card-title">${t.title || ""}</div>
                    <div class="tour-card-meta">
                         <span>📍 ${location || "--"}</span>
                         <span>🗓 ${duration}</span>
                    </div>
                    <div class="tour-card-meta">
                         <span class="tour-card-price">${fmtPrice(t.basePrice)} <small>/người</small></span>
                    </div>
                    <div class="tour-card-tags">${cats}</div>
                    <div class="tour-card-meta">
                         <span class="badge ${statusClass}">${status}</span>
                    </div>
            </div>
        </div>
    `
}

function renderTourDetail(detail, listItem) {
     const status = listItem && listItem.isActive ? "Đang hiển thị" : "Đã ẩn"
     const statusClass = listItem && listItem.isActive ? "badge-confirmed" : "badge-canceled"
     const thumb = (listItem && listItem.thumbnailUrl) || "/images/auth/placeholder.jpg"
     const cats = (listItem && listItem.categories) ? listItem.categories.map(c => `<span class="tour-tag">${c}</span>`).join("") : ""
     const overviewHtml = detail && detail.overviewHtml ? detail.overviewHtml : (detail && detail.additionalInfoHtml ? detail.additionalInfoHtml : "")
     const notesHtml = detail && detail.notesHtml ? detail.notesHtml : ""
     const location = (detail && detail.destinationNames && detail.destinationNames.length)
          ? detail.destinationNames.join(" - ")
          : "--"
     const startLocation = (detail && detail.startLocationName)
          ? detail.startLocationName
          : "--"
     const durationLabel = `${detail.durationDays || 0} ngày / ${detail.durationNights || 0} đêm`
     return `
        <div class="tour-detail-hero"><img src="${thumb}" alt="thumb"></div>
        <div class="tour-detail-head">
             <div>
                  <h3 class="tour-detail-title">${detail.title || ""}</h3>
             </div>
             <div class="tour-detail-head-actions">
                  <a class="btn tour-edit-btn" href="/admin/tours/edit?id=${detail.id}">Sửa tour</a>
             </div>
        </div>
        <div class="tour-detail-tags">${cats}</div>
        <div class="tour-detail-info">
             <div class="tour-info-item">
                  <span class="tour-info-ic">📍</span>
                  <div>
                       <div class="tour-info-label">Địa điểm</div>
                       <div class="tour-info-value">${location}</div>
                  </div>
             </div>
             <div class="tour-info-item">
                  <span class="tour-info-ic">🚩</span>
                  <div>
                       <div class="tour-info-label">Điểm khởi hành</div>
                       <div class="tour-info-value">${startLocation}</div>
                  </div>
             </div>
             <div class="tour-info-item">
                  <span class="tour-info-ic">🗓</span>
                  <div>
                       <div class="tour-info-label">Thời lượng</div>
                       <div class="tour-info-value">${durationLabel}</div>
                  </div>
             </div>
             <div class="tour-info-item">
                  <span class="tour-info-ic">💰</span>
                  <div>
                       <div class="tour-info-label">Giá</div>
                       <div class="tour-info-value price">${fmtPrice(detail.basePrice)} / người</div>
                  </div>
             </div>
             <div class="tour-info-item">
                  <span class="tour-info-ic">🧭</span>
                  <div>
                       <div class="tour-info-label">Dòng tour</div>
                       <div class="tour-info-value">${listItem && listItem.tourLineName ? listItem.tourLineName : "--"}</div>
                  </div>
             </div>
             <div class="tour-info-item">
                  <span class="tour-info-ic">🚌</span>
                  <div>
                       <div class="tour-info-label">Phương tiện</div>
                       <div class="tour-info-value">${listItem && listItem.transportModeName ? listItem.transportModeName : "--"}</div>
                  </div>
             </div>
        </div>
        <div class="tour-detail-block">
             <div class="tour-detail-block-title">Giới thiệu</div>
             <div class="tour-detail-rich">${overviewHtml || "<div class=\"tour-detail-empty-small\">Chưa có nội dung giới thiệu</div>"}</div>
        </div>
        <div class="tour-detail-block">
             <div class="tour-detail-block-title">Lưu ý</div>
             <div class="tour-detail-rich">${notesHtml || "<div class=\"tour-detail-empty-small\">Chưa có lưu ý</div>"}</div>
        </div>
        <div class="tour-detail-actions">
            <button class="btn" type="button" data-detail-toggle="${detail.id}">${listItem && listItem.isActive ? "Ẩn tour" : "Hiện tour"}</button>
            <span class="badge ${statusClass}">${status}</span>
        </div>
    `
}

function renderTourSchedule(detail) {
     const days = (detail && detail.itineraryDays) ? detail.itineraryDays : []
     if (!days.length) {
          return "<div class=\"tour-detail-empty\">Chưa có lịch trình</div>"
     }
     const ordered = days.slice().sort((a, b) => (a.dayNo || 0) - (b.dayNo || 0))
     return `
          <div class="tour-schedule-list">
               ${ordered.map(d => `
                    <div class="tour-schedule-item">
                         <div class="tour-schedule-day">Ngày ${d.dayNo || 1}</div>
                         <div class="tour-schedule-content">
                              <h5>${d.titleRoute || "Chưa cập nhật"}</h5>
                              <p>${d.meals ? "Bữa ăn: " + d.meals : ""}</p>
                         </div>
                    </div>
               `).join("")}
          </div>
     `
}

async function loadTourDetail(id) {
     const detailEl = qs("tourDetailMain")
     const scheduleEl = qs("tourDetailSchedule")
     if (!detailEl) return
     detailEl.classList.add("tour-detail-empty")
     detailEl.innerHTML = "<div class=\"tour-detail-empty\">Đang tải chi tiết...</div>"
     if (scheduleEl) {
          scheduleEl.classList.add("tour-detail-empty")
          scheduleEl.innerHTML = "<div class=\"tour-detail-empty\">Đang tải lịch trình...</div>"
     }
     try {
          const detail = await api(`/api/admin/tours/${id}`)
          const listItem = tourState.map.get(Number(id))
          detailEl.classList.remove("tour-detail-empty")
          detailEl.innerHTML = renderTourDetail(detail, listItem)
          if (scheduleEl) {
               scheduleEl.classList.remove("tour-detail-empty")
               scheduleEl.innerHTML = renderTourSchedule(detail)
          }
     } catch (e) {
          detailEl.innerHTML = "<div class=\"tour-detail-empty\">Không thể tải chi tiết tour</div>"
          if (scheduleEl) scheduleEl.innerHTML = "<div class=\"tour-detail-empty\">Không thể tải lịch trình</div>"
     }
}

async function loadTourList() {
     const filterCategory = qs("filterCategory")
     const filterStatus = qs("filterStatus")
     const filterQ = qs("filterQ")
     const msg = qs("tourMsg")
     const listEl = qs("tourList")
     const miniEl = qs("tourMiniList")

     if (!listEl) return

     const categoryId = filterCategory ? filterCategory.value : ""
     const active = filterStatus ? filterStatus.value : ""
     const q = filterQ ? filterQ.value.trim() : ""

     const params = new URLSearchParams()
     if (categoryId) params.set("categoryId", categoryId)
     if (active != "") params.set("active", active)
     if (q) params.set("q", q)

     try {
          const data = await api(`/api/admin/tours?${params.toString()}`)
          tourState.list = data
          tourState.map = new Map(data.map(t => [Number(t.id), t]))
          if (msg) msg.textContent = data.length ? `Có ${data.length} tour phù hợp` : "Không có tour nào"
          listEl.innerHTML = data.map(renderTourCard).join("")

          if (miniEl) miniEl.innerHTML = ""

          if (data.length > 0) {
               if (!tourState.selectedId || !tourState.map.has(Number(tourState.selectedId))) {
                    tourState.selectedId = data[0].id
               }
               highlightSelected(tourState.selectedId)
               await loadTourDetail(tourState.selectedId)
          } else {
               tourState.selectedId = null
               const detailEl = qs("tourDetailMain")
               const scheduleEl = qs("tourDetailSchedule")
               if (detailEl) {
                    detailEl.classList.add("tour-detail-empty")
                    detailEl.innerHTML = "<div class=\"tour-detail-empty\">Chưa có tour để hiển thị</div>"
               }
               if (scheduleEl) {
                    scheduleEl.classList.add("tour-detail-empty")
                    scheduleEl.innerHTML = "<div class=\"tour-detail-empty\">Chưa có lịch trình</div>"
               }
          }
     } catch (e) {
          if (msg) msg.textContent = "Không thể tải danh sách"
          listEl.innerHTML = ""
          toast("err", "Lỗi", e.message || "Không thể tải danh sách tour")
     }
}

function highlightSelected(id) {
     const listEl = qs("tourList")
     if (!listEl) return
     qsa(".tour-card-item", listEl).forEach(card => {
          card.classList.toggle("active", String(card.getAttribute("data-id")) === String(id))
     })
}

function setLoading(el, isLoading) {
     if (!el) return
     el.classList.toggle("is-loading", !!isLoading)
}

function initEditors() {
     const toolbars = qsa("[data-editor-tools]")
     toolbars.forEach(tb => {
          tb.innerHTML = `
            <button type="button" data-cmd="bold"><b>B</b></button>
            <button type="button" data-cmd="italic"><i>I</i></button>
            <button type="button" data-cmd="underline"><u>U</u></button>
            <button type="button" data-cmd="insertUnorderedList">• List</button>
            <button type="button" data-cmd="insertOrderedList">1. List</button>
            <button type="button" data-cmd="createLink">Link</button>
            <button type="button" data-cmd="removeFormat">Clear</button>
        `
     })

     document.addEventListener("click", e => {
          const btn = e.target.closest("[data-cmd]")
          if (!btn) return
          const cmd = btn.getAttribute("data-cmd")
          if (cmd === "createLink") {
               const url = prompt("Nhập URL")
               if (url) document.execCommand(cmd, false, url)
               return
          }
          document.execCommand(cmd, false, null)
     })
}

function renderItineraryItem(day) {
     const idx = day.dayNo || 1
     return `
        <div class="itinerary-item" data-day="${idx}">
            <div class="itinerary-item-head">
                <div class="itinerary-title">Ngày ${idx}</div>
                <div class="itinerary-actions">
                    <button class="btn" type="button" data-day-up>↑</button>
                    <button class="btn" type="button" data-day-down>↓</button>
                    <button class="btn" type="button" data-day-remove>×</button>
                </div>
            </div>
            <div class="itinerary-grid">
                <div class="tour-field">
                    <label>Tiêu đề tuyến</label>
                    <input class="input" data-day-title value="${day.titleRoute || ""}" placeholder="VD: Hà Nội - Hạ Long">
                </div>
                <div class="tour-field">
                    <label>Bữa ăn</label>
                    <input class="input" data-day-meals value="${day.meals || ""}" placeholder="SÁNG,TRƯA,TỐI">
                </div>
            </div>
            <div class="tour-editor">
                <div class="editor-head"><span>Nội dung</span></div>
                <div class="editor-body" contenteditable="true" data-day-content>${day.contentHtml || ""}</div>
            </div>
        </div>
    `
}

function syncItineraryNumbers() {
     const items = qsa(".itinerary-item")
     items.forEach((item, i) => {
          const n = i + 1
          item.setAttribute("data-day", n)
          const title = item.querySelector(".itinerary-title")
          if (title) title.textContent = `Ngày ${n}`
     })
}

function bindItineraryActions() {
     const list = qs("itineraryList")
     if (!list) return

     list.addEventListener("click", e => {
          const item = e.target.closest(".itinerary-item")
          if (!item) return
          if (e.target.closest("[data-day-remove]")) {
               item.remove()
               syncItineraryNumbers()
          }
          if (e.target.closest("[data-day-up]")) {
               const prev = item.previousElementSibling
               if (prev) list.insertBefore(item, prev)
               syncItineraryNumbers()
          }
          if (e.target.closest("[data-day-down]")) {
               const next = item.nextElementSibling
               if (next) list.insertBefore(next, item)
               syncItineraryNumbers()
          }
     })
}

function getItineraryPayload() {
     const items = qsa(".itinerary-item")
     return items.map((item, idx) => ({
          dayNo: idx + 1,
          titleRoute: item.querySelector("[data-day-title]").value.trim(),
          meals: item.querySelector("[data-day-meals]").value.trim(),
          contentHtml: item.querySelector("[data-day-content]").innerHTML,
          sortOrder: idx
     }))
}

function renderGallery(urls) {
     const gallery = qs("gallery")
     if (!gallery) return
     gallery.innerHTML = urls.map((url, idx) => `
        <div class="tour-gallery-item" data-idx="${idx}">
            <img src="${url}" alt="gallery">
            <button type="button" data-remove>×</button>
        </div>
    `).join("")
}

function getGalleryUrls() {
     const val = qs("galleryUrls")
     if (!val || !val.value) return []
     try { return JSON.parse(val.value) || [] } catch (_) { return [] }
}

function setGalleryUrls(arr) {
     const val = qs("galleryUrls")
     if (val) val.value = JSON.stringify(arr)
     renderGallery(arr)
}

function bindGalleryRemove() {
     const gallery = qs("gallery")
     if (!gallery) return
     gallery.addEventListener("click", e => {
          const btn = e.target.closest("[data-remove]")
          if (!btn) return
          const item = btn.closest(".tour-gallery-item")
          if (!item) return
          const idx = Number(item.getAttribute("data-idx"))
          const arr = getGalleryUrls()
          arr.splice(idx, 1)
          setGalleryUrls(arr)
     })
}

async function uploadFile(file, name) {
     const fd = new FormData()
     fd.append("file", file)
     if (name) fd.append("name", name)
     const res = await apiForm("/api/admin/uploads/tours/images", fd)
     return res ? res.url : null
}

async function initTourForm() {
     const idHidden = qs("tourId")
     const id = idHidden && idHidden.value ? idHidden.value : ""
     const isEdit = !!id

     const titleEl = qs("tourFormTitle")
     if (titleEl) titleEl.textContent = isEdit ? "Cập nhật tour" : "Thêm tour"

     await Promise.all([
          loadCategories(qs("categoryIds")),
          loadCategories(qs("filterCategory"), true),
          loadTransportModes(qs("transportModeId")),
          loadDestinations(qs("destinationIds")),
          loadStartLocations(qs("startLocationId"))
     ])

     initEditors()
     bindItineraryActions()
     bindGalleryRemove()

     const thumbBox = qs("thumbBox")
     const thumbPreview = qs("thumbPreview")
     const thumbEmpty = thumbBox ? thumbBox.querySelector(".tour-image-empty") : null

     const setThumb = (url) => {
          const hidden = qs("thumbnailUrl")
          if (hidden) hidden.value = url || ""
          if (thumbPreview) {
               thumbPreview.src = url || ""
               thumbPreview.style.display = url ? "block" : "none"
          }
          if (thumbEmpty) thumbEmpty.style.display = url ? "none" : "block"
     }

     const btnChooseThumb = qs("btnChooseThumb")
     const thumbFile = qs("thumbFile")
     if (btnChooseThumb && thumbFile) {
          btnChooseThumb.addEventListener("click", () => thumbFile.click())
          thumbFile.addEventListener("change", async () => {
               if (!thumbFile.files || !thumbFile.files[0]) return
               try {
                    setLoading(thumbBox, true)
                    btnChooseThumb.disabled = true
                    if (btnRemoveThumb) btnRemoveThumb.disabled = true
                    const url = await uploadFile(thumbFile.files[0], "thumbnail")
                    setThumb(url)
                    toast("ok", "Đã tải ảnh", "Ảnh đại diện đã cập nhật")
               } catch (e) {
                    toast("err", "Lỗi", e.message || "Upload thất bại")
               } finally {
                    setLoading(thumbBox, false)
                    btnChooseThumb.disabled = false
                    if (btnRemoveThumb) btnRemoveThumb.disabled = false
               }
          })
     }
     const btnRemoveThumb = qs("btnRemoveThumb")
     if (btnRemoveThumb) {
          btnRemoveThumb.addEventListener("click", () => setThumb(""))
     }

     const btnChooseGallery = qs("btnChooseGallery")
     const galleryFiles = qs("galleryFiles")
     if (btnChooseGallery && galleryFiles) {
          btnChooseGallery.addEventListener("click", () => galleryFiles.click())
          galleryFiles.addEventListener("change", async () => {
               if (!galleryFiles.files || galleryFiles.files.length === 0) return
               const arr = getGalleryUrls()
               const gallery = qs("gallery")
               setLoading(gallery, true)
               btnChooseGallery.disabled = true
               for (const f of Array.from(galleryFiles.files)) {
                    try {
                         const placeholder = document.createElement("div")
                         placeholder.className = "tour-gallery-item loading"
                         if (gallery) gallery.appendChild(placeholder)
                         const url = await uploadFile(f, "gallery")
                         if (url) arr.push(url)
                         if (placeholder) placeholder.remove()
                    } catch (e) {
                         toast("err", "Lỗi", e.message || "Upload thất bại")
                    }
               }
               setGalleryUrls(arr)
               setLoading(gallery, false)
               btnChooseGallery.disabled = false
          })
     }

     const addDay = () => {
          const list = qs("itineraryList")
          if (!list) return
          const next = list.children.length + 1
          list.insertAdjacentHTML("beforeend", renderItineraryItem({ dayNo: next }))
          syncItineraryNumbers()
     }

     const btnAddDay = qs("btnAddDay")
     if (btnAddDay) btnAddDay.addEventListener("click", addDay)

     if (isEdit) {
          try {
               const data = await api(`/api/admin/tours/${id}`)
               qs("code").value = data.code || ""
               qs("title").value = data.title || ""
               qs("slug").value = data.slug || ""
               qs("durationDays").value = data.durationDays || 1
               qs("durationNights").value = data.durationNights || 0
               qs("basePrice").value = data.basePrice || ""
               qs("overviewHtml").innerHTML = data.overviewHtml || ""
               qs("additionalInfoHtml").innerHTML = data.additionalInfoHtml || ""
               qs("notesHtml").innerHTML = data.notesHtml || ""
               qs("isActive").checked = data.isActive !== false

               if (data.tourLineId) {
                    qs("tourLineId").value = String(data.tourLineId)
                    const line = await api(`/api/admin/tour-lines/${data.tourLineId}`, { method: "GET" })
                    if (line && qs("tourLineName")) qs("tourLineName").value = line.name || ""
               }
               if (data.transportModeId) qs("transportModeId").value = String(data.transportModeId)
               if (data.startLocationId) qs("startLocationId").value = String(data.startLocationId)

               if (data.categoryIds) {
                    qsa("option", qs("categoryIds")).forEach(o => {
                         if (data.categoryIds.includes(Number(o.value))) o.selected = true
                    })
               }
               if (data.destinationIds) {
                    qsa("option", qs("destinationIds")).forEach(o => {
                         if (data.destinationIds.includes(Number(o.value))) o.selected = true
                    })
               }

               const images = data.images || []
               const thumb = images.find(i => i.isThumbnail) || images[0]
               setThumb(thumb ? thumb.url : "")
               setGalleryUrls(images.filter(i => !i.isThumbnail).map(i => i.url))

               const list = qs("itineraryList")
               if (list) {
                    list.innerHTML = ""
                    const days = (data.itineraryDays || []).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
                    if (days.length === 0) addDay()
                    else days.forEach(d => { list.insertAdjacentHTML("beforeend", renderItineraryItem(d)) })
                    syncItineraryNumbers()
               }
          } catch (e) {
               toast("err", "Lỗi", e.message || "Không thể tải dữ liệu")
          }
     } else {
          const list = qs("itineraryList")
          if (list && list.children.length === 0) addDay()
     }

     const priceInput = qs("basePrice")
     const tourLineIdInput = qs("tourLineId")
     const tourLineNameInput = qs("tourLineName")
     const setTourLine = (line) => {
          if (tourLineIdInput) tourLineIdInput.value = line && line.id ? String(line.id) : ""
          if (tourLineNameInput) tourLineNameInput.value = line && line.name ? line.name : ""
     }
     const autoResolve = debounce(async () => {
          const price = priceInput && priceInput.value ? Number(priceInput.value) : null
          if (!price || price <= 0) { setTourLine(null); return }
          const line = await resolveTourLineByPrice(price)
          setTourLine(line)
     }, 250)
     if (priceInput) priceInput.addEventListener("input", autoResolve)

     const submit = async () => {
          const err = qs("tourErr")
          const setErr = (m) => { if (err) { err.style.display = "block"; err.textContent = m || "" } }
          const clearErr = () => { if (err) { err.style.display = "none"; err.textContent = "" } }

          clearErr()
          const payload = {
               code: qs("code").value.trim(),
               title: qs("title").value.trim(),
               slug: qs("slug").value.trim(),
               tourLineId: qs("tourLineId").value ? Number(qs("tourLineId").value) : null,
               transportModeId: qs("transportModeId").value ? Number(qs("transportModeId").value) : null,
               durationDays: Number(qs("durationDays").value || 0),
               durationNights: Number(qs("durationNights").value || 0),
               startLocationId: qs("startLocationId").value ? Number(qs("startLocationId").value) : null,
               basePrice: qs("basePrice").value ? Number(qs("basePrice").value) : null,
               overviewHtml: qs("overviewHtml").innerHTML,
               additionalInfoHtml: qs("additionalInfoHtml").innerHTML,
               notesHtml: qs("notesHtml").innerHTML,
               isActive: qs("isActive").checked,
               categoryIds: qsa("option", qs("categoryIds")).filter(o => o.selected).map(o => Number(o.value)),
               destinationIds: qsa("option", qs("destinationIds")).filter(o => o.selected).map(o => Number(o.value)),
               images: [],
               itineraryDays: getItineraryPayload()
          }

          const thumbUrl = qs("thumbnailUrl").value
          const galleryUrls = getGalleryUrls()
          if (thumbUrl) {
               payload.images.push({ url: thumbUrl, isThumbnail: true, sortOrder: 0 })
          }
          galleryUrls.forEach((u, idx) => {
               payload.images.push({ url: u, isThumbnail: false, sortOrder: idx + 1 })
          })

          if (!payload.title) { setErr("Tiêu đề không được rỗng"); return }
          if (!payload.tourLineId) { setErr("Vui lòng chọn dòng tour"); return }
          if (!payload.transportModeId) { setErr("Vui lòng chọn phương tiện"); return }

          try {
               if (isEdit) {
                    await api(`/api/admin/tours/${id}`, { method: "PUT", body: JSON.stringify(payload) })
                    toast("ok", "Đã cập nhật", "Tour đã được lưu")
               } else {
                    await api(`/api/admin/tours`, { method: "POST", body: JSON.stringify(payload) })
                    toast("ok", "Đã tạo", "Tour đã được tạo")
               }
               setTimeout(() => { window.location.href = "/admin/tours" }, 450)
          } catch (e) {
               setErr(e.message || "Lưu thất bại")
               toast("err", "Lỗi", e.message || "Không thể lưu")
          }
     }

     const btnSave = qs("btnSave")
     if (btnSave) btnSave.addEventListener("click", submit)

     const titleInput = qs("title")
     const slugInput = qs("slug")
     if (titleInput && slugInput) {
          titleInput.addEventListener("input", debounce(() => {
               if (!slugInput.value.trim()) slugInput.value = toSlug(titleInput.value)
          }, 200))
     }
}

async function initTourList() {
     await loadCategories(qs("filterCategory"), true)
     await loadTourList()

     const filterCategory = qs("filterCategory")
     const filterStatus = qs("filterStatus")
     const filterQ = qs("filterQ")
     const btnFilter = qs("btnFilter")
     const filterMenu = qs("tourFilterMenu")
     const btnApply = qs("btnApplyFilter")
     const btnReset = qs("btnResetFilter")

     const reload = debounce(loadTourList, 250)
     if (filterQ) filterQ.addEventListener("input", reload)
     if (btnApply) btnApply.addEventListener("click", () => loadTourList())
     if (btnReset) btnReset.addEventListener("click", () => {
          if (filterCategory) filterCategory.value = ""
          if (filterStatus) filterStatus.value = ""
          if (filterQ) filterQ.value = ""
          loadTourList()
     })

     if (btnFilter && filterMenu) {
          btnFilter.addEventListener("click", (e) => {
               e.stopPropagation()
               filterMenu.classList.toggle("show")
          })
          document.addEventListener("click", (e) => {
               if (!filterMenu.contains(e.target) && !btnFilter.contains(e.target)) {
                    filterMenu.classList.remove("show")
               }
          })
     }

     const listEl = qs("tourList")
     if (listEl) {
          listEl.addEventListener("click", async (e) => {
               const toggleBtn = e.target.closest("[data-toggle-id]")
               if (toggleBtn) {
                    e.preventDefault()
                    e.stopPropagation()
                    const id = toggleBtn.getAttribute("data-toggle-id")
                    try {
                         await api(`/api/admin/tours/${id}/toggle`, { method: "PATCH" })
                         toast("ok", "Thành công", "Đã cập nhật trạng thái tour")
                         await loadTourList()
                    } catch (e) {
                         toast("err", "Lỗi", e.message || "Không thể cập nhật")
                    }
                    return
               }

               const card = e.target.closest(".tour-card-item")
               if (!card) return
               const id = card.getAttribute("data-id")
               tourState.selectedId = id
               highlightSelected(id)
               await loadTourDetail(id)
          })
     }

     const detailEl = qs("tourDetailMain")
     if (detailEl) {
          detailEl.addEventListener("click", async (e) => {
               const btn = e.target.closest("[data-detail-toggle]")
               if (!btn) return
               const id = btn.getAttribute("data-detail-toggle")
               try {
                    await api(`/api/admin/tours/${id}/toggle`, { method: "PATCH" })
                    toast("ok", "Thành công", "Đã cập nhật trạng thái tour")
                    await loadTourList()
               } catch (err) {
                    toast("err", "Lỗi", err.message || "Không thể cập nhật")
               }
          })
     }
}

(function () {
     const page = getPage()
     if (page === "tours-index") initTourList()
     if (page === "tours-form") initTourForm()
})();
