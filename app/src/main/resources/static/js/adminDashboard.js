// adminDashboard.js — Admin dashboard controller
// Responsibilities:
//  - Load & render doctor cards
//  - Filter doctors by name / time / specialty
//  - Add new doctor via modal form

import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./doctorCard.js";

// Utility helpers
const $ = (id) => document.getElementById(id);
const contentEl = () => $("content");

function openModalSafe(name) {
    if (typeof window.openModal === "function") return window.openModal(name);
    document.dispatchEvent(new CustomEvent("open-modal", { detail: { modal: name } }));
}

function closeModalSafe() {
    if (typeof window.closeModal === "function") return window.closeModal();
    // fallback: hide common modal containers if present
    const modal = $("modal");
    if (modal) modal.classList.add("hidden");
}

// Attach Add Doctor button listener (from header)
(function wireAddDoctorButton() {
    document.addEventListener("DOMContentLoaded", () => {
        const addBtn = $("addDocBtn");
        if (addBtn) {
            addBtn.addEventListener("click", () => openModalSafe("addDoctor"));
        }
    });
})();

// Load & render all doctors
export async function loadDoctorCards() {
    try {
        const container = contentEl();
        if (!container) return;
        container.innerHTML = ""; // clear

        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (err) {
        console.error("loadDoctorCards error:", err);
    }
}

// Render helper
export function renderDoctorCards(doctors = []) {
    const container = contentEl();
    if (!container) return;
    container.innerHTML = "";

    if (!Array.isArray(doctors) || doctors.length === 0) {
        const empty = document.createElement("p");
        empty.className = "noDoctorRecord";
        empty.textContent = "No doctors to display.";
        container.appendChild(empty);
        return;
    }

    const frag = document.createDocumentFragment();
    doctors.forEach((doc) => frag.appendChild(createDoctorCard(doc)));
    container.appendChild(frag);
}

// Filter handler (on search / dropdown changes)
export async function filterDoctorsOnChange() {
    try {
        const name = ($("searchBar")?.value || "").trim();
        const time = ($("filterTime")?.value || "").trim();
        const specialty = ($("filterSpecialty")?.value || "").trim();

        const { doctors } = await filterDoctors(name || null, time || null, specialty || null);

        if (Array.isArray(doctors) && doctors.length) {
            renderDoctorCards(doctors);
        } else {
            const container = contentEl();
            if (!container) return;
            container.innerHTML = "";
            const msg = document.createElement("p");
            msg.className = "noDoctorRecord";
            msg.textContent = "No doctors found with the given filters.";
            container.appendChild(msg);
        }
    } catch (err) {
        console.error("filterDoctorsOnChange error:", err);
        alert("Unable to filter doctors right now. Please try again.");
    }
}

// Wire search & filters
(function wireFilters() {
    document.addEventListener("DOMContentLoaded", () => {
        const search = $("searchBar");
        const time = $("filterTime");
        const spec = $("filterSpecialty");

        if (search) search.addEventListener("input", filterDoctorsOnChange);
        if (time) time.addEventListener("change", filterDoctorsOnChange);
        if (spec) spec.addEventListener("change", filterDoctorsOnChange);
    });
})();

// Admin: add doctor from modal form
export async function adminAddDoctor() {
    try {
        const token = localStorage.getItem("token");
        if (!token) {
            alert("Admin session expired. Please log in again.");
            return;
        }

        // Prefer conventional addDoctor form IDs; fall back to generic ones if needed
        const getVal = (...ids) => {
            for (const id of ids) {
                const el = $(id);
                if (el) return (el.value || "").trim();
            }
            return "";
        };

        const firstName = getVal("addDoc-firstName", "firstName");
        const lastName = getVal("addDoc-lastName", "lastName");
        const email = getVal("addDoc-email", "email");
        const phone = getVal("addDoc-phone", "phone");
        const password = getVal("addDoc-password", "password");
        const specialization = getVal("addDoc-specialty", "specialty");
        const timesRaw = getVal("addDoc-times", "availableTimes");

        if (!firstName || !lastName || !email || !password || !specialization) {
            alert("Please fill the required fields (first, last, email, password, specialty).");
            return;
        }

        const availableTimes = timesRaw
            ? timesRaw.split(",").map((s) => s.trim()).filter(Boolean)
            : [];

        const doctor = {
            firstName,
            lastName,
            email,
            phone,
            password,
            specialization,
            availableTimes,
        };

        const btn = $("addDocSubmitBtn");
        if (btn) { btn.disabled = true; btn.textContent = "Saving..."; }

        const res = await saveDoctor(doctor, token);

        if (res?.success) {
            alert("Doctor added successfully.");
            closeModalSafe();
            await loadDoctorCards();
        } else {
            alert(res?.message || "Failed to add doctor.");
        }
    } catch (err) {
        console.error("adminAddDoctor error:", err);
        alert("An error occurred while saving the doctor. Please try again.");
    } finally {
        const btn = $("addDocSubmitBtn");
        if (btn) { btn.disabled = false; btn.textContent = "Add Doctor"; }
    }
}

// Expose add handler globally for modal forms to call
window.adminAddDoctor = adminAddDoctor;

// Initial load
document.addEventListener("DOMContentLoaded", loadDoctorCards);
