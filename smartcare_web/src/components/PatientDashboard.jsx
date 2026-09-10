import React, { useState, useEffect } from 'react';
import { Calendar, Clock, User, FileText, Heart, Plus, Search, ChevronRight, CheckCircle, AlertCircle, Activity, Stethoscope, Phone, Mail } from 'lucide-react';

export default function PatientDashboard({ user }) {
  const [activeTab, setActiveTab] = useState('APPOINTMENTS'); // 'APPOINTMENTS', 'HISTORY', 'PROFILE'
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [bookingForm, setBookingForm] = useState({
    doctorId: '',
    date: '',
    timeSlot: '',
    reason: ''
  });
  const [doctors, setDoctors] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);

  useEffect(() => {
    const fetchAppointments = async () => {
      if (!user?.patientId) return;

      setLoading(true);
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8080/api/v1/appointments?patientId=${user.patientId}`, {
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

    fetchAppointments();
  }, [user?.patientId]);

  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/api/v1/doctors', {
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
      const response = await fetch(`http://localhost:8080/api/v1/appointments/slots?doctorId=${doctorId}&date=${date}`, {
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
        const errorText = await response.text();
        console.error('Error:', errorText);
        setAvailableSlots([]);
      }
    } catch (error) {
      console.error('Failed to fetch slots:', error);
      setAvailableSlots([]);
    }
  };

  const handleBookAppointment = async () => {
    if (!bookingForm.doctorId || !bookingForm.date || !bookingForm.timeSlot) {
      alert('Please fill in all required fields');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/appointments/book', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          patientId: user.patientId,
          patientName: user.fullName,
          doctorId: bookingForm.doctorId,
          date: bookingForm.date,
          timeSlot: bookingForm.timeSlot
        })
      });

      if (response.ok) {
        const newAppointment = await response.json();
        setAppointments([...appointments, newAppointment]);
        setShowBookingModal(false);
        setBookingForm({ doctorId: '', date: '', timeSlot: '', reason: '' });
        alert('Appointment booked successfully!');
      } else {
        const error = await response.json();
        alert(error.message || 'Failed to book appointment');
      }
    } catch (error) {
      console.error('Failed to book appointment:', error);
      alert('Failed to book appointment');
    }
  };

  const handleSaveProfile = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/patients/me', {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          fullName: user.fullName,
          dob: user.dob,
          gender: user.gender,
          bloodGroup: user.bloodGroup
        })
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
                    <ChevronRight style={{ color: '#64748b' }} size={20} />
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
                defaultValue={user?.fullName || ''}
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
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Email</label>
              <input
                type="email"
                defaultValue={user?.email || ''}
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
                defaultValue="+1-555-0199"
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
                defaultValue="1988-04-12"
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
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Blood Group</label>
              <select style={{
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
                <option>O+</option>
                <option>A+</option>
                <option>B+</option>
                <option>AB+</option>
                <option>O-</option>
                <option>A-</option>
                <option>B-</option>
                <option>AB-</option>
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
                    setBookingForm({ ...bookingForm, doctorId: selectedDoctorId, timeSlot: '' });
                    setAvailableSlots([]);
                    if (bookingForm.date) {
                      fetchAvailableSlots(selectedDoctorId, bookingForm.date);
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
                      fetchAvailableSlots(bookingForm.doctorId, selectedDate);
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

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginBottom: '0.5rem' }}>Select Time Slot</label>
                <select
                  value={bookingForm.timeSlot}
                  onChange={(e) => setBookingForm({ ...bookingForm, timeSlot: e.target.value })}
                  disabled={!bookingForm.doctorId || !bookingForm.date || availableSlots.length === 0}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    background: '#f1f5f9',
                    border: '1px solid #cbd5e1',
                    borderRadius: '10px',
                    color: '#0f172a',
                    fontSize: '0.95rem',
                    outline: 'none',
                    opacity: (!bookingForm.doctorId || !bookingForm.date || availableSlots.length === 0) ? 0.5 : 1
                  }}
                >
                  <option value="">Choose a time slot</option>
                  {availableSlots.map((slot) => (
                    <option key={slot} value={slot}>{slot}</option>
                  ))}
                </select>
                {bookingForm.doctorId && bookingForm.date && availableSlots.length === 0 && (
                  <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.25rem' }}>No available slots for this date</p>
                )}
              </div>

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
