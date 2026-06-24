package com.rapidx.taskflow.servlet;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

public class SystemApiServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final long servletStartTime = System.currentTimeMillis();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memoryStats = new HashMap<>();
        memoryStats.put("maxMemoryBytes", maxMemory);
        memoryStats.put("totalMemoryBytes", totalMemory);
        memoryStats.put("freeMemoryBytes", freeMemory);
        memoryStats.put("usedMemoryBytes", usedMemory);
        memoryStats.put("usedMemoryPercentage", (double) usedMemory / totalMemory * 100);

        Map<String, Object> osStats = new HashMap<>();
        osStats.put("osName", System.getProperty("os.name"));
        osStats.put("osVersion", System.getProperty("os.version"));
        osStats.put("osArch", System.getProperty("os.arch"));
        osStats.put("availableProcessors", runtime.availableProcessors());

        Map<String, Object> jvmStats = new HashMap<>();
        jvmStats.put("javaVersion", System.getProperty("java.version"));
        jvmStats.put("javaVendor", System.getProperty("java.vendor"));
        jvmStats.put("jvmName", runtimeMXBean.getVmName());

        Map<String, Object> serverStats = new HashMap<>();
        long jvmUptimeMs = runtimeMXBean.getUptime();
        long servletUptimeMs = System.currentTimeMillis() - servletStartTime;
        serverStats.put("jvmUptimeMs", jvmUptimeMs);
        serverStats.put("servletUptimeMs", servletUptimeMs);
        serverStats.put("formattedUptime", formatDuration(jvmUptimeMs));

        Map<String, Object> rootResponse = new HashMap<>();
        rootResponse.put("memory", memoryStats);
        rootResponse.put("os", osStats);
        rootResponse.put("jvm", jvmStats);
        rootResponse.put("server", serverStats);
        rootResponse.put("timestamp", System.currentTimeMillis());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(rootResponse));
            out.flush();
        }
    }

    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
