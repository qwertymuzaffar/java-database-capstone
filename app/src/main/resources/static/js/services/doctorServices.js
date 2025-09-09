// doctorServices.js — API helpers for doctor resources
// Imports
import { API_BASE_URL } from '../config.js';

// Base endpoint for doctor-related actions (adjust to your backend routes)
export const DOCTOR_API = `${API_BASE_URL}/api/doctors`;

// Internal: safely encode path params; use 'all' for empty values
const enc = (v) => encodeURIComponent(String(v ?? '').trim() || 'all');

/**
 * Fetch all doctors
 * @returns {Promise<Array>} doctors array (empty array on error)
 */
export async function getDoctors() {
    try {
        const resp = await fetch(`${DOCTOR_API}/all`, { method: 'GET' });
        const data = await resp.json().catch(() => ({}));
        return Array.isArray(data?.doctors) ? data.doctors : (Array.isArray(data) ? data : []);
    } catch (err) {
        console.error('getDoctors error:', err);
        return [];
    }
}

/**
 * Delete a doctor by id using token auth (token in path as per spec)
 * @param {string} doctorId
 * @param {string} token
 * @returns {Promise<{success:boolean, message:string}>}
 */
export async function deleteDoctor(doctorId, token) {
    try {
        const url = `${DOCTOR_API}/delete/${enc(doctorId)}/${enc(token)}`;
        const resp = await fetch(url, { method: 'DELETE' });
        const data = await resp.json().catch(() => ({}));
        const success = Boolean(data?.success ?? resp.ok);
        const message = data?.message || (success ? 'Doctor deleted.' : 'Failed to delete doctor.');
        return { success, message };
    } catch (err) {
        console.error('deleteDoctor error:', err);
        return { success: false, message: 'Network error while deleting doctor.' };
    }
}

/**
 * Save (create) a new doctor (token in path, JSON body)
 * @param {object} doctor
 * @param {string} token
 * @returns {Promise<{success:boolean, message:string, doctor?:object}>}
 */
export async function saveDoctor(doctor, token) {
    try {
        const url = `${DOCTOR_API}/save/${enc(token)}`;
        const resp = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor || {}),
        });
        const data = await resp.json().catch(() => ({}));
        const success = Boolean(data?.success ?? resp.ok);
        const message = data?.message || (success ? 'Doctor saved.' : 'Failed to save doctor.');
        return { success, message, doctor: data?.doctor };
    } catch (err) {
        console.error('saveDoctor error:', err);
        return { success: false, message: 'Network error while saving doctor.' };
    }
}

/**
 * Filter doctors by name, time (AM/PM), and specialty — all via path params
 * @param {string} name
 * @param {string} time
 * @param {string} specialty
 * @returns {Promise<{doctors: Array}>}
 */
export async function filterDoctors(name, time, specialty) {
    try {
        const url = `${DOCTOR_API}/filter/${enc(name)}/${enc(time)}/${enc(specialty)}`;
        const resp = await fetch(url, { method: 'GET' });
        if (!resp.ok) {
            console.error('filterDoctors non-OK response:', resp.status, resp.statusText);
            return { doctors: [] };
        }
        const data = await resp.json().catch(() => ({}));
        if (Array.isArray(data?.doctors)) return { doctors: data.doctors };
        if (Array.isArray(data)) return { doctors: data };
        return { doctors: [] };
    } catch (err) {
        console.error('filterDoctors error:', err);
        alert('Unable to filter doctors right now. Please try again.');
        return { doctors: [] };
    }
}
