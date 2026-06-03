// SUMMARY: This controller handles the home page / dashboard of the web application.
// Unlike other controllers, this returns HTML pages (not JSON) because it uses @Controller.
// URL pattern: / - root of the application.

package com.example.OFFUPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// @Controller (NOT @RestController) - This class returns HTML views/pages, not JSON data
// @RestController = @Controller + @ResponseBody (auto-converts to JSON)
// @Controller alone means we return template names (like dashboard.html)
@Controller
public class HomeController {

    // This method handles GET requests to the root URL: /
    // Purpose: Show the dashboard page when user visits the website
    @GetMapping("/")
    public String home() {

        // Return "dashboard" - this tells Spring to find a template called dashboard.html
        // Spring looks in src/main/resources/templates/dashboard.html
        // The .html extension is automatically added by the template engine (Thymeleaf)
        return "dashboard";
    }
}