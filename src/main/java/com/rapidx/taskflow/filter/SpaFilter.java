package com.rapidx.taskflow.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SpaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();
        
        // Strip the context path from the URI
        String path = uri.substring(contextPath.length());

        // Check if the request is for API, or has a file extension (static asset), or is root
        if (path.startsWith("/api") || path.contains(".") || path.equals("/")) {
            chain.doFilter(request, response);
        } else {
            // Forward to index.html for Angular routing to handle client-side
            httpRequest.getRequestDispatcher("/index.html").forward(request, response);
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
