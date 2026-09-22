import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/doctor.dart';
import '../models/appointment.dart';
import '../models/queue_item.dart';
import '../models/notification_item.dart';

class ApiService {
  static const String baseUrl = String.fromEnvironment('API_URL', defaultValue: 'http://localhost:8080/api/v1');

  static Future<List<DoctorModel>> getDoctors() async {
    final response = await http.get(Uri.parse('$baseUrl/doctors')).timeout(const Duration(seconds: 10));
    if (response.statusCode == 200) {
      final List list = json.decode(response.body);
      return list.map((e) => DoctorModel.fromJson(e)).toList();
    }
    throw Exception('Failed to load doctors');
  }

  static Future<List<String>> getAvailableSlots(String doctorId, String date) async {
    final response = await http.get(Uri.parse('$baseUrl/appointments/slots?doctorId=$doctorId&date=$date')).timeout(const Duration(seconds: 10));
    if (response.statusCode == 200) {
      final List list = json.decode(response.body);
      return list.cast<String>();
    }
    throw Exception('Failed to load available slots');
  }

  static Future<AppointmentModel> bookAppointment({
    required String patientId,
    required String patientName,
    required String doctorId,
    required String date,
    required String timeSlot,
  }) async {
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
    ).timeout(const Duration(seconds: 10));

    if (response.statusCode == 200) {
      return AppointmentModel.fromJson(json.decode(response.body));
    }
    throw Exception('Failed to book appointment');
  }
}
