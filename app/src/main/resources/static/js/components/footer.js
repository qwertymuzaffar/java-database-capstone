/* footer.js — dynamic footer rendering
   Injects footer HTML with logo, copyright, and link groups.
*/
(function () {
    function renderFooter() {
        const footerDiv = document.getElementById("footer");
        if (!footerDiv) return;

        footerDiv.innerHTML = `
      <footer class="footer">
        <div class="footer-container">
          <div class="footer-logo">
            <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo" class="footer-logo-img" />
            <p>© Copyright ${new Date().getFullYear()}. All Rights Reserved by Hospital CMS.</p>
          </div>

          <div class="footer-links">
            <div class="footer-column">
              <h4>Company</h4>
              <a href="#">About</a>
              <a href="#">Careers</a>
              <a href="#">Press</a>
            </div>

            <div class="footer-column">
              <h4>Support</h4>
              <a href="#">Account</a>
              <a href="#">Help Center</a>
              <a href="#">Contact Us</a>
            </div>

            <div class="footer-column">
              <h4>Legals</h4>
              <a href="#">Terms &amp; Conditions</a>
              <a href="#">Privacy Policy</a>
              <a href="#">Licensing</a>
            </div>
          </div>
        </div>
      </footer>
    `;
    }

    // Expose to global scope if needed
    window.renderFooter = renderFooter;

    // Auto-run when DOM is ready
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", renderFooter);
    } else {
        renderFooter();
    }
})();
