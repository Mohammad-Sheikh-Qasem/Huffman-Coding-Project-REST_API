// Developed by: Mohammad Sheikh Qasem
package com.huffman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * HuffmanApplication.java
 * ------------------------------------------------------------
 * Main entry point for the Huffman Coding Spring Boot application.
 * Starts the embedded Tomcat server and bootstraps all beans.
 * ============================================================
 */
@SpringBootApplication
public class HuffmanApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuffmanApplication.class, args);
        System.out.println("""
                ╔══════════════════════════════════════════════════╗
                ║     Huffman Coding Compression System            ║
                ║     Spring Boot Application Started              ║
                ║     API Base: http://localhost:8080/api/huffman  ║
                ╚══════════════════════════════════════════════════╝
                """);
    }
}
