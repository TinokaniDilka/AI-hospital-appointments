import 'package:flutter/material.dart';
import '../models/doctor.dart';
import '../services/api_service.dart';
import 'doctor_profile_screen.dart';

class DoctorSearchScreen extends StatefulWidget {
  const DoctorSearchScreen({super.key});

  @override
  State<DoctorSearchScreen> createState() => _DoctorSearchScreenState();
}

class _DoctorSearchScreenState extends State<DoctorSearchScreen> {
  List<DoctorModel> doctors = [];
  List<DoctorModel> filteredDoctors = [];
  String selectedDept = 'All';
  String selectedBranch = 'All';
  DateTime? selectedDate;
  String selectedAvailability = 'All';
  final _searchCtrl = TextEditingController();
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadDoctors();
  }

  void _loadDoctors() async {
    final list = await ApiService.getDoctors();
    setState(() {
      doctors = list;
      filteredDoctors = list;
      isLoading = false;
    });
  }

  void _filter() {
    final query = _searchCtrl.text.toLowerCase();
    setState(() {
      filteredDoctors = doctors.where((doc) {
        final matchesQuery = doc.doctorName.toLowerCase().contains(query) ||
            doc.specialization.toLowerCase().contains(query);
        final matchesDept = selectedDept == 'All' || doc.departmentName == selectedDept;
        final matchesBranch = selectedBranch == 'All' || doc.branchName == selectedBranch;
        final matchesAvailability = selectedAvailability == 'All' ||
            (selectedAvailability == 'Available Today' && doc.active) ||
            (selectedAvailability == 'Available Tomorrow' && doc.active);
        return matchesQuery && matchesDept && matchesBranch && matchesAvailability;
      }).toList();
    });
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 30)),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.dark(
              primary: Color(0xFF2563EB),
              onPrimary: Colors.white,
              surface: Color(0xFF131E3A),
              onSurface: Colors.white,
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null) {
      setState(() {
        selectedDate = picked;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B1329),
      appBar: AppBar(
        backgroundColor: const Color(0xFF131E3A),
        title: const Text('Find Doctors & Specialists', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      ),
      body: Column(
        children: [
          // Search & Filters Header
          Container(
            padding: const EdgeInsets.all(16),
            color: const Color(0xFF131E3A),
            child: Column(
              children: [
                TextField(
                  controller: _searchCtrl,
                  onChanged: (_) => _filter(),
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    hintText: 'Search doctor name or specialty...',
                    hintStyle: const TextStyle(color: Color(0xFF64748B)),
                    prefixIcon: const Icon(Icons.search, color: Color(0xFF64748B)),
                    filled: true,
                    fillColor: const Color(0xFF0B1329),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: ['All', 'Cardiology', 'Pediatrics', 'Orthopedics', 'General Medicine'].map((dept) {
                      final isSel = selectedDept == dept;
                      return Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: ChoiceChip(
                          label: Text(dept),
                          selected: isSel,
                          selectedColor: const Color(0xFF2563EB),
                          backgroundColor: const Color(0xFF0B1329),
                          labelStyle: TextStyle(
                            color: isSel ? Colors.white : const Color(0xFF94A3B8),
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                          onSelected: (val) {
                            setState(() {
                              selectedDept = dept;
                              _filter();
                            });
                          },
                        ),
                      );
                    }).toList(),
                  ),
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: ['All', 'Main Hospital', 'Downtown Branch', 'West Wing'].map((branch) {
                      final isSel = selectedBranch == branch;
                      return Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: ChoiceChip(
                          label: Text(branch),
                          selected: isSel,
                          selectedColor: const Color(0xFF06B6D4),
                          backgroundColor: const Color(0xFF0B1329),
                          labelStyle: TextStyle(
                            color: isSel ? Colors.white : const Color(0xFF94A3B8),
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                          onSelected: (val) {
                            setState(() {
                              selectedBranch = branch;
                              _filter();
                            });
                          },
                        ),
                      );
                    }).toList(),
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: InkWell(
                        onTap: _selectDate,
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                          decoration: BoxDecoration(
                            color: const Color(0xFF0B1329),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: Colors.white.withOpacity(0.08)),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.calendar_today, color: Color(0xFF64748B), size: 20),
                              const SizedBox(width: 12),
                              Text(
                                selectedDate != null
                                    ? '${selectedDate!.day}/${selectedDate!.month}/${selectedDate!.year}'
                                    : 'Select Date',
                                style: const TextStyle(color: Colors.white, fontSize: 14),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: selectedAvailability,
                        dropdownColor: const Color(0xFF131E3A),
                        decoration: InputDecoration(
                          filled: true,
                          fillColor: const Color(0xFF0B1329),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                            borderSide: BorderSide.none,
                          ),
                          contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        ),
                        style: const TextStyle(color: Colors.white, fontSize: 14),
                        items: ['All', 'Available Today', 'Available Tomorrow'].map((availability) {
                          return DropdownMenuItem(
                            value: availability,
                            child: Text(availability),
                          );
                        }).toList(),
                        onChanged: (value) {
                          setState(() {
                            selectedAvailability = value!;
                            _filter();
                          });
                        },
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Doctors List
          Expanded(
            child: isLoading
                ? const Center(child: CircularProgressIndicator(color: Color(0xFF2563EB)))
                : ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: filteredDoctors.length,
                    itemBuilder: (context, index) {
                      final doc = filteredDoctors[index];
                      return Container(
                        margin: const EdgeInsets.only(bottom: 14),
                        decoration: BoxDecoration(
                          color: const Color(0xFF131E3A),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: Colors.white.withOpacity(0.08)),
                        ),
                        child: ListTile(
                          contentPadding: const EdgeInsets.all(14),
                          leading: CircleAvatar(
                            radius: 28,
                            backgroundColor: const Color(0xFF2563EB).withOpacity(0.2),
                            child: const Icon(Icons.person_rounded, color: Color(0xFF38BDF8), size: 30),
                          ),
                          title: Text(doc.doctorName, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const SizedBox(height: 4),
                              Text(doc.specialization, style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 13, fontWeight: FontWeight.w600)),
                              const SizedBox(height: 4),
                              Text('${doc.departmentName} • ${doc.branchName}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                            ],
                          ),
                          trailing: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text('\$${doc.consultationFee.toInt()}', style: const TextStyle(color: Color(0xFF34D399), fontWeight: FontWeight.bold, fontSize: 16)),
                              const SizedBox(height: 4),
                              const Icon(Icons.arrow_forward_ios_rounded, color: Color(0xFF64748B), size: 14),
                            ],
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(builder: (_) => DoctorProfileScreen(doctor: doc)),
                            );
                          },
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
