document.addEventListener("DOMContentLoaded",()=>{
    const burger=document.getElementById("burgerBtn");
    const mobileNav=document.getElementById("mobileNav");
    if(burger&&mobileNav){
        burger.addEventListener("click",()=>{
            const open=mobileNav.style.display==="block";
            mobileNav.style.display=open?"none":"block";
        });
    }

    const currencyBtn=document.getElementById("currencyBtn");
    if(currencyBtn){
        const list=["USD","VND","EUR"];
        currencyBtn.addEventListener("click",()=>{
            const cur=currencyBtn.textContent.trim();
            const idx=list.indexOf(cur);
            currencyBtn.textContent=list[(idx+1)%list.length];
        });
    }
});
