package com.rapidx.taskflow.service;

import com.rapidx.taskflow.model.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TaskService {
    private static final TaskService INSTANCE = new TaskService();
    private final List<Task> tasks = Collections.synchronizedList(new ArrayList<>());

    private TaskService() {
        resetDefaults();
    }

    public static TaskService getInstance() {
        return INSTANCE;
    }

    public List<Task> getAllTasks() {
        // Return a copy to prevent concurrent modification issues
        synchronized (tasks) {
            return new ArrayList<>(tasks);
        }
    }

    public Optional<Task> getTaskById(String id) {
        synchronized (tasks) {
            return tasks.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst();
        }
    }

    public Task addTask(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (task.getCategory() == null || task.getCategory().trim().isEmpty()) {
            task.setCategory("Personal");
        }
        tasks.add(0, task); // Prepend so new tasks appear first
        return task;
    }

    public boolean updateTask(Task updatedTask) {
        synchronized (tasks) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId().equals(updatedTask.getId())) {
                    tasks.set(i, updatedTask);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteTask(String id) {
        synchronized (tasks) {
            return tasks.removeIf(t -> t.getId().equals(id));
        }
    }

    public void clearAll() {
        tasks.clear();
    }

    public final void resetDefaults() {
        synchronized (tasks) {
            tasks.clear();
            tasks.add(new Task("Deploy App to Tomcat", "Package the Java 17 and Angular 21 application as a WAR file and run it inside Apache Tomcat 9.", "Work"));
            tasks.add(new Task("Build Glassmorphic UI", "Style the dashboard using modern CSS custom properties, backdrop-filters, and interactive animations.", "Personal"));
            tasks.add(new Task("Verify REST API Endpoints", "Test CORS, SPA routing filters, and JSON serialization using Java Servlets.", "Work"));
            tasks.add(new Task("Refactor Task Service", "Implement thread-safe CRUD operations with java.util.Collections.synchronizedList.", "Idea"));
            
            // Mark a couple as completed
            tasks.get(1).setCompleted(true);
            tasks.get(3).setCompleted(true);
        }
    }
}
