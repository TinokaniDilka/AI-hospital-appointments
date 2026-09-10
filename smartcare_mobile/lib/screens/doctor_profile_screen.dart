import 'package:flutter/material.dart';
import '../models/doctor.dart';
import '../models/appointment.dart';
import '../services/api_service.dart';
import 'live_queue_screen.dart';

class DoctorProfileScreen extends StatefulWidget {
  final DoctorModel doctor;

  const DoctorProfileScreen({super.key, required this.doctor});

  @override
  State<DoctorProfileScreen> createState() => _DoctorProfileScreenState();
}

class _DoctorProfileScreenState extends State<DoctorProfileScreen> {
  String selectedDate = DateTime.now().toString().split(' ')[0];
  String? selectedSlot;
  List<String> availableSlots = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadSlots();
  }

  void _loadSlots() async {
    setState(() => isLoading = true);
    final slots = await ApiService.getAvailableSlots(widget.doctor.id, selectedDate);
    setState(() {
      availableSlots = slots;
      if (slots.isNotEmpty) selectedSlot = slots.first;
      isLoading = false;
    });
  }

  void _bookAppointment() async {
    if (selectedSlot == null) return;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => const Center(child: CircularProgressIndicator(color: Color(0xFF2563EB))),
    );

    final apt = await ApiService.bookAppointment(
      patientId: 'pat_1',
      patientName: 'John Doe',
      doctorId: widget.doctor.id,
      date: selectedDate,
      timeSlot: selectedSlot!,
    );

    if (mounted) {
      Navigator.pop(context); // close loader
      if (apt != null) {
        showDialog(
          context: context,
          builder: (ctx) => AlertDialog(
            backgroundColor: const Color(0xFF131E3A),
            title: Row(
              children: const [
                Icon(Icons.check_circle_rounded, color: Color(0xFF34D399)),
                SizedBox(width: 8),
                Text('Booking Confirmed!', style: TextStyle(color: Colors.white)),
              ],
            ),
            content: Text(
              'Your appointment (${apt.appointmentId}) is confirmed for $selectedDate at $selectedSlot.\n\nQueue Number: #${apt.queueNumber}',
              style: const TextStyle(color: Color(0xFF94A3B8)),
            ),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(ctx);
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(builder: (_) => LiveQueueScreen(appointment: apt)),
                  );
                },
                child: const Text('View Live Queue Tracker', style: TextStyle(color: Color(0xFF38BDF8), fontWeight: FontWeight.bold)),
              )
            ],
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final doc = widget.doctor;

    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      appBar: AppBar(
        backgroundColor: const Color(0xFF131E3A),
        title: Text(doc.doctorName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Doctor Card Header
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF131E3A),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: Colors.white.withOpacity(0.08)),
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 36,
                    backgroundColor: const Color(0xFF2563EB).withOpacity(0.2),
                    child: const Icon(Icons.person_rounded, color: Color(0xFF38BDF8), size: 40),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(doc.doctorName, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white)),
                        const SizedBox(height: 4),
                        Text(doc.specialization, style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 13, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 4),
                        Text('${doc.qualification} • ${doc.experienceYears}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                        const SizedBox(height: 6),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: const Color(0xFF34D399).withOpacity(0.15),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text('Consultation Fee: \$${doc.consultationFee.toInt()}', style: const TextStyle(color: Color(0xFF34D399), fontWeight: FontWeight.bold, fontSize: 12)),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),
            const Text('Select Appointment Date', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
            const SizedBox(height: 12),

            // Date Picker Row
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: List.generate(5, (index) {
                  final date = DateTime.now().add(Duration(days: index));
                  final dateStr = date.toString().split(' ')[0];
                  final isSel = selectedDate == dateStr;

                  return GestureDetector(
                    onTap: () {
                      setState(() {
                        selectedDate = dateStr;
                        _loadSlots();
                      });
                    },
                    child: Container(
                      margin: const EdgeInsets.only(right: 10),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      decoration: BoxDecoration(
                        color: isSel ? const Color(0xFF2563EB) : const Color(0xFF131E3A),
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(color: isSel ? const Color(0xFF2563EB) : Colors.white.withOpacity(0.08)),
                      ),
                      child: Column(
                        children: [
                          Text('Day ${index + 1}', style: TextStyle(color: isSel ? Colors.white70 : const Color(0xFF94A3B8), fontSize: 11)),
                          const SizedBox(height: 4),
                          Text(dateStr.substring(5), style: TextStyle(color: isSel ? Colors.white : Colors.white70, fontWeight: FontWeight.bold, fontSize: 14)),
                        ],
                      ),
                    ),
                  );
                }),
              ),
            ),

            const SizedBox(height: 24),
            const Text('Available Time Slots', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
            const SizedBox(height: 12),

            isLoading
                ? const Center(child: CircularProgressIndicator(color: Color(0xFF2563EB)))
                : availableSlots.isEmpty
                    ? Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: const Color(0xFF131E3A),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Text('No available time slots on this date.', style: TextStyle(color: Color(0xFF94A3B8))),
                      )
                    : Wrap(
                        spacing: 10,
                        runSpacing: 10,
                        children: availableSlots.map((slot) {
                          final isSel = selectedSlot == slot;
                          return GestureDetector(
                            onTap: () => setState(() => selectedSlot = slot),
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                              decoration: BoxDecoration(
                                color: isSel ? const Color(0xFF06B6D4) : const Color(0xFF131E3A),
                                borderRadius: BorderRadius.circular(10),
                                border: Border.all(color: isSel ? const Color(0xFF06B6D4) : Colors.white.withOpacity(0.08)),
                              ),
                              child: Text(
                                slot,
                                style: TextStyle(
                                  color: isSel ? Colors.white : const Color(0xFF94A3B8),
                                  fontWeight: FontWeight.bold,
                                  fontSize: 13,
                                ),
                              ),
                            ),
                          );
                        }).toList(),
                      ),

            const SizedBox(height: 40),

            SizedBox(
              width: double.infinity,
              height: 52,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF2563EB),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                  elevation: 4,
                ),
                onPressed: selectedSlot != null ? _bookAppointment : null,
                child: const Text('Confirm & Book Appointment', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
