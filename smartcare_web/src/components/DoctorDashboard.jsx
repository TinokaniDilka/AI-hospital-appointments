import React, { useState, useEffect } from 'react';
import { Users, UserCheck, Clock, AlertTriangle, Play, CheckCircle2, UserX, Flame, Calendar, Settings, Sparkles, ChevronRight, Plus, Trash2 } from 'lucide-react';
import { api } from '../services/api';

export default function DoctorDashboard({ user }) {
  const [activeTab, setActiveTab] = useState('QUEUE'); // 'QUEUE' or 'SCHEDULE'
  const [doctorsList, setDoctorsList] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(user?.doctorId || '');
  const [queueState, setQueueState] = useState({ doctorId: '', doctorName: '', date: '', currentServingNumber: 0, items: [] });
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [actionAlert, setActionAlert] = useState(null);
  const [scheduleForm, setScheduleForm] = useState({
    workingDays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
    shiftStart: '09:00',
    shiftEnd: '17:00',
    lunchBreakStart: '13:00',
    lunchBreakEnd: '14:00',
    avgSlotDuration: '20',
    leaveDates: [],
    dailyCapacity: '20'
  });

  const token = localStorage.getItem('token');
  const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
      const response = await fetch(`${API_URL}/api/v1/doctors`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const docs = await response.json();
        setDoctorsList(docs);
        if (docs.length > 0 && !selectedDoctor) {
          const userDoc = docs.find(d => d.userId === user?.id) || docs[0];
          setSelectedDoctor(userDoc.id);
        }
      }
    } catch (err) {
      console.error('Failed to fetch doctors:', err);
    }
  };

  useEffect(() => {
    if (selectedDoctor) {
      fetchQueue(selectedDoctor);
      fetchSchedule(selectedDoctor);
    }
  }, [selectedDoctor]);

  const fetchQueue = async (docId) => {
    try {
      const response = await fetch(`${API_URL}/api/v1/queues/${docId}/today`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setQueueState(data);
      }
    } catch (err) {
      console.error('Failed to fetch queue state:', err);
    }
  };

  const fetchSchedule = async (docId) => {
    try {
      const response = await fetch(`${API_URL}/api/v1/schedules/${docId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setScheduleForm({
          workingDays: data.workingDays || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
          shiftStart: data.startTime || '09:00',
          shiftEnd: data.endTime || '17:00',
          lunchBreakStart: data.breakStartTime || '13:00',
          lunchBreakEnd: data.breakEndTime || '14:00',
          avgSlotDuration: String(data.slotDurationMinutes || 20),
          leaveDates: data.leaveDates || [],
          dailyCapacity: String(data.dailyCapacity || 20)
        });
      }
    } catch (err) {
      console.error('Failed to fetch schedule:', err);
    }
  };

  const items = queueState.items || [];
  const currentServing = items.find(i => i.status === 'NOW_SERVING');
  const nextWaiting = items.find(i => i.status === 'WAITING' || i.status === 'APPROACHING');

  const totalCount = items.length;
  const waitingCount = items.filter(i => i.status === 'WAITING' || i.status === 'APPROACHING').length;
  const completedCount = items.filter(i => i.status === 'COMPLETED').length;
  const noShowCount = items.filter(i => i.status === 'NO_SHOW').length;

  const showNotification = (msg) => {
    setActionAlert(msg);
    setTimeout(() => setActionAlert(null), 3500);
  };

  const handleSaveSchedule = async () => {
    if (!selectedDoctor) return;
    try {
      const response = await fetch(`${API_URL}/api/v1/schedules/${selectedDoctor}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          doctorId: selectedDoctor,
          workingDays: scheduleForm.workingDays,
          startTime: scheduleForm.shiftStart,
          endTime: scheduleForm.shiftEnd,
          breakStartTime: scheduleForm.lunchBreakStart,
          breakEndTime: scheduleForm.lunchBreakEnd,
          slotDurationMinutes: parseInt(scheduleForm.avgSlotDuration),
          leaveDates: scheduleForm.leaveDates,
          dailyCapacity: parseInt(scheduleForm.dailyCapacity)
        })
      });

      if (response.ok) {
        showNotification('Schedule saved successfully!');
      } else {
        const error = await response.json();
        showNotification('Failed to save schedule: ' + (error.error || error.message || 'Unknown error'));
      }
    } catch (error) {
      console.error('Failed to save schedule:', error);
      showNotification('Failed to save schedule');
    }
  };

  const handleCallNext = async () => {
    if (!selectedDoctor) return;
    try {
      const response = await fetch(`${API_URL}/api/v1/queues/${selectedDoctor}/call-next`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (response.ok) {
        const updatedQueue = await response.json();
        setQueueState(updatedQueue);
        const serving = updatedQueue.items.find(i => i.status === 'NOW_SERVING');
        if (serving) {
          showNotification(`Called Queue #${serving.queueNumber} (${serving.patientName}) to Consultation Room.`);
        } else {
          showNotification('No more patients waiting in queue.');
        }
      }
    } catch (err) {
      console.error('Failed to call next:', err);
      showNotification('Failed to call next patient');
    }
  };

  const handleCompleteCurrent = async () => {
    if (!currentServing || !selectedDoctor) return;
    try {
      const response = await fetch(`${API_URL}/api/v1/queues/${selectedDoctor}/status`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          appointmentId: currentServing.appointmentId,
          status: 'COMPLETED'
        })
      });

      if (response.ok) {
        const updatedQueue = await response.json();
        setQueueState(updatedQueue);
        showNotification(`Consultation for ${currentServing.patientName} marked as Completed.`);
      }
    } catch (err) {
      console.error('Failed to complete consultation:', err);
    }
  };

  const handleMarkNoShow = async () => {
    const target = currentServing || nextWaiting;
    if (!target || !selectedDoctor) return;
    try {
      const response = await fetch(`${API_URL}/api/v1/queues/${selectedDoctor}/status`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          appointmentId: target.appointmentId,
          status: 'NO_SHOW'
        })
      });

      if (response.ok) {
        const updatedQueue = await response.json();
        setQueueState(updatedQueue);
        showNotification(`Queue #${target.queueNumber} (${target.patientName}) marked as NO_SHOW.`);
      }
    } catch (err) {
      console.error('Failed to mark no-show:', err);
    }
  };

  const handleTogglePriority = async (aptId, currentPriority) => {
    if (!selectedDoctor) return;
    try {
      const response = await fetch(`${API_URL}/api/v1/queues/${selectedDoctor}/priority`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          appointmentId: aptId,
          isPriority: !currentPriority,
          reason: !currentPriority ? 'Emergency priority requested' : ''
        })
      });

      if (response.ok) {
        const updatedQueue = await response.json();
        setQueueState(updatedQueue);
        showNotification(`Priority status updated for appointment.`);
      }
    } catch (err) {
      console.error('Failed to toggle priority:', err);
    }
  };

  const handleToggleWorkingDay = (day) => {
    const currentDays = [...scheduleForm.workingDays];
    const updatedDays = currentDays.includes(day)
      ? currentDays.filter(d => d !== day)
      : [...currentDays, day];
    setScheduleForm({ ...scheduleForm, workingDays: updatedDays });
  };

  const handleAddLeaveDate = () => {
    const dateInput = prompt('Enter Leave Date (YYYY-MM-DD):', new Date().toISOString().split('T')[0]);
    if (dateInput && dateInput.trim()) {
      if (!scheduleForm.leaveDates.includes(dateInput.trim())) {
        setScheduleForm({
          ...scheduleForm,
          leaveDates: [...scheduleForm.leaveDates, dateInput.trim()]
        });
      }
    }
  };

  const handleRemoveLeaveDate = (dateToRemove) => {
    setScheduleForm({
      ...scheduleForm,
      leaveDates: scheduleForm.leaveDates.filter(d => d !== dateToRemove)
    });
  };

  const filteredItems = items.filter(item => {
    if (filterStatus === 'ALL') return true;
    if (filterStatus === 'WAITING') return item.status === 'WAITING' || item.status === 'APPROACHING';
    return item.status === filterStatus;
  });

  return (
    <div style={{ maxWidth: '1450px', margin: '1.5rem auto', padding: '0 1rem' }}>
      
      {/* Toast Alert Banner */}
      {actionAlert && (
        <div style={{
          position: 'fixed',
          bottom: '24px',
          right: '24px',
          background: 'linear-gradient(135deg, #1e293b, #0f172a)',
          border: '1px solid #3b82f6',
          color: '#ffffff',
          padding: '12px 20px',
          borderRadius: '12px',
          boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
          zIndex: 9999,
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}>
          <Sparkles color="#38bdf8" size={20} />
          <span style={{ fontSize: '0.9rem', fontWeight: 600 }}>{actionAlert}</span>
        </div>
      )}

      {/* Top Header & Tab Navigation */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>Doctor Consultation & Live Queue Control</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Manage today's consultations, advance live queue numbers, and configure schedule rules.</p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', background: 'rgba(255,255,255,0.04)', padding: '4px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
          <button
            onClick={() => setActiveTab('QUEUE')}
            className={activeTab === 'QUEUE' ? 'btn-primary' : 'btn-secondary'}
            style={{ fontSize: '0.85rem' }}
          >
            <Clock size={16} /> Live Queue Manager
          </button>
          <button
            onClick={() => setActiveTab('SCHEDULE')}
            className={activeTab === 'SCHEDULE' ? 'btn-primary' : 'btn-secondary'}
            style={{ fontSize: '0.85rem' }}
          >
            <Calendar size={16} /> Schedule & Working Hours
          </button>
        </div>
      </div>

      {activeTab === 'QUEUE' ? (
        <>
          {/* Summary Stat Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.25rem', marginBottom: '1.5rem' }}>
            
            <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ background: 'rgba(59, 130, 246, 0.15)', padding: '12px', borderRadius: '12px', color: '#3b82f6' }}>
                <Users size={24} />
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>TODAY'S APPOINTMENTS</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800 }}>{totalCount}</div>
              </div>
            </div>

            <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ background: 'rgba(245, 158, 11, 0.15)', padding: '12px', borderRadius: '12px', color: '#f59e0b' }}>
                <Clock size={24} />
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>CURRENTLY WAITING</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#fbbf24' }}>{waitingCount}</div>
              </div>
            </div>

            <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ background: 'rgba(16, 185, 129, 0.15)', padding: '12px', borderRadius: '12px', color: '#10b981' }}>
                <UserCheck size={24} />
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>CONSULTATIONS COMPLETED</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#34d399' }}>{completedCount}</div>
              </div>
            </div>

            <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ background: 'rgba(239, 68, 68, 0.15)', padding: '12px', borderRadius: '12px', color: '#ef4444' }}>
                <UserX size={24} />
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>NO-SHOWS & CANCELLED</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#f87171' }}>{noShowCount}</div>
              </div>
            </div>

          </div>

          {/* Main Grid: Live Caller Card & Queue Table */}
          <div style={{ display: 'grid', gridTemplateColumns: '400px 1fr', gap: '1.5rem' }}>
            
            {/* Live Caller Card */}
            <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--cyan)', letterSpacing: '0.05em' }}>
                    LIVE PATIENT CALLER
                  </div>
                  <span className="badge badge-now-serving">ACTIVE SESSION</span>
                </div>

                {currentServing ? (
                  <div style={{ textAlign: 'center', background: 'rgba(6, 182, 212, 0.08)', borderRadius: '16px', padding: '1.5rem', border: '1px solid rgba(6, 182, 212, 0.25)', marginBottom: '1.25rem' }}>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>NOW SERVING QUEUE NUMBER</div>
                    <div style={{ fontSize: '4.2rem', fontWeight: 900, color: '#38bdf8', lineHeight: 1, margin: '0.5rem 0' }}>
                      #{currentServing.queueNumber}
                    </div>
                    <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#ffffff' }}>{currentServing.patientName}</div>
                    <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                      ID: {currentServing.patientId} • Apt: {currentServing.appointmentId}
                    </div>

                    {currentServing.isPriority && (
                      <div style={{ marginTop: '0.75rem' }}>
                        <span className="badge badge-priority"><Flame size={12} /> PRIORITY EMERGENCY</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ textAlign: 'center', background: 'rgba(255,255,255,0.03)', borderRadius: '16px', padding: '2rem 1rem', border: '1px border-dashed var(--border-color)', marginBottom: '1.25rem' }}>
                    <Clock size={40} color="var(--text-subtle)" style={{ marginBottom: '0.5rem' }} />
                    <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-muted)' }}>No Patient Currently Serving</div>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', marginTop: '4px' }}>Click "Call Next Patient" to bring the first waiting patient into consultation.</p>
                  </div>
                )}

                {/* Next Up Preview */}
                {nextWaiting && (
                  <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '1rem', borderRadius: '12px', border: '1px solid var(--border-color)', marginBottom: '1.25rem' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', fontWeight: 600, textTransform: 'uppercase' }}>NEXT PATIENT IN QUEUE</div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '6px' }}>
                      <div>
                        <span style={{ fontWeight: 800, fontSize: '1.1rem', color: '#fbbf24' }}>#{nextWaiting.queueNumber}</span>
                        <span style={{ fontWeight: 600, marginLeft: '8px', fontSize: '0.95rem' }}>{nextWaiting.patientName}</span>
                      </div>
                      <span style={{ fontSize: '0.78rem', color: 'var(--cyan)' }}>Est. Wait: {nextWaiting.formattedWaitRange}</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <button
                  onClick={handleCallNext}
                  className="btn-success"
                  style={{ width: '100%', justifyContent: 'center', padding: '0.9rem', fontSize: '1rem' }}
                >
                  <Play size={18} fill="#ffffff" /> CALL NEXT PATIENT
                </button>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <button
                    onClick={handleCompleteCurrent}
                    disabled={!currentServing}
                    className="btn-primary"
                    style={{ justifyContent: 'center', opacity: currentServing ? 1 : 0.5 }}
                  >
                    <CheckCircle2 size={16} /> Complete
                  </button>

                  <button
                    onClick={handleMarkNoShow}
                    className="btn-danger"
                    style={{ justifyContent: 'center' }}
                  >
                    <UserX size={16} /> No-Show
                  </button>
                </div>
              </div>

            </div>

            {/* Today's Queue Table */}
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Today's Patient Queue List</h3>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Real-time synchronization with patient mobile app & AI estimation engine.</p>
                </div>

                {/* Filter Pills */}
                <div style={{ display: 'flex', gap: '6px' }}>
                  {['ALL', 'WAITING', 'NOW_SERVING', 'COMPLETED', 'NO_SHOW'].map(st => (
                    <button
                      key={st}
                      onClick={() => setFilterStatus(st)}
                      style={{
                        background: filterStatus === st ? 'rgba(59, 130, 246, 0.2)' : 'transparent',
                        color: filterStatus === st ? '#60a5fa' : 'var(--text-muted)',
                        border: filterStatus === st ? '1px solid #3b82f6' : '1px solid transparent',
                        padding: '4px 10px',
                        borderRadius: '6px',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        cursor: 'pointer'
                      }}
                    >
                      {st}
                    </button>
                  ))}
                </div>
              </div>

              {/* Table */}
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                      <th style={{ padding: '10px' }}>Q#</th>
                      <th style={{ padding: '10px' }}>Patient Name</th>
                      <th style={{ padding: '10px' }}>Apt ID</th>
                      <th style={{ padding: '10px' }}>Status</th>
                      <th style={{ padding: '10px' }}>Priority</th>
                      <th style={{ padding: '10px' }}>AI Est. Wait</th>
                      <th style={{ padding: '10px', textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredItems.map(item => (
                      <tr
                        key={item.appointmentId}
                        style={{
                          borderBottom: '1px solid var(--border-color)',
                          background: item.status === 'NOW_SERVING' ? 'rgba(6, 182, 212, 0.06)' : 'transparent'
                        }}
                      >
                        <td style={{ padding: '12px 10px', fontWeight: 800, fontSize: '1rem', color: item.status === 'NOW_SERVING' ? '#38bdf8' : 'inherit' }}>
                          #{item.queueNumber}
                        </td>
                        <td style={{ padding: '12px 10px', fontWeight: 600 }}>{item.patientName}</td>
                        <td style={{ padding: '12px 10px', color: 'var(--text-muted)', fontSize: '0.8rem' }}>{item.appointmentId}</td>
                        <td style={{ padding: '12px 10px' }}>
                          {item.status === 'NOW_SERVING' && <span className="badge badge-now-serving">NOW SERVING</span>}
                          {item.status === 'APPROACHING' && <span className="badge badge-approaching">APPROACHING</span>}
                          {item.status === 'WAITING' && <span className="badge badge-waiting">WAITING</span>}
                          {item.status === 'COMPLETED' && <span className="badge badge-completed">COMPLETED</span>}
                          {item.status === 'NO_SHOW' && <span className="badge badge-noshow">NO SHOW</span>}
                        </td>
                        <td style={{ padding: '12px 10px' }}>
                          {item.isPriority ? (
                            <span className="badge badge-priority"><Flame size={12} /> Emergency</span>
                          ) : (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>Standard</span>
                          )}
                        </td>
                        <td style={{ padding: '12px 10px', color: 'var(--cyan)', fontWeight: 600, fontSize: '0.82rem' }}>
                          {item.formattedWaitRange}
                        </td>
                        <td style={{ padding: '12px 10px', textAlign: 'right' }}>
                          <button
                            onClick={() => handleTogglePriority(item.appointmentId, item.isPriority)}
                            style={{
                              background: item.isPriority ? 'rgba(239, 68, 68, 0.2)' : 'rgba(255,255,255,0.05)',
                              color: item.isPriority ? '#f87171' : 'var(--text-muted)',
                              border: '1px solid var(--border-color)',
                              padding: '4px 8px',
                              borderRadius: '6px',
                              fontSize: '0.75rem',
                              cursor: 'pointer'
                            }}
                          >
                            {item.isPriority ? 'Remove Priority' : 'Set Priority'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

            </div>

          </div>
        </>
      ) : (
        /* Schedule Management Tab */
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '0.5rem' }}>Doctor Working Hours & Slot Configuration</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
            Updating your working schedule automatically recalculates available booking slots shown to patients on the mobile app.
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            
            <div>
              <h4 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '1rem', color: '#60a5fa' }}>Regular Working Schedule</h4>
              
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Select Doctor</label>
                <select
                  className="input-field"
                  value={selectedDoctor}
                  onChange={(e) => setSelectedDoctor(e.target.value)}
                >
                  <option value="">Select Doctor</option>
                  {doctorsList.map(doc => (
                    <option key={doc.id} value={doc.id}>{doc.doctorName} ({doc.specialization})</option>
                  ))}
                </select>
              </div>

              <div style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Working Days</label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(day => (
                    <button
                      key={day}
                      type="button"
                      onClick={() => handleToggleWorkingDay(day)}
                      style={{
                        background: scheduleForm.workingDays.includes(day) ? 'rgba(37, 99, 235, 0.3)' : 'rgba(255,255,255,0.05)',
                        color: scheduleForm.workingDays.includes(day) ? '#ffffff' : 'var(--text-subtle)',
                        border: scheduleForm.workingDays.includes(day) ? '1px solid #3b82f6' : '1px solid var(--border-color)',
                        padding: '6px 12px',
                        borderRadius: '8px',
                        fontSize: '0.8rem',
                        fontWeight: 600,
                        cursor: 'pointer'
                      }}
                    >
                      {day}
                    </button>
                  ))}
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Shift Start Time</label>
                  <input
                    type="text"
                    className="input-field"
                    value={scheduleForm.shiftStart}
                    onChange={(e) => setScheduleForm({ ...scheduleForm, shiftStart: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Shift End Time</label>
                  <input
                    type="text"
                    className="input-field"
                    value={scheduleForm.shiftEnd}
                    onChange={(e) => setScheduleForm({ ...scheduleForm, shiftEnd: e.target.value })}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Lunch Break Start</label>
                  <input
                    type="text"
                    className="input-field"
                    value={scheduleForm.lunchBreakStart}
                    onChange={(e) => setScheduleForm({ ...scheduleForm, lunchBreakStart: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Lunch Break End</label>
                  <input
                    type="text"
                    className="input-field"
                    value={scheduleForm.lunchBreakEnd}
                    onChange={(e) => setScheduleForm({ ...scheduleForm, lunchBreakEnd: e.target.value })}
                  />
                </div>
              </div>

              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Average Slot Duration (minutes)</label>
                <input
                  type="number"
                  className="input-field"
                  value={scheduleForm.avgSlotDuration}
                  onChange={(e) => setScheduleForm({ ...scheduleForm, avgSlotDuration: e.target.value })}
                />
              </div>

              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Daily Patient Capacity</label>
                <input
                  type="number"
                  className="input-field"
                  value={scheduleForm.dailyCapacity}
                  onChange={(e) => setScheduleForm({ ...scheduleForm, dailyCapacity: e.target.value })}
                  min="1"
                  max="50"
                />
                <p style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginTop: '4px' }}>
                  Maximum number of patients you can see per day. This controls automatic appointment scheduling.
                </p>
              </div>

              <button className="btn-primary" onClick={handleSaveSchedule}>
                Save Schedule Changes
              </button>
            </div>

            <div>
              <h4 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '1rem', color: '#f87171' }}>Leave & Unavailability Calendar</h4>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Dates marked as Leave will automatically block patients from booking appointments.
              </p>

              {scheduleForm.leaveDates.map((leaveDate) => (
                <div key={leaveDate} style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border-color)', borderRadius: '12px', padding: '1rem', marginBottom: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{leaveDate}</span>
                    <span style={{ fontSize: '0.75rem', color: '#f87171', background: 'rgba(239, 68, 68, 0.15)', padding: '2px 8px', borderRadius: '6px', marginLeft: '8px' }}>Full Day Leave</span>
                  </div>
                  <button onClick={() => handleRemoveLeaveDate(leaveDate)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer' }}>
                    <Trash2 size={16} />
                  </button>
                </div>
              ))}
              {scheduleForm.leaveDates.length === 0 && (
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>No leave dates scheduled.</p>
              )}

              <button className="btn-secondary" onClick={handleAddLeaveDate} style={{ width: '100%', justifyContent: 'center' }}>
                + Add Leave Date
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}
