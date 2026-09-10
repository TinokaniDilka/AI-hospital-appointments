import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthScreen extends StatefulWidget {
  final Function(String name, String email) onLoginSuccess;

  const AuthScreen({super.key, required this.onLoginSuccess});

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  bool isLogin = true;
  final _emailCtrl = TextEditingController(text: 'john.doe@email.com');
  final _passCtrl = TextEditingController(text: 'patient123');
  final _nameCtrl = TextEditingController(text: 'John Doe');
  final _phoneCtrl = TextEditingController(text: '+1-555-0199');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 30),
              Center(
                child: Container(
                  width: 70,
                  height: 70,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF2563EB), Color(0xFF06B6D4)],
                    ),
                    borderRadius: BorderRadius.circular(20),
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFF2563EB).withOpacity(0.4),
                        blurRadius: 20,
                        spreadRadius: 2,
                      )
                    ],
                  ),
                  child: const Icon(Icons.medical_services_rounded, color: Colors.white, size: 36),
                ),
              ),
              const SizedBox(height: 20),
              Center(
                child: Text(
                  'SmartCare',
                  style: TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                    letterSpacing: -0.5,
                  ),
                ),
              ),
              Center(
                child: Text(
                  'AI-Powered Hospital Appointment & Queue System',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 13,
                    color: const Color(0xFF94A3B8),
                  ),
                ),
              ),
              const SizedBox(height: 40),

              // Segmented Toggle
              Container(
                decoration: BoxDecoration(
                  color: const Color(0xFF131E3A),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.white.withOpacity(0.08)),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: GestureDetector(
                        onTap: () => setState(() => isLogin = true),
                        child: Container(
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          decoration: BoxDecoration(
                            color: isLogin ? const Color(0xFF2563EB) : Colors.transparent,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Center(
                            child: Text(
                              'Patient Login',
                              style: TextStyle(
                                color: isLogin ? Colors.white : const Color(0xFF94A3B8),
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                    Expanded(
                      child: GestureDetector(
                        onTap: () => setState(() => isLogin = false),
                        child: Container(
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          decoration: BoxDecoration(
                            color: !isLogin ? const Color(0xFF2563EB) : Colors.transparent,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Center(
                            child: Text(
                              'Register Account',
                              style: TextStyle(
                                color: !isLogin ? Colors.white : const Color(0xFF94A3B8),
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 30),

              if (!isLogin) ...[
                Text('Full Name', style: TextStyle(color: const Color(0xFF94A3B8), fontSize: 13)),
                const SizedBox(height: 6),
                TextField(
                  controller: _nameCtrl,
                  style: const TextStyle(color: Colors.white),
                  decoration: _inputDecoration('Enter full name', Icons.person_outline),
                ),
                const SizedBox(height: 16),
                Text('Phone Number', style: TextStyle(color: const Color(0xFF94A3B8), fontSize: 13)),
                const SizedBox(height: 6),
                TextField(
                  controller: _phoneCtrl,
                  style: const TextStyle(color: Colors.white),
                  decoration: _inputDecoration('Enter phone number', Icons.phone_outlined),
                ),
                const SizedBox(height: 16),
              ],

              Text('Email Address', style: TextStyle(color: const Color(0xFF94A3B8), fontSize: 13)),
              const SizedBox(height: 6),
              TextField(
                controller: _emailCtrl,
                style: const TextStyle(color: Colors.white),
                decoration: _inputDecoration('patient@email.com', Icons.email_outlined),
              ),
              const SizedBox(height: 16),

              Text('Password', style: TextStyle(color: const Color(0xFF94A3B8), fontSize: 13)),
              const SizedBox(height: 6),
              TextField(
                controller: _passCtrl,
                obscureText: true,
                style: const TextStyle(color: Colors.white),
                decoration: _inputDecoration('••••••••', Icons.lock_outline),
              ),

              const SizedBox(height: 30),

              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF2563EB),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    elevation: 4,
                  ),
                  onPressed: () async {
                    final prefs = await SharedPreferences.getInstance();
                    await prefs.setBool('isLoggedIn', true);
                    await prefs.setString('patientName', _nameCtrl.text.isNotEmpty ? _nameCtrl.text : 'John Doe');
                    await prefs.setString('patientEmail', _emailCtrl.text.isNotEmpty ? _emailCtrl.text : 'john.doe@email.com');
                    
                    widget.onLoginSuccess(
                      _nameCtrl.text.isNotEmpty ? _nameCtrl.text : 'John Doe',
                      _emailCtrl.text.isNotEmpty ? _emailCtrl.text : 'john.doe@email.com',
                    );
                  },
                  child: Text(
                    isLogin ? 'Sign In to SmartCare' : 'Create Patient Account',
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  InputDecoration _inputDecoration(String hint, IconData icon) {
    return InputDecoration(
      hintText: hint,
      hintStyle: const TextStyle(color: Color(0xFF64748B)),
      prefixIcon: Icon(icon, color: const Color(0xFF64748B)),
      filled: true,
      fillColor: const Color(0xFF131E3A),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: Colors.white.withOpacity(0.08)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Color(0xFF2563EB)),
      ),
    );
  }
}
