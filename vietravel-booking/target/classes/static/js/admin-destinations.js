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
    if(ct.includes("application/json")){
        data=await res.json().catch(()=>null)
    }else{
        text=await res.text().catch(()=>"")
    }
    if(!res.ok){
        const msg=(data&&data.message)?data.message:(text?text:("HTTP "+res.status))
        throw new Error(msg)
    }
    return data
}

function toast(type,title,desc){
    const wrap=qs("tlToastWrap")
    if(!wrap)return
    const el=document.createElement("div")
    el.className="tl-toast "+(type==="err"?"err":"ok")
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
    const m=qs("tlModal")
    const t=qs("tlModalTitle")
    const b=qs("tlModalBody")
    const ok=qs("tlModalOk")
    const cancel=qs("tlModalCancel")
    const close=qs("tlModalClose")
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

function toSlug(v){
    if(!v)return""
    return v
        .normalize("NFD").replace(/[\u0300-\u036f]/g,"")
        .replace(/đ/g,"d").replace(/Đ/g,"D")
        .toLowerCase()
        .replace(/[^a-z0-9]+/g,"-")
        .replace(/^-+|-+$/g,"")
        .replace(/-+/g,"-")
}

/* =========================
   PAGINATION (client-side)
========================= */
const dState={page:1,pageSize:5,filtered:[]}

function renderPager(total){
    const pageInfo=qs("pageInfo")
    const pageNums=qs("pageNums")
    const btnPrev=qs("btnPrev")
    const btnNext=qs("btnNext")

    const size=dState.pageSize
    const totalPages=Math.max(1,Math.ceil(total/size))
    if(dState.page>totalPages)dState.page=totalPages

    const start=total===0?0:((dState.page-1)*size+1)
    const end=Math.min(total,dState.page*size)

    if(pageInfo)pageInfo.textContent=total?`Hiển thị ${start}-${end} / ${total}`:"0 kết quả"

    if(btnPrev){
        btnPrev.disabled=dState.page<=1
        btnPrev.onclick=()=>{if(dState.page>1){dState.page--;renderListPage()}}
    }
    if(btnNext){
        btnNext.disabled=dState.page>=totalPages
        btnNext.onclick=()=>{if(dState.page<totalPages){dState.page++;renderListPage()}}
    }

    if(pageNums){
        pageNums.innerHTML=""
        const maxBtns=7
        let from=Math.max(1,dState.page-3)
        let to=Math.min(totalPages,from+maxBtns-1)
        from=Math.max(1,to-maxBtns+1)

        for(let p=from;p<=to;p++){
            const b=document.createElement("button")
            b.type="button"
            b.className="tl-btn sm tl-pageNum"+(p===dState.page?" active":"")
            b.textContent=String(p)
            b.addEventListener("click",()=>{dState.page=p;renderListPage()})
            pageNums.appendChild(b)
        }
    }
}

function bindRowActions(rows){
    qsa("[data-toggle]",rows).forEach(btn=>{
        btn.addEventListener("click",async()=>{
            const id=btn.getAttribute("data-toggle")
            const name=btn.getAttribute("data-name")||""
            const ok=await modalConfirm("Đổi trạng thái",`Bạn muốn đổi trạng thái điểm đến "${name}"?`,"Đồng ý")
            if(!ok)return
            try{
                await api(`/api/admin/destinations/${id}/toggle`,{method:"PATCH"})
                toast("ok","Thành công","Đã cập nhật trạng thái")
                loadList()
            }catch(e){
                toast("err","Thất bại",e.message||"Không thể cập nhật")
            }
        })
    })
}

function renderListPage(){
    const rows=qs("rows")
    const msg=qs("msg")
    if(!rows)return

    const total=dState.filtered.length
    const size=dState.pageSize
    const startIdx=(dState.page-1)*size
    const pageItems=dState.filtered.slice(startIdx,startIdx+size)

    renderPager(total)

    if(!pageItems.length){
        rows.innerHTML=`<tr><td colspan="8" style="color:var(--muted);font-weight:900;padding:16px">Không có dữ liệu</td></tr>`
        if(msg)msg.textContent="0 kết quả"
        return
    }

    rows.innerHTML=pageItems.map(x=>{
        const badge=x.isActive
            ?`<span class="tl-badge on"><i></i>ON</span>`
            :`<span class="tl-badge off"><i></i>OFF</span>`
        const toggleText=x.isActive?"Tắt":"Bật"

        return `
            <tr>
                <td>${x.id}</td>
                <td style="font-weight:900">${x.name||""}</td>
                <td class="tl-code">${x.slug||""}</td>
                <td style="font-weight:900">${x.categoryName||""}</td>
                <td style="font-weight:900">${x.type||""}</td>
                <td>${badge}</td>
                <td style="font-weight:900">${x.sortOrder??0}</td>
                <td class="tl-right">
                    <span class="tl-btnRow">
                        <a class="tl-btn sm" href="/admin/destinations/edit?id=${x.id}">Sửa</a>
                        <button class="tl-btn sm ${x.isActive?"danger":"primary"}"
                                data-toggle="${x.id}"
                                data-name="${(x.name||"").replace(/"/g,"&quot;")}">${toggleText}</button>
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
    const categoryId=qs("categoryId")
    if(!rows)return

    rows.innerHTML=`
        <tr class="tl-skeleton"><td colspan="8"><div class="tl-skel" style="height:14px;width:55%"></div></td></tr>
        <tr class="tl-skeleton"><td colspan="8"><div class="tl-skel" style="height:14px;width:75%"></div></td></tr>
        <tr class="tl-skeleton"><td colspan="8"><div class="tl-skel" style="height:14px;width:65%"></div></td></tr>
    `
    if(msg) msg.textContent=""

    try{
        const active=onlyActive&&onlyActive.checked
        const cat=(categoryId&&categoryId.value)?categoryId.value.trim():""

        const params=[]
        if(active) params.push("active=true")
        if(cat) params.push("categoryId="+encodeURIComponent(cat))

        const url="/api/admin/destinations"+(params.length?("?"+params.join("&")):"")
        const items=await api(url,{method:"GET"})

        const key=(q&&q.value?q.value.trim().toLowerCase():"")
        const filtered=key?items.filter(x=>{
            const n=(x.name||"").toLowerCase()
            const s=(x.slug||"").toLowerCase()
            const c=(x.categoryName||"").toLowerCase()
            return n.includes(key)||s.includes(key)||c.includes(key)
        }):items

        dState.filtered=filtered||[]
        if(dState.page<1)dState.page=1
        renderListPage()
    }catch(e){
        rows.innerHTML=`<tr><td colspan="8" style="color:#b42318;font-weight:950;padding:16px">${e.message||"Lỗi tải dữ liệu"}</td></tr>`
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
    const err=qs("err")

    const setErr=(m)=>{if(!err)return;err.style.display="block";err.textContent=m||""}
    const clearErr=()=>{if(!err)return;err.style.display="none";err.textContent=""}

    const nameEl=qs("name")
    const slugEl=qs("slug")
    let manual=false

    if(slugEl){
        slugEl.addEventListener("input",()=>{manual=slugEl.value.trim().length>0})
    }
    if(nameEl&&slugEl){
        nameEl.addEventListener("input",()=>{
            if(manual)return
            slugEl.value=toSlug(nameEl.value)
        })
    }

    if(title) title.textContent=isEdit?"Sửa điểm đến":"Thêm điểm đến"

    if(isEdit){
        try{
            const data=await api(`/api/admin/destinations/${idParam}`,{method:"GET"})
            qs("name").value=data.name||""
            qs("slug").value=data.slug||""
            qs("categoryId").value=data.categoryId||""
            qs("type").value=data.type||"CITY"
            qs("sortOrder").value=data.sortOrder??0
            qs("isActive").checked=!!data.isActive
        }catch(e){
            setErr(e.message||"Không tải được dữ liệu")
            toast("err","Lỗi",e.message||"Không tải được dữ liệu")
        }
    }

    form.addEventListener("submit",async(e)=>{
        e.preventDefault()
        clearErr()

        const payload={
            name:qs("name").value.trim(),
            slug:qs("slug").value.trim(),
            categoryId:Number(qs("categoryId").value||0)||null,
            type:qs("type").value,
            sortOrder:Number(qs("sortOrder").value||0),
            isActive:qs("isActive").checked
        }

        if(!payload.name){setErr("Tên điểm đến không được rỗng");return}
        if(!payload.slug){
            payload.slug=toSlug(payload.name)
            qs("slug").value=payload.slug
        }
        if(!payload.categoryId){setErr("Vui lòng chọn danh mục");return}

        try{
            if(isEdit){
                await api(`/api/admin/destinations/${idParam}`,{method:"PUT",body:JSON.stringify(payload)})
                toast("ok","Đã cập nhật","Điểm đến đã được lưu")
            }else{
                await api(`/api/admin/destinations`,{method:"POST",body:JSON.stringify(payload)})
                toast("ok","Đã tạo","Điểm đến đã được tạo")
            }
            setTimeout(()=>{window.location.href="/admin/destinations"},450)
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
    if(page==="destinations-list"){
        dState.page=1
        loadList()

        const q=qs("q")
        const onlyActive=qs("onlyActive")
        const btnReload=qs("btnReload")
        const categoryId=qs("categoryId")

        if(btnReload) btnReload.addEventListener("click",()=>{dState.page=1;loadList()})
        if(onlyActive) onlyActive.addEventListener("change",()=>{dState.page=1;loadList()})
        if(categoryId) categoryId.addEventListener("change",()=>{dState.page=1;loadList()})
        if(q) q.addEventListener("input",debounce(()=>{dState.page=1;loadList()},260))
    }
    if(page==="destinations-form"){
        loadForm()
    }
})
