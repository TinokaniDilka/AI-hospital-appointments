import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import DoctorDashboard from './components/DoctorDashboard';
import AdminDashboard from './components/AdminDashboard';
import PatientDashboard from './components/PatientDashboard';
import LoginPage from './components/LoginPage';

export default function App() {
  const [user, setUser] = useState(null);
  const [apiConnected, setApiConnected] = useState(false);
  const [aiConnected, setAiConnected] = useState(true);

  // Check for existing session on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  // Initial Queue Data State
  const [queueData, setQueueData] = useState({
    doctorId: 'doc_1',
    doctorName: 'Dr. Sarah Jenkins',
    departmentName: 'Cardiology',
    date: new Date().toISOString().split('T')[0],
    currentServingNumber: 2,
    items: [
      { appointmentId: 'APT-10001', patientId: 'pat_1', patientName: 'John Doe', queueNumber: 1, position: -1, status: 'COMPLETED', isPriority: false, formattedWaitRange: 'Completed' },
      { appointmentId: 'APT-10002', patientId: 'pat_2', patientName: 'Maria Garcia', queueNumber: 2, position: 0, status: 'NOW_SERVING', isPriority: false, formattedWaitRange: 'Now Serving in Room' },
      { appointmentId: 'APT-10003', patientId: 'pat_3', patientName: 'David Smith', queueNumber: 3, position: 1, status: 'APPROACHING', isPriority: true, formattedWaitRange: '5–10 mins' },
      { appointmentId: 'APT-10004', patientId: 'pat_4', patientName: 'Lisa Taylor', queueNumber: 4, position: 2, status: 'WAITING', isPriority: false, formattedWaitRange: '25–30 mins' },
      { appointmentId: 'APT-10005', patientId: 'pat_5', patientName: 'Robert Johnson', queueNumber: 5, position: 3, status: 'WAITING', isPriority: false, formattedWaitRange: '40–45 mins' },
    ]
  });

  const [doctorsList] = useState([
    { id: 'doc_1', doctorName: 'Dr. Sarah Jenkins', specialization: 'Cardiology Specialist', departmentName: 'Cardiology', branchName: 'Central Medical Complex', roomNumber: 'Room 204', avgConsultationMinutes: 20 },
    { id: 'doc_2', doctorName: 'Dr. Michael Chang', specialization: 'Pediatric Specialist', departmentName: 'Pediatrics', branchName: 'Central Medical Complex', roomNumber: 'Room 108', avgConsultationMinutes: 15 },
    { id: 'doc_3', doctorName: 'Dr. Emily Rodriguez', specialization: 'Orthopedic Surgeon', departmentName: 'Orthopedics', branchName: 'Northside Care Center', roomNumber: 'Room 302', avgConsultationMinutes: 25 },
    { id: 'doc_4', doctorName: 'Dr. James Wilson', specialization: 'General Practitioner', departmentName: 'General Medicine', branchName: 'Central Medical Complex', roomNumber: 'Room 102', avgConsultationMinutes: 15 },
  ]);

  const [auditLogs] = useState([
    { action: 'Queue Priority Escalated', actorRole: 'ROLE_DOCTOR', details: 'Set priority emergency for APT-10003 (Chest Pain)', timestamp: 'Today, 09:42 AM' },
    { action: 'Doctor Schedule Updated', actorRole: 'ROLE_ADMIN', details: 'Updated Dr. Sarah Jenkins working hours to 09:00 - 17:00', timestamp: 'Today, 08:30 AM' },
    { action: 'Appointment Booked', actorRole: 'ROLE_PATIENT', details: 'Patient Lisa Taylor booked slot 10:00 - 10:20', timestamp: 'Today, 08:15 AM' },
  ]);

  useEffect(() => {
    // Attempt pinging Spring Boot backend
    fetch('http://localhost:8080/api/v1/hospitals')
      .then(res => {
        if (res.ok) setApiConnected(true);
      })
      .catch(() => setApiConnected(false));
  }, []);

  const handleLogin = (loggedInUser) => {
    setUser(loggedInUser);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  // Show login page if not authenticated
  if (!user) {
    return <LoginPage onLogin={handleLogin} />;
  }

  // Route to appropriate dashboard based on user role
  const renderDashboard = () => {
    switch (user?.role) {
      case 'ROLE_DOCTOR':
        return (
          <DoctorDashboard
            queueData={queueData}
            setQueueData={setQueueData}
          />
        );
      case 'ROLE_ADMIN':
        return (
          <AdminDashboard
            doctorsList={doctorsList}
            auditLogs={auditLogs}
          />
        );
      case 'ROLE_PATIENT':
        return <PatientDashboard user={user} />;
      default:
        return <div className="p-8 text-center">Unknown role</div>;
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-main)', color: 'var(--text-main)', paddingBottom: '3rem' }}>
      <Navbar
        apiConnected={apiConnected}
        aiConnected={aiConnected}
        user={user}
        onLogout={handleLogout}
      />

      <main>
        {renderDashboard()}
      </main>
    </div>
  );
}
