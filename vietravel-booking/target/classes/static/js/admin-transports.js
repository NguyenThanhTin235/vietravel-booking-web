function qs(id){return document.getElementById(id)}
function qsa(sel,root){return Array.from((root||document).querySelectorAll(sel))}

function debounce(fn,ms){
    let t
    return (...args)=>{clearTimeout(t);t=setTimeout(()=>fn(...args),ms)}
}

async function api(url,opts){
    const res=await fetch(url,Object.assign({headers:{"Content-Type":"application/json"}},opts||{}))
    if(res.status===204) return null
    const data=await res.json().catch(()=>null)
    if(!res.ok){
        const msg=(data&&data.message)?data.message:("HTTP "+res.status)
        throw new Error(msg)
    }
    return data
}

function toast(type,title,desc){
    const wrap=qs("tmToastWrap")
    if(!wrap) return
    const el=document.createElement("div")
    el.className="tm-toast "+(type==="err"?"err":"ok")
    el.innerHTML=`
        <div class="dot"></div>
        <div>
            <div class="t">${title||""}</div>
            <div class="d">${desc||""}</div>
        </div>
        <button class="x">×</button>
    `
    wrap.appendChild(el)
    const kill=()=>{el.remove()}
    el.querySelector(".x").addEventListener("click",kill)
    setTimeout(kill,3200)
}

function modalConfirm(title,body,okText){
    const m=qs("tmModal")
    const t=qs("tmModalTitle")
    const b=qs("tmModalBody")
    const ok=qs("tmModalOk")
    const cancel=qs("tmModalCancel")
    const close=qs("tmModalClose")
    if(!m||!t||!b||!ok||!cancel||!close) return Promise.resolve(false)

    t.textContent=title||"Xác nhận"
    b.textContent=body||""
    ok.textContent=okText||"Đồng ý"

    return new Promise(resolve=>{
        const end=(v)=>{
            m.classList.remove("show")
            ok.onclick=null
            cancel.onclick=null
            close.onclick=null
            m.onclick=null
            resolve(v)
        }
        ok.onclick=()=>end(true)
        cancel.onclick=()=>end(false)
        close.onclick=()=>end(false)
        m.onclick=(e)=>{if(e.target===m) end(false)}
        m.classList.add("show")
    })
}

function getPage(){
    const el=document.querySelector("[data-page]")
    return el?el.getAttribute("data-page"):""
}

/* ===== NEW: breadcrumb current ===== */
function setTransportBreadcrumb(){
    const nav=document.querySelector(".tm-breadcrumb")
    if(!nav) return

    const page=getPage()

    let currentText=""
    if(page==="transport-list"){
        currentText="Danh sách"
    }else if(page==="transport-form"){
        const id=qs("id")?.value
        currentText=id?"Sửa":"Thêm"
    }else{
        return
    }

    let cur=nav.querySelector(".current")
    if(!cur){
        const sep=document.createElement("span")
        sep.className="sep"
        sep.textContent="/"
        nav.appendChild(sep)

        cur=document.createElement("span")
        cur.className="current"
        nav.appendChild(cur)
    }

    cur.textContent=currentText
}

async function loadList(){
    const rows=qs("rows")
    const onlyActive=qs("onlyActive")
    const q=qs("q")
    const msg=qs("msg")
    if(!rows) return

    const skel=`
        <tr class="tm-skeleton"><td colspan="6"><div class="tm-skel" style="height:14px;width:55%"></div></td></tr>
        <tr class="tm-skeleton"><td colspan="6"><div class="tm-skel" style="height:14px;width:75%"></div></td></tr>
        <tr class="tm-skeleton"><td colspan="6"><div class="tm-skel" style="height:14px;width:65%"></div></td></tr>
    `
    rows.innerHTML=skel
    if(msg) msg.textContent=""

    try{
        const active=onlyActive&&onlyActive.checked
        const items=await api(`/api/admin/transport-modes${active?"?active=true":""}`,{method:"GET"})
        const key=(q&&q.value?q.value.trim().toLowerCase():"")
        const filtered=key?items.filter(x=>{
            const c=(x.code||"").toLowerCase()
            const n=(x.name||"").toLowerCase()
            return c.includes(key)||n.includes(key)
        }):items

        if(!filtered.length){
            rows.innerHTML=`<tr><td colspan="6" style="color:var(--muted);font-weight:900;padding:16px">Không có dữ liệu</td></tr>`
            if(msg) msg.textContent="0 kết quả"
            return
        }

        rows.innerHTML=filtered.map(x=>{
            const badge=x.isActive
                ?`<span class="tm-badge on"><i></i>ON</span>`
                :`<span class="tm-badge off"><i></i>OFF</span>`
            return `
                <tr>
                    <td>${x.id}</td>
                    <td class="tm-code">${x.code||""}</td>
                    <td style="font-weight:900">${x.name||""}</td>
                    <td class="tm-center">${badge}</td>
                    <td class="tm-center" style="font-weight:900">${x.sortOrder??0}</td>
                    <td class="tm-right">
                        <span class="tm-btnRow">
                            <a class="tm-btn sm" href="/admin/transports/edit?id=${x.id}">Sửa</a>
                            <button class="tm-btn sm danger" data-del="${x.id}" data-name="${(x.name||"").replace(/"/g,"&quot;")}">Xóa</button>
                        </span>
                    </td>
                </tr>
            `
        }).join("")

        qsa("[data-del]",rows).forEach(btn=>{
            btn.addEventListener("click",async()=>{
                const id=btn.getAttribute("data-del")
                const name=btn.getAttribute("data-name")||""
                const ok=await modalConfirm("Xóa phương tiện",`Bạn chắc chắn muốn xóa "${name}"?`,"Xóa")
                if(!ok) return
                try{
                    await api(`/api/admin/transport-modes/${id}`,{method:"DELETE"})
                    toast("ok","Đã xóa","Phương tiện đã được xóa")
                    loadList()
                }catch(e){
                    toast("err","Xóa thất bại",e.message||"Không thể xóa")
                }
            })
        })

        if(msg) msg.textContent=`${filtered.length} kết quả`
    }catch(e){
        rows.innerHTML=`<tr><td colspan="6" style="color:#b42318;font-weight:950;padding:16px">${e.message||"Lỗi tải dữ liệu"}</td></tr>`
        toast("err","Lỗi",e.message||"Không tải được dữ liệu")
    }
}

function slugToCode(name){
    const s=(name||"").trim().toUpperCase()
    return s
        .normalize("NFD").replace(/[\u0300-\u036f]/g,"")
        .replace(/[^A-Z0-9]+/g,"_")
        .replace(/^_+|_+$/g,"")
        .replace(/_+/g,"_")
}

async function loadForm(){
    const form=qs("form")
    if(!form) return

    const idHidden=qs("id")
    const idParam=(idHidden&&idHidden.value)?idHidden.value:""
    const isEdit=!!idParam
    const title=qs("title")
    const btnDelete=qs("btnDelete")
    const err=qs("err")

    if(title) title.textContent=isEdit?"Sửa phương tiện":"Thêm phương tiện"
    if(btnDelete) btnDelete.style.display=isEdit?"":"none"

    const setErr=(m)=>{
        if(!err) return
        err.style.display="block"
        err.textContent=m||""
    }
    const clearErr=()=>{
        if(!err) return
        err.style.display="none"
        err.textContent=""
    }

    const nameEl=qs("name")
    const codeEl=qs("code")
    if(nameEl&&codeEl){
        let locked=false
        codeEl.addEventListener("input",()=>{locked=true})
        nameEl.addEventListener("input",debounce(()=>{
            if(locked) return
            codeEl.value=slugToCode(nameEl.value)
        },120))
    }

    if(isEdit){
        try{
            const data=await api(`/api/admin/transport-modes/${idParam}`,{method:"GET"})
            qs("code").value=data.code||""
            qs("name").value=data.name||""
            qs("sortOrder").value=data.sortOrder??0
            qs("isActive").checked=!!data.isActive
        }catch(e){
            setErr(e.message||"Không tải được dữ liệu")
            toast("err","Lỗi",e.message||"Không tải được dữ liệu")
        }
    }

    if(btnDelete){
        btnDelete.addEventListener("click",async()=>{
            const name=qs("name").value||""
            const ok=await modalConfirm("Xóa phương tiện",`Bạn chắc chắn muốn xóa "${name}"?`,"Xóa")
            if(!ok) return
            clearErr()
            try{
                await api(`/api/admin/transport-modes/${idParam}`,{method:"DELETE"})
                toast("ok","Đã xóa","Chuyển về danh sách")
                window.location.href="/admin/transports"
            }catch(e){
                setErr(e.message||"Xóa thất bại")
                toast("err","Xóa thất bại",e.message||"Không thể xóa")
            }
        })
    }

    form.addEventListener("submit",async(e)=>{
        e.preventDefault()
        clearErr()

        const payload={
            code:qs("code").value.trim(),
            name:qs("name").value.trim(),
            sortOrder:Number(qs("sortOrder").value||0),
            isActive:qs("isActive").checked
        }

        if(!payload.code||!payload.name){setErr("Code và Tên không được rỗng");return}

        try{
            if(isEdit){
                await api(`/api/admin/transport-modes/${idParam}`,{method:"PUT",body:JSON.stringify(payload)})
                toast("ok","Đã cập nhật","Phương tiện đã được lưu")
            }else{
                await api(`/api/admin/transport-modes`,{method:"POST",body:JSON.stringify(payload)})
                toast("ok","Đã tạo","Phương tiện đã được tạo")
            }
            setTimeout(()=>{window.location.href="/admin/transports"},450)
        }catch(ex){
            setErr(ex.message||"Lưu thất bại")
            toast("err","Lưu thất bại",ex.message||"Không thể lưu")
        }
    })
}

document.addEventListener("DOMContentLoaded",()=>{
    setTransportBreadcrumb()

    const page=getPage()
    if(page==="transport-list"){
        loadList()
        const q=qs("q")
        const onlyActive=qs("onlyActive")
        const btnReload=qs("btnReload")
        if(btnReload) btnReload.addEventListener("click",loadList)
        if(onlyActive) onlyActive.addEventListener("change",loadList)
        if(q) q.addEventListener("input",debounce(loadList,260))
    }
    if(page==="transport-form"){
        loadForm()
    }
})
