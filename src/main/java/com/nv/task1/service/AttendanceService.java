package com.nv.task1.service;

import com.nv.task1.entity.*;
import com.nv.task1.repository.AttendanceRepository;
import com.nv.task1.repository.EmployeeRepository;
import com.nv.task1.repository.HolidayRepository;
import com.nv.task1.repository.LeaveRequestRepository;
import com.nv.task1.repository.UserRepository;
import com.nv.task1.repository.WFHRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final HolidayRepository holidayRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final WFHRequestRepository wfhRequestRepository;

    public void punchIn(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Employee emp = user.getEmployee();
        LocalDate today = LocalDate.now();

        Optional<Holiday> holiday = holidayRepository.findByDate(today);
        if (holiday.isPresent()) {
            throw new RuntimeException("Today is a holiday (" + holiday.get().getName() + "). Punch-in is disabled.");
        }

        Optional<Attendance> existing =
                attendanceRepository.findByEmployeeAndDate(emp, today);

        if (existing.isPresent()) {
            Attendance a = existing.get();
            if (a.getStatus() == AttendanceStatus.ABSENT || a.getStatus() == AttendanceStatus.MISSED_PUNCHOUT) {
                throw new RuntimeException("Today's attendance was already closed out. Submit a regularization request instead.");
            }
            throw new RuntimeException("You've already punched in today.");
        }

        Attendance a = new Attendance();
        a.setEmployee(emp);
        a.setDate(today);
        a.setPunchIn(LocalTime.now());
        a.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(a);
    }

    public void punchOut(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Employee emp = user.getEmployee();
        LocalDate today = LocalDate.now();

        Attendance a = attendanceRepository.findByEmployeeAndDate(emp, today)
                .orElseThrow(() -> new RuntimeException("You haven't punched in today."));

        if (a.getStatus() == AttendanceStatus.ABSENT || a.getStatus() == AttendanceStatus.MISSED_PUNCHOUT) {
            throw new RuntimeException("Today's attendance was already closed out. Submit a regularization request instead.");
        }
        if (a.getPunchOut() != null) {
            throw new RuntimeException("You've already punched out today.");
        }

        a.setPunchOut(LocalTime.now());
        attendanceRepository.save(a);
    }

    public List<Attendance> getByEmployee(Employee employee) {
        return attendanceRepository.findByEmployee(employee);
    }

    public List<Attendance> getByManager(Employee manager) {
        return attendanceRepository.findByEmployee_Manager(manager);
    }

    public List<Attendance> getAll() {
        return attendanceRepository.findAll();
    }

    // ---- Absent + missed-punchout detection ----

    // Runs automatically every night at 00:05 for the day that just ended.
    // (Cron: sec min hour day month weekday)
    @Scheduled(cron = "0 5 0 * * *")
    public void runNightlyAttendanceClose() {
        closeAttendanceForDate(LocalDate.now().minusDays(1));
    }

    // Can also be triggered manually (e.g. from an admin button) for any date,
    // which is useful for testing without waiting for midnight.
    public void closeAttendanceForDate(LocalDate date) {
        markAbsentees(date);
        markMissedPunchOuts(date);
    }

    private void markAbsentees(LocalDate date) {
        // Skip holidays
        if (holidayRepository.findByDate(date).isPresent()) return;

        // Skip weekends (5-day work week assumption)
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return;

        Set<Long> onApprovedLeave = leaveRequestRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        LeaveStatus.APPROVED, date, date)
                .stream().map(l -> l.getEmployee().getId()).collect(Collectors.toSet());

        Set<Long> onApprovedWfh = wfhRequestRepository
                .findByDateAndStatus(date, WFHStatus.APPROVED)
                .stream().map(w -> w.getEmployee().getId()).collect(Collectors.toSet());

        for (Employee emp : employeeRepository.findAll()) {
            // Not joined yet as of this date
            if (emp.getJoiningDate() != null && emp.getJoiningDate().isAfter(date)) continue;
            if (onApprovedLeave.contains(emp.getId())) continue;
            if (onApprovedWfh.contains(emp.getId())) continue;

            // Already has a record for the day (punched in, WFH, etc.) - leave it alone
            if (attendanceRepository.findByEmployeeAndDate(emp, date).isPresent()) continue;

            Attendance a = new Attendance();
            a.setEmployee(emp);
            a.setDate(date);
            a.setStatus(AttendanceStatus.ABSENT);
            attendanceRepository.save(a);
        }
    }

    private void markMissedPunchOuts(LocalDate date) {
        List<Attendance> punchedInNotOut =
                attendanceRepository.findByDateAndPunchInIsNotNullAndPunchOutIsNull(date);

        for (Attendance a : punchedInNotOut) {
            if (a.getStatus() == AttendanceStatus.PRESENT) {
                a.setStatus(AttendanceStatus.MISSED_PUNCHOUT);
                attendanceRepository.save(a);
            }
        }
    }
}