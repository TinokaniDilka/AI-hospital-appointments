import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class ProfileScreen extends StatefulWidget {
  final String patientName;

  const ProfileScreen({super.key, required this.patientName});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  Map<String, dynamic>? patientData;
  bool isLoading = true;
  String? error;

  @override
  void initState() {
    super.initState();
    _fetchPatientProfile();
  }

  Future<void> _fetchPatientProfile() async {
    setState(() {
      isLoading = true;
      error = null;
    });

    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/v1/patients/me'),
        headers: {
          'Content-Type': 'application/json',
          // Add authorization header if you have token storage
        },
      );

      if (response.statusCode == 200) {
        setState(() {
          patientData = json.decode(response.body);
          isLoading = false;
        });
      } else {
        setState(() {
          error = 'Failed to load profile';
          isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        error = 'Error: $e';
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Profile'),
        backgroundColor: const Color(0xFF667EEA),
      ),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(error!),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _fetchPatientProfile,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : patientData != null
                  ? SingleChildScrollView(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Center(
                            child: CircleAvatar(
                              radius: 50,
                              backgroundColor: const Color(0xFF667EEA),
                              child: Text(
                                patientData!['fullName']?.substring(0, 2).toUpperCase() ?? widget.patientName.substring(0, 2).toUpperCase(),
                                style: const TextStyle(
                                  fontSize: 24,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 24),
                          _buildInfoCard('Full Name', patientData!['fullName'] ?? 'N/A'),
                          _buildInfoCard('Date of Birth', patientData!['dob'] ?? 'N/A'),
                          _buildInfoCard('Gender', patientData!['gender'] ?? 'N/A'),
                          _buildInfoCard('Blood Group', patientData!['bloodGroup'] ?? 'N/A'),
                          _buildInfoCard('Emergency Contact', patientData!['emergencyContact'] ?? 'N/A'),
                          const SizedBox(height: 16),
                          const Text(
                            'Medical Notes',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: Colors.grey[100],
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              patientData!['medicalNotes'] ?? 'No medical notes available',
                              style: const TextStyle(fontSize: 14),
                            ),
                          ),
                        ],
                      ),
                    )
                  : const Center(child: Text('No profile data available')),
    );
  }

  Widget _buildInfoCard(String label, String value) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: const TextStyle(
                fontSize: 14,
                color: Colors.grey,
              ),
            ),
            Text(
              value,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
