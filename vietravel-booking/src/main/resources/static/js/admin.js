(function(){
    function qs(s,root){return (root||document).querySelector(s);}
    function qsa(s,root){return Array.from((root||document).querySelectorAll(s));}

    function clamp(n,min,max){return Math.max(min,Math.min(max,n));}
    function easeOutCubic(t){return 1-Math.pow(1-t,3);}

    function animateNumber(el,to,duration){
        var from=parseFloat((el.getAttribute("data-from")||"0").replace(/[^\d.-]/g,""))||0;
        var start=performance.now();
        var isCurrency=/₫/.test(el.textContent)||el.hasAttribute("data-currency");
        var decimals=parseInt(el.getAttribute("data-decimals")||"0",10);
        function fmt(v){
            if(isCurrency){
                var n=Math.round(v);
                return n.toLocaleString("vi-VN")+" ₫";
            }
            if(decimals>0)return v.toFixed(decimals);
            return Math.round(v).toString();
        }
        function frame(now){
            var p=clamp((now-start)/duration,0,1);
            var v=from+(to-from)*easeOutCubic(p);
            el.textContent=fmt(v);
            if(p<1)requestAnimationFrame(frame);
        }
        requestAnimationFrame(frame);
    }

    function closeAllDropdown(){
        qsa("[data-dropdown]").forEach(function(box){box.classList.remove("open");});
    }

    function initDropdowns(){
        document.addEventListener("click",function(e){
            var btn=e.target.closest("[data-dropdown-btn]");
            if(btn){
                var box=btn.closest("[data-dropdown]");
                if(!box)return;
                var isOpen=box.classList.contains("open");
                closeAllDropdown();
                if(!isOpen)box.classList.add("open");
                return;
            }
            if(!e.target.closest("[data-dropdown]"))closeAllDropdown();
        });
        document.addEventListener("keydown",function(e){
            if(e.key==="Escape")closeAllDropdown();
        });
    }

    function initSidebarToggle(){
        qsa("[data-sidebar-toggle]").forEach(function(btn){
            btn.addEventListener("click",function(){
                document.body.classList.toggle("sidebar-collapsed");
            });
        });
    }

    function toast(msg,type){
        var wrap=qs(".toast-wrap");
        if(!wrap){
            wrap=document.createElement("div");
            wrap.className="toast-wrap";
            document.body.appendChild(wrap);
        }
        var t=document.createElement("div");
        t.className="toast "+(type?("toast-"+type):"");
        t.innerHTML='<div class="toast-dot"></div><div class="toast-msg"></div><button class="toast-x" type="button">×</button>';
        qs(".toast-msg",t).textContent=msg;
        qs(".toast-x",t).addEventListener("click",function(){t.classList.add("out");setTimeout(function(){t.remove();},220);});
        wrap.appendChild(t);
        setTimeout(function(){t.classList.add("show");},10);
        setTimeout(function(){t.classList.add("out");setTimeout(function(){t.remove();},220);},2600);
    }

    function initPageTransitions(){
        var shell=qs(".admin-shell");
        if(!shell)return;

        document.documentElement.classList.add("js");
        document.body.classList.add("page-enter");
        requestAnimationFrame(function(){document.body.classList.add("page-enter-active");});
        setTimeout(function(){
            document.body.classList.remove("page-enter");
            document.body.classList.remove("page-enter-active");
        },280);

        document.addEventListener("click",function(e){
            var a=e.target.closest("a");
            if(!a)return;

            var href=a.getAttribute("href")||"";
            var target=a.getAttribute("target");
            if(target==="_blank")return;
            if(href.startsWith("#")||href.startsWith("mailto:")||href.startsWith("tel:"))return;
            if(href.startsWith("javascript:"))return;
            if(a.hasAttribute("download"))return;

            var origin=location.origin;
            try{
                var u=new URL(href,location.href);
                if(u.origin!==origin)return;
            }catch(_){
                return;
            }

            if(e.metaKey||e.ctrlKey||e.shiftKey||e.altKey)return;

            e.preventDefault();
            document.body.classList.add("page-exit");
            setTimeout(function(){location.href=href;},160);
        });
    }

    function initRevealOnScroll(){
        var els=qsa(".card,.kpi,.list-item,.table tbody tr");
        if(els.length===0)return;

        els.forEach(function(el){el.classList.add("reveal");});

        var io=new IntersectionObserver(function(entries){
            entries.forEach(function(en){
                if(en.isIntersecting){
                    en.target.classList.add("reveal-in");
                    io.unobserve(en.target);
                }
            });
        },{threshold:0.12});

        els.forEach(function(el){io.observe(el);});
    }

    function initKPICounters(){
        var els=qsa("[data-counter]");
        els.forEach(function(el){
            var to=parseFloat((el.getAttribute("data-to")||"0").replace(/[^\d.-]/g,""))||0;
            var duration=parseInt(el.getAttribute("data-duration")||"900",10);
            var once=false;

            var io=new IntersectionObserver(function(entries){
                entries.forEach(function(en){
                    if(en.isIntersecting && !once){
                        once=true;
                        animateNumber(el,to,duration);
                        io.disconnect();
                    }
                });
            },{threshold:0.35});

            io.observe(el);
        });
    }

    function initTableSearch(){
        var input=qs("[data-table-search]");
        var table=qs("[data-table]");
        if(!input||!table)return;

        var rows=qsa("tbody tr",table);
        function norm(s){return (s||"").toLowerCase().trim();}
        input.addEventListener("input",function(){
            var q=norm(input.value);
            var shown=0;
            rows.forEach(function(r){
                var text=norm(r.textContent);
                var ok=!q || text.indexOf(q)>-1;
                r.style.display=ok?"":"none";
                if(ok)shown++;
            });
            var c=qs("[data-table-count]");
            if(c)c.textContent=shown.toString();
        });
    }

    function initSkeletonLoading(){
        var btn=qs("[data-demo-loading]");
        if(!btn)return;

        btn.addEventListener("click",function(){
            var blocks=qsa("[data-skeleton]");
            blocks.forEach(function(b){b.classList.add("skeleton");});
            toast("Đang tải dữ liệu...","info");
            setTimeout(function(){
                blocks.forEach(function(b){b.classList.remove("skeleton");});
                toast("Tải dữ liệu thành công","success");
            },900);
        });
    }

    function initActiveNavInk(){
        var active=qs(".nav-item.active");
        if(!active)return;
        active.classList.add("pulse-active");
        setTimeout(function(){active.classList.remove("pulse-active");},900);
    }

    function initSidebarAutoScroll(){
        var sidebar=qs(".admin-sidebar");
        var nav=qs(".sidebar-nav",sidebar);
        if(!sidebar||!nav)return;

        var target=qs(".nav-sub-item.active",nav)||qs(".nav-item.active",nav);
        if(!target)return;

        var group=target.closest(".nav-group");
        if(group)group.classList.add("open");

        requestAnimationFrame(function(){
            try{
                target.scrollIntoView({block:"center",inline:"nearest",behavior:"instant"});
            }catch(_){
                target.scrollIntoView();
            }
        });
    }

    initDropdowns();
    initSidebarToggle();
    initPageTransitions();
    initRevealOnScroll();
    initKPICounters();
    initTableSearch();
    initSkeletonLoading();
    initActiveNavInk();
    initSidebarAutoScroll();

    window.AdminUI={toast:toast};
})();
