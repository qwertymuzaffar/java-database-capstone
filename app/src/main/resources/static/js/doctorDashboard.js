// doctorDashboard.js — appointments listing, search, and date filters for doctors
// Imports
import { getAllAppointments } from "./services/appointmentServices.js";
import { createPatientRow } from "./patientRow.js";

// DOM refs (lazy getters to handle SSR/static inclusion)
const $ = (id) => document.getElementById(id);
const tbody = () => $("patientTableBody");
const searchBar = () => $("searchBar");
const todayBtn = () => $("todayButton");
const datePicker = () => $("datePicker");

// State
let selectedDate = formatDateYYYYMMDD(new Date());
let patientName = null; // backend expects string "null" when empty; handled in load
let token = null;

// Helpers
function formatDateYYYYMMDD(d) {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function clearTableBody() {
    const body = tbody();
    if (body) body.innerHTML = "";
}

function appendMessageRow(text) {
    const body = tbody();
    if (!body) return;
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.colSpan = 5; // Patient ID, Name, Phone No., Email, Prescription
    td.textContent = text;
    td.className = "noPatientRecord";
    tr.appendChild(td);
    body.appendChild(tr);
}

// Core: fetch & render appointments
export async function loadAppointments() {
    try {
        token = localStorage.getItem("token") || null;

        clearTableBody();

        const nameParam = (patientName && patientName.trim()) ? patientName.trim() : "null"; // backend expects "null" when empty
        const data = await getAllAppointments(selectedDate, nameParam, token);

        const list = Array.isArray(data?.appointments) ? data.appointments : (Array.isArray(data) ? data : []);

        if (!list.length) {
            appendMessageRow("No Appointments found for today.");
            return;
        }

        const body = tbody();
        if (!body) return;

        const frag = document.createDocumentFragment();
        for (const appt of list) {
            const patient = {
                id: appt?.patientId ?? appt?.patient?.id ?? "",
                name: appt?.patientName ?? appt?.patient?.name ?? "",
                phone: appt?.patientPhone ?? appt?.patient?.phone ?? "",
                email: appt?.patientEmail ?? appt?.patient?.email ?? "",
            };

            // createPatientRow should return a <tr> element
            const row = createPatientRow(appt, patient);
            frag.appendChild(row);
        }
        body.appendChild(frag);
    } catch (err) {
        console.error("loadAppointments error:", err);
        appendMessageRow("Error loading appointments. Try again later.");
    }
}

// Filters & events
function wireSearchAndFilters() {
    const sb = searchBar();
    if (sb) {
        sb.addEventListener("input", (e) => {
            const val = (e.target.value || "").trim();
            patientName = val || null; // null -> will map to "null" in load
            loadAppointments();
        });
    }

    const tb = todayBtn();
    if (tb) {
        tb.addEventListener("click", () => {
            const today = new Date();
            selectedDate = formatDateYYYYMMDD(today);
            const dp = datePicker();
            if (dp) dp.value = selectedDate;
            loadAppointments();
        });
    }

    const dp = datePicker();
    if (dp) {
        // initialize with today on first load
        if (!dp.value) dp.value = selectedDate;
        dp.addEventListener("change", (e) => {
            const v = (e.target.value || "").trim();
            selectedDate = v || formatDateYYYYMMDD(new Date());
            loadAppointments();
        });
    }
}

// Boot
document.addEventListener("DOMContentLoaded", () => {
    try {
        // Optional: if your render.js exposes this
        if (typeof window.renderContent === "function") {
            window.renderContent();
        }
    } catch (_) {}

    wireSearchAndFilters();
    loadAppointments();
});
