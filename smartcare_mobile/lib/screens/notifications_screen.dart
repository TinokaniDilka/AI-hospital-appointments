import 'package:flutter/material.dart';

class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final notifs = [
      {
        'title': 'Consultation Approaching!',
        'body': 'Your Queue #4 with Dr. Sarah Jenkins is approaching. Only 2 patients ahead in room 204.',
        'time': '10 mins ago',
        'type': 'QUEUE_APPROACHING'
      },
      {
        'title': 'Appointment Confirmed',
        'body': 'Your appointment APT-10004 for Cardiology is confirmed for today at 10:00 AM.',
        'time': '2 hours ago',
        'type': 'APPOINTMENT_BOOKED'
      },
      {
        'title': 'SmartCare AI Wait Time Update',
        'body': 'AI estimated wait time recalculated: approximately 25–30 minutes based on current doctor consultation pace.',
        'time': '3 hours ago',
        'type': 'AI_UPDATE'
      },
    ];

    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      appBar: AppBar(
        backgroundColor: const Color(0xFF131E3A),
        title: const Text('Notifications & Alerts', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: notifs.length,
        itemBuilder: (context, index) {
          final n = notifs[index];
          return Container(
            margin: const EdgeInsets.only(bottom: 12),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFF131E3A),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.white.withOpacity(0.08)),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: const Color(0xFF2563EB).withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(Icons.notifications_active_rounded, color: Color(0xFF38BDF8), size: 20),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(n['title']!, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15)),
                      const SizedBox(height: 4),
                      Text(n['body']!, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
                      const SizedBox(height: 6),
                      Text(n['time']!, style: const TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
