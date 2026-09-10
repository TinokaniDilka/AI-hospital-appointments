import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class ConfirmationScreen extends StatelessWidget {
  final Map<String, dynamic> appointmentData;

  const ConfirmationScreen({
    super.key,
    required this.appointmentData,
  });

  @override
  Widget build(BuildContext context) {
    final doctorName = appointmentData['doctorName'] ?? 'Doctor';
    final date = appointmentData['date'] ?? DateTime.now();
    final time = appointmentData['time'] ?? '10:00 AM';
    final department = appointmentData['department'] ?? 'General Medicine';

    return Scaffold(
      appBar: AppBar(
        title: const Text('Appointment Confirmed'),
        backgroundColor: const Color(0xFF667EEA),
      ),
      body: Container(
        color: Colors.grey[50],
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Container(
                  width: 100,
                  height: 100,
                  decoration: BoxDecoration(
                    color: Colors.green[100],
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.check_circle,
                    size: 60,
                    color: Colors.green,
                  ),
                ),
                const SizedBox(height: 24),
                const Text(
                  'Appointment Confirmed!',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Colors.green,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Your appointment has been successfully booked',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.grey,
                  ),
                ),
                const SizedBox(height: 32),
                Card(
                  elevation: 4,
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        _buildDetailRow(Icons.person, 'Doctor', doctorName),
                        const Divider(),
                        _buildDetailRow(Icons.calendar_today, 'Date', DateFormat('MMM dd, yyyy').format(date)),
                        const Divider(),
                        _buildDetailRow(Icons.access_time, 'Time', time),
                        const Divider(),
                        _buildDetailRow(Icons.local_hospital, 'Department', department),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                const Text(
                  'You will receive a notification when your turn is approaching',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey,
                  ),
                ),
                const SizedBox(height: 32),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.popUntil(context, (route) => route.isFirst);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF667EEA),
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    child: const Text(
                      'Back to Home',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildDetailRow(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Row(
        children: [
          Icon(icon, color: const Color(0xFF667EEA), size: 20),
          const SizedBox(width: 12),
          Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              color: Colors.grey,
            ),
          ),
          const Spacer(),
          Text(
            value,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }
}
