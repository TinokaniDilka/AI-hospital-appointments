import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'screens/splash_screen.dart';
import 'screens/auth_screen.dart';
import 'screens/home_screen.dart';
import 'screens/doctor_search_screen.dart';
import 'screens/history_screen.dart';
import 'screens/notifications_screen.dart';
import 'screens/profile_screen.dart';
import 'screens/settings_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(const SmartCareApp());
}

class SmartCareApp extends StatefulWidget {
  const SmartCareApp({super.key});

  @override
  State<SmartCareApp> createState() => _SmartCareAppState();
}

class _SmartCareAppState extends State<SmartCareApp> {
  bool isLoggedIn = false;
  String patientName = 'John Doe';
  String patientEmail = 'john.doe@email.com';

  @override
  void initState() {
    super.initState();
    _loadUserData();
  }

  Future<void> _loadUserData() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      isLoggedIn = prefs.getBool('isLoggedIn') ?? false;
      patientName = prefs.getString('patientName') ?? 'John Doe';
      patientEmail = prefs.getString('patientEmail') ?? 'john.doe@email.com';
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SmartCare - AI Queue & Appointments',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0B1329),
        primaryColor: const Color(0xFF2563EB),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF2563EB),
          secondary: Color(0xFF06B6D4),
          surface: Color(0xFF131E3A),
        ),
        useMaterial3: true,
      ),
      home: isLoggedIn
          ? MainNavigationWrapper(
              patientName: patientName,
              onLogout: () => setState(() => isLoggedIn = false),
            )
          : const SplashScreen(),
    );
  }
}

class MainNavigationWrapper extends StatefulWidget {
  final String patientName;
  final VoidCallback onLogout;

  const MainNavigationWrapper({
    super.key,
    required this.patientName,
    required this.onLogout,
  });

  @override
  State<MainNavigationWrapper> createState() => _MainNavigationWrapperState();
}

class _MainNavigationWrapperState extends State<MainNavigationWrapper> {
  int _currentIndex = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      HomeScreen(
        patientName: widget.patientName,
        onNavigateToHistory: () => setState(() => _currentIndex = 2),
      ),
      const DoctorSearchScreen(),
      const HistoryScreen(),
      const NotificationsScreen(),
      ProfileScreen(patientName: widget.patientName),
      SettingsScreen(onLogout: widget.onLogout),
    ];

    return Scaffold(
      body: pages[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        backgroundColor: const Color(0xFF131E3A),
        selectedItemColor: const Color(0xFF38BDF8),
        unselectedItemColor: const Color(0xFF64748B),
        type: BottomNavigationBarType.fixed,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home_rounded), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.search_rounded), label: 'Find Doctors'),
          BottomNavigationBarItem(icon: Icon(Icons.history_rounded), label: 'History'),
          BottomNavigationBarItem(icon: Icon(Icons.notifications_rounded), label: 'Alerts'),
          BottomNavigationBarItem(icon: Icon(Icons.person_rounded), label: 'Profile'),
          BottomNavigationBarItem(icon: Icon(Icons.settings_rounded), label: 'Settings'),
        ],
      ),
    );
  }
}
