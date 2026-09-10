class QueueItemModel {
  final String appointmentId;
  final String patientId;
  final String patientName;
  final int queueNumber;
  final int position;
  final String status;
  final bool isPriority;
  final double predictedWaitMinutes;
  final String formattedWaitRange;

  QueueItemModel({
    required this.appointmentId,
    required this.patientId,
    required this.patientName,
    required this.queueNumber,
    required this.position,
    required this.status,
    required this.isPriority,
    required this.predictedWaitMinutes,
    required this.formattedWaitRange,
  });

  factory QueueItemModel.fromJson(Map<String, dynamic> json) {
    return QueueItemModel(
      appointmentId: json['appointmentId'] ?? '',
      patientId: json['patientId'] ?? '',
      patientName: json['patientName'] ?? '',
      queueNumber: json['queueNumber'] ?? 1,
      position: json['position'] ?? 0,
      status: json['status'] ?? 'WAITING',
      isPriority: json['priority'] ?? false,
      predictedWaitMinutes: (json['predictedWaitMinutes'] ?? 0.0).toDouble(),
      formattedWaitRange: json['formattedWaitRange'] ?? '—',
    );
  }
}
