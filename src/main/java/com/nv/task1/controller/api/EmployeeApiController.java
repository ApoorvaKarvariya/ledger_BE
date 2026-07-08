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
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeApiController {

    private final UserRepository userRepository;
    private final TaskService taskService;
    private final LeaveService leaveService;
    private final WFHService wfhService;
    private final DailyUpdateService dailyUpdateService;
    private final AttendanceService attendanceService;
    private final RatingService ratingService;
    private final RegularizationService regularizationService;

    private User getUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication auth) {
        return ResponseEntity.ok(getUser(auth).getEmployee());
    }

    @GetMapping("/tasks")
    public List<Task> tasks(Authentication auth) {
        return taskService.getTasksForEmployee(getUser(auth).getEmployee());
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        taskService.updateStatus(id, TaskStatus.valueOf(body.get("status")));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> applyLeave(@RequestBody LeaveRequest leaveRequest,
                                        Authentication auth) {
        leaveService.saveLeaveRequest(leaveRequest, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Leave applied"));
    }

    @PostMapping("/wfh")
    public ResponseEntity<?> applyWFH(@RequestBody WFHRequest wfhRequest,
                                      Authentication auth) {
        wfhService.applyWFH(wfhRequest, auth.getName());
        return ResponseEntity.ok(Map.of("message", "WFH applied"));
    }

    @PostMapping("/daily-update")
    public ResponseEntity<?> dailyUpdate(@RequestBody Map<String, String> body,
                                         Authentication auth) {
        dailyUpdateService.saveUpdate(body.get("updateText"), auth.getName());
        return ResponseEntity.ok(Map.of("message", "Update saved"));
    }

    @PostMapping("/punch-in")
    public ResponseEntity<?> punchIn(Authentication auth) {
        try {
            attendanceService.punchIn(auth.getName());
            return ResponseEntity.ok(Map.of("message", "Punched in"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/punch-out")
    public ResponseEntity<?> punchOut(Authentication auth) {
        try {
            attendanceService.punchOut(auth.getName());
            return ResponseEntity.ok(Map.of("message", "Punched out"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/attendance")
    public List<Attendance> attendance(Authentication auth) {
        return attendanceService.getByEmployee(getUser(auth).getEmployee());
    }

    @GetMapping("/ratings")
    public List<Rating> ratings(Authentication auth) {
        return ratingService.getByEmployee(getUser(auth).getEmployee());
    }

    // Works for both Employee and Manager (a manager regularizing their own attendance)
    @PostMapping("/regularization")
    public ResponseEntity<?> applyRegularization(@RequestBody Regularization regularization,
                                                  Authentication auth) {
        regularizationService.applyRegularization(regularization, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Regularization request submitted"));
    }

    @GetMapping("/regularization")
    public List<Regularization> myRegularizations(Authentication auth) {
        return regularizationService.getByEmployee(getUser(auth).getEmployee());
    }
}