document.addEventListener('DOMContentLoaded', function() {
    const toggleBtn = document.querySelector(".toggle-btn");
    const sidebar = document.querySelector(".navbar-vertical");
    const navWrap = document.querySelector(".navbar-vertical-nav");
    const mainWrapper = document.querySelector(".main-wrapper");

    if (toggleBtn && sidebar && navWrap) {
        toggleBtn.addEventListener("click", function () {

            // Collapse both outer wrapper + actual sidebar
            navWrap.classList.toggle("collapse");
            sidebar.classList.toggle("collapse");

            // Optional: add class to wrapper if cần
            if (mainWrapper) {
                mainWrapper.classList.toggle("sidebar-collapsed");
            }
        });
    }
});
