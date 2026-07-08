package com.nv.task1.controller.api;

import com.nv.task1.entity.*;
import com.nv.task1.repository.UserRepository;
import com.nv.task1.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerRestController {

    private final UserRepository userRepository;
    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final TaskService taskService;
    private final WFHService wfhService;
    private final DailyUpdateService dailyUpdateService;
    private final RatingService ratingService;
    private final AttendanceService attendanceService;
    private final RegularizationService regularizationService;

    private Employee getManager(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow().getEmployee();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication auth) {
        Employee manager = getManager(auth);
        return ResponseEntity.ok(Map.of("manager", manager));
    }

    @GetMapping("/team")
    public ResponseEntity<?> team(Authentication auth) {
        Employee manager = getManager(auth);
        List<Employee> team = employeeService.getTeamMembers(manager.getId());
        return ResponseEntity.ok(Map.of("manager", manager, "team", team));
    }

    @PostMapping("/assign-task")
    public ResponseEntity<?> assignTask(@RequestBody Map<String, Object> body,
                                        Authentication auth) {
        Employee manager = getManager(auth);
        Task task = new Task();
        task.setTitle((String) body.get("title"));
        task.setDescription((String) body.get("description"));
        if (body.get("dueDate") != null)
            task.setDueDate(java.time.LocalDate.parse((String) body.get("dueDate")));
        Long assignedToId = Long.valueOf(body.get("assignedToId").toString());
        taskService.assignTask(assignedToId, manager.getId(), task);
        return ResponseEntity.ok(Map.of("message", "Task assigned"));
    }

    @GetMapping("/leaves")
    public List<LeaveRequest> leaves(Authentication auth) {
        return leaveService.getLeavesByManager(getManager(auth));
    }

    @PutMapping("/leaves/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) {
        leaveService.approveLeave(id);
        return ResponseEntity.ok(Map.of("message", "Approved"));
    }

    @PutMapping("/leaves/{id}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long id) {
        leaveService.rejectLeave(id);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }

    @GetMapping("/wfh")
    public List<WFHRequest> wfh(Authentication auth) {
        return wfhService.getByManager(getManager(auth));
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

    @GetMapping("/daily-updates")
    public List<DailyUpdate> dailyUpdates(Authentication auth) {
        return dailyUpdateService.getByManager(getManager(auth));
    }

    @GetMapping("/attendance")
    public List<Attendance> attendance(Authentication auth) {
        return attendanceService.getByManager(getManager(auth));
    }

    @GetMapping("/regularization")
    public List<Regularization> regularizations(Authentication auth) {
        return regularizationService.getByManager(getManager(auth));
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

    @PostMapping("/give-rating")
    public ResponseEntity<?> giveRating(@RequestBody Map<String, Object> body,
                                        Authentication auth) {
        Employee manager = getManager(auth);
        Long empId = Long.valueOf(body.get("employeeId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String comment = (String) body.get("comment");
        ratingService.giveRating(empId, manager, rating, comment);
        return ResponseEntity.ok(Map.of("message", "Rating saved"));
    }
}