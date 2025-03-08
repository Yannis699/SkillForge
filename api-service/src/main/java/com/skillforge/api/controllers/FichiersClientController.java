package com.skillforge.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@RestController
@RequestMapping("/api/files")
public class FichiersClientController {

    private final WebClient fichiersWebClient;

    // Corrige le nom du constructeur ici 👇
    public FichiersClientController(WebClient fichiersWebClient) {
        this.fichiersWebClient = fichiersWebClient;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllFiles() {
        return ResponseEntity.ok(
                Objects.requireNonNull(fichiersWebClient.get()
                        .uri("/files/listAll")
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block())
        );
    }
}
