const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` })
  };
};

export const api = {
  // Analytics
  getAnalytics: async () => {
    const response = await fetch(`${API_BASE_URL}/analytics/summary`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch analytics');
    return response.json();
  },

  // Doctors
  getDoctors: async () => {
    const response = await fetch(`${API_BASE_URL}/doctors`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch doctors');
    return response.json();
  },

  createDoctor: async (doctorData) => {
    const response = await fetch(`${API_BASE_URL}/doctors`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(doctorData)
    });
    if (!response.ok) throw new Error('Failed to create doctor');
    return response.json();
  },

  // Queue
  getQueue: async (doctorId) => {
    const response = await fetch(`${API_BASE_URL}/queues/doctor/${doctorId}`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch queue');
    return response.json();
  },

  callNextPatient: async (queueId) => {
    const response = await fetch(`${API_BASE_URL}/queues/${queueId}/call-next`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to call next patient');
    return response.json();
  },

  updateQueueStatus: async (queueId, status) => {
    const response = await fetch(`${API_BASE_URL}/queues/${queueId}/status`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ status })
    });
    if (!response.ok) throw new Error('Failed to update queue status');
    return response.json();
  },

  // Audit Logs
  getAuditLogs: async () => {
    const response = await fetch(`${API_BASE_URL}/audit-logs`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch audit logs');
    return response.json();
  },

  // AI
  predictWaitTime: async (data) => {
    const response = await fetch(`${API_BASE_URL}/ai/predict-wait-time`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error('Failed to predict wait time');
    return response.json();
  },

  // Appointments
  getAppointments: async (patientId, doctorId) => {
    let url = `${API_BASE_URL}/appointments`;
    if (patientId) {
      url += `?patientId=${patientId}`;
    } else if (doctorId) {
      url += `?doctorId=${doctorId}`;
    }
    const response = await fetch(url, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch appointments');
    return response.json();
  },

  getDoctorAppointments: async (doctorId) => {
    const response = await fetch(`${API_BASE_URL}/appointments?doctorId=${doctorId}`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch doctor appointments');
    return response.json();
  },

  cancelAppointment: async (id, reason = 'Patient requested cancellation') => {
    const response = await fetch(`${API_BASE_URL}/appointments/${id}/cancel`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ reason })
    });
    if (!response.ok) throw new Error('Failed to cancel appointment');
    return response.json();
  },

  rescheduleAppointment: async (id, newDate, newTimeSlot) => {
    const response = await fetch(`${API_BASE_URL}/appointments/${id}/reschedule`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ newDate, newTimeSlot })
    });
    if (!response.ok) throw new Error('Failed to reschedule appointment');
    return response.json();
  },

  getAvailableSlots: async (doctorId, date) => {
    const response = await fetch(`${API_BASE_URL}/appointments/slots?doctorId=${doctorId}&date=${date}`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch slots');
    return response.json();
  },

  payAppointment: async (id) => {
    const response = await fetch(`${API_BASE_URL}/appointments/${id}/pay`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to process payment');
    return response.json();
  },

  // Notifications
  getNotifications: async (patientId) => {
    const response = await fetch(`${API_BASE_URL}/notifications/${patientId}`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch notifications');
    return response.json();
  },

  markNotificationRead: async (id) => {
    const response = await fetch(`${API_BASE_URL}/notifications/${id}/read`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to mark notification read');
    return response.ok;
  },

  sendNotification: async (patientId, title, message, type = 'GENERAL') => {
    const response = await fetch(`${API_BASE_URL}/notifications`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ patientId, title, message, type })
    });
    if (!response.ok) throw new Error('Failed to send notification');
    return response.json();
  },

  // Queue for patient
  getPatientQueue: async (doctorId) => {
    const response = await fetch(`${API_BASE_URL}/queues/${doctorId}/today`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch queue');
    return response.json();
  },

  // Patients
  getPatients: async () => {
    const response = await fetch(`${API_BASE_URL}/patients`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch patients');
    return response.json();
  },

  getPatientById: async (id) => {
    const response = await fetch(`${API_BASE_URL}/patients/${id}`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch patient');
    return response.json();
  },

  // Doctor schedule
  getDoctorSchedule: async (doctorId) => {
    const response = await fetch(`${API_BASE_URL}/schedules/${doctorId}`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch schedule');
    return response.json();
  },

  updateDoctorSchedule: async (doctorId, scheduleData) => {
    const response = await fetch(`${API_BASE_URL}/schedules/${doctorId}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(scheduleData)
    });
    if (!response.ok) throw new Error('Failed to update schedule');
    return response.json();
  },

  // Update doctor (for fee management)
  updateDoctor: async (id, doctorData) => {
    const response = await fetch(`${API_BASE_URL}/doctors/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(doctorData)
    });
    if (!response.ok) throw new Error('Failed to update doctor');
    return response.json();
  },

  // Analytics trends
  getAnalyticsTrends: async () => {
    const response = await fetch(`${API_BASE_URL}/analytics/trends`, {
      headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch trends');
    return response.json();
  }
};
