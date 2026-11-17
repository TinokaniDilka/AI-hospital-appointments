import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Calendar, Clock, User, FileText, Heart, Plus, Search, ChevronRight, CheckCircle, AlertCircle, Activity, Stethoscope, Phone, Mail } from 'lucide-react';

export default function PatientDashboard({ user }) {
  const [activeTab, setActiveTab] = useState('APPOINTMENTS'); // 'APPOINTMENTS', 'HISTORY', 'PROFILE'
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [bookingForm, setBookingForm] = useState({
    doctorId: '',
    date: '',
    reason: ''
  });
  const [previewSlot, setPreviewSlot] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [profileForm, setProfileForm] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phoneNumber: user?.phone || user?.phoneNumber || '',
    dob: user?.dob || '',
    gender: user?.gender || '',
    bloodGroup: user?.bloodGroup || ''
  });

  useEffect(() => {
    const fetchAppointments = async () => {
      if (!user?.patientId) return;

      setLoading(true);
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/appointments?patientId=${user.patientId}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const data = await response.json();
          setAppointments(data);
        } else {
          setAppointments([]);
        }
      } catch (error) {
        console.error('Failed to fetch appointments:', error);
        setAppointments([]);
      } finally {
        setLoading(false);
      }
    };

    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/patients/me`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const data = await response.json();
          setProfileForm({
            fullName: data.fullName || user?.fullName || '',
            email: data.email || user?.email || '',
            phoneNumber: data.phoneNumber || data.phone || data.emergencyContact || user?.phone || '',
            dob: data.dob || '',
            gender: data.gender || '',
            bloodGroup: data.bloodGroup || ''
          });
        }
      } catch (error) {
        console.error('Failed to fetch profile:', error);
      }
    };

    fetchAppointments();
    fetchProfile();
  }, [user?.patientId]);

  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/doctors`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const data = await response.json();
          setDoctors(data);
        }
      } catch (error) {
        console.error('Failed to fetch doctors:', error);
      }
    };

    fetchDoctors();
  }, []);

  const fetchAvailableSlots = async (doctorId, date) => {
    if (!doctorId || !date) return;

    try {
      const token = localStorage.getItem('token');
      console.log('Fetching slots for doctorId:', doctorId, 'date:', date);
      
      // Use the original slots endpoint that returns time slots
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/appointments/slots?doctorId=${doctorId}&date=${date}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('Available slots:', data);
        setAvailableSlots(data);
      } else {
        console.error('Failed to fetch slots. Status:', response.status);
        setAvailableSlots([]);
      }
    } catch (error) {
      console.error('Failed to fetch slots:', error);
      setAvailableSlots([]);
    }
  };

  const previewAppointmentSlot = async (doctorId, date) => {
    if (!doctorId || !date) {
      setPreviewSlot(null);
      return;
    }

    try {
      const token = localStorage.getItem('token');
      console.log('Previewing slot for doctorId:', doctorId, 'date:', date);
      
      // Fetch doctor details to get working hours
      const doctorResponse = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/doctors/${doctorId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!doctorResponse.ok) {
        console.error('Failed to fetch doctor:', doctorResponse.status);
        setPreviewSlot(null);
        return;
      }

      const doctor = await doctorResponse.json();
      console.log('Doctor data:', doctor);
      
      // Get existing appointments for this doctor and date
      const aptResponse = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/appointments?doctorId=${doctorId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!aptResponse.ok) {
        console.error('Failed to fetch appointments:', aptResponse.status);
        setPreviewSlot(null);
        return;
      }

      const appointments = await aptResponse.json();
      const sameDayAppointments = appointments.filter(apt => 
        (apt.scheduledDate === date || apt.preferredDate === date) && 
        apt.status !== 'CANCELLED'
      );
      
      console.log('Same day appointments:', sameDayAppointments.length);
      
      // Check if doctor has reached daily capacity
      const maxPatients = doctor.maxPatientsPerDay || 25;
      if (sameDayAppointments.length >= maxPatients) {
        setPreviewSlot({
          error: 'No slots available - doctor at full capacity',
          queueNumber: null,
          timeSlot: null
        });
        return;
      }
      
      // Calculate next queue number
      const nextQueueNumber = sameDayAppointments.length + 1;
      
      // Calculate time slot based on doctor's working hours
      const startHour = parseInt(doctor.startTime?.split(':')[0]) || 9;
      const startMinute = parseInt(doctor.startTime?.split(':')[1]) || 0;
      const endHour = parseInt(doctor.endTime?.split(':')[0]) || 17;
      
      console.log('Doctor hours:', doctor.startTime, doctor.endTime);
      console.log('Start hour:', startHour, 'End hour:', endHour);
      
      // Calculate slot duration based on working hours and capacity
      const totalWorkingMinutes = (endHour - startHour) * 60 + (startMinute || 0);
      const slotDuration = Math.floor(totalWorkingMinutes / maxPatients);
      
      console.log('Total working minutes:', totalWorkingMinutes, 'Slot duration:', slotDuration);
      
      const totalMinutes = (nextQueueNumber - 1) * slotDuration;
      const hour = startHour + Math.floor(totalMinutes / 60);
      const minute = (startMinute + totalMinutes % 60) % 60;
      const timeSlot = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
      
      console.log('Calculated time slot:', timeSlot, 'Queue number:', nextQueueNumber);
      
      setPreviewSlot({
        queueNumber: nextQueueNumber,
        timeSlot: timeSlot,
        date: date,
        doctorName: doctor.doctorName,
        workingHours: `${doctor.startTime || '09:00'} - ${doctor.endTime || '17:00'}`
      });
    } catch (error) {
      console.error('Failed to preview slot:', error);
      setPreviewSlot(null);
    }
  };

  const handleBookAppointment = async () => {
    if (!bookingForm.doctorId || !bookingForm.date) {
      alert('Please fill in all required fields');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/appointments/book`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          patientId: user.patientId,
          patientName: user.fullName,
          doctorId: bookingForm.doctorId,
          preferredDate: bookingForm.date,
          timeSlot: 'TBD' // Time slot will be assigned by scheduler
        })
      });

      if (response.ok) {
        const newAppointment = await response.json();
        setAppointments([...appointments, newAppointment]);
        setShowBookingModal(false);
        setBookingForm({ doctorId: '', date: '', reason: '' });
        
        // Show assigned time slot and queue number
        const message = newAppointment.timeSlot && newAppointment.timeSlot !== 'TBD'
          ? `Appointment booked successfully!\nTime: ${newAppointment.timeSlot}\nYour Number: #${newAppointment.queuePosition || newAppointment.queueNumber || 'N/A'}`
          : 'Appointment booked successfully!';
        alert(message);
      } else {
        const errorText = await response.text();
        console.error('Booking failed:', errorText);
        alert('Failed to book appointment. Please make sure the backend is running.');
      }
    } catch (error) {
      console.error('Failed to book appointment:', error);
      alert('Failed to book appointment. Please check if the backend is running.');
    }
  };

  const handleCancelAppointment = async (aptId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/appointments/${aptId}/cancel`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        alert('Appointment cancelled successfully!');
        if (user?.patientId) {
          const res = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/appointments?patientId=${user.patientId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          if (res.ok) setAppointments(await res.json());
        }
      } else {
        alert('Failed to cancel appointment');
      }
    } catch (error) {
      console.error('Failed to cancel appointment:', error);
      alert('Failed to cancel appointment');
    }
  };

  const handleSaveProfile = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/patients/me`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(profileForm)
      });

      if (response.ok) {
        const updatedUser = await response.json();
        alert('Profile saved successfully!');
      } else {
        const error = await response.json();
        alert(error.error || 'Failed to save profile');
      }
    } catch (error) {
      console.error('Failed to save profile:', error);
      alert('Failed to save profile');
    }
  };

  const getStatusStyle = (status) => {
    switch (status) {
      case 'COMPLETED': return { background: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' };
      case 'CONFIRMED': return { background: 'rgba(37, 99, 235, 0.15)', color: '#3b82f6', border: '1px solid rgba(37, 99, 235, 0.3)' };
      case 'PENDING': return { background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b', border: '1px solid rgba(245, 158, 11, 0.3)' };
      case 'CANCELLED': return { background: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' };
      default: return { background: 'rgba(107, 114, 128, 0.15)', color: '#6b7280', border: '1px solid rgba(107, 114, 128, 0.3)' };
    }
  };

  const upcomingAppointments = appointments.filter(a => a.status !== 'COMPLETED' && a.status !== 'CANCELLED');
  const pastAppointments = appointments.filter(a => a.status === 'COMPLETED');

  return (
    <div style={{
      background: 'rgba(255, 255, 255, 0.8)',
      backdropFilter: 'blur(16px)',
      borderRadius: '16px',
      padding: '1.5rem',
      margin: '1.5rem',
      maxWidth: '1400px',
      marginLeft: 'auto',
      marginRight: 'auto',
      border: '1px solid #e2e8f0',
      boxShadow: '0 4px 20px -5px rgba(0, 0, 0, 0.1)'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ background: 'linear-gradient(90deg, #0284c7, #38bdf8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', fontWeight: 800, letterSpacing: '-0.02em', fontSize: '1.875rem' }}>
            Patient Portal
          </h1>
          <p style={{ color: '#64748b', marginTop: '0.25rem', fontSize: '0.9rem' }}>
            Welcome back, {user?.fullName || 'Patient'} • Manage your healthcare journey
          </p>
        </div>
        <div style={{
          background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
          padding: '0.75rem 1.5rem',
          borderRadius: '12px',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          color: '#fff',
          fontWeight: 600,
          fontSize: '0.9rem',
          boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)'
        }}>
          <Heart size={18} />
          Health Profile Active
        </div>
      </div>

      {/* Quick Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          gap: '1rem',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
        }}>
          <div style={{
            background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
            width: '56px',
            height: '56px',
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)'
          }}>
            <Calendar color="#ffffff" size={28} />
          </div>
          <div>
            <p style={{ color: '#64748b', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Upcoming Appointments</p>
            <p style={{ fontSize: '2rem', fontWeight: 800, color: '#0f172a', lineHeight: 1 }}>{upcomingAppointments.length}</p>
          </div>
        </div>

        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          gap: '1rem',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
        }}>
          <div style={{
            background: 'linear-gradient(135deg, #10b981, #059669)',
            width: '56px',
            height: '56px',
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 15px rgba(16, 185, 129, 0.3)'
          }}>
            <CheckCircle color="#ffffff" size={28} />
          </div>
          <div>
            <p style={{ color: '#64748b', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Completed Visits</p>
            <p style={{ fontSize: '2rem', fontWeight: 800, color: '#0f172a', lineHeight: 1 }}>{pastAppointments.length}</p>
          </div>
        </div>

        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          gap: '1rem',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
        }}>
          <div style={{
            background: 'linear-gradient(135deg, #8b5cf6, #7c3aed)',
            width: '56px',
            height: '56px',
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 15px rgba(139, 92, 246, 0.3)'
          }}>
            <Activity color="#ffffff" size={28} />
          </div>
          <div>
            <p style={{ color: '#64748b', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Health Score</p>
            <p style={{ fontSize: '2rem', fontWeight: 800, color: '#0f172a', lineHeight: 1 }}>95%</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', background: '#f1f5f9', padding: '0.375rem', borderRadius: '12px', marginBottom: '1.5rem', border: '1px solid #cbd5e1' }}>
        {['APPOINTMENTS', 'HISTORY', 'PROFILE'].map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{
              flex: 1,
              padding: '0.75rem 1rem',
              borderRadius: '10px',
              border: 'none',
              fontSize: '0.9rem',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.5rem',
              transition: 'all 0.2s',
              background: activeTab === tab
                ? 'linear-gradient(135deg, #0ea5e9, #0284c7)'
                : 'transparent',
              color: activeTab === tab ? '#fff' : '#64748b',
              boxShadow: activeTab === tab ? '0 4px 15px rgba(14, 165, 233, 0.3)' : 'none'
            }}
          >
            {tab === 'APPOINTMENTS' && <Calendar size={16} />}
            {tab === 'HISTORY' && <FileText size={16} />}
            {tab === 'PROFILE' && <User size={16} />}
            {tab}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'APPOINTMENTS' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#0f172a' }}>Upcoming Appointments</h2>
            <button
              onClick={() => setShowBookingModal(true)}
              style={{
                background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
                color: '#fff',
                padding: '0.75rem 1.5rem',
                borderRadius: '10px',
                border: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontWeight: 600,
                fontSize: '0.9rem',
                boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)',
                transition: 'all 0.2s'
              }}
            >
              <Plus size={18} />
              Book New Appointment
            </button>
          </div>

          {upcomingAppointments.length === 0 ? (
            <div style={{
              background: '#ffffff',
              border: '1px solid #e2e8f0',
              borderRadius: '16px',
              padding: '3rem',
              textAlign: 'center',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
            }}>
              <Calendar size={64} style={{ color: '#64748b', marginBottom: '1rem' }} />
              <p style={{ color: '#64748b', marginBottom: '1rem' }}>No upcoming appointments</p>
              <button
                onClick={() => setShowBookingModal(true)}
                style={{ color: '#0284c7', background: 'none', border: 'none', cursor: 'pointer', fontSize: '0.95rem', fontWeight: 600 }}
              >
                Book your first appointment
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {upcomingAppointments.map((apt) => (
                <div key={apt.id} style={{
                  background: '#ffffff',
                  border: '1px solid #e2e8f0',
                  borderRadius: '16px',
                  padding: '1.5rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
                    <div style={{
                      background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
                      width: '56px',
                      height: '56px',
                      borderRadius: '12px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)'
                    }}>
                      <Stethoscope color="#ffffff" size={28} />
                    </div>
                    <div>
                      <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.25rem' }}>{apt.doctorName}</h3>
                      <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '0.5rem' }}>{apt.departmentName || apt.department}</p>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', fontSize: '0.85rem', color: '#64748b' }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                          <Clock size={16} />
                          {apt.scheduledDate || apt.preferredDate || apt.date} • {apt.timeSlot || 'TBD'}
                        </span>
                      </div>
                      {(apt.queuePosition || apt.queueNumber) && (
                        <p style={{ color: '#0ea5e9', fontSize: '0.85rem', marginTop: '0.5rem', fontWeight: 600 }}>
                          Your Number: #{apt.queuePosition || apt.queueNumber}
                        </p>
                      )}
                      {apt.status === 'WAITLISTED' && apt.queuePosition && (
                        <p style={{ color: '#f59e0b', fontSize: '0.85rem', marginTop: '0.25rem', fontWeight: 600 }}>
                          Queue Position: #{apt.queuePosition}
                        </p>
                      )}
                      <p style={{ color: '#64748b', fontSize: '0.85rem', marginTop: '0.5rem' }}>Reason: {apt.reason}</p>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <span style={{
                      padding: '0.375rem 1rem',
                      borderRadius: '20px',
                      fontSize: '0.8rem',
                      fontWeight: 600,
                      ...getStatusStyle(apt.status)
                    }}>
                      {apt.status}
                    </span>
                    {apt.status !== 'CANCELLED' && apt.status !== 'COMPLETED' && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleCancelAppointment(apt.id || apt.appointmentId);
                        }}
                        style={{
                          background: 'rgba(239, 68, 68, 0.1)',
                          color: '#ef4444',
                          border: '1px solid rgba(239, 68, 68, 0.3)',
                          padding: '0.375rem 0.75rem',
                          borderRadius: '8px',
                          fontSize: '0.8rem',
                          fontWeight: 600,
                          cursor: 'pointer'
                        }}
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'HISTORY' && (
        <div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#0f172a', marginBottom: '1.5rem' }}>Medical History</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {pastAppointments.map((apt) => (
              <div key={apt.id} style={{
                background: '#ffffff',
                border: '1px solid #e2e8f0',
                borderRadius: '16px',
                padding: '1.5rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
                  <div style={{
                    background: 'linear-gradient(135deg, #10b981, #059669)',
                    width: '56px',
                    height: '56px',
                    borderRadius: '12px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 15px rgba(16, 185, 129, 0.3)'
                  }}>
                    <FileText color="#ffffff" size={28} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.25rem' }}>{apt.doctorName}</h3>
                    <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '0.5rem' }}>{apt.department}</p>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', fontSize: '0.85rem', color: '#64748b' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                        <Clock size={16} />
                        {apt.date} • {apt.timeSlot}
                      </span>
                    </div>
                    <p style={{ color: '#64748b', fontSize: '0.85rem', marginTop: '0.5rem' }}>Reason: {apt.reason}</p>
                  </div>
                </div>
                <span style={{
                  padding: '0.375rem 1rem',
                  borderRadius: '20px',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  background: 'rgba(16, 185, 129, 0.15)',
                  color: '#10b981',
                  border: '1px solid rgba(16, 185, 129, 0.3)'
                }}>
                  COMPLETED
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'PROFILE' && (
        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '16px',
          padding: '2rem',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', marginBottom: '2rem', paddingBottom: '2rem', borderBottom: '1px solid #e2e8f0' }}>
            <div style={{
              background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
              width: '80px',
              height: '80px',
              borderRadius: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)'
            }}>
              <User color="#ffffff" size={40} />
            </div>
            <div>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.25rem' }}>{user?.fullName || 'Patient Name'}</h2>
              <p style={{ color: '#64748b', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Mail size={16} />
                {user?.email || 'patient@email.com'}
              </p>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Full Name</label>
              <input
                type="text"
                value={profileForm.fullName}
                onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '10px',
                  color: '#0f172a',
                  fontSize: '0.95rem',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Email (Gmail)</label>
              <input
                type="email"
                value={profileForm.email}
                onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                placeholder="patient@gmail.com"
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '10px',
                  color: '#0f172a',
                  fontSize: '0.95rem',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Phone Number</label>
              <input
                type="tel"
                value={profileForm.phoneNumber}
                onChange={(e) => setProfileForm({ ...profileForm, phoneNumber: e.target.value })}
                placeholder="+1-555-0199"
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '10px',
                  color: '#0f172a',
                  fontSize: '0.95rem',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Date of Birth</label>
              <input
                type="date"
                value={profileForm.dob}
                onChange={(e) => setProfileForm({ ...profileForm, dob: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '10px',
                  color: '#0f172a',
                  fontSize: '0.95rem',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Gender</label>
              <select
                value={profileForm.gender}
                onChange={(e) => setProfileForm({ ...profileForm, gender: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '10px',
                  color: '#0f172a',
                  fontSize: '0.95rem',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}>
                <option value="">Select Gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Blood Group</label>
              <select
                value={profileForm.bloodGroup}
                onChange={(e) => setProfileForm({ ...profileForm, bloodGroup: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '10px',
                  color: '#0f172a',
                  fontSize: '0.95rem',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}>
                <option value="">Select Blood Group</option>
                <option value="O+">O+</option>
                <option value="A+">A+</option>
                <option value="B+">B+</option>
                <option value="AB+">AB+</option>
                <option value="O-">O-</option>
                <option value="A-">A-</option>
                <option value="B-">B-</option>
                <option value="AB-">AB-</option>
              </select>
            </div>
          </div>

          <button
            onClick={handleSaveProfile}
            style={{
              marginTop: '2rem',
              background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
              color: '#fff',
              padding: '0.875rem 2rem',
              borderRadius: '10px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: '0.95rem',
              boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)',
              transition: 'all 0.2s'
            }}
          >
            Save Changes
          </button>
        </div>
      )}

      {/* Booking Modal */}
      {showBookingModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: '#ffffff',
            borderRadius: '16px',
            padding: '2rem',
            width: '100%',
            maxWidth: '500px',
            maxHeight: '90vh',
            overflowY: 'auto',
            boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#0f172a' }}>Book Appointment</h2>
              <button
                onClick={() => setShowBookingModal(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '1.5rem',
                  color: '#64748b'
                }}
              >
                ×
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Select Doctor</label>
                <select
                  value={bookingForm.doctorId}
                  onChange={(e) => {
                    const selectedDoctorId = e.target.value;
                    console.log('Doctor selected:', selectedDoctorId);
                    setBookingForm({ ...bookingForm, doctorId: selectedDoctorId });
                    setPreviewSlot(null);
                    if (bookingForm.date) {
                      previewAppointmentSlot(selectedDoctorId, bookingForm.date);
                    }
                  }}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    background: '#f1f5f9',
                    border: '1px solid #cbd5e1',
                    borderRadius: '10px',
                    color: '#0f172a',
                    fontSize: '0.95rem',
                    outline: 'none'
                  }}
                >
                  <option value="">Choose a doctor</option>
                  {doctors.map((doc) => (
                    <option key={doc.id} value={doc.id}>
                      {doc.doctorName} - {doc.specialization}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Select Date</label>
                <input
                  type="date"
                  value={bookingForm.date}
                  onChange={(e) => {
                    const selectedDate = e.target.value;
                    console.log('Date selected:', selectedDate);
                    setBookingForm({ ...bookingForm, date: selectedDate });
                    if (bookingForm.doctorId) {
                      previewAppointmentSlot(bookingForm.doctorId, selectedDate);
                    }
                  }}
                  min={new Date().toISOString().split('T')[0]}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    background: '#f1f5f9',
                    border: '1px solid #cbd5e1',
                    borderRadius: '10px',
                    color: '#0f172a',
                    fontSize: '0.95rem',
                    outline: 'none'
                  }}
                />
              </div>

              {previewSlot && (
                <div style={{ 
                  padding: '1rem', 
                  borderRadius: '10px', 
                  border: '1px solid',
                  background: previewSlot.error ? '#fef2f2' : '#f0fdf4',
                  borderColor: previewSlot.error ? '#fecaca' : '#86efac'
                }}>
                  {previewSlot.error ? (
                    <p style={{ fontSize: '0.85rem', color: '#dc2626', margin: 0, fontWeight: 600 }}>
                      {previewSlot.error}
                    </p>
                  ) : (
                    <>
                      <p style={{ fontSize: '0.85rem', color: '#166534', margin: 0, fontWeight: 600 }}>
                        Your Allocated Slot:
                      </p>
                      <div style={{ marginTop: '0.5rem', display: 'flex', gap: '1rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                          <Clock size={16} color="#166534" />
                          <span style={{ fontSize: '0.9rem', color: '#166534', fontWeight: 600 }}>
                            {previewSlot.timeSlot}
                          </span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                          <User size={16} color="#166534" />
                          <span style={{ fontSize: '0.9rem', color: '#166534', fontWeight: 600 }}>
                            #{previewSlot.queueNumber}
                          </span>
                        </div>
                      </div>
                      {previewSlot.workingHours && (
                        <p style={{ fontSize: '0.75rem', color: '#15803d', marginTop: '0.5rem', margin: '0.5rem 0 0 0' }}>
                          Doctor Hours: {previewSlot.workingHours}
                        </p>
                      )}
                    </>
                  )}
                </div>
              )}

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Reason (Optional)</label>
                <input
                  type="text"
                  value={bookingForm.reason}
                  onChange={(e) => setBookingForm({ ...bookingForm, reason: e.target.value })}
                  placeholder="e.g., Routine checkup"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    background: '#f1f5f9',
                    border: '1px solid #cbd5e1',
                    borderRadius: '10px',
                    color: '#0f172a',
                    fontSize: '0.95rem',
                    outline: 'none'
                  }}
                />
              </div>

              <button
                onClick={handleBookAppointment}
                style={{
                  marginTop: '1rem',
                  background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
                  color: '#fff',
                  padding: '0.875rem 2rem',
                  borderRadius: '10px',
                  border: 'none',
                  cursor: 'pointer',
                  fontWeight: 600,
                  fontSize: '0.95rem',
                  boxShadow: '0 4px 15px rgba(14, 165, 233, 0.3)',
                  transition: 'all 0.2s'
                }}
              >
                Book Appointment
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
