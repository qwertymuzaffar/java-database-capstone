/* header.js — dynamic header rendering
   Renders a role‑aware header (logo, nav actions) and wires up login/logout.
   Expects localStorage keys:
     - userRole: "admin" | "doctor" | "patient" | "loggedPatient"
     - token: auth token for admin/doctor/loggedPatient sessions
*/
(function () {
    const ROOT_PATHS = ["/", "/index.html", "", null, undefined];

    function isRootPath() {
        const p = (window.location.pathname || "").toLowerCase();
        return p === "/" || p.endsWith("/index") || p.endsWith("/index.html");
    }

    function safeRedirect(href) {
        try {
            window.location.href = href;
        } catch (_) {
            // fallback for very old browsers
            window.location.assign(href);
        }
    }

    function getEl(id) {
        return document.getElementById(id);
    }

    function logoSection() {
        // Use root‑relative asset path so it works from any nested page.
        return (
            '<div class="logo-section">' +
            '  <img src="/assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img" />' +
            '  <span class="logo-title">Hospital CMS</span>' +
            "</div>"
        );
    }

    function renderHeader() {
        const headerDiv = getEl("header");
        if (!headerDiv) return;

        // If we are on the root (landing) page, clear role and show bare header
        if (isRootPath()) {
            try { localStorage.removeItem("userRole"); } catch (_) {}
            headerDiv.innerHTML =
                '<header class="header">' +
                logoSection() +
                "</header>";
            return;
        }

        // Retrieve session
        const role = (localStorage.getItem("userRole") || "").trim();
        const token = localStorage.getItem("token");

        // Base header scaffold
        let headerContent = '<header class="header">' + logoSection() + '<nav class="nav-actions">';

        // Guard: session required for certain roles
        if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
            try { localStorage.removeItem("userRole"); localStorage.removeItem("token"); } catch (_) {}
            alert("Session expired or invalid login. Please log in again.");
            safeRedirect("/");
            return;
        }

        // Role‑specific actions
        if (role === "admin") {
            headerContent +=
                '<button id="addDocBtn" class="adminBtn" type="button" data-action="add-doctor">Add Doctor</button>' +
                '<a href="#" id="logoutLink" class="logout-link" data-action="logout">Logout</a>';
        } else if (role === "doctor") {
            headerContent +=
                '<button class="adminBtn" id="doctorHomeBtn" type="button" data-action="doctor-home">Home</button>' +
                '<a href="#" id="logoutLink" class="logout-link" data-action="logout">Logout</a>';
        } else if (role === "patient") {
            headerContent +=
                '<button id="patientLogin" class="adminBtn" type="button" data-action="patient-login">Login</button>' +
                '<button id="patientSignup" class="adminBtn" type="button" data-action="patient-signup">Sign Up</button>';
        } else if (role === "loggedPatient") {
            headerContent +=
                '<button id="homeBtn" class="adminBtn" type="button" data-action="lp-home">Home</button>' +
                '<button id="appointmentsBtn" class="adminBtn" type="button" data-action="lp-appointments">Appointments</button>' +
                '<a href="#" id="logoutPatientLink" class="logout-link" data-action="logout-patient">Logout</a>';
        }

        // Close header
        headerContent += "</nav></header>";

        // Render
        headerDiv.innerHTML = headerContent;

        // Wire listeners
        attachHeaderButtonListeners();
    }

    function attachHeaderButtonListeners() {
        // Admin: Add Doctor -> open modal
        const addDocBtn = getEl("addDocBtn");
        if (addDocBtn) {
            addDocBtn.addEventListener("click", function () {
                if (typeof window.openModal === "function") {
                    window.openModal("addDoctor");
                } else {
                    // Dispatch a custom event so the page script can handle it
                    document.dispatchEvent(new CustomEvent("open-modal", { detail: { modal: "addDoctor" } }));
                }
            });
        }

        // Doctor: home
        const doctorHomeBtn = getEl("doctorHomeBtn");
        if (doctorHomeBtn) {
            doctorHomeBtn.addEventListener("click", function () {
                // If you have a route that sets role=doctor, you can reuse existing handler
                if (typeof window.selectRole === "function") {
                    window.selectRole("doctor");
                } else {
                    safeRedirect("/pages/doctorDashboard.html");
                }
            });
        }

        // Patient (not logged): login + signup
        const patientLogin = getEl("patientLogin");
        if (patientLogin) {
            patientLogin.addEventListener("click", function () {
                if (typeof window.openModal === "function") {
                    window.openModal("patientLogin");
                } else {
                    document.dispatchEvent(new CustomEvent("open-modal", { detail: { modal: "patientLogin" } }));
                }
            });
        }

        const patientSignup = getEl("patientSignup");
        if (patientSignup) {
            patientSignup.addEventListener("click", function () {
                if (typeof window.openModal === "function") {
                    window.openModal("patientSignup");
                } else {
                    document.dispatchEvent(new CustomEvent("open-modal", { detail: { modal: "patientSignup" } }));
                }
            });
        }

        // Logged patient shortcuts
        const homeBtn = getEl("homeBtn");
        if (homeBtn) {
            homeBtn.addEventListener("click", function () {
                safeRedirect("/pages/loggedPatientDashboard.html");
            });
        }

        const apptsBtn = getEl("appointmentsBtn");
        if (apptsBtn) {
            apptsBtn.addEventListener("click", function () {
                safeRedirect("/pages/patientAppointments.html");
            });
        }

        // Logout links
        const logoutLink = getEl("logoutLink");
        if (logoutLink) logoutLink.addEventListener("click", function (e) { e.preventDefault(); logout(); });

        const logoutPatientLink = getEl("logoutPatientLink");
        if (logoutPatientLink) logoutPatientLink.addEventListener("click", function (e) { e.preventDefault(); logoutPatient(); });
    }

    function logout() {
        try {
            localStorage.removeItem("userRole");
            localStorage.removeItem("token");
        } catch (_) {}
        safeRedirect("/");
    }

    function logoutPatient() {
        try {
            localStorage.removeItem("token");
            localStorage.setItem("userRole", "patient"); // return to anon patient state
        } catch (_) {}
        safeRedirect("/pages/loggedPatientDashboard.html");
    }

    // Expose globals (if needed elsewhere)
    window.renderHeader = renderHeader;
    window.attachHeaderButtonListeners = attachHeaderButtonListeners;
    window.logout = logout;
    window.logoutPatient = logoutPatient;

    // Auto-render on DOM ready if #header exists
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", renderHeader);
    } else {
        renderHeader();
    }
})();
