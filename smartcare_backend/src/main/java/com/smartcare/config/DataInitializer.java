package com.smartcare.config;

import com.smartcare.model.*;
import com.smartcare.repository.*;
import com.smartcare.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueService queueService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("MongoDB database already seeded with SmartCare records.");
            return;
        }

        log.info("Seeding SmartCare MongoDB database with comprehensive demonstration data...");

        // 1. Create Admin User
        User admin = userRepository.save(User.builder()
                .email("admin@smartcare.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Chief Administrator")
                .role("ROLE_ADMIN")
                .phoneNumber("+1-800-555-0100")
                .active(true)
                .build());

        // 2. Create Hospital, Branch, Departments
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("SmartCare Metropolitan Health System")
                .code("SCH-METRO")
                .address("100 Healthcare Blvd, Metro City")
                .contactPhone("+1-800-SMARTCARE")
                .active(true)
                .build());

        Branch branch1 = branchRepository.save(Branch.builder()
                .hospitalId(hospital.getId())
                .branchName("Central Medical Complex")
                .city("Metro City")
                .address("100 Healthcare Blvd, Suite A")
                .phone("+1-800-555-0111")
                .active(true)
                .build());

        Branch branch2 = branchRepository.save(Branch.builder()
                .hospitalId(hospital.getId())
                .branchName("Northside Care Center")
                .city("Metro City North")
                .address("450 Boulevard West")
                .phone("+1-800-555-0122")
                .active(true)
                .build());

        Department deptCardio = departmentRepository.save(Department.builder()
                .branchId(branch1.getId())
                .name("Cardiology")
                .code("CARDIO")
                .description("Heart and cardiovascular specialty clinic")
                .active(true)
                .build());

        Department deptPeds = departmentRepository.save(Department.builder()
                .branchId(branch1.getId())
                .name("Pediatrics")
                .code("PEDS")
                .description("Child and adolescent healthcare department")
                .active(true)
                .build());

        Department deptOrtho = departmentRepository.save(Department.builder()
                .branchId(branch2.getId())
                .name("Orthopedics")
                .code("ORTHO")
                .description("Bone, joint, and musculoskeletal care")
                .active(true)
                .build());

        Department deptGen = departmentRepository.save(Department.builder()
                .branchId(branch1.getId())
                .name("General Medicine")
                .code("GENMED")
                .description("Primary outpatient consultations and health assessments")
                .active(true)
                .build());

        // 3. Create Doctors
        Doctor doc1 = createDoctor("dr.jenkins@smartcare.com", "Dr. Sarah Jenkins", "Cardiology Specialist", deptCardio, branch1, "Room 204", 150.0, 20);
        Doctor doc2 = createDoctor("dr.chang@smartcare.com", "Dr. Michael Chang", "Pediatric Specialist", deptPeds, branch1, "Room 108", 120.0, 15);
        Doctor doc3 = createDoctor("dr.rodriguez@smartcare.com", "Dr. Emily Rodriguez", "Orthopedic Surgeon", deptOrtho, branch2, "Room 302", 180.0, 25);
        Doctor doc4 = createDoctor("dr.wilson@smartcare.com", "Dr. James Wilson", "General Practitioner", deptGen, branch1, "Room 102", 90.0, 15);

        // 4. Create Doctor Schedules
        createSchedule(doc1.getId());
        createSchedule(doc2.getId());
        createSchedule(doc3.getId());
        createSchedule(doc4.getId());

        // 5. Create Patients
        Patient pat1 = createPatient("john.doe@email.com", "John Doe", "1988-04-12", "Male", "O+", "+1-555-0199");
        Patient pat2 = createPatient("maria.garcia@email.com", "Maria Garcia", "1992-09-24", "Female", "A+", "+1-555-0188");
        Patient pat3 = createPatient("david.smith@email.com", "David Smith", "1975-11-03", "Male", "B-", "+1-555-0177");
        Patient pat4 = createPatient("lisa.taylor@email.com", "Lisa Taylor", "1999-01-18", "Female", "AB+", "+1-555-0166");

        // 6. Create Today's Appointments & Seed Queue
        String today = LocalDate.now().toString();

        createAppointment("APT-10001", pat1, doc1, today, "09:00 - 09:20", AppointmentStatus.COMPLETED, 1, false);
        createAppointment("APT-10002", pat2, doc1, today, "09:20 - 09:40", AppointmentStatus.IN_PROGRESS, 2, false);
        createAppointment("APT-10003", pat3, doc1, today, "09:40 - 10:00", AppointmentStatus.CONFIRMED, 3, false);
        createAppointment("APT-10004", pat4, doc1, today, "10:00 - 10:20", AppointmentStatus.WAITLISTED, 4, true);

        queueService.getOrCreateQueueForDoctor(doc1.getId(), today);

        log.info("SmartCare database successfully initialized with demo records!");
    }

    private Doctor createDoctor(String email, String name, String spec, Department dept, Branch branch, String room, double fee, int avgMins) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("doctor123"))
                .fullName(name)
                .role("ROLE_DOCTOR")
                .phoneNumber("+1-555-DOCS")
                .active(true)
                .build());

        return doctorRepository.save(Doctor.builder()
                .userId(user.getId())
                .doctorName(name)
                .email(email)
                .specialization(spec)
                .departmentId(dept.getId())
                .departmentName(dept.getName())
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .roomNumber(room)
                .consultationFee(fee)
                .avgConsultationMinutes(avgMins)
                .qualification("MD, Board Certified")
                .experienceYears("12 years")
                .active(true)
                .build());
    }

    private Patient createPatient(String email, String name, String dob, String gender, String blood, String phone) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("patient123"))
                .fullName(name)
                .role("ROLE_PATIENT")
                .phoneNumber(phone)
                .active(true)
                .build());

        return patientRepository.save(Patient.builder()
                .userId(user.getId())
                .fullName(name)
                .email(email)
                .phoneNumber(phone)
                .dob(dob)
                .gender(gender)
                .bloodGroup(blood)
                .emergencyContact(phone)
                .medicalNotes("No known allergies")
                .build());
    }

    private void createSchedule(String doctorId) {
        scheduleRepository.save(DoctorSchedule.builder()
                .doctorId(doctorId)
                .workingDays(List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))
                .startTime("09:00")
                .endTime("17:00")
                .slotDurationMinutes(20)
                .breakStartTime("13:00")
                .breakEndTime("14:00")
                .leaveDates(List.of())
                .build());
    }

    private void createAppointment(String aptId, Patient pat, Doctor doc, String date, String timeSlot, AppointmentStatus status, int qNum, boolean isPriority) {
        appointmentRepository.save(Appointment.builder()
                .appointmentId(aptId)
                .patientId(pat.getId())
                .patientName(pat.getFullName())
                .doctorId(doc.getId())
                .doctorName(doc.getDoctorName())
                .branchId(doc.getBranchId())
                .branchName(doc.getBranchName())
                .departmentId(doc.getDepartmentId())
                .departmentName(doc.getDepartmentName())
                .appointmentDate(date)
                .timeSlot(timeSlot)
                .status(status)
                .queuePosition(qNum)
                .isPriority(isPriority)
                .priorityReason(isPriority ? "Chest pain emergency evaluation" : null)
                .build());
    }
}
