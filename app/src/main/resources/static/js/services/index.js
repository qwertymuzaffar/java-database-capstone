// index.js — landing page services: role selection + admin/doctor login handlers
// NOTE: This file is loaded with <script type="module"> in index.html

// 1) Import helpers
//    - openModal: used to open login modals (admin/doctor)
//    - API_BASE_URL: base URL for backend APIs
// Use dynamic import for openModal so this file works even if the modal module isn't present yet.
let openModalFn = (typeof window !== "undefined" && window.openModal) ? window.openModal : null;
try {
    const modalMod = await import('../components/modal.js');
    if (modalMod && typeof modalMod.openModal === 'function') {
        openModalFn = modalMod.openModal;
    }
} catch (_) {
    // If modal module not found, we'll fallback to dispatching an event
}

import { API_BASE_URL } from '../config.js';

// 2) Define endpoints (adjust these paths to match your backend routes)
export const ADMIN_API = `${API_BASE_URL}/api/admin/login`;
export const DOCTOR_API = `${API_BASE_URL}/api/doctor/login`;

// Utility: safe modal opener
function openModal(name) {
    if (typeof openModalFn === 'function') return openModalFn(name);
    // Fallback: dispatch an event for any page-level listener to handle
    document.dispatchEvent(new CustomEvent('open-modal', { detail: { modal: name } }));
}

// Utility: pick element by id
const $ = (id) => document.getElementById(id);

// 3) Wire landing buttons after the page loads
window.addEventListener('load', () => {
    const adminBtn = $('adminLogin');
    if (adminBtn) {
        adminBtn.addEventListener('click', () => openModal('adminLogin'));
    }

    const doctorBtn = $('doctorLogin');
    if (doctorBtn) {
        doctorBtn.addEventListener('click', () => openModal('doctorLogin'));
    }
});

// 4) Admin login handler (global)
window.adminLoginHandler = async function adminLoginHandler() {
    try {
        // Step 1: Read credentials
        const username = ($('admin-username')?.value || '').trim();
        const password = ($('admin-password')?.value || '').trim();
        if (!username || !password) {
            alert('Please enter username and password.');
            return;
        }

        // Step 2: Compose payload
        const admin = { username, password };

        // Step 3: POST to admin auth endpoint
        const resp = await fetch(ADMIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(admin),
        });

        if (!resp.ok) {
            // e.g. 401/403
            alert('Invalid admin credentials.');
            return;
        }

        // Step 4: Parse token, store, and continue
        const data = await resp.json();
        const token = data?.token || data?.accessToken || data?.jwt || '';
        if (!token) {
            alert('Login succeeded but no token returned.');
            return;
        }

        localStorage.setItem('token', token);
        localStorage.setItem('userRole', 'admin');

        if (typeof window.selectRole === 'function') {
            window.selectRole('admin');
        } else {
            // Fallback: redirect to admin dashboard
            window.location.href = '/pages/adminDashboard.html';
        }
    } catch (err) {
        console.error('adminLoginHandler error:', err);
        alert('An error occurred during admin login. Please try again.');
    }
};

// 5) Doctor login handler (global)
window.doctorLoginHandler = async function doctorLoginHandler() {
    try {
        // Step 1: Read credentials
        const email = ($('doctor-email')?.value || '').trim();
        const password = ($('doctor-password')?.value || '').trim();
        if (!email || !password) {
            alert('Please enter email and password.');
            return;
        }

        // Step 2: Compose payload
        const doctor = { email, password };

        // Step 3: POST to doctor auth endpoint
        const resp = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor),
        });

        if (!resp.ok) {
            alert('Invalid doctor credentials.');
            return;
        }

        // Step 4: Parse token, store, and continue
        const data = await resp.json();
        const token = data?.token || data?.accessToken || data?.jwt || '';
        if (!token) {
            alert('Login succeeded but no token returned.');
            return;
        }

        localStorage.setItem('token', token);
        localStorage.setItem('userRole', 'doctor');

        if (typeof window.selectRole === 'function') {
            window.selectRole('doctor');
        } else {
            // Fallback: redirect to doctor dashboard
            window.location.href = '/pages/doctorDashboard.html';
        }
    } catch (err) {
        console.error('doctorLoginHandler error:', err);
        alert('An error occurred during doctor login. Please try again.');
    }
};
