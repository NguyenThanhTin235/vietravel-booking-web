document.addEventListener("DOMContentLoaded",()=>{
    const els=[...document.querySelectorAll(".reveal")];
    if(!els.length)return;

    const io=new IntersectionObserver((entries)=>{
        entries.forEach(e=>{
            if(e.isIntersecting)e.target.classList.add("is-in");
        });
    },{threshold:0.12});

    els.forEach(el=>io.observe(el));

    const form=document.getElementById("heroSearch");
    if(form){
        form.addEventListener("submit",()=>{
            form.classList.add("is-submitting");
            setTimeout(()=>form.classList.remove("is-submitting"),500);
        });
    }
});
