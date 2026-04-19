function qs(id){return document.getElementById(id)}
function qsa(sel,root){return Array.from((root||document).querySelectorAll(sel))}

function fmtVnd(v){
    const n=Number(v)
    if(Number.isNaN(n)) return String(v??"")
    return n.toLocaleString("vi-VN")
}

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
    if(!wrap) return
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

async function loadList(){
    const rows=qs("rows")
    const onlyActive=qs("onlyActive")
    const q=qs("q")
    const msg=qs("msg")
    if(!rows) return

    const skel=`
        <tr class="tl-skeleton"><td colspan="7"><div class="tl-skel" style="height:14px;width:55%"></div></td></tr>
        <tr class="tl-skeleton"><td colspan="7"><div class="tl-skel" style="height:14px;width:75%"></div></td></tr>
        <tr class="tl-skeleton"><td colspan="7"><div class="tl-skel" style="height:14px;width:65%"></div></td></tr>
    `
    rows.innerHTML=skel
    if(msg) msg.textContent=""

    try{
        const active=onlyActive&&onlyActive.checked
        const items=await api(`/api/admin/tour-lines${active?"?active=true":""}`,{method:"GET"})
        const key=(q&&q.value?q.value.trim().toLowerCase():"")
        const filtered=key?items.filter(x=>{
            const c=(x.code||"").toLowerCase()
            const n=(x.name||"").toLowerCase()
            return c.includes(key)||n.includes(key)
        }):items

        if(!filtered.length){
            rows.innerHTML=`<tr><td colspan="7" style="color:var(--muted);font-weight:900;padding:16px">Không có dữ liệu</td></tr>`
            if(msg) msg.textContent="0 kết quả"
            return
        }

        rows.innerHTML=filtered.map(x=>{
            const badge=x.isActive
                ?`<span class="tl-badge on"><i></i>ON</span>`
                :`<span class="tl-badge off"><i></i>OFF</span>`
            return `
                <tr>
                    <td>${x.id}</td>
                    <td class="tl-code">${x.code||""}</td>
                    <td style="font-weight:900">${x.name||""}</td>
                    <td class="tl-money">${fmtVnd(x.minPrice)} <span style="color:var(--muted);font-weight:900">—</span> ${fmtVnd(x.maxPrice)}</td>
                    <td>${badge}</td>
                    <td style="font-weight:900">${x.sortOrder??0}</td>
                    <td class="tl-right">
                        <span class="tl-btnRow">
                            <a class="tl-btn sm" href="/admin/tour-lines/edit?id=${x.id}">Sửa</a>
                            <button class="tl-btn sm danger" data-del="${x.id}" data-name="${(x.name||"").replace(/"/g,"&quot;")}">Xóa</button>
                        </span>
                    </td>
                </tr>
            `
        }).join("")

        qsa("[data-del]",rows).forEach(btn=>{
            btn.addEventListener("click",async()=>{
                const id=btn.getAttribute("data-del")
                const name=btn.getAttribute("data-name")||""
                const ok=await modalConfirm("Xóa dòng tour",`Bạn chắc chắn muốn xóa "${name}"?`,"Xóa")
                if(!ok) return
                try{
                    await api(`/api/admin/tour-lines/${id}`,{method:"DELETE"})
                    toast("ok","Đã xóa","Dòng tour đã được xóa")
                    loadList()
                }catch(e){
                    toast("err","Xóa thất bại",e.message||"Không thể xóa")
                }
            })
        })

        if(msg) msg.textContent=`${filtered.length} kết quả`
    }catch(e){
        rows.innerHTML=`<tr><td colspan="7" style="color:#b42318;font-weight:950;padding:16px">${e.message||"Lỗi tải dữ liệu"}</td></tr>`
        toast("err","Lỗi",e.message||"Không tải được dữ liệu")
    }
}

async function resolveByPrice(){
    const inp=qs("testPrice")
    const out=qs("resolveResult")
    if(!inp||!out) return
    const v=inp.value.trim()
    if(!v){out.className="tl-res bad";out.textContent="Nhập giá để kiểm tra";return}
    try{
        const r=await api(`/api/admin/tour-lines/resolve?price=${encodeURIComponent(v)}`,{method:"GET"})
        out.className="tl-res ok"
        out.textContent=`→ ${r.name} (${r.code})`
        toast("ok","Resolve thành công",`${r.name} • ${fmtVnd(v)}`)
    }catch(e){
        out.className="tl-res bad"
        out.textContent=e.message||"Không tìm thấy"
        toast("err","Không resolve được",e.message||"Không có dòng tour phù hợp")
    }
}

function toCode(v){
    if(!v)return"";
    return v
        .normalize("NFD").replace(/[\u0300-\u036f]/g,"")
        .replace(/đ/g,"d").replace(/Đ/g,"D")
        .replace(/[^a-zA-Z0-9]+/g,"_")
        .replace(/^_+|_+$/g,"")
        .replace(/_+/g,"_")
        .toUpperCase();
}

function initAutoCode(){
    const nameEl=qs("name");
    const codeEl=qs("code");
    if(!nameEl||!codeEl)return;

    let manual=false;

    codeEl.addEventListener("input",()=>{
        manual=codeEl.value.trim().length>0;
    });

    nameEl.addEventListener("input",()=>{
        if(manual)return;
        codeEl.value=toCode(nameEl.value);
    });
}

function rangesOverlap(aMin,aMax,bMin,bMax){
    return aMin<=bMax&&aMax>=bMin;
}

async function validateRangeUnique(minPrice,maxPrice,currentId){
    const items=await api(`/api/admin/tour-lines`,{method:"GET"});
    const cid=currentId?String(currentId):"";
    const conflict=items.find(x=>{
        if(cid&&String(x.id)===cid)return false;
        const mn=Number(x.minPrice??0);
        const mx=Number(x.maxPrice??0);
        return rangesOverlap(minPrice,maxPrice,mn,mx);
    });
    if(conflict){
        throw new Error(`Khoảng giá bị trùng với "${conflict.name}" (${conflict.code}) [${fmtVnd(conflict.minPrice)} - ${fmtVnd(conflict.maxPrice)}]`);
    }
}


async function loadForm(){
    const form=qs("form")
    if(!form)return

    const idHidden=qs("id")
    const idParam=(idHidden&&idHidden.value)?idHidden.value:""
    const isEdit=!!idParam
    const title=qs("title")
    const btnDelete=qs("btnDelete")
    const err=qs("err")

    const setErr=(m)=>{
        if(!err)return
        err.style.display="block"
        err.textContent=m||""
    }
    const clearErr=()=>{
        if(!err)return
        err.style.display="none"
        err.textContent=""
    }

    function toCode(v){
        if(!v)return""
        return v
            .normalize("NFD").replace(/[\u0300-\u036f]/g,"")
            .replace(/đ/g,"d").replace(/Đ/g,"D")
            .replace(/[^a-zA-Z0-9]+/g,"_")
            .replace(/^_+|_+$/g,"")
            .replace(/_+/g,"_")
            .toUpperCase()
    }

    function rangesOverlap(aMin,aMax,bMin,bMax){
        return aMin<=bMax&&aMax>=bMin
    }

    async function validateRangeUnique(minPrice,maxPrice,currentId){
        const items=await api(`/api/admin/tour-lines`,{method:"GET"})
        const cid=currentId?String(currentId):""
        const conflict=items.find(x=>{
            if(cid&&String(x.id)===cid)return false
            const mn=Number(x.minPrice??0)
            const mx=Number(x.maxPrice??0)
            return rangesOverlap(minPrice,maxPrice,mn,mx)
        })
        if(conflict){
            throw new Error(`Khoảng giá bị trùng với "${conflict.name}" (${conflict.code}) [${fmtVnd(conflict.minPrice)} - ${fmtVnd(conflict.maxPrice)}]`)
        }
    }

    function initAutoCode(){
        const nameEl=qs("name")
        const codeEl=qs("code")
        if(!nameEl||!codeEl)return
        let manual=false

        codeEl.addEventListener("input",()=>{
            manual=codeEl.value.trim().length>0
        })

        nameEl.addEventListener("input",()=>{
            if(manual)return
            codeEl.value=toCode(nameEl.value)
        })
    }

    if(title)title.textContent=isEdit?"Sửa dòng tour":"Thêm dòng tour"
    if(btnDelete)btnDelete.style.display=isEdit?"":"none"

    initAutoCode()

    if(isEdit){
        try{
            const data=await api(`/api/admin/tour-lines/${idParam}`,{method:"GET"})
            qs("code").value=data.code||""
            qs("name").value=data.name||""
            qs("minPrice").value=data.minPrice??0
            qs("maxPrice").value=data.maxPrice??0
            qs("sortOrder").value=data.sortOrder??0
            qs("isActive").checked=!!data.isActive
        }catch(e){
            setErr(e.message||"Không tải được dữ liệu")
            toast("err","Lỗi",e.message||"Không tải được dữ liệu")
        }
    }

    const liveCheck=debounce(async()=>{
        clearErr()
        const minPrice=Number(qs("minPrice").value||0)
        const maxPrice=Number(qs("maxPrice").value||0)
        if(minPrice>maxPrice)return
        try{
            await validateRangeUnique(minPrice,maxPrice,idParam)
        }catch(e){
            setErr(e.message||"Khoảng giá bị trùng")
        }
    },260)

    if(qs("minPrice"))qs("minPrice").addEventListener("input",liveCheck)
    if(qs("maxPrice"))qs("maxPrice").addEventListener("input",liveCheck)

    if(btnDelete){
        btnDelete.addEventListener("click",async()=>{
            const name=qs("name").value||""
            const ok=await modalConfirm("Xóa dòng tour",`Bạn chắc chắn muốn xóa "${name}"?`,"Xóa")
            if(!ok)return
            clearErr()
            try{
                await api(`/api/admin/tour-lines/${idParam}`,{method:"DELETE"})
                toast("ok","Đã xóa","Chuyển về danh sách")
                window.location.href="/admin/tour-lines"
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
            minPrice:Number(qs("minPrice").value||0),
            maxPrice:Number(qs("maxPrice").value||0),
            sortOrder:Number(qs("sortOrder").value||0),
            isActive:qs("isActive").checked
        }

        if(!payload.name){setErr("Tên không được rỗng");return}
        if(!payload.code){
            payload.code=toCode(payload.name)
            qs("code").value=payload.code
        }
        if(payload.minPrice>payload.maxPrice){setErr("Min price phải <= Max price");return}

        try{
            await validateRangeUnique(payload.minPrice,payload.maxPrice,idParam)
        }catch(vex){
            setErr(vex.message||"Khoảng giá bị trùng")
            toast("err","Không hợp lệ",vex.message||"Khoảng giá bị trùng")
            return
        }

        try{
            if(isEdit){
                await api(`/api/admin/tour-lines/${idParam}`,{method:"PUT",body:JSON.stringify(payload)})
                toast("ok","Đã cập nhật","Dòng tour đã được lưu")
            }else{
                await api(`/api/admin/tour-lines`,{method:"POST",body:JSON.stringify(payload)})
                toast("ok","Đã tạo","Dòng tour đã được tạo")
            }
            setTimeout(()=>{window.location.href="/admin/tour-lines"},450)
        }catch(ex){
            setErr(ex.message||"Lưu thất bại")
            toast("err","Lưu thất bại",ex.message||"Không thể lưu")
        }
    })
}


document.addEventListener("DOMContentLoaded",()=>{
    const page=getPage()
    if(page==="tour-lines-list"){
        loadList()
        const q=qs("q")
        const onlyActive=qs("onlyActive")
        const btnReload=qs("btnReload")
        const btnResolve=qs("btnResolve")

        if(btnReload) btnReload.addEventListener("click",loadList)
        if(onlyActive) onlyActive.addEventListener("change",loadList)
        if(q) q.addEventListener("input",debounce(loadList,260))
        if(btnResolve) btnResolve.addEventListener("click",resolveByPrice)

        const tp=qs("testPrice")
        if(tp) tp.addEventListener("keydown",(e)=>{if(e.key==="Enter"){e.preventDefault();resolveByPrice()}})
    }
    if(page==="tour-lines-form"){
        loadForm()
    }
})
