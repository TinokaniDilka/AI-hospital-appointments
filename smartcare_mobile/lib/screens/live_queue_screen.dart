import 'package:flutter/material.dart';
import '../models/appointment.dart';

class LiveQueueScreen extends StatelessWidget {
  final AppointmentModel appointment;

  const LiveQueueScreen({super.key, required this.appointment});

  @override
  Widget build(BuildContext context) {
    // Calculated live status simulation
    int queueNumber = appointment.queueNumber;
    int currentlyServing = 2;
    int patientsAhead = (queueNumber - currentlyServing) > 0 ? (queueNumber - currentlyServing) : 0;
    String status = appointment.status;
    String aiWaitRange = patientsAhead == 0 ? "0 mins (Your Turn!)" : "${patientsAhead * 12}–${patientsAhead * 15} mins";

    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      appBar: AppBar(
        backgroundColor: const Color(0xFF131E3A),
        elevation: 0,
        title: const Text('Live Queue & AI Wait Tracker', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            // Push Notification Banner Simulation
            if (patientsAhead <= 2 && patientsAhead > 0) ...[
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(colors: [Color(0xFF0284C7), Color(0xFF06B6D4)]),
                  borderRadius: BorderRadius.circular(14),
                  boxShadow: [
                    BoxShadow(color: const Color(0xFF06B6D4).withOpacity(0.3), blurRadius: 10)
                  ],
                ),
                child: Row(
                  children: [
                    const Icon(Icons.notifications_active_rounded, color: Colors.white, size: 24),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: const [
                          Text('APPROACHING TURN ALERT', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12)),
                          SizedBox(height: 2),
                          Text('Your consultation is approaching! Only 2 patients remain ahead.', style: TextStyle(color: Colors.white, fontSize: 13)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
            ],

            // Giant Queue Number Card
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: const Color(0xFF131E3A),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF2563EB).withOpacity(0.4)),
                boxShadow: [
                  BoxShadow(color: const Color(0xFF2563EB).withOpacity(0.15), blurRadius: 20)
                ],
              ),
              child: Column(
                children: [
                  Text('YOUR QUEUE NUMBER', style: TextStyle(color: const Color(0xFF94A3B8), fontSize: 12, fontWeight: FontWeight.bold, letterSpacing: 1)),
                  const SizedBox(height: 8),
                  Text(
                    '#$queueNumber',
                    style: const TextStyle(fontSize: 54, fontWeight: FontWeight.w900, color: Color(0xFF38BDF8), height: 1),
                  ),
                  const SizedBox(height: 12),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                    decoration: BoxDecoration(
                      color: const Color(0xFF06B6D4).withOpacity(0.15),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: const Color(0xFF06B6D4).withOpacity(0.4)),
                    ),
                    child: Text(
                      status,
                      style: const TextStyle(color: Color(0xFF38BDF8), fontWeight: FontWeight.bold, fontSize: 12),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            // 3 KPI Boxes
            Row(
              children: [
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: const Color(0xFF131E3A),
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(color: Colors.white.withOpacity(0.08)),
                    ),
                    child: Column(
                      children: [
                        const Text('NOW SERVING', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 6),
                        Text('#$currentlyServing', style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF34D399))),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: const Color(0xFF131E3A),
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(color: Colors.white.withOpacity(0.08)),
                    ),
                    child: Column(
                      children: [
                        const Text('PATIENTS AHEAD', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 6),
                        Text('$patientsAhead', style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFFBBF24))),
                      ],
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 20),

            // AI Estimated Waiting Time Box
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF1E1B4B), Color(0xFF311B92)],
                ),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF8B5CF6).withOpacity(0.5)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: const [
                      Icon(Icons.auto_awesome_rounded, color: Color(0xFFA855F7), size: 20),
                      SizedBox(width: 8),
                      Text('SMARTCARE AI WAIT TIME PREDICTION', style: TextStyle(color: Color(0xFFA855F7), fontWeight: FontWeight.bold, fontSize: 12)),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    aiWaitRange,
                    style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w800, color: Colors.white),
                  ),
                  const SizedBox(height: 6),
                  Row(
                    children: const [
                      Icon(Icons.check_circle_outline_rounded, color: Color(0xFF34D399), size: 14),
                      SizedBox(width: 4),
                      Text('95.5% ML Model Confidence • Historical Patterns Analyzed', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11)),
                    ],
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            // Doctor & Room Details
            Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: const Color(0xFF131E3A),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Colors.white.withOpacity(0.08)),
              ),
              child: Column(
                children: [
                  _infoRow('Doctor', appointment.doctorName),
                  const Divider(color: Colors.white10),
                  _infoRow('Department', appointment.departmentName),
                  const Divider(color: Colors.white10),
                  _infoRow('Hospital Branch', appointment.branchName),
                  const Divider(color: Colors.white10),
                  _infoRow('Time Slot', appointment.timeSlot),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _infoRow(String label, String val) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
          Text(val, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
        ],
      ),
    );
  }
}
