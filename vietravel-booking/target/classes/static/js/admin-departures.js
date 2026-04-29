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

function toast(msg, type) {
     if (window.AdminUI && typeof window.AdminUI.toast === "function") {
          window.AdminUI.toast(msg, type || "info")
          return
     }
     alert(msg)
}

const START_LOCATIONS = [
     { code: "HN", name: "Hà Nội" },
     { code: "DN", name: "Đà Nẵng" },
     { code: "HCM", name: "Hồ Chí Minh" }
]

const depState = {
     tours: [],
     tourMap: new Map(),
     startLocations: [],
     selectedTourId: null,
     departures: [],
     selectedDepartureId: null,
     selectedDate: null,
     formTourId: null,
     year: new Date().getFullYear(),
     month: new Date().getMonth() + 1
}

function fmtMonthLabel(year, month) {
     const d = new Date(year, month - 1, 1)
     return `Tháng ${d.getMonth() + 1} / ${d.getFullYear()}`
}

function toISODate(d) {
     const y = d.getFullYear()
     const m = String(d.getMonth() + 1).padStart(2, "0")
     const day = String(d.getDate()).padStart(2, "0")
     return `${y}-${m}-${day}`
}

function normalizePrice(v) {
     if (v == null) return ""
     return Number(v).toString()
}

async function loadTours() {
     const list = await api("/api/admin/tours")
     depState.tours = Array.isArray(list) ? list : []
     depState.tourMap = new Map(depState.tours.map(t => [String(t.id), t]))

     const select = qs("depTourSelect")
     if (!select) return
     select.innerHTML = ""
     const placeholder = document.createElement("option")
     placeholder.value = ""
     placeholder.textContent = "Chọn tour..."
     select.appendChild(placeholder)

     depState.tours.forEach(t => {
          const opt = document.createElement("option")
          opt.value = t.id
          opt.textContent = `${t.title || ""}`
          select.appendChild(opt)
     })

     if (depState.selectedTourId) {
          select.value = depState.selectedTourId
     }
}

function loadStartLocations() {
     depState.startLocations = START_LOCATIONS.slice()
     renderStartLocations()
}

function renderStartLocations() {
     const select = qs("depStartLocation")
     if (!select) return
     select.innerHTML = ""
     depState.startLocations.forEach(loc => {
          const opt = document.createElement("option")
          opt.value = loc.code
          opt.textContent = loc.name
          select.appendChild(opt)
     })
}

async function loadDepartures() {
     const { year, month } = depState
     const tourQuery = depState.selectedTourId ? `tourId=${depState.selectedTourId}&` : ""
     const list = await api(`/api/admin/departures?${tourQuery}year=${year}&month=${month}`)
     depState.departures = Array.isArray(list) ? list : []
     renderCalendar()
     renderDetail()
}

function renderCalendar() {
     const grid = qs("depCalendarGrid")
     const label = qs("depMonthLabel")
     if (!grid || !label) return

     label.textContent = fmtMonthLabel(depState.year, depState.month)
     grid.innerHTML = ""

     const year = depState.year
     const month = depState.month
     const firstDay = new Date(year, month - 1, 1)
     const startWeekday = firstDay.getDay()
     const daysInMonth = new Date(year, month, 0).getDate()
     const totalCells = Math.ceil((startWeekday + daysInMonth) / 7) * 7

     const depByDate = new Map()
     depState.departures.forEach(d => {
          if (!d.startDate) return
          if (!depByDate.has(d.startDate)) depByDate.set(d.startDate, [])
          depByDate.get(d.startDate).push(d)
     })

     for (let i = 0; i < totalCells; i++) {
          const dayOffset = i - startWeekday + 1
          const date = new Date(year, month - 1, dayOffset)
          const inMonth = dayOffset >= 1 && dayOffset <= daysInMonth
          const dateStr = toISODate(date)

          const cell = document.createElement("div")
          cell.className = "dep-day" + (inMonth ? "" : " muted")
          if (depState.selectedDate === dateStr) cell.classList.add("selected")
          cell.dataset.date = dateStr

          const num = document.createElement("div")
          num.className = "dep-day-num"
          num.textContent = String(date.getDate())
          cell.appendChild(num)

          const items = document.createElement("div")
          items.className = "dep-day-items"

          const list = depByDate.get(dateStr) || []
          list.slice(0, 3).forEach(dep => {
               const pill = document.createElement("div")
               const completed = dep.completed
               const closed = dep.status === "CLOSED"
               pill.className = "dep-pill" + (completed ? " completed" : (closed ? " closed" : ""))
               const showTour = !depState.selectedTourId
               const tourLabel = dep.tourTitle || "Tour"
               const locLabel = dep.startLocationName || "Khởi hành"
               pill.textContent = showTour ? `${tourLabel} • ${locLabel}` : locLabel
               pill.dataset.id = dep.id
               pill.addEventListener("click", (e) => {
                    e.stopPropagation()
                    depState.selectedDepartureId = dep.id
                    depState.selectedDate = dep.startDate
                    renderCalendar()
                    renderDetail()
               })
               items.appendChild(pill)
          })

          if (list.length > 3) {
               const more = document.createElement("div")
               more.className = "dep-pill closed"
               more.textContent = `+${list.length - 3} lịch`
               items.appendChild(more)
          }

          cell.appendChild(items)

          if (inMonth) {
               cell.addEventListener("click", () => {
                    depState.selectedDate = dateStr
                    depState.selectedDepartureId = null
                    renderCalendar()
                    renderDetail()
               })
          }

          grid.appendChild(cell)
     }
}

function renderDetail() {
     const empty = qs("depDetailEmpty")
     const detail = qs("depDetail")
     if (!empty || !detail) return

     const emptyThumb = qs("depEmptyThumb")
     const emptyTitle = qs("depEmptyTitle")
     const emptySub = qs("depEmptySub")
     const detailThumb = qs("depDetailThumb")
     const title = qs("depDetailTitle")
     const sub = qs("depDetailSub")
     const badges = qs("depDetailBadges")
     const current = depState.departures.find(d => String(d.id) === String(depState.selectedDepartureId)) || null
     const tour = depState.selectedTourId ? depState.tourMap.get(String(depState.selectedTourId)) : null
     const canShowForm = !!current || !!depState.selectedTourId

     if (!canShowForm) {
          if (!depState.selectedTourId) {
               if (emptyThumb) emptyThumb.removeAttribute("src")
               if (emptyTitle) emptyTitle.textContent = ""
               if (emptySub) emptySub.textContent = ""
               if (detailThumb) detailThumb.removeAttribute("src")
               if (title) title.textContent = ""
               if (sub) sub.textContent = ""
               if (badges) badges.innerHTML = ""
               empty.hidden = true
               detail.hidden = true
               depState.formTourId = null
               return
          }
          const thumb = tour && tour.thumbnailUrl ? tour.thumbnailUrl : "/images/auth/placeholder.jpg"
          if (emptyThumb) emptyThumb.src = thumb
          if (emptyTitle) emptyTitle.textContent = tour ? (tour.title || "") : ""
          if (emptySub) {
               const duration = tour ? `${tour.durationDays || 0} ngày / ${tour.durationNights || 0} đêm` : ""
               const basePrice = tour && tour.basePrice != null
                    ? Number(tour.basePrice).toLocaleString("vi-VN") + " ₫"
                    : ""
               emptySub.textContent = [duration, basePrice ? `Giá cơ bản: ${basePrice}` : ""]
                    .filter(Boolean)
                    .join(" • ")
          }
          if (title) title.textContent = ""
          if (sub) sub.textContent = ""
          if (badges) badges.innerHTML = ""
          empty.hidden = false
          detail.hidden = true
          depState.formTourId = null
          return
     }

     empty.hidden = true
     detail.hidden = false

     if (detailThumb) {
          const thumb = (current && current.thumbnailUrl) || (tour && tour.thumbnailUrl) || "/images/auth/placeholder.jpg"
          detailThumb.src = thumb
     }

     if (title) title.textContent = tour ? (tour.title || "") : (current ? (current.tourTitle || "") : "")
     if (sub) {
          const duration = tour
               ? `${tour.durationDays || 0} ngày / ${tour.durationNights || 0} đêm`
               : (current ? `${current.durationDays || 0} ngày / ${current.durationNights || 0} đêm` : "")
          const basePriceValue = tour && tour.basePrice != null
               ? tour.basePrice
               : (current && current.basePrice != null ? current.basePrice : null)
          const basePrice = basePriceValue != null ? Number(basePriceValue).toLocaleString("vi-VN") + " ₫" : ""
          sub.textContent = [duration, basePrice ? `Giá cơ bản: ${basePrice}` : ""].filter(Boolean).join(" • ")
     }

     if (badges) {
          badges.innerHTML = ""
          if (current) {
               const statusBadge = document.createElement("span")
               statusBadge.className = "departure-badge" + (current.status === "CLOSED" ? " closed" : "")
               statusBadge.textContent = current.status === "CLOSED" ? "Đã đóng" : "Đang mở"
               badges.appendChild(statusBadge)

               if (current.completed) {
                    const done = document.createElement("span")
                    done.className = "departure-badge completed"
                    done.textContent = "Đã hoàn thành"
                    badges.appendChild(done)
               }
          }
     }

     fillForm(current)
}

function fillForm(dep) {
     const dateInput = qs("depStartDate")
     const locationSelect = qs("depStartLocation")
     const priceAdult = qs("depPriceAdult")
     const priceChild = qs("depPriceChild")
     const capacity = qs("depCapacity")
     const available = qs("depAvailable")
     const status = qs("depStatus")
     const idInput = qs("depId")

     if (!dateInput || !locationSelect || !priceAdult || !priceChild || !capacity || !available || !status || !idInput) return

     if (dep) {
          idInput.value = dep.id || ""
          dateInput.value = dep.startDate || ""
          locationSelect.value = dep.startLocation || ""
          priceAdult.value = normalizePrice(dep.priceAdult)
          priceChild.value = normalizePrice(dep.priceChild)
          capacity.value = dep.capacity || ""
          available.value = dep.available != null ? dep.available : ""
          status.value = dep.status || "OPEN"
          depState.formTourId = dep.tourId || null
     } else {
          const tour = depState.tourMap.get(String(depState.selectedTourId))
          idInput.value = ""
          dateInput.value = depState.selectedDate || ""
          if (locationSelect.options.length) {
               locationSelect.selectedIndex = 0
          }
          priceAdult.value = tour && tour.basePrice != null ? normalizePrice(tour.basePrice) : ""
          priceChild.value = ""
          capacity.value = ""
          available.value = ""
          status.value = "OPEN"
          depState.formTourId = depState.selectedTourId || null
     }
}

async function submitForm(e) {
     e.preventDefault()
     const id = qs("depId").value
     const payload = {
          tourId: depState.formTourId ? Number(depState.formTourId) : null,
          startDate: qs("depStartDate").value,
          startLocation: qs("depStartLocation").value || null,
          priceAdult: qs("depPriceAdult").value ? Number(qs("depPriceAdult").value) : null,
          priceChild: qs("depPriceChild").value ? Number(qs("depPriceChild").value) : null,
          capacity: qs("depCapacity").value ? Number(qs("depCapacity").value) : null,
          available: qs("depAvailable").value ? Number(qs("depAvailable").value) : null,
          status: qs("depStatus").value || "OPEN"
     }

     try {
          if (id) {
               await api(`/api/admin/departures/${id}`, { method: "PUT", body: JSON.stringify(payload) })
               toast("Cập nhật lịch khởi hành thành công", "success")
          } else {
               await api("/api/admin/departures", { method: "POST", body: JSON.stringify(payload) })
               toast("Thêm lịch khởi hành thành công", "success")
          }
          depState.selectedDepartureId = null
          await loadDepartures()
     } catch (err) {
          toast(err.message || "Không thể lưu lịch khởi hành", "error")
     }
}

function bindEvents() {
     const tourSelect = qs("depTourSelect")
     if (tourSelect) {
          tourSelect.addEventListener("change", async () => {
               depState.selectedTourId = tourSelect.value || null
               depState.selectedDepartureId = null
               depState.selectedDate = null
               depState.formTourId = null
               renderCalendar()
               renderDetail()
               await loadDepartures()
          })
     }

     function clampSelectionToMonth() {
          if (!depState.selectedDate) return
          const parts = depState.selectedDate.split("-")
          if (parts.length < 2) return
          const y = Number(parts[0])
          const m = Number(parts[1])
          if (y !== depState.year || m !== depState.month) {
               depState.selectedDate = null
               depState.selectedDepartureId = null
          }
     }

     async function changeMonth(step) {
          depState.month += step
          if (depState.month < 1) {
               depState.month = 12
               depState.year -= 1
          }
          if (depState.month > 12) {
               depState.month = 1
               depState.year += 1
          }
          depState.selectedDepartureId = null
          clampSelectionToMonth()
          await loadDepartures()
     }

     const prevBtn = qs("btnPrevMonth")
     const nextBtn = qs("btnNextMonth")
     if (prevBtn) {
          prevBtn.addEventListener("click", async () => {
               await changeMonth(-1)
          })
     }
     if (nextBtn) {
          nextBtn.addEventListener("click", async () => {
               await changeMonth(1)
          })
     }

     const addBtn = qs("btnAddDeparture")
     if (addBtn) {
          addBtn.addEventListener("click", () => {
               depState.selectedDepartureId = null
               if (!depState.selectedDate) {
                    const today = new Date()
                    depState.selectedDate = toISODate(today)
               }
               renderCalendar()
               renderDetail()
          })
     }

     const form = qs("depForm")
     if (form) {
          form.addEventListener("submit", submitForm)
     }

     const resetBtn = qs("btnResetForm")
     if (resetBtn) {
          resetBtn.addEventListener("click", () => {
               depState.selectedDepartureId = null
               renderDetail()
          })
     }

     const deleteBtn = qs("btnDeleteDeparture")
     if (deleteBtn) {
          deleteBtn.addEventListener("click", async () => {
               const id = qs("depId").value
               if (!id) return
               const ok = confirm("Bạn chắc chắn muốn hủy ngày khởi hành này?")
               if (!ok) return
               try {
                    await api(`/api/admin/departures/${id}`, { method: "DELETE" })
                    toast("Đã hủy ngày khởi hành", "success")
                    depState.selectedDepartureId = null
                    await loadDepartures()
               } catch (err) {
                    toast(err.message || "Không thể hủy ngày khởi hành", "error")
               }
          })
     }

     const cap = qs("depCapacity")
     const avail = qs("depAvailable")
     if (cap && avail) {
          const syncAvail = () => {
               const capVal = Number(cap.value || 0)
               if (!capVal) return
               avail.value = String(capVal)
          }
          cap.addEventListener("input", syncAvail)
     }
}

async function initDeparturesPage() {
     const page = document.querySelector("[data-page='departures-index']")
     if (!page) return

     try {
          await loadTours()
          await loadStartLocations()
          bindEvents()
          await loadDepartures()
     } catch (err) {
          toast(err.message || "Không thể tải dữ liệu", "error")
     }
}

initDeparturesPage()
