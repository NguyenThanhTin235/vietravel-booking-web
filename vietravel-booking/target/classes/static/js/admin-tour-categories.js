function qs(id){return document.getElementById(id)}
function qsa(sel,root){return Array.from((root||document).querySelectorAll(sel))}

function debounce(fn,ms){
    let t
    return (...args)=>{clearTimeout(t);t=setTimeout(()=>fn(...args),ms)}
}

async function api(url,opts){
    const res=await fetch(url,Object.assign({headers:{"Content-Type":"application/json"}},opts||{}))
    if(res.status===204)return null
    const ct=res.headers.get("content-type")||""
    let data=null
    let text=""
    if(ct.includes("application/json")) data=await res.json().catch(()=>null)
    else text=await res.text().catch(()=>"")
    if(!res.ok){
        const msg=(data&&data.message)?data.message:(text?text:("HTTP "+res.status))
        throw new Error(msg)
    }
    return data
}

function toast(type,title,desc){
    const wrap=qs("tcToastWrap")
    if(!wrap)return
    const el=document.createElement("div")
    el.className="tc-toast "+(type==="err"?"err":"ok")
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
    const m=qs("tcModal")
    const t=qs("tcModalTitle")
    const b=qs("tcModalBody")
    const ok=qs("tcModalOk")
    const cancel=qs("tcModalCancel")
    const close=qs("tcModalClose")
    if(!m||!t||!b||!ok||!cancel||!close)return Promise.resolve(false)

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
        m.onclick=(e)=>{if(e.target===m)end(false)}
        m.classList.add("show")
    })
}

function getPage(){
    const el=document.querySelector("[data-page]")
    return el?el.getAttribute("data-page"):""
}

function toSlug(v){
    if(!v)return""
    return v.trim().toLowerCase()
        .normalize("NFD").replace(/[\u0300-\u036f]/g,"")
        .replace(/đ/g,"d").replace(/Đ/g,"d")
        .replace(/[^a-z0-9]+/g,"-")
        .replace(/^-+|-+$/g,"")
        .replace(/-+/g,"-")
}

async function loadParents(selectEl,currentId){
    if(!selectEl)return
    const items=await api(`/api/admin/tour-categories`,{method:"GET"})
    const opts=items
        .filter(x=>!currentId||String(x.id)!==String(currentId))
        .sort((a,b)=>(a.sortOrder||0)-(b.sortOrder||0))
        .map(x=>`<option value="${x.id}">${x.name}${x.parentName?(" • ("+x.parentName+")"):""}</option>`)
        .join("")
    selectEl.innerHTML=`<option value="">-- Không có --</option>`+opts
}

/* =========================
   PAGINATION (5 records)
========================= */
const tcState={page:1,pageSize:5,filtered:[]}

function renderPager(total){
    const pageInfo=qs("pageInfo")
    const pageNums=qs("pageNums")
    const btnPrev=qs("btnPrev")
    const btnNext=qs("btnNext")

    const size=tcState.pageSize
    const totalPages=Math.max(1,Math.ceil(total/size))
    if(tcState.page>totalPages)tcState.page=totalPages

    const start=total===0?0:((tcState.page-1)*size+1)
    const end=Math.min(total,tcState.page*size)

    if(pageInfo)pageInfo.textContent=total?`Hiển thị ${start}-${end} / ${total}`:"0 kết quả"

    if(btnPrev){
        btnPrev.disabled=tcState.page<=1
        btnPrev.onclick=()=>{if(tcState.page>1){tcState.page--;renderListPage()}}
    }
    if(btnNext){
        btnNext.disabled=tcState.page>=totalPages
        btnNext.onclick=()=>{if(tcState.page<totalPages){tcState.page++;renderListPage()}}
    }

    if(pageNums){
        pageNums.innerHTML=""
        const maxBtns=7
        let from=Math.max(1,tcState.page-3)
        let to=Math.min(totalPages,from+maxBtns-1)
        from=Math.max(1,to-maxBtns+1)

        for(let p=from;p<=to;p++){
            const b=document.createElement("button")
            b.type="button"
            b.className="tc-btn sm tc-pageNum"+(p===tcState.page?" active":"")
            b.textContent=String(p)
            b.addEventListener("click",()=>{tcState.page=p;renderListPage()})
            pageNums.appendChild(b)
        }
    }
}

function bindRowActions(rows){
    qsa("[data-del]",rows).forEach(btn=>{
        btn.addEventListener("click",async()=>{
            const id=btn.getAttribute("data-del")
            const name=btn.getAttribute("data-name")||""
            const ok=await modalConfirm("Xóa danh mục",`Bạn chắc chắn muốn xóa "${name}"?`,"Xóa")
            if(!ok)return
            try{
                await api(`/api/admin/tour-categories/${id}`,{method:"DELETE"})
                toast("ok","Đã xóa","Danh mục đã được xóa")
                tcState.page=1
                loadList()
            }catch(e){
                toast("err","Xóa thất bại",e.message||"Không thể xóa")
            }
        })
    })
}

function renderListPage(){
    const rows=qs("rows")
    const msg=qs("msg")
    if(!rows)return

    const total=tcState.filtered.length
    const size=tcState.pageSize
    const startIdx=(tcState.page-1)*size
    const pageItems=tcState.filtered.slice(startIdx,startIdx+size)

    renderPager(total)

    if(!pageItems.length){
        rows.innerHTML=`<tr><td colspan="7" style="color:var(--muted);font-weight:900;padding:16px">Không có dữ liệu</td></tr>`
        if(msg)msg.textContent="0 kết quả"
        return
    }

    rows.innerHTML=pageItems.map(x=>{
        const badge=x.isActive
            ?`<span class="tc-badge on"><i></i>ON</span>`
            :`<span class="tc-badge off"><i></i>OFF</span>`
        return `
            <tr>
                <td>${x.id}</td>
                <td style="font-weight:900">${x.name||""}</td>
                <td>${x.parentName?(`<span class="tc-parent">${x.parentName}</span>`):`<span style="color:var(--muted);font-weight:900">—</span>`}</td>
                <td class="tc-code">${x.slug||""}</td>
                <td>${badge}</td>
                <td style="font-weight:900">${x.sortOrder??0}</td>
                <td class="tc-right">
                    <span class="tc-btnRow">
                        <a class="tc-btn sm" href="/admin/tour-categories/edit?id=${x.id}">Sửa</a>
                        <button class="tc-btn sm danger" data-del="${x.id}" data-name="${(x.name||"").replace(/"/g,"&quot;")}">Xóa</button>
                    </span>
                </td>
            </tr>
        `
    }).join("")

    bindRowActions(rows)

    if(msg)msg.textContent=`${total} kết quả`
}

/* =========================
   LIST
========================= */
async function loadList(){
    const rows=qs("rows")
    const onlyActive=qs("onlyActive")
    const q=qs("q")
    const msg=qs("msg")
    if(!rows)return

    rows.innerHTML=`
        <tr class="tc-skeleton"><td colspan="7"><div class="tc-skel" style="height:14px;width:55%"></div></td></tr>
        <tr class="tc-skeleton"><td colspan="7"><div class="tc-skel" style="height:14px;width:75%"></div></td></tr>
        <tr class="tc-skeleton"><td colspan="7"><div class="tc-skel" style="height:14px;width:65%"></div></td></tr>
    `
    if(msg)msg.textContent=""

    try{
        const active=onlyActive&&onlyActive.checked
        const items=await api(`/api/admin/tour-categories${active?"?active=true":""}`,{method:"GET"})
        const key=(q&&q.value?q.value.trim().toLowerCase():"")
        const filtered=key?items.filter(x=>{
            const n=(x.name||"").toLowerCase()
            const s=(x.slug||"").toLowerCase()
            const p=(x.parentName||"").toLowerCase()
            return n.includes(key)||s.includes(key)||p.includes(key)
        }):items

        tcState.filtered=filtered||[]
        if(tcState.page<1)tcState.page=1
        renderListPage()
    }catch(e){
        rows.innerHTML=`<tr><td colspan="7" style="color:#b42318;font-weight:950;padding:16px">${e.message||"Lỗi tải dữ liệu"}</td></tr>`
        toast("err","Lỗi",e.message||"Không tải được dữ liệu")
    }
}

/* =========================
   FORM
========================= */
async function loadForm(){
    const form=qs("form")
    if(!form)return

    const idHidden=qs("id")
    const idParam=(idHidden&&idHidden.value)?idHidden.value:""
    const isEdit=!!idParam
    const title=qs("title")
    const btnDelete=qs("btnDelete")
    const err=qs("err")

    const setErr=(m)=>{if(!err)return;err.style.display="block";err.textContent=m||""}
    const clearErr=()=>{if(!err)return;err.style.display="none";err.textContent=""}

    if(title)title.textContent=isEdit?"Sửa danh mục":"Thêm danh mục"
    if(btnDelete)btnDelete.style.display=isEdit?"":"none"

    const parentSel=qs("parentId")
    await loadParents(parentSel,idParam)

    const nameEl=qs("name")
    const slugEl=qs("slug")
    let slugManual=false

    if(slugEl){
        slugEl.addEventListener("input",()=>{slugManual=slugEl.value.trim().length>0})
    }
    if(nameEl&&slugEl){
        nameEl.addEventListener("input",()=>{
            if(slugManual)return
            slugEl.value=toSlug(nameEl.value)
        })
    }

    if(isEdit){
        try{
            const data=await api(`/api/admin/tour-categories/${idParam}`,{method:"GET"})
            qs("name").value=data.name||""
            qs("slug").value=data.slug||""
            qs("sortOrder").value=data.sortOrder??0
            qs("isActive").checked=!!data.isActive
            if(parentSel&&data.parentId) parentSel.value=String(data.parentId)
        }catch(e){
            setErr(e.message||"Không tải được dữ liệu")
            toast("err","Lỗi",e.message||"Không tải được dữ liệu")
        }
    }

    if(btnDelete){
        btnDelete.addEventListener("click",async()=>{
            const name=qs("name").value||""
            const ok=await modalConfirm("Xóa danh mục",`Bạn chắc chắn muốn xóa "${name}"?`,"Xóa")
            if(!ok)return
            clearErr()
            try{
                await api(`/api/admin/tour-categories/${idParam}`,{method:"DELETE"})
                toast("ok","Đã xóa","Chuyển về danh sách")
                window.location.href="/admin/tour-categories"
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
            name:qs("name").value.trim(),
            slug:qs("slug").value.trim(),
            parentId:(qs("parentId").value||"").trim()?Number(qs("parentId").value):null,
            sortOrder:Number(qs("sortOrder").value||0),
            isActive:qs("isActive").checked
        }

        if(!payload.name){setErr("Tên không được rỗng");return}
        if(!payload.slug){
            payload.slug=toSlug(payload.name)
            qs("slug").value=payload.slug
        }

        try{
            if(isEdit){
                await api(`/api/admin/tour-categories/${idParam}`,{method:"PUT",body:JSON.stringify(payload)})
                toast("ok","Đã cập nhật","Danh mục đã được lưu")
            }else{
                await api(`/api/admin/tour-categories`,{method:"POST",body:JSON.stringify(payload)})
                toast("ok","Đã tạo","Danh mục đã được tạo")
            }
            setTimeout(()=>{window.location.href="/admin/tour-categories"},450)
        }catch(ex){
            setErr(ex.message||"Lưu thất bại")
            toast("err","Lưu thất bại",ex.message||"Không thể lưu")
        }
    })
}

/* =========================
   INIT
========================= */
document.addEventListener("DOMContentLoaded",()=>{
    const page=getPage()

    if(page==="tour-categories-list"){
        tcState.page=1
        loadList()

        const q=qs("q")
        const onlyActive=qs("onlyActive")
        const btnReload=qs("btnReload")

        if(btnReload)btnReload.addEventListener("click",()=>{tcState.page=1;loadList()})
        if(onlyActive)onlyActive.addEventListener("change",()=>{tcState.page=1;loadList()})
        if(q)q.addEventListener("input",debounce(()=>{tcState.page=1;loadList()},260))
    }

    if(page==="tour-categories-form"){
        loadForm()
    }
})
