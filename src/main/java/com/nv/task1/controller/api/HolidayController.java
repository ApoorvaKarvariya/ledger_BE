package com.nv.task1.controller.api;

import com.nv.task1.entity.Holiday;
import com.nv.task1.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    // ---- Shared read-only endpoints (any logged-in role: admin, manager, employee) ----

    @GetMapping("/api/holidays")
    public List<Holiday> getAllHolidays() {
        return holidayService.getAll();
    }

    @GetMapping("/api/holidays/upcoming")
    public List<Holiday> getUpcomingHolidays() {
        return holidayService.getUpcoming();
    }

    // ---- Admin-only management endpoints ----

    @PostMapping("/api/admin/holidays")
    public ResponseEntity<?> addHoliday(@RequestBody Holiday holiday) {
        try {
            Holiday saved = holidayService.addHoliday(holiday);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/holidays/{id}")
    public ResponseEntity<?> updateHoliday(@PathVariable Long id, @RequestBody Holiday holiday) {
        try {
            Holiday saved = holidayService.updateHoliday(id, holiday);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/holidays/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.ok(Map.of("message", "Holiday deleted successfully"));
    }
}
