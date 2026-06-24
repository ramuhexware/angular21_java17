package com.rapidx.taskflow.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rapidx.taskflow.model.Task;
import com.rapidx.taskflow.service.TaskService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class TaskApiServlet extends HttpServlet {
    private final TaskService taskService = TaskService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id != null) {
            taskService.getTaskById(id).ifPresentOrElse(
                task -> sendJsonResponse(response, HttpServletResponse.SC_OK, task),
                () -> sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Task not found")
            );
        } else {
            sendJsonResponse(response, HttpServletResponse.SC_OK, taskService.getAllTasks());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action != null) {
            if ("clear".equalsIgnoreCase(action)) {
                taskService.clearAll();
                sendSuccessResponse(response, "All tasks cleared");
                return;
            } else if ("reset".equalsIgnoreCase(action)) {
                taskService.resetDefaults();
                sendJsonResponse(response, HttpServletResponse.SC_OK, taskService.getAllTasks());
                return;
            }
        }

        try {
            Task task = parseRequestBody(request, Task.class);
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Task title is required");
                return;
            }
            Task created = taskService.addTask(new Task(task.getTitle().trim(), task.getDescription(), task.getCategory()));
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, created);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Task task = parseRequestBody(request, Task.class);
            if (task.getId() == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Task ID is required for update");
                return;
            }
            boolean success = taskService.updateTask(task);
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, task);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Task ID query parameter is required");
            return;
        }

        boolean success = taskService.deleteTask(id.trim());
        if (success) {
            sendSuccessResponse(response, "Task deleted successfully");
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
        }
    }

    private <T> T parseRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException, JsonSyntaxException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        } catch (IOException e) {
            log("Error sending JSON response", e);
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        sendJsonResponse(response, HttpServletResponse.SC_OK, result);
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        sendJsonResponse(response, statusCode, result);
    }
}
