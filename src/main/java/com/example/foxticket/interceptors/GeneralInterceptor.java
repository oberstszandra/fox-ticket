package com.example.foxticket.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Map;

@Component
public class GeneralInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(GeneralInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (response.getStatus() >= 400) {
            logger.error("ERROR: " + HttpStatus.valueOf(response.getStatus()));
        } else {
            logger.info(createInfoMessage(request, response));
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (response.getStatus() >= 400) {
            logger.error("ERROR: " + HttpStatus.valueOf(response.getStatus()));
        } else {
            logger.info(createInfoMessage(request, response));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            logger.error("Error message: " + ex.getMessage());
        }

        if (response.getStatus() >= 400) {
            logger.error("ERROR: " + HttpStatus.valueOf(response.getStatus()));
        } else {
            logger.info(createInfoMessage(request, response));
        }
    }

    private String createInfoMessage(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        StringBuilder parameters = new StringBuilder();
        if (requestParameterMap.isEmpty()) {
            parameters.append("null ");
        }
        for (String parameter : requestParameterMap.keySet()) {
            parameters.append(parameter).append("=").append(Arrays.toString(requestParameterMap.get(parameter))).append(" ");
        }
        return "Method: " + request.getMethod() + " Path: " + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) + " Parameters: " + parameters + "Status code: " + response.getStatus();
    }
}