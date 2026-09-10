class NotificationItemModel {
  final String id;
  final String title;
  final String body;
  final String type;
  final bool isRead;
  final String sentAt;

  NotificationItemModel({
    required this.id,
    required this.title,
    required this.body,
    required this.type,
    required this.isRead,
    required this.sentAt,
  });

  factory NotificationItemModel.fromJson(Map<String, dynamic> json) {
    return NotificationItemModel(
      id: json['id'] ?? '',
      title: json['title'] ?? 'Notification',
      body: json['body'] ?? '',
      type: json['type'] ?? 'GENERAL',
      isRead: json['read'] ?? false,
      sentAt: json['sentAt'] ?? '',
    );
  }
}
