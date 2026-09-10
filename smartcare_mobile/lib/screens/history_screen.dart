import 'package:flutter/material.dart';
import '../models/appointment.dart';

class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  List<AppointmentModel> appointments = [
    AppointmentModel(
      id: 'apt_1',
      appointmentId: 'APT-10004',
      patientId: 'pat_1',
      patientName: 'John Doe',
      doctorId: 'doc_1',
      doctorName: 'Dr. Sarah Jenkins',
      branchName: 'Central Medical Complex',
      departmentName: 'Cardiology',
      appointmentDate: DateTime.now().toString().split(' ')[0],
      timeSlot: '10:00 - 10:20',
      status: 'WAITING',
      queueNumber: 4,
    ),
    AppointmentModel(
      id: 'apt_2',
      appointmentId: 'APT-09882',
      patientId: 'pat_1',
      patientName: 'John Doe',
      doctorId: 'doc_4',
      doctorName: 'Dr. James Wilson',
      branchName: 'Central Medical Complex',
      departmentName: 'General Medicine',
      appointmentDate: '2026-08-28',
      timeSlot: '11:00 - 11:20',
      status: 'COMPLETED',
      queueNumber: 2,
    ),
    AppointmentModel(
      id: 'apt_3',
      appointmentId: 'APT-09210',
      patientId: 'pat_1',
      patientName: 'John Doe',
      doctorId: 'doc_2',
      doctorName: 'Dr. Michael Chang',
      branchName: 'Central Medical Complex',
      departmentName: 'Pediatrics',
      appointmentDate: '2026-08-15',
      timeSlot: '02:00 - 02:20',
      status: 'CANCELLED',
      queueNumber: 5,
      cancellationReason: 'Patient rescheduled due to personal conflict',
    ),
  ];

  void _cancelAppointment(String aptId) {
    setState(() {
      appointments = appointments.map((a) {
        if (a.appointmentId == aptId) {
          return AppointmentModel(
            id: a.id,
            appointmentId: a.appointmentId,
            patientId: a.patientId,
            patientName: a.patientName,
            doctorId: a.doctorId,
            doctorName: a.doctorName,
            branchName: a.branchName,
            departmentName: a.departmentName,
            appointmentDate: a.appointmentDate,
            timeSlot: a.timeSlot,
            status: 'CANCELLED',
            queueNumber: a.queueNumber,
            cancellationReason: 'Cancelled by patient',
          );
        }
        return a;
      }).toList();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      appBar: AppBar(
        backgroundColor: const Color(0xFF131E3A),
        title: const Text('My Appointment History', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: appointments.length,
        itemBuilder: (context, index) {
          final apt = appointments[index];
          final isWaiting = apt.status == 'WAITING';

          return Container(
            margin: const EdgeInsets.only(bottom: 14),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFF131E3A),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.white.withOpacity(0.08)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(apt.appointmentId, style: const TextStyle(color: Color(0xFF94A3B8), fontWeight: FontWeight.bold, fontSize: 12)),
                    _statusBadge(apt.status),
                  ],
                ),
                const SizedBox(height: 8),
                Text(apt.doctorName, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text('${apt.departmentName} • ${apt.branchName}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                const SizedBox(height: 8),
                Row(
                  children: [
                    const Icon(Icons.calendar_today_rounded, color: Color(0xFF38BDF8), size: 14),
                    const SizedBox(width: 6),
                    Text('${apt.appointmentDate} (${apt.timeSlot})', style: const TextStyle(color: Colors.white70, fontSize: 12, fontWeight: FontWeight.w600)),
                    const Spacer(),
                    Text('Queue #${apt.queueNumber}', style: const TextStyle(color: Color(0xFF38BDF8), fontWeight: FontWeight.bold, fontSize: 13)),
                  ],
                ),

                if (isWaiting) ...[
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton(
                          style: OutlinedButton.styleFrom(
                            side: const BorderSide(color: Color(0xFFEF4444)),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                          ),
                          onPressed: () => _cancelAppointment(apt.appointmentId),
                          child: const Text('Cancel Appointment', style: TextStyle(color: Color(0xFFEF4444), fontSize: 12)),
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _statusBadge(String status) {
    Color bg = const Color(0xFF2563EB);
    Color txt = Colors.white;
    if (status == 'COMPLETED') {
      bg = const Color(0xFF10B981).withOpacity(0.2);
      txt = const Color(0xFF34D399);
    } else if (status == 'CANCELLED') {
      bg = const Color(0xFFEF4444).withOpacity(0.2);
      txt = const Color(0xFFF87171);
    } else if (status == 'WAITING') {
      bg = const Color(0xFFF59E0B).withOpacity(0.2);
      txt = const Color(0xFFFBBF24);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(8)),
      child: Text(status, style: TextStyle(color: txt, fontWeight: FontWeight.bold, fontSize: 10)),
    );
  }
}
