package com.medicalml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Medical ML Platform backend application.
 *
 * This class bootstraps the Spring Boot application which provides:
 * - REST API endpoints for patient data management, file ingestion,
 * ML analysis orchestration, risk scoring, and visualization.
 * - JWT-based authentication and role-based access control.
 * - Integration with a Python ML microservice for health analytics.
 *
 * The @SpringBootApplication annotation enables:
 * - @Configuration: Marks this class as a source of bean definitions.
 * - @EnableAutoConfiguration: Auto-configures Spring beans based on classpath
 * dependencies.
 * - @ComponentScan: Scans the 'com.medicalml' package and sub-packages for
 * Spring components.
 */
@SpringBootApplication
public class MedicalMlApplication {

    /**
     * Main method — application entry point.
     * Launches the embedded Tomcat server and initializes all Spring beans.
     *
     * @param args Command-line arguments (e.g., --server.port=8080)
     */
    public static void main(String[] args) {
        SpringApplication.run(MedicalMlApplication.class, args);
    }
}
