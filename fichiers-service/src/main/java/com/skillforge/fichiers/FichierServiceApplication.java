package com.skillforge.fichiers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.skillforge.fichiers")
@EnableJpaRepositories(basePackages = "com.skillforge.fichiers.repository")
public class FichierServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FichierServiceApplication.class, args);
    }
}