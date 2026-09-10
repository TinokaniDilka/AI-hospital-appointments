import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LineChart, Line, PieChart, Pie, Cell } from 'recharts';
import { Building2, Shield, Activity, Users, Clock, AlertCircle, FileText, Cpu, CheckCircle2, Sliders, RefreshCw } from 'lucide-react';
import { api } from '../services/api';

export default function AdminDashboard({ doctorsList, auditLogs }) {
  const [activeTab, setActiveTab] = useState('ANALYTICS'); // 'ANALYTICS', 'DOCTORS', 'PATIENTS', 'APPOINTMENTS', 'QUEUE', 'AUDIT'
  const [analyticsData, setAnalyticsData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showAddDoctorModal, setShowAddDoctorModal] = useState(false);
  const [newDoctor, setNewDoctor] = useState({
    name: '',
    email: '',
    specialization: '',
    department: '',
    branch: '',
    roomNumber: '',
    avgConsultationMinutes: 20
  });
  const [patients, setPatients] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [queues, setQueues] = useState([]);
  const [hospitals, setHospitals] = useState([]);
  const [branches, setBranches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [hospitalSubTab, setHospitalSubTab] = useState('HOSPITALS'); // 'HOSPITALS', 'BRANCHES', 'DEPARTMENTS'

  useEffect(() => {
    fetchAnalytics();
    fetchPatients();
    fetchAppointments();
    fetchQueues();
    fetchHospitals();
    fetchBranches();
    fetchDepartments();
  }, []);

  const fetchHospitals = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/hospitals', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setHospitals(data);
      }
    } catch (error) {
      console.error('Failed to fetch hospitals:', error);
    }
  };

  const fetchBranches = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/branches', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setBranches(data);
      }
    } catch (error) {
      console.error('Failed to fetch branches:', error);
    }
  };

  const fetchDepartments = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/departments', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setDepartments(data);
      }
    } catch (error) {
      console.error('Failed to fetch departments:', error);
    }
  };

  const fetchPatients = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/patients', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setPatients(data);
      }
    } catch (error) {
      console.error('Failed to fetch patients:', error);
    }
  };

  const fetchAppointments = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/appointments', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setAppointments(data);
      }
    } catch (error) {
      console.error('Failed to fetch appointments:', error);
    }
  };

  const fetchQueues = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/v1/queues', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setQueues(data);
      }
    } catch (error) {
      console.error('Failed to fetch queues:', error);
    }
  };

  const fetchAnalytics = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getAnalytics();
      setAnalyticsData(data);
    } catch (err) {
      setError('Failed to load analytics data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddDoctor = async () => {
    try {
      await api.createDoctor(newDoctor);
      setShowAddDoctorModal(false);
      setNewDoctor({
        name: '',
        email: '',
        specialization: '',
        department: '',
        branch: '',
        roomNumber: '',
        avgConsultationMinutes: 20
      });
      // Refresh doctors list would go here
      alert('Doctor added successfully!');
    } catch (err) {
      alert('Failed to add doctor: ' + err.message);
    }
  };

  const workloadData = [
    { name: 'Dr. Jenkins', department: 'Cardiology', appointments: 18, avgWait: 22 },
    { name: 'Dr. Chang', department: 'Pediatrics', appointments: 24, avgWait: 18 },
    { name: 'Dr. Rodriguez', department: 'Orthopedics', appointments: 14, avgWait: 28 },
    { name: 'Dr. Wilson', department: 'Gen. Medicine', appointments: 30, avgWait: 15 },
  ];

  const waitTimeTrend = [
    { hour: '08:00 AM', waitMins: 10, aiPredicted: 11 },
    { hour: '09:00 AM', waitMins: 22, aiPredicted: 20 },
    { hour: '10:00 AM', waitMins: 35, aiPredicted: 33 },
    { hour: '11:00 AM', waitMins: 42, aiPredicted: 40 },
    { hour: '12:00 PM', waitMins: 28, aiPredicted: 27 },
    { hour: '02:00 PM', waitMins: 30, aiPredicted: 29 },
    { hour: '04:00 PM', waitMins: 15, aiPredicted: 16 },
  ];

  const deptColors = ['#3b82f6', '#06b6d4', '#10b981', '#f59e0b'];
  const deptData = [
    { name: 'General Medicine', value: 35 },
    { name: 'Pediatrics', value: 28 },
    { name: 'Cardiology', value: 22 },
    { name: 'Orthopedics', value: 15 },
  ];

  return (
    <div style={{ maxWidth: '1450px', margin: '1.5rem auto', padding: '0 1rem' }}>
      
      {/* Header & Tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>Hospital Administrator Management Dashboard</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Centralized operational visibility, multi-branch control, and AI waiting time statistics.</p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {activeTab === 'ANALYTICS' && (
            <button
              onClick={fetchAnalytics}
              disabled={loading}
              style={{
                background: 'rgba(255,255,255,0.05)',
                border: '1px solid var(--border-color)',
                padding: '6px 12px',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                fontSize: '0.85rem',
                color: 'var(--text-muted)'
              }}
            >
              <RefreshCw size={14} style={{ animation: loading ? 'spin 1s linear infinite' : 'none' }} />
              {loading ? 'Loading...' : 'Refresh'}
            </button>
          )}
          <div style={{ display: 'flex', gap: '0.5rem', background: 'rgba(255,255,255,0.04)', padding: '4px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
            <button
              onClick={() => setActiveTab('ANALYTICS')}
              className={activeTab === 'ANALYTICS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Activity size={16} /> Analytics
            </button>
            <button
              onClick={() => setActiveTab('DOCTORS')}
              className={activeTab === 'DOCTORS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Building2 size={16} /> Doctors
            </button>
            <button
              onClick={() => setActiveTab('PATIENTS')}
              className={activeTab === 'PATIENTS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Users size={16} /> Patients
            </button>
            <button
              onClick={() => setActiveTab('APPOINTMENTS')}
              className={activeTab === 'APPOINTMENTS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Clock size={16} /> Appointments
            </button>
            <button
              onClick={() => setActiveTab('QUEUE')}
              className={activeTab === 'QUEUE' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Sliders size={16} /> Live Queue
            </button>
            <button
              onClick={() => setActiveTab('AUDIT')}
              className={activeTab === 'AUDIT' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Shield size={16} /> Audit
            </button>
          </div>
        </div>
      </div>

      {activeTab === 'ANALYTICS' && (
        <>
          {/* Executive Overview KPI Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.25rem', marginBottom: '1.5rem' }}>
            
            {error && (
              <div style={{ gridColumn: '1 / -1', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid #ef4444', borderRadius: '8px', padding: '1rem', color: '#f87171' }}>
                {error}
              </div>
            )}

            <div className="glass-card">
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>TOTAL SYSTEM APPOINTMENTS</div>
              <div style={{ fontSize: '2rem', fontWeight: 900, color: '#ffffff', margin: '4px 0' }}>
                {analyticsData?.totalAppointments || 86}
              </div>
              <div style={{ fontSize: '0.78rem', color: '#34d399' }}>
                {analyticsData?.appointmentGrowth || '↑ 12%'} from previous week
              </div>
            </div>

            <div className="glass-card">
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>AVERAGE PATIENT WAIT TIME</div>
              <div style={{ fontSize: '2rem', fontWeight: 900, color: '#38bdf8', margin: '4px 0' }}>
                {analyticsData?.averageWaitTime ? `${analyticsData.averageWaitTime.toFixed(1)} Mins` : '24.5 Mins'}
              </div>
              <div style={{ fontSize: '0.78rem', color: '#34d399' }}>
                {analyticsData?.waitTimeImprovement || '↓ 18%'} improvement with AI routing
              </div>
            </div>

            <div className="glass-card">
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>NO-SHOW & CANCEL RATE</div>
              <div style={{ fontSize: '2rem', fontWeight: 900, color: '#f59e0b', margin: '4px 0' }}>
                {analyticsData?.noShowRate ? `${(analyticsData.noShowRate * 100).toFixed(1)}%` : '6.2%'}
              </div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                {analyticsData?.noShowStatus || 'Within target threshold (< 10%)'}
              </div>
            </div>

            <div className="glass-card">
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>AI PREDICTION ACCURACY (MAE)</div>
              <div style={{ fontSize: '2rem', fontWeight: 900, color: '#a855f7', margin: '4px 0' }}>
                {analyticsData?.aiAccuracyMAE ? `± ${analyticsData.aiAccuracyMAE.toFixed(1)} Mins` : '± 2.4 Mins'}
              </div>
              <div style={{ fontSize: '0.78rem', color: '#a855f7' }}>
                {analyticsData?.modelPerformance || '95.5% R² Model Performance Score'}
              </div>
            </div>

          </div>

          {/* Interactive Recharts Analytics Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
            
            {/* Waiting Time Trends Chart */}
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Waiting Time Trends & AI Prediction Accuracy</h3>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Comparison between actual observed wait times and AI predicted values.</p>
                </div>
                <div style={{ display: 'flex', gap: '12px', fontSize: '0.78rem' }}>
                  <span style={{ color: '#38bdf8' }}>● Actual Observed</span>
                  <span style={{ color: '#a855f7' }}>● AI Predicted</span>
                </div>
              </div>

              <div style={{ width: '100%', height: '280px' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={waitTimeTrend}>
                    <XAxis dataKey="hour" stroke="#64748b" />
                    <YAxis stroke="#64748b" />
                    <Tooltip contentStyle={{ background: '#131e3a', borderColor: 'rgba(255,255,255,0.1)', color: '#fff' }} />
                    <Line type="monotone" dataKey="waitMins" stroke="#38bdf8" strokeWidth={3} dot={{ r: 4 }} />
                    <Line type="monotone" dataKey="aiPredicted" stroke="#a855f7" strokeWidth={2} strokeDasharray="4 4" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Department Breakdown */}
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '0.25rem' }}>Department Workload Share</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>Percentage distribution of appointments.</p>

              <div style={{ width: '100%', height: '200px' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={deptData} innerRadius={55} outerRadius={80} paddingAngle={4} dataKey="value">
                      {deptData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={deptColors[index % deptColors.length]} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ background: '#131e3a', borderColor: 'rgba(255,255,255,0.1)', color: '#fff' }} />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '0.78rem' }}>
                {deptData.map((item, idx) => (
                  <div key={item.name} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: deptColors[idx] }}></div>
                    <span>{item.name} ({item.value}%)</span>
                  </div>
                ))}
              </div>
            </div>

          </div>

          {/* Doctor Workload Bar Chart */}
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '0.25rem' }}>Doctor Daily Workload & Patient Consultations</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>Number of patients managed per practitioner today.</p>

            <div style={{ width: '100%', height: '220px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={workloadData}>
                  <XAxis dataKey="name" stroke="#64748b" />
                  <YAxis stroke="#64748b" />
                  <Tooltip contentStyle={{ background: '#131e3a', borderColor: 'rgba(255,255,255,0.1)', color: '#fff' }} />
                  <Bar dataKey="appointments" fill="#3b82f6" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </>
      )}

      {activeTab === 'DOCTORS' && (
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Hospital Management</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Manage hospitals, branches, departments, and medical staff</p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', background: 'rgba(255,255,255,0.04)', padding: '4px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
            <button
              onClick={() => setHospitalSubTab('HOSPITALS')}
              className={hospitalSubTab === 'HOSPITALS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Building2 size={16} /> Hospitals
            </button>
            <button
              onClick={() => setHospitalSubTab('BRANCHES')}
              className={hospitalSubTab === 'BRANCHES' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Building2 size={16} /> Branches
            </button>
            <button
              onClick={() => setHospitalSubTab('DEPARTMENTS')}
              className={hospitalSubTab === 'DEPARTMENTS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <FileText size={16} /> Departments
            </button>
            <button
              onClick={() => setHospitalSubTab('DOCTORS')}
              className={hospitalSubTab === 'DOCTORS' ? 'btn-primary' : 'btn-secondary'}
              style={{ fontSize: '0.85rem' }}
            >
              <Users size={16} /> Doctors
            </button>
          </div>

          {hospitalSubTab === 'HOSPITALS' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h4 style={{ fontSize: '1rem', fontWeight: 700 }}>Hospitals</h4>
                <button className="btn-primary" style={{ fontSize: '0.8rem' }}>+ Add Hospital</button>
              </div>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                      <th style={{ padding: '12px' }}>Hospital Name</th>
                      <th style={{ padding: '12px' }}>Address</th>
                      <th style={{ padding: '12px' }}>Phone</th>
                      <th style={{ padding: '12px' }}>Email</th>
                      <th style={{ padding: '12px' }}>Status</th>
                      <th style={{ padding: '12px' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {hospitals.map(hospital => (
                      <tr key={hospital.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ padding: '14px 12px', fontWeight: 700 }}>{hospital.name}</td>
                        <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{hospital.address}</td>
                        <td style={{ padding: '14px 12px' }}>{hospital.phone}</td>
                        <td style={{ padding: '14px 12px' }}>{hospital.email}</td>
                        <td style={{ padding: '14px 12px' }}>
                          <span className="badge badge-now-serving">{hospital.active ? 'ACTIVE' : 'INACTIVE'}</span>
                        </td>
                        <td style={{ padding: '14px 12px' }}>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem' }}>Edit</button>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
                              {hospital.active ? 'Deactivate' : 'Activate'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                    {hospitals.length === 0 && (
                      <tr>
                        <td colSpan="6" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                          No hospitals found
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {hospitalSubTab === 'BRANCHES' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h4 style={{ fontSize: '1rem', fontWeight: 700 }}>Branches</h4>
                <button className="btn-primary" style={{ fontSize: '0.8rem' }}>+ Add Branch</button>
              </div>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                      <th style={{ padding: '12px' }}>Branch Name</th>
                      <th style={{ padding: '12px' }}>Hospital</th>
                      <th style={{ padding: '12px' }}>Address</th>
                      <th style={{ padding: '12px' }}>Phone</th>
                      <th style={{ padding: '12px' }}>Status</th>
                      <th style={{ padding: '12px' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {branches.map(branch => (
                      <tr key={branch.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ padding: '14px 12px', fontWeight: 700 }}>{branch.name}</td>
                        <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{branch.hospitalName}</td>
                        <td style={{ padding: '14px 12px' }}>{branch.address}</td>
                        <td style={{ padding: '14px 12px' }}>{branch.phone}</td>
                        <td style={{ padding: '14px 12px' }}>
                          <span className="badge badge-now-serving">{branch.active ? 'ACTIVE' : 'INACTIVE'}</span>
                        </td>
                        <td style={{ padding: '14px 12px' }}>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem' }}>Edit</button>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
                              {branch.active ? 'Deactivate' : 'Activate'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                    {branches.length === 0 && (
                      <tr>
                        <td colSpan="6" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                          No branches found
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {hospitalSubTab === 'DEPARTMENTS' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h4 style={{ fontSize: '1rem', fontWeight: 700 }}>Departments</h4>
                <button className="btn-primary" style={{ fontSize: '0.8rem' }}>+ Add Department</button>
              </div>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                      <th style={{ padding: '12px' }}>Department Name</th>
                      <th style={{ padding: '12px' }}>Branch</th>
                      <th style={{ padding: '12px' }}>Floor</th>
                      <th style={{ padding: '12px' }}>Status</th>
                      <th style={{ padding: '12px' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {departments.map(dept => (
                      <tr key={dept.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ padding: '14px 12px', fontWeight: 700 }}>{dept.name}</td>
                        <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{dept.branchName}</td>
                        <td style={{ padding: '14px 12px' }}>{dept.floorNumber}</td>
                        <td style={{ padding: '14px 12px' }}>
                          <span className="badge badge-now-serving">{dept.active ? 'ACTIVE' : 'INACTIVE'}</span>
                        </td>
                        <td style={{ padding: '14px 12px' }}>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem' }}>Edit</button>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
                              {dept.active ? 'Deactivate' : 'Activate'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                    {departments.length === 0 && (
                      <tr>
                        <td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                          No departments found
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {hospitalSubTab === 'DOCTORS' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h4 style={{ fontSize: '1rem', fontWeight: 700 }}>Doctors</h4>
                <button className="btn-primary" onClick={() => setShowAddDoctorModal(true)}>+ Add New Doctor</button>
              </div>

              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                      <th style={{ padding: '12px' }}>Doctor Name</th>
                      <th style={{ padding: '12px' }}>Specialization</th>
                      <th style={{ padding: '12px' }}>Department</th>
                      <th style={{ padding: '12px' }}>Branch</th>
                      <th style={{ padding: '12px' }}>Room</th>
                      <th style={{ padding: '12px' }}>Avg Consultation</th>
                      <th style={{ padding: '12px' }}>Status</th>
                      <th style={{ padding: '12px' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {doctorsList.map(doc => (
                      <tr key={doc.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ padding: '14px 12px', fontWeight: 700 }}>{doc.doctorName}</td>
                        <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{doc.specialization}</td>
                        <td style={{ padding: '14px 12px' }}>{doc.departmentName}</td>
                        <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{doc.branchName}</td>
                        <td style={{ padding: '14px 12px', fontWeight: 600, color: 'var(--cyan)' }}>{doc.roomNumber}</td>
                        <td style={{ padding: '14px 12px' }}>{doc.avgConsultationMinutes} mins</td>
                        <td style={{ padding: '14px 12px' }}>
                          <span className="badge badge-now-serving">{doc.active ? 'ACTIVE' : 'INACTIVE'}</span>
                        </td>
                        <td style={{ padding: '14px 12px' }}>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem' }}>Edit</button>
                            <button className="btn-secondary" style={{ padding: '4px 8px', fontSize: '0.75rem', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
                              {doc.active ? 'Deactivate' : 'Activate'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'PATIENTS' && (
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Patient Management</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>View and manage all registered patients</p>
            </div>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                  <th style={{ padding: '12px' }}>Patient ID</th>
                  <th style={{ padding: '12px' }}>Name</th>
                  <th style={{ padding: '12px' }}>Email</th>
                  <th style={{ padding: '12px' }}>Phone</th>
                  <th style={{ padding: '12px' }}>Blood Group</th>
                  <th style={{ padding: '12px' }}>Gender</th>
                  <th style={{ padding: '12px' }}>DOB</th>
                </tr>
              </thead>
              <tbody>
                {patients.map(patient => (
                  <tr key={patient.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                    <td style={{ padding: '14px 12px', fontWeight: 600, color: 'var(--cyan)' }}>{patient.patientId || patient.id}</td>
                    <td style={{ padding: '14px 12px', fontWeight: 700 }}>{patient.fullName}</td>
                    <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{patient.email}</td>
                    <td style={{ padding: '14px 12px' }}>{patient.phone}</td>
                    <td style={{ padding: '14px 12px' }}>{patient.bloodGroup || 'N/A'}</td>
                    <td style={{ padding: '14px 12px' }}>{patient.gender || 'N/A'}</td>
                    <td style={{ padding: '14px 12px' }}>{patient.dob || 'N/A'}</td>
                  </tr>
                ))}
                {patients.length === 0 && (
                  <tr>
                    <td colSpan="7" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                      No patients found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'APPOINTMENTS' && (
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Appointment Monitoring</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>View all appointments across the hospital</p>
            </div>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                  <th style={{ padding: '12px' }}>Appointment ID</th>
                  <th style={{ padding: '12px' }}>Patient</th>
                  <th style={{ padding: '12px' }}>Doctor</th>
                  <th style={{ padding: '12px' }}>Date</th>
                  <th style={{ padding: '12px' }}>Time Slot</th>
                  <th style={{ padding: '12px' }}>Department</th>
                  <th style={{ padding: '12px' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map(apt => (
                  <tr key={apt.appointmentId || apt.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                    <td style={{ padding: '14px 12px', fontWeight: 600, color: 'var(--cyan)' }}>{apt.appointmentId || apt.id}</td>
                    <td style={{ padding: '14px 12px', fontWeight: 700 }}>{apt.patientName}</td>
                    <td style={{ padding: '14px 12px' }}>{apt.doctorName}</td>
                    <td style={{ padding: '14px 12px' }}>{apt.appointmentDate}</td>
                    <td style={{ padding: '14px 12px' }}>{apt.timeSlot}</td>
                    <td style={{ padding: '14px 12px', color: 'var(--text-muted)' }}>{apt.departmentName}</td>
                    <td style={{ padding: '14px 12px' }}>
                      <span className={`badge ${apt.status === 'COMPLETED' ? 'badge-completed' : apt.status === 'CANCELLED' ? 'badge-cancelled' : 'badge-now-serving'}`}>
                        {apt.status}
                      </span>
                    </td>
                  </tr>
                ))}
                {appointments.length === 0 && (
                  <tr>
                    <td colSpan="7" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                      No appointments found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'QUEUE' && (
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Live Queue Monitoring</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Real-time queue status for all doctors</p>
            </div>
            <button className="btn-secondary" onClick={fetchQueues}>
              <RefreshCw size={14} /> Refresh
            </button>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1rem' }}>
            {queues.map(queue => (
              <div key={queue.id} className="glass-card" style={{ padding: '1.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h4 style={{ fontSize: '1rem', fontWeight: 700 }}>{queue.doctorName || 'Doctor'}</h4>
                  <span className="badge badge-now-serving">LIVE</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', fontSize: '0.85rem' }}>
                  <div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Current Serving</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--cyan)' }}>{queue.currentlyServing || 0}</div>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>In Queue</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f59e0b' }}>{queue.queueLength || 0}</div>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Avg Wait</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{queue.avgWaitTime || 'N/A'}</div>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Date</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{queue.date || 'N/A'}</div>
                  </div>
                </div>
              </div>
            ))}
            {queues.length === 0 && (
              <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No active queues found
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'AUDIT' && (
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '0.5rem' }}>System Operational & Security Audit Trail</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
            Auditing administrative events, queue overrides, schedule changes, and security actions.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {auditLogs.map((log, idx) => (
              <div key={idx} style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border-color)', borderRadius: '10px', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 700, fontSize: '0.9rem', color: '#ffffff' }}>{log.action}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '2px' }}>{log.details}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '0.75rem', background: 'rgba(59, 130, 246, 0.15)', color: '#60a5fa', padding: '2px 8px', borderRadius: '6px' }}>{log.actorRole}</span>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-subtle)', marginTop: '4px' }}>{log.timestamp}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Add Doctor Modal */}
      {showAddDoctorModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.7)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: '#1e293b',
            borderRadius: '16px',
            padding: '2rem',
            width: '100%',
            maxWidth: '500px',
            border: '1px solid var(--border-color)'
          }}>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1.5rem' }}>Add New Doctor</h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Full Name</label>
                <input
                  type="text"
                  value={newDoctor.name}
                  onChange={(e) => setNewDoctor({ ...newDoctor, name: e.target.value })}
                  className="input-field"
                  placeholder="Dr. John Smith"
                />
              </div>
              
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Email</label>
                <input
                  type="email"
                  value={newDoctor.email}
                  onChange={(e) => setNewDoctor({ ...newDoctor, email: e.target.value })}
                  className="input-field"
                  placeholder="doctor@hospital.com"
                />
              </div>
              
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Specialization</label>
                <input
                  type="text"
                  value={newDoctor.specialization}
                  onChange={(e) => setNewDoctor({ ...newDoctor, specialization: e.target.value })}
                  className="input-field"
                  placeholder="Cardiologist"
                />
              </div>
              
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Department</label>
                <input
                  type="text"
                  value={newDoctor.department}
                  onChange={(e) => setNewDoctor({ ...newDoctor, department: e.target.value })}
                  className="input-field"
                  placeholder="Cardiology"
                />
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Branch</label>
                  <input
                    type="text"
                    value={newDoctor.branch}
                    onChange={(e) => setNewDoctor({ ...newDoctor, branch: e.target.value })}
                    className="input-field"
                    placeholder="Central Medical"
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Room Number</label>
                  <input
                    type="text"
                    value={newDoctor.roomNumber}
                    onChange={(e) => setNewDoctor({ ...newDoctor, roomNumber: e.target.value })}
                    className="input-field"
                    placeholder="Room 204"
                  />
                </div>
              </div>
              
              <div>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>Avg Consultation (mins)</label>
                <input
                  type="number"
                  value={newDoctor.avgConsultationMinutes}
                  onChange={(e) => setNewDoctor({ ...newDoctor, avgConsultationMinutes: parseInt(e.target.value) })}
                  className="input-field"
                  placeholder="20"
                />
              </div>
            </div>
            
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button
                onClick={() => setShowAddDoctorModal(false)}
                className="btn-secondary"
                style={{ flex: 1, justifyContent: 'center' }}
              >
                Cancel
              </button>
              <button
                onClick={handleAddDoctor}
                className="btn-primary"
                style={{ flex: 1, justifyContent: 'center' }}
              >
                Add Doctor
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
