import 'package:flutter/material.dart';
import '../models/doctor.dart';
import '../models/appointment.dart';
import '../services/api_service.dart';
import 'doctor_search_screen.dart';
import 'doctor_profile_screen.dart';
import 'live_queue_screen.dart';

class HomeScreen extends StatefulWidget {
  final String patientName;
  final VoidCallback onNavigateToHistory;

  const HomeScreen({super.key, required this.patientName, required this.onNavigateToHistory});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<DoctorModel> featuredDoctors = [];
  bool isLoading = true;

  // Active appointment demo state
  late AppointmentModel activeAppointment;

  @override
  void initState() {
    super.initState();
    activeAppointment = AppointmentModel(
      id: 'apt_active_1',
      appointmentId: 'APT-10004',
      patientId: 'pat_1',
      patientName: widget.patientName,
      doctorId: 'doc_1',
      doctorName: 'Dr. Sarah Jenkins',
      branchName: 'Central Medical Complex',
      departmentName: 'Cardiology',
      appointmentDate: DateTime.now().toString().split(' ')[0],
      timeSlot: '10:00 - 10:20',
      status: 'WAITING',
      queueNumber: 4,
    );
    _loadData();
  }

  void _loadData() async {
    final docs = await ApiService.getDoctors();
    setState(() {
      featuredDoctors = docs;
      isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header Row
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Welcome back,', style: TextStyle(color: const Color(0xFF94A3B8), fontSize: 13)),
                      Text(widget.patientName, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white)),
                    ],
                  ),
                  CircleAvatar(
                    radius: 22,
                    backgroundColor: const Color(0xFF2563EB),
                    child: Text(widget.patientName.substring(0, 1), style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18)),
                  ),
                ],
              ),

              const SizedBox(height: 20),

              // Active Queue Card Banner
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFF1E3A8A), Color(0xFF2563EB)],
                  ),
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(color: const Color(0xFF2563EB).withOpacity(0.3), blurRadius: 15)
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.white.withOpacity(0.2),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: const Text('TODAY\'S ACTIVE APPOINTMENT', style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold)),
                        ),
                        Text('Queue #${activeAppointment.queueNumber}', style: const TextStyle(color: Color(0xFF38BDF8), fontWeight: FontWeight.w900, fontSize: 16)),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(activeAppointment.doctorName, style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 4),
                    Text('${activeAppointment.departmentName} • ${activeAppointment.timeSlot}', style: const TextStyle(color: Colors.white70, fontSize: 13)),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF06B6D4),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          padding: const EdgeInsets.symmetric(vertical: 12),
                        ),
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (_) => LiveQueueScreen(appointment: activeAppointment)),
                          );
                        },
                        icon: const Icon(Icons.speed_rounded, color: Colors.white, size: 18),
                        label: const Text('Track Live Queue & AI Wait Time', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),
              const Text('Quick Hospital Actions', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
              const SizedBox(height: 12),

              // Action Grid
              Row(
                children: [
                  Expanded(
                    child: _actionCard(
                      icon: Icons.search_rounded,
                      color: const Color(0xFF2563EB),
                      title: 'Find Doctors',
                      onTap: () {
                        Navigator.push(context, MaterialPageRoute(builder: (_) => const DoctorSearchScreen()));
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _actionCard(
                      icon: Icons.history_rounded,
                      color: const Color(0xFF06B6D4),
                      title: 'My Bookings',
                      onTap: widget.onNavigateToHistory,
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 24),
              const Text('Top Hospital Specialists', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
              const SizedBox(height: 12),

              // Doctors List Preview
              isLoading
                  ? const Center(child: CircularProgressIndicator(color: Color(0xFF2563EB)))
                  : Column(
                      children: featuredDoctors.map((doc) {
                        return Container(
                          margin: const EdgeInsets.only(bottom: 12),
                          decoration: BoxDecoration(
                            color: const Color(0xFF131E3A),
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(color: Colors.white.withOpacity(0.08)),
                          ),
                          child: ListTile(
                            leading: CircleAvatar(
                              backgroundColor: const Color(0xFF2563EB).withOpacity(0.2),
                              child: const Icon(Icons.person, color: Color(0xFF38BDF8)),
                            ),
                            title: Text(doc.doctorName, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15)),
                            subtitle: Text('${doc.specialization} • ${doc.departmentName}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                            trailing: const Icon(Icons.chevron_right_rounded, color: Color(0xFF64748B)),
                            onTap: () {
                              Navigator.push(context, MaterialPageRoute(builder: (_) => DoctorProfileScreen(doctor: doc)));
                            },
                          ),
                        );
                      }).toList(),
                    ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _actionCard({required IconData icon, required Color color, required String title, required VoidCallback onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: const Color(0xFF131E3A),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.white.withOpacity(0.08)),
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: color.withOpacity(0.2),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: color, size: 22),
            ),
            const SizedBox(width: 12),
            Text(title, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
          ],
        ),
      ),
    );
  }
}
