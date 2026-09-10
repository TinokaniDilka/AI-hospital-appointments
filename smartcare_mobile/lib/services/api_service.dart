import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/doctor.dart';
import '../models/appointment.dart';
import '../models/queue_item.dart';
import '../models/notification_item.dart';

class ApiService {
  static const String baseUrl = 'http://localhost:8080/api/v1';

  // Demo Fallback Data for offline / initial state
  static final List<DoctorModel> mockDoctors = [
    DoctorModel(
      id: 'doc_1',
      doctorName: 'Dr. Sarah Jenkins',
      specialization: 'Cardiology Specialist',
      departmentId: 'dept_1',
      departmentName: 'Cardiology',
      branchId: 'branch_1',
      branchName: 'Central Medical Complex',
      roomNumber: 'Room 204',
      consultationFee: 150.0,
      avgConsultationMinutes: 20,
      qualification: 'MD, FACC',
      experienceYears: '14 years',
    ),
    DoctorModel(
      id: 'doc_2',
      doctorName: 'Dr. Michael Chang',
      specialization: 'Pediatric Specialist',
      departmentId: 'dept_2',
      departmentName: 'Pediatrics',
      branchId: 'branch_1',
      branchName: 'Central Medical Complex',
      roomNumber: 'Room 108',
      consultationFee: 120.0,
      avgConsultationMinutes: 15,
      qualification: 'MD, FAAP',
      experienceYears: '10 years',
    ),
    DoctorModel(
      id: 'doc_3',
      doctorName: 'Dr. Emily Rodriguez',
      specialization: 'Orthopedic Surgeon',
      departmentId: 'dept_3',
      departmentName: 'Orthopedics',
      branchId: 'branch_2',
      branchName: 'Northside Care Center',
      roomNumber: 'Room 302',
      consultationFee: 180.0,
      avgConsultationMinutes: 25,
      qualification: 'MD, FAAOS',
      experienceYears: '12 years',
    ),
    DoctorModel(
      id: 'doc_4',
      doctorName: 'Dr. James Wilson',
      specialization: 'General Practitioner',
      departmentId: 'dept_4',
      departmentName: 'General Medicine',
      branchId: 'branch_1',
      branchName: 'Central Medical Complex',
      roomNumber: 'Room 102',
      consultationFee: 90.0,
      avgConsultationMinutes: 15,
      qualification: 'MBBS, MRCGP',
      experienceYears: '8 years',
    ),
  ];

  static Future<List<DoctorModel>> getDoctors() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/doctors')).timeout(const Duration(seconds: 3));
      if (response.statusCode == 200) {
        final List list = json.decode(response.body);
        return list.map((e) => DoctorModel.fromJson(e)).toList();
      }
    } catch (_) {}
    return mockDoctors;
  }

  static Future<List<String>> getAvailableSlots(String doctorId, String date) async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/appointments/slots?doctorId=$doctorId&date=$date')).timeout(const Duration(seconds: 3));
      if (response.statusCode == 200) {
        final List list = json.decode(response.body);
        return list.cast<String>();
      }
    } catch (_) {}

    return [
      '09:40 - 10:00',
      '10:00 - 10:20',
      '10:20 - 10:40',
      '11:00 - 11:20',
      '11:40 - 12:00',
      '02:00 - 02:20',
      '02:40 - 03:00'
    ];
  }

  static Future<AppointmentModel?> bookAppointment({
    required String patientId,
    required String patientName,
    required String doctorId,
    required String date,
    required String timeSlot,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/appointments/book'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'patientId': patientId,
          'patientName': patientName,
          'doctorId': doctorId,
          'date': date,
          'timeSlot': timeSlot,
        }),
      ).timeout(const Duration(seconds: 4));

      if (response.statusCode == 200) {
        return AppointmentModel.fromJson(json.decode(response.body));
      }
    } catch (_) {}

    // Fallback Mock Booking
    final doc = mockDoctors.firstWhere((d) => d.id == doctorId, orElse: () => mockDoctors.first);
    return AppointmentModel(
      id: 'apt_mock_${DateTime.now().millisecondsSinceEpoch}',
      appointmentId: 'APT-${10000 + (DateTime.now().millisecondsSinceEpoch % 89999)}',
      patientId: patientId,
      patientName: patientName,
      doctorId: doc.id,
      doctorName: doc.doctorName,
      branchName: doc.branchName,
      departmentName: doc.departmentName,
      appointmentDate: date,
      timeSlot: timeSlot,
      status: 'WAITING',
      queueNumber: 4,
    );
  }
}
