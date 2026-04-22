function qs(id) { return document.getElementById(id) }

function setMsg(text, isErr) {
     const el = qs("adminProfileMsg")
     if (!el) return
     if (!text) {
          el.style.display = "none"
          el.textContent = ""
          el.classList.remove("err")
          return
     }
     el.style.display = "block"
     el.textContent = text
     if (isErr) el.classList.add("err")
     else el.classList.remove("err")
}

function setAvatar(url) {
     const img = qs("adminAvatarImg")
     const fb = qs("adminAvatarFallback")
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
     const avatarInp = qs("adminAvatar")
     if (avatarInp) avatarInp.value = url || ""
}

async function api(url, opts) {
     const res = await fetch(url, opts)
     if (!res.ok) {
          const ct = res.headers.get("content-type") || ""
          let msg = "Có lỗi xảy ra"
          if (ct.includes("application/json")) {
               const data = await res.json().catch(() => null)
               msg = (data && data.message) ? data.message : msg
          } else {
               const t = await res.text().catch(() => "")
               msg = t || msg
          }
          throw new Error(msg)
     }
     return res.json().catch(() => null)
}

async function loadProfile() {
     try {
          setMsg("", false)
          const data = await api("/api/profile")
          if (!data) return
          if (qs("adminEmail")) qs("adminEmail").value = data.email || ""
          if (qs("adminFullName")) qs("adminFullName").value = data.fullName || ""
          if (qs("adminPhone")) qs("adminPhone").value = data.phone || ""
          if (qs("adminGender")) qs("adminGender").value = data.gender || ""
          if (qs("adminDob")) qs("adminDob").value = data.dob || ""
          if (qs("adminAddress")) qs("adminAddress").value = data.address || ""

          if (qs("adminDisplayName")) qs("adminDisplayName").textContent = data.fullName || "Admin"
          if (qs("adminDisplayEmail")) qs("adminDisplayEmail").textContent = data.email || ""
          setAvatar(data.avatar || "")
     } catch (e) {
          setMsg(e.message || "Không thể tải hồ sơ", true)
     }
}

async function uploadAvatar(file) {
     const fd = new FormData()
     fd.append("file", file)
     const res = await fetch("/api/profile/avatar", { method: "POST", body: fd })
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

function bindAvatarUploader() {
     const fileInp = qs("adminAvatarFile")
     const btnClear = qs("adminAvatarClear")
     const hint = qs("adminAvatarHint")
     const img = qs("adminAvatarImg")
     const fb = qs("adminAvatarFallback")
     let selectedFile = null

     if (!fileInp) return

     const setHint = (text, isErr) => {
          if (!hint) return
          hint.textContent = text || ""
          hint.style.color = isErr ? "#b42318" : "var(--muted)"
     }

     fileInp.addEventListener("change", async () => {
          const file = fileInp.files && fileInp.files[0]
          if (!file) {
               selectedFile = null
               setHint("", false)
               return
          }
          if (!file.type.startsWith("image/")) {
               selectedFile = null
               fileInp.value = ""
               setHint("File không hợp lệ (chỉ cho phép ảnh)", true)
               return
          }
          selectedFile = file
          const previewUrl = URL.createObjectURL(file)
          if (img && fb) {
               img.src = previewUrl
               img.style.display = "block"
               fb.style.display = "none"
          }

          try {
               fileInp.disabled = true
               setHint("Đang tải ảnh lên Cloudinary...", false)
               const url = await uploadAvatar(selectedFile)
               setAvatar(url)
               selectedFile = null
               fileInp.value = ""
               setHint("Đã tải ảnh lên thành công", false)
          } catch (e) {
               setHint(e.message || "Upload thất bại", true)
          } finally {
               fileInp.disabled = false
          }
     })

     if (btnClear) {
          btnClear.addEventListener("click", () => {
               selectedFile = null
               if (fileInp) fileInp.value = ""
               setAvatar("")
               setHint("Đã xóa avatar (nhớ bấm Lưu)", false)
          })
     }
}

function bindSave() {
     const form = qs("adminProfileForm")
     const btn = qs("adminSaveBtn")
     if (!form) return
     form.addEventListener("submit", async (e) => {
          e.preventDefault()
          try {
               setMsg("", false)
               if (btn) btn.disabled = true
               const payload = {
                    fullName: qs("adminFullName") ? qs("adminFullName").value.trim() : "",
                    phone: qs("adminPhone") ? qs("adminPhone").value.trim() : "",
                    gender: qs("adminGender") ? qs("adminGender").value : "",
                    dob: qs("adminDob") ? (qs("adminDob").value || null) : null,
                    address: qs("adminAddress") ? qs("adminAddress").value.trim() : "",
                    avatar: qs("adminAvatar") ? qs("adminAvatar").value.trim() : ""
               }
               if (!payload.fullName) {
                    setMsg("Họ tên không được rỗng", true)
                    return
               }
               const data = await api("/api/profile", { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) })
               if (data) {
                    if (qs("adminDisplayName")) qs("adminDisplayName").textContent = data.fullName || "Admin"
                    setMsg("Đã lưu thông tin cá nhân", false)
               }
          } catch (e) {
               setMsg(e.message || "Lưu thất bại", true)
          } finally {
               if (btn) btn.disabled = false
          }
     })
}

document.addEventListener("DOMContentLoaded", () => {
     loadProfile()
     bindAvatarUploader()
     bindSave()
})
