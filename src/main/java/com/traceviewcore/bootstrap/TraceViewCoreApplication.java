package com.traceviewcore.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.traceviewcore")
public class TraceViewCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceViewCoreApplication.class, args);
    }
}
