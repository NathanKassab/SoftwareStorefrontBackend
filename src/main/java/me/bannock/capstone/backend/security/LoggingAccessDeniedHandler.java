package me.bannock.capstone.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() == null){
            logger.info("User was not authenticated and could not access resource, session={}, endpoint={}, error={}",
                    request.getSession().getId(),
                    request.getRequestURI(),
                    accessDeniedException);
        }else{
            logger.info("User was not authorized to access resource, session={}, endpoint={}, user={}, error={}",
                    request.getSession().getId(),
                    request.getRequestURI(),
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                    accessDeniedException);
        }
        response.sendRedirect("/403.html");
    }

}
