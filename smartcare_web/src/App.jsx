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
  if (storedUser && storedToken && storedUser !== 'undefined' && storedToken !== 'undefined') {
    try {
      setUser(JSON.parse(storedUser));
    } catch (e) {
      console.error('Failed to parse stored user, clearing session', e);
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
  }
}, []);

  useEffect(() => {
    // Attempt pinging Spring Boot backend
    fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/hospitals`)
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
        return <DoctorDashboard user={user} />;
      case 'ROLE_ADMIN':
        return <AdminDashboard user={user} />;
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
