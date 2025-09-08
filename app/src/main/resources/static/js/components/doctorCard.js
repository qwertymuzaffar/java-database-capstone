// doctorCard.js — builds a role-aware doctor card with actions
// Imports (adjust paths to your structure)
import { showBookingOverlay } from "./loggedPatient.js"; // booking overlay UI
import { deleteDoctor } from "./services/doctorServices.js"; // admin API
import { getPatientDetails } from "./services/patientServices.js"; // patient API

/**
 * Create and return a DOM element representing a single doctor card.
 * @param {Object} doctor
 * @param {string} doctor.id
 * @param {string} doctor.firstName
 * @param {string} doctor.lastName
 * @param {string} doctor.specialization
 * @param {string} doctor.email
 * @param {string[]} [doctor.availableTimes] e.g., ["09:00 AM", "10:30 AM"]
 * @returns {HTMLElement}
 */
export function createDoctorCard(doctor) {
    // --- Main card container ---
    const card = document.createElement("div");
    card.className = "doctor-card";
    card.dataset.id = doctor.id;

    // --- Current role ---
    const role = (localStorage.getItem("userRole") || "").trim();

    // --- Info container ---
    const info = document.createElement("div");
    info.className = "doctor-info";

    const nameEl = document.createElement("h3");
    nameEl.className = "doctor-name";
    nameEl.textContent = `${doctor.firstName ?? ""} ${doctor.lastName ?? ""}`.trim() || "Unnamed Doctor";

    const specEl = document.createElement("p");
    specEl.className = "doctor-spec";
    specEl.textContent = doctor.specialization || "Specialization: N/A";

    const emailEl = document.createElement("p");
    emailEl.className = "doctor-email";
    emailEl.textContent = doctor.email || "Email: N/A";

    const timesWrap = document.createElement("div");
    timesWrap.className = "doctor-times";
    const timesLabel = document.createElement("span");
    timesLabel.className = "times-label";
    timesLabel.textContent = "Available:";
    const timesList = document.createElement("ul");
    timesList.className = "times-list";
    (doctor.availableTimes || []).forEach((t) => {
        const li = document.createElement("li");
        li.textContent = t;
        timesList.appendChild(li);
    });
    timesWrap.appendChild(timesLabel);
    timesWrap.appendChild(timesList);

    info.appendChild(nameEl);
    info.appendChild(specEl);
    info.appendChild(emailEl);
    info.appendChild(timesWrap);

    // --- Actions container ---
    const actions = document.createElement("div");
    actions.className = "doctor-actions";

    // === ADMIN ROLE ACTIONS ===
    if (role === "admin") {
        const delBtn = document.createElement("button");
        delBtn.className = "adminBtn delete-doctor-btn";
        delBtn.type = "button";
        delBtn.textContent = "Delete";

        delBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Admin session expired. Please log in again.");
                window.location.href = "/";
                return;
            }

            if (!confirm(`Delete Dr. ${nameEl.textContent}? This cannot be undone.`)) return;

            try {
                delBtn.disabled = true;
                delBtn.textContent = "Deleting...";
                const res = await deleteDoctor(doctor.id, token);
                if (res?.success) {
                    card.remove();
                    alert("Doctor removed successfully.");
                } else {
                    const msg = res?.message || "Failed to delete doctor.";
                    alert(msg);
                }
            } catch (err) {
                console.error("deleteDoctor error", err);
                alert("An unexpected error occurred while deleting the doctor.");
            } finally {
                delBtn.disabled = false;
                delBtn.textContent = "Delete";
            }
        });

        actions.appendChild(delBtn);
    }

    // === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
    if (role === "patient") {
        const bookBtn = document.createElement("button");
        bookBtn.className = "btn book-now-btn";
        bookBtn.type = "button";
        bookBtn.textContent = "Book Now";

        bookBtn.addEventListener("click", () => {
            alert("Please log in to book an appointment.");
            // Optionally open login modal
            if (typeof window.openModal === "function") {
                window.openModal("patientLogin");
            } else {
                document.dispatchEvent(new CustomEvent("open-modal", { detail: { modal: "patientLogin" } }));
            }
        });

        actions.appendChild(bookBtn);
    }

    // === LOGGED-IN PATIENT ROLE ACTIONS ===
    if (role === "loggedPatient") {
        const bookBtn = document.createElement("button");
        bookBtn.className = "btn book-now-btn";
        bookBtn.type = "button";
        bookBtn.textContent = "Book Now";

        bookBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Session expired. Please log in again.");
                window.location.href = "/";
                return;
            }

            try {
                bookBtn.disabled = true;
                bookBtn.textContent = "Loading...";

                // fetch current patient details
                const patient = await getPatientDetails(token);
                if (!patient) {
                    alert("Unable to fetch patient details. Please try again.");
                    return;
                }

                // open booking overlay UI
                await showBookingOverlay({
                    doctor: {
                        id: doctor.id,
                        name: nameEl.textContent,
                        specialization: doctor.specialization,
                        email: doctor.email,
                        availableTimes: doctor.availableTimes || [],
                    },
                    patient,
                    token,
                });
            } catch (err) {
                console.error("book-now error", err);
                alert("Could not open booking. Please try again.");
            } finally {
                bookBtn.disabled = false;
                bookBtn.textContent = "Book Now";
            }
        });

        actions.appendChild(bookBtn);
    }

    // --- Assemble card ---
    card.appendChild(info);
    card.appendChild(actions);
    return card;
}
