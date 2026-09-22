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
  }
};
