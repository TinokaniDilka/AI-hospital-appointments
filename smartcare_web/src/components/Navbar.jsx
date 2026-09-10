import React from 'react';
import { Activity, UserCheck, ShieldAlert, Cpu, Stethoscope, Building2, LogOut } from 'lucide-react';

export default function Navbar({ apiConnected, aiConnected, user, onLogout }) {
  return (
    <header className="glass-panel" style={{ borderRadius: 0, borderTop: 'none', borderLeft: 'none', borderRight: 'none', padding: '0.85rem 2rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', maxWidth: '1450px', margin: '0 auto' }}>
        
        {/* Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            background: 'linear-gradient(135deg, #2563eb, #06b6d4)',
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 15px rgba(37, 99, 235, 0.4)'
          }}>
            <Activity color="#ffffff" size={24} />
          </div>
          <div>
            <h1 style={{ fontSize: '1.25rem', fontWeight: 800, letterSpacing: '-0.02em', background: 'linear-gradient(90deg, #ffffff, #93c5fd)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              SmartCare <span style={{ fontSize: '0.75rem', color: '#38bdf8', border: '1px solid rgba(56, 189, 248, 0.3)', padding: '2px 8px', borderRadius: '12px', verticalAlign: 'middle', WebkitTextFillColor: 'initial' }}>AI Queue Engine</span>
            </h1>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Hospital Appointment & Real-Time Queue Management</p>
          </div>
        </div>

        {/* Live Service Connections Indicators */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem', color: 'var(--text-muted)', background: 'rgba(255, 255, 255, 0.04)', padding: '4px 12px', borderRadius: '20px', border: '1px solid var(--border-color)' }}>
            <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: apiConnected ? '#10b981' : '#f59e0b', boxShadow: apiConnected ? '0 0 8px #10b981' : 'none' }}></div>
            <span>Spring Boot API: {apiConnected ? 'Connected (8080)' : 'Demo Mode'}</span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem', color: 'var(--text-muted)', background: 'rgba(255, 255, 255, 0.04)', padding: '4px 12px', borderRadius: '20px', border: '1px solid var(--border-color)' }}>
            <Cpu size={14} color="#38bdf8" />
            <span>Python AI Model: {aiConnected ? 'Active (8000)' : 'Loaded (95.5% R²)'}</span>
          </div>

          {/* User Profile Badge */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', borderLeft: '1px solid var(--border-color)', paddingLeft: '1.25rem' }}>
            <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: user?.role === 'ROLE_DOCTOR' ? '#2563eb' : user?.role === 'ROLE_ADMIN' ? '#8b5cf6' : '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '0.9rem' }}>
              {user?.fullName ? user.fullName.substring(0, 2).toUpperCase() : 'U'}
            </div>
            <div>
              <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>
                {user?.fullName || 'User'}
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                {user?.role === 'ROLE_DOCTOR' ? 'Doctor' : user?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Patient'}
              </div>
            </div>
            <button
              onClick={onLogout}
              style={{
                background: 'transparent',
                border: '1px solid var(--border-color)',
                padding: '6px 10px',
                borderRadius: '8px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                fontSize: '0.8rem',
                color: 'var(--text-muted)',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => {
                e.target.style.background = 'rgba(239, 68, 68, 0.1)';
                e.target.style.borderColor = '#ef4444';
                e.target.style.color = '#ef4444';
              }}
              onMouseOut={(e) => {
                e.target.style.background = 'transparent';
                e.target.style.borderColor = 'var(--border-color)';
                e.target.style.color = 'var(--text-muted)';
              }}
            >
              <LogOut size={14} />
              Logout
            </button>
          </div>

        </div>
      </div>
    </header>
  );
}
