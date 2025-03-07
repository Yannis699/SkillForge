package com.skillforge.fichiers.controller;

import com.skillforge.fichiers.entity.FileEntity;
import com.skillforge.fichiers.model.FileFormat;
import com.skillforge.fichiers.repository.FileRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private static final String STORAGE_DIRECTORY = "uploads_files";

    private final FileRepository fileRepository;
    private final Counter uploadErrorCounter;
    private final Counter successfulUploadCounter;
    private final io.micrometer.core.instrument.Timer uploadTimer;

    public FileController(FileRepository fileRepository, MeterRegistry meterRegistry) {
        this.fileRepository = fileRepository;
        this.uploadErrorCounter = meterRegistry.counter("files.upload.errors");
        this.successfulUploadCounter = meterRegistry.counter("files.upload.success");
        this.uploadTimer = meterRegistry.timer("files.upload.time");
        createStorageDirectory();
    }

    private void createStorageDirectory() {
        Path uploadPath = Paths.get(STORAGE_DIRECTORY);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Dossier de stockage créé : {}", STORAGE_DIRECTORY);
            }
        } catch (IOException e) {
            log.error("Erreur création dossier stockage", e);
        }
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "Upload d'un fichier", description = "Upload d'un fichier sur le serveur.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichier uploadé avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'upload")
    })
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Upload du fichier : {}", file.getOriginalFilename());
        Instant startTime = Instant.now();

        try {
            Path filePath = saveFileToDisk(file);
            saveFileMetadata(file, filePath);
            return ResponseEntity.ok("Fichier " + file.getOriginalFilename() + " uploadé !");
        } catch (Exception e) {
            log.error("Erreur lors de l'upload : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur upload.");
        } finally {
            recordMetrics(startTime);
        }
    }

    private Path saveFileToDisk(MultipartFile file) throws IOException {
        Path filePath = Paths.get(STORAGE_DIRECTORY).resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Fichier sauvegardé : {}", file.getOriginalFilename());
        return filePath;
    }

    private void saveFileMetadata(MultipartFile file, Path filePath) {
        FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), file.getSize(), file.getContentType(), filePath.toString(), LocalDateTime.now());
        if (fileRepository.findByFilename(file.getOriginalFilename()).isPresent()) {
            log.warn("Fichier {} existe déjà en base.", file.getOriginalFilename());
            return;
        }
        fileRepository.save(fileEntity);
        log.info("Fichier enregistré en base : {}", file.getOriginalFilename());
    }

    private void recordMetrics(Instant startTime) {
        try {
            uploadTimer.record(Duration.between(startTime, Instant.now()).toNanos(), TimeUnit.NANOSECONDS);
            successfulUploadCounter.increment();
        } catch (Exception e) {
            uploadErrorCounter.increment();
        }
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Convertir un fichier", description = "Convertit un fichier .txt en CSV ou PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichier converti avec succès"),
            @ApiResponse(responseCode = "400", description = "Format non supporté"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la conversion")
    })
    public ResponseEntity<String> convertFile(@RequestParam("file") MultipartFile file, @RequestParam("format") FileFormat format) {
        try {
            Path convertedFile = Paths.get(STORAGE_DIRECTORY, file.getOriginalFilename().replace(".txt", "." + format.name().toLowerCase()));
            Files.write(convertedFile, file.getBytes());
            return ResponseEntity.ok("Fichier converti avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la conversion.");
        }
    }

    @GetMapping("/listAll")
    @Operation(summary = "Lister tous les fichiers", description = "Récupère la liste des fichiers stockés")
    public ResponseEntity<List<String>> getAllFiles() {
        try (Stream<Path> stream = Files.list(Paths.get(STORAGE_DIRECTORY))) {
            return ResponseEntity.ok(stream.map(Path::getFileName).map(Path::toString).collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Erreur récupération fichiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/metadata")
    @Operation(summary = "Obtenir les métadonnées", description = "Récupère les métadonnées d'un fichier")
    public ResponseEntity<Object> getFileMetadata(@RequestParam("file") String filename) {
        Path filePath = Paths.get(STORAGE_DIRECTORY, filename);
        if (!Files.exists(filePath)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier inexistant");

        try {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            Map<String, Object> metadata = Map.of(
                    "Nom du fichier", filename,
                    "Taille", attrs.size(),
                    "Création", attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "Dernière modification", attrs.lastModifiedTime().toString()
            );
            return ResponseEntity.ok(metadata);
        } catch (IOException e) {
            log.error("Erreur récupération métadonnées", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur récupération métadonnées.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de fichier", description = "Permet de vérifier si un fichier existe dans le dossier de stockage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Le fichier a été trouvé"),
            @ApiResponse(responseCode = "404", description = "Le fichier n'existe pas"),
            @ApiResponse(responseCode = "500", description = "Une erreur technique est survenue")
    })
    public ResponseEntity<String> findFile(@RequestParam("file") String filename) {
        Path filePath = Paths.get(STORAGE_DIRECTORY).resolve(filename);

        if (!Files.exists(filePath)) {
            log.warn("Le fichier {} n'existe pas.", filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le fichier " + filename + " n'existe pas.");
        }

        log.info("Le fichier {} a été trouvé.", filename);
        return ResponseEntity.ok("Le fichier " + filename + " a été trouvé !");
    }

    @DeleteMapping("/{filename}")
    @Operation(summary = "Supprimer un fichier", description = "Supprime un fichier stocké")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        Path filePath = Paths.get(STORAGE_DIRECTORY, filename);
        if (!Files.exists(filePath)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier inexistant");

        try {
            Files.delete(filePath);
            log.info("Fichier supprimé : {}", filename);
            return ResponseEntity.ok("Fichier supprimé !");
        } catch (IOException e) {
            log.error("Erreur suppression fichier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur suppression.");
        }
    }
}
