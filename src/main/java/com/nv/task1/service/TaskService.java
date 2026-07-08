package com.nv.task1.service;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Task;
import com.nv.task1.entity.TaskStatus;
import com.nv.task1.repository.EmployeeRepository;
import com.nv.task1.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    // Admin assigns task to manager


    // Get tasks assigned to a specific employee
    public List<Task> getTasksForEmployee(Employee employee) {
        return taskRepository.findByAssignedTo(employee);
    }

    // Get tasks assigned by a manager to his team
    public List<Task> getTasksAssignedByManager(Employee manager) {
        return taskRepository.findByAssignedBy(manager);
    }

    // Get all tasks (admin)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Update task status (employee)
    public void updateStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }public void assignTask(Long assignedToId, Long assignedById, Task task) {

        Employee assignedTo = employeeRepository.findById(assignedToId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        task.setAssignedTo(assignedTo);
        task.setStatus(TaskStatus.PENDING);

        // Admin ke paas employee nahi hoti, isliye assignedById 0 aata hai — skip karo
        if (assignedById != null && assignedById != 0) {
            employeeRepository.findById(assignedById)
                    .ifPresent(task::setAssignedBy);
        }

        taskRepository.save(task);
    }
}