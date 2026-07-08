package com.nv.task1.controller.api;

import com.nv.task1.dto.EmployeeDTO;
import com.nv.task1.entity.*;
import com.nv.task1.repository.EmployeeRepository;
import com.nv.task1.repository.UserRepository;
import com.nv.task1.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final TaskService taskService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;
    private final RegularizationService regularizationService;
    private final AttendanceReportService attendanceReportService;
    private final WFHService wfhService;

    // Dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(Map.of(
                "totalEmployees", employeeRepository.count(),
                "itEmployees", employeeRepository.countByDepartment("IT"),
                "hrEmployees", employeeRepository.countByDepartment("HR")
        ));
    }

    // All employees
    @GetMapping("/employees")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // Add employee
    @PostMapping("/employees")
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeDTO dto) {
        employeeService.saveEmployee(dto);
        return ResponseEntity.ok(Map.of("message", "Employee added successfully"));
    }

    // Get employee by id
    @GetMapping("/employees/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    // Update employee
    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,
                                            @RequestBody Employee employee) {
        employee.setId(id);
        employeeService.updateEmployee(employee);
        return ResponseEntity.ok(Map.of("message", "Updated successfully"));
    }

    // Delete employee
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    // Search employees
    @GetMapping("/employees/search")
    public List<Employee> search(@RequestParam String name) {
        return employeeService.searchByName(name);
    }

    // All leave requests
    @GetMapping("/leaves")
    public List<LeaveRequest> getAllLeaves() {
        return leaveService.getAllLeaves();
    }

    // Approve leave
    @PutMapping("/leaves/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) {
        leaveService.approveLeave(id);
        return ResponseEntity.ok(Map.of("message", "Approved"));
    }

    // Reject leave
    @PutMapping("/leaves/{id}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long id) {
        leaveService.rejectLeave(id);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }

    // All WFH requests, company-wide (includes a manager's own requests,
    // which have no manager above them to approve otherwise)
    @GetMapping("/wfh")
    public List<WFHRequest> getAllWFH() {
        return wfhService.getAll();
    }

    @PutMapping("/wfh/{id}/approve")
    public ResponseEntity<?> approveWFH(@PathVariable Long id) {
        wfhService.approve(id);
        return ResponseEntity.ok(Map.of("message", "Approved"));
    }

    @PutMapping("/wfh/{id}/reject")
    public ResponseEntity<?> rejectWFH(@PathVariable Long id) {
        wfhService.reject(id);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }

    // All attendance records, company-wide
    @GetMapping("/attendance")
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAll();
    }

    // Manually run the absent / missed-punchout detection for a given date
    // (defaults to yesterday). Runs automatically every night too, but this
    // lets it be triggered on demand - handy for testing/demoing.
    @PostMapping("/attendance/close-day")
    public ResponseEntity<?> closeAttendanceDay(@RequestParam(required = false) String date) {
        LocalDate target = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now().minusDays(1);
        attendanceService.closeAttendanceForDate(target);
        return ResponseEntity.ok(Map.of("message", "Attendance closed for " + target, "date", target.toString()));
    }

    // Download company-wide attendance as an Excel file. Optional ?from=&to= (yyyy-MM-dd) to filter a date range.
    @GetMapping("/attendance/export/excel")
    public ResponseEntity<byte[]> exportAttendanceExcel(@RequestParam(required = false) String from,
                                                         @RequestParam(required = false) String to) throws IOException {
        byte[] file = attendanceReportService.exportToExcel(filteredAttendance(from, to));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance-report.xlsx\"");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // Download company-wide attendance as a PDF. Optional ?from=&to= (yyyy-MM-dd) to filter a date range.
    @GetMapping("/attendance/export/pdf")
    public ResponseEntity<byte[]> exportAttendancePdf(@RequestParam(required = false) String from,
                                                       @RequestParam(required = false) String to) throws IOException {
        byte[] file = attendanceReportService.exportToPdf(filteredAttendance(from, to));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance-report.pdf\"");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    private List<Attendance> filteredAttendance(String from, String to) {
        List<Attendance> all = attendanceService.getAll();
        LocalDate fromDate = (from != null && !from.isBlank()) ? LocalDate.parse(from) : LocalDate.MIN;
        LocalDate toDate = (to != null && !to.isBlank()) ? LocalDate.parse(to) : LocalDate.MAX;
        return all.stream()
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(fromDate) && !a.getDate().isAfter(toDate))
                .sorted(Comparator.comparing(Attendance::getDate)
                        .thenComparing(a -> a.getEmployee() != null && a.getEmployee().getName() != null ? a.getEmployee().getName() : ""))
                .collect(Collectors.toList());
    }

    // All regularization requests, company-wide (includes ones still pending
    // with a manager, and a manager's own requests which have no manager to approve them)
    @GetMapping("/regularization")
    public List<Regularization> getAllRegularizations() {
        return regularizationService.getAll();
    }

    @PutMapping("/regularization/{id}/approve")
    public ResponseEntity<?> approveRegularization(@PathVariable Long id) {
        regularizationService.approve(id);
        return ResponseEntity.ok(Map.of("message", "Approved"));
    }

    @PutMapping("/regularization/{id}/reject")
    public ResponseEntity<?> rejectRegularization(@PathVariable Long id) {
        regularizationService.reject(id);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }

    // All managers
    @GetMapping("/managers")
    public List<User> getAllManagers() {
        return userRepository.findByRole(Role.ROLE_MANAGER);
    }

    // Assign manager
    @PostMapping("/assign-manager")
    public ResponseEntity<?> assignManager(@RequestBody Map<String, Long> body) {
        employeeService.assignManager(body.get("employeeId"), body.get("managerId"));
        return ResponseEntity.ok(Map.of("message", "Manager assigned"));
    }

    // Assign task
    @PostMapping("/assign-task")
    public ResponseEntity<?> assignTask(@RequestBody Map<String, Object> body) {
        Task task = new Task();
        task.setTitle((String) body.get("title"));
        task.setDescription((String) body.get("description"));
        task.setDueDate(java.time.LocalDate.parse((String) body.get("dueDate")));
        Long assignedToId = Long.valueOf(body.get("assignedToId").toString());
        taskService.assignTask(assignedToId, 0L, task);
        return ResponseEntity.ok(Map.of("message", "Task assigned"));
    }

    // Teams view
    @GetMapping("/teams")
    public ResponseEntity<?> getTeams() {
        List<User> managers = userRepository.findByRole(Role.ROLE_MANAGER);
        List<Map<String, Object>> result = managers.stream()
                .filter(m -> m.getEmployee() != null)
                .map(m -> {
                    Employee mgrEmp = m.getEmployee();
                    List<Employee> team = employeeService.getTeamMembers(mgrEmp.getId());
                    return Map.<String, Object>of(
                            "manager", mgrEmp,
                            "team", team
                    );
                }).toList();
        return ResponseEntity.ok(result);
    }
}