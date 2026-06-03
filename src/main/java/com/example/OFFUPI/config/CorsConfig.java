// SUMMARY: This file handles CORS (Cross-Origin Resource Sharing) settings.
// CORS allows your frontend (like a website on port 5500) to talk to your backend API (on port 8080).
// Without CORS, browsers block requests from different origins for security reasons.

package com.example.OFFUPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// @Configuration tells Spring: "This class contains setup instructions for the application"
// Spring will read this class when the app starts and apply the settings
@Configuration
public class CorsConfig {

    // @Bean tells Spring: "Run this method and store the returned object in memory"
    // This CorsFilter object will be automatically used to handle CORS for all requests
    @Bean
    public CorsFilter corsFilter() {

        // Create a new CORS configuration object that will hold our rules
        CorsConfiguration config = new CorsConfiguration();

        // setAllowCredentials(true) allows sending cookies and authentication headers
        // Example: If your frontend logs in, it can send the session cookie with requests
        config.setAllowCredentials(true);

        // addAllowedOrigin() specifies which websites can call our API
        // http://localhost:5500 - common port for VS Code Live Server (frontend)
        config.addAllowedOrigin("http://localhost:5500");
        // http://127.0.0.1:5500 - same as localhost but using IP address
        config.addAllowedOrigin("http://127.0.0.1:5500");
        // http://localhost:8081 - alternative frontend port
        config.addAllowedOrigin("http://localhost:8081");

        // addAllowedHeader("*") allows ANY HTTP headers in the request
        // Headers carry extra info like authentication tokens, content type, etc.
        // "*" means "allow everything" - convenient for development
        config.addAllowedHeader("*");

        // addAllowedMethod("*") allows ANY HTTP method (GET, POST, PUT, DELETE, etc.)
        // "*" means all HTTP methods are allowed
        config.addAllowedMethod("*");

        // This source object maps URL patterns to CORS rules
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // registerCorsConfiguration("/**", config) applies our CORS rules to ALL API paths
        // "/**" is a wildcard meaning "every possible URL in our application"
        source.registerCorsConfiguration("/**", config);

        // Create and return the CorsFilter with our configuration
        // Spring will automatically use this filter for every incoming request
        return new CorsFilter(source);
    }
}