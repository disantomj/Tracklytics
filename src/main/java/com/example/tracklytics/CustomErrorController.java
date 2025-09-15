package com.example.tracklytics;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public String handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
        Exception exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        System.err.println("=== ERROR DETAILS ===");
        System.err.println("Status Code: " + statusCode);
        System.err.println("Error Message: " + errorMessage);
        System.err.println("Request URI: " + requestUri);
        if (exception != null) {
            System.err.println("Exception: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
            exception.printStackTrace();
        }
        System.err.println("====================");

        return "<h1>Error " + statusCode + "</h1>" +
                "<p>Message: " + errorMessage + "</p>" +
                "<p>URI: " + requestUri + "</p>" +
                "<p><a href='/'>Go Home</a> | <a href='/oauth2/authorization/spotify'>Try Login Again</a></p>" +
                "<p>Check the console for detailed error information.</p>";
    }
}