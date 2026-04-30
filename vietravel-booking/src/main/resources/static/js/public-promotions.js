document.addEventListener("DOMContentLoaded", () => {
     const btn = document.getElementById("copyCodeBtn")
     const codeEl = document.getElementById("promoCode")
     if (!btn || !codeEl) return
     btn.addEventListener("click", async () => {
          const code = codeEl.textContent.trim()
          if (!code) return
          try {
               await navigator.clipboard.writeText(code)
               btn.textContent = "Đã sao chép"
               setTimeout(() => { btn.textContent = "Sao chép mã" }, 1500)
          } catch (e) {
               alert("Không thể sao chép mã")
          }
     })
})
