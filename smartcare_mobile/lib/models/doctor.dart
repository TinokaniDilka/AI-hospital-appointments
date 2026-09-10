class DoctorModel {
  final String id;
  final String doctorName;
  final String specialization;
  final String departmentId;
  final String departmentName;
  final String branchId;
  final String branchName;
  final String roomNumber;
  final double consultationFee;
  final int avgConsultationMinutes;
  final String qualification;
  final String experienceYears;

  DoctorModel({
    required this.id,
    required this.doctorName,
    required this.specialization,
    required this.departmentId,
    required this.departmentName,
    required this.branchId,
    required this.branchName,
    required this.roomNumber,
    required this.consultationFee,
    required this.avgConsultationMinutes,
    required this.qualification,
    required this.experienceYears,
  });

  factory DoctorModel.fromJson(Map<String, dynamic> json) {
    return DoctorModel(
      id: json['id'] ?? '',
      doctorName: json['doctorName'] ?? '',
      specialization: json['specialization'] ?? '',
      departmentId: json['departmentId'] ?? '',
      departmentName: json['departmentName'] ?? '',
      branchId: json['branchId'] ?? '',
      branchName: json['branchName'] ?? '',
      roomNumber: json['roomNumber'] ?? '',
      consultationFee: (json['consultationFee'] ?? 0.0).toDouble(),
      avgConsultationMinutes: json['avgConsultationMinutes'] ?? 15,
      qualification: json['qualification'] ?? 'MD',
      experienceYears: json['experienceYears'] ?? '10 yrs',
    );
  }
}
