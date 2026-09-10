class AppointmentModel {
  final String id;
  final String appointmentId;
  final String patientId;
  final String patientName;
  final String doctorId;
  final String doctorName;
  final String branchName;
  final String departmentName;
  final String appointmentDate;
  final String timeSlot;
  final String status;
  final int queueNumber;
  final bool isPriority;
  final String? cancellationReason;

  AppointmentModel({
    required this.id,
    required this.appointmentId,
    required this.patientId,
    required this.patientName,
    required this.doctorId,
    required this.doctorName,
    required this.branchName,
    required this.departmentName,
    required this.appointmentDate,
    required this.timeSlot,
    required this.status,
    required this.queueNumber,
    this.isPriority = false,
    this.cancellationReason,
  });

  factory AppointmentModel.fromJson(Map<String, dynamic> json) {
    return AppointmentModel(
      id: json['id'] ?? '',
      appointmentId: json['appointmentId'] ?? '',
      patientId: json['patientId'] ?? '',
      patientName: json['patientName'] ?? '',
      doctorId: json['doctorId'] ?? '',
      doctorName: json['doctorName'] ?? '',
      branchName: json['branchName'] ?? 'Central Branch',
      departmentName: json['departmentName'] ?? 'General',
      appointmentDate: json['appointmentDate'] ?? '',
      timeSlot: json['timeSlot'] ?? '',
      status: json['status'] ?? 'WAITING',
      queueNumber: json['queueNumber'] ?? 1,
      isPriority: json['priority'] ?? false,
      cancellationReason: json['cancellationReason'],
    );
  }
}
