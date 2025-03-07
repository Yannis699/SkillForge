package com.skillforge.fichiers.controller;


import com.skillforge.fichiers.model.FileFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private static final String DOSSIER_STOCKAGE_FICHIER = "uploads_files"; // Dossier où stocker les fichiers dans répertoire courant

    public FileController() throws Exception {
        Path uploadPath = Paths.get(DOSSIER_STOCKAGE_FICHIER);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Le dossier de stockage a été créé avec succès : {}", DOSSIER_STOCKAGE_FICHIER);
        }
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "Upload de fichier", description = "Permet d'uploader un fichier sur le serveur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier a été uploadé avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'upload")
    })
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Tentative d'upload du fichier : {}", file.getOriginalFilename());
        try {
            Path filePath = Paths.get(DOSSIER_STOCKAGE_FICHIER).resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Fichier {} uploadé avec succès !", file.getOriginalFilename());
            return ResponseEntity.ok("Fichier " + file.getOriginalFilename() + " uploadé avec succès !");
        } catch (Exception e) {
            log.error("Erreur lors de l'upload du fichier : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload du fichier.");
        }
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Convertir un fichier", description = "Permet de convertir un fichier .txt au format CSV ou pdf")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier converti avec succès"),
            @ApiResponse(responseCode = "400", description = "Format non supporté"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la conversion")
    })
    public ResponseEntity<String> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") FileFormat format) {

        try {
            // Création du fichier converti
            String originalFilename = file.getOriginalFilename();
            String newFilename = originalFilename.replace(".txt", "." + format.name().toLowerCase());
            Path filePath = Paths.get("uploads_files").resolve(newFilename);

            // Simulation de la conversion
            Files.write(filePath, file.getBytes());

            return ResponseEntity.ok("Fichier converti avec succès ! Téléchargez-le à /files/download/" + newFilename);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la conversion.");
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
        Path filePath = Paths.get(DOSSIER_STOCKAGE_FICHIER).resolve(filename);

        if (!Files.exists(filePath)) {
            log.warn("Le fichier {} n'existe pas.", filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le fichier " + filename + " n'existe pas.");
        }

        log.info("Le fichier {} a été trouvé.", filename);
        return ResponseEntity.ok("Le fichier " + filename + " a été trouvé !");
    }

    @GetMapping("/listAll")
    @Operation(summary = "Obtenir tous les fichiers", description = "Récupération de l'ensemble des fichiers dans le dossier courant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Les fichiers sont bien récupérés"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la récupération des fichiers"),
    })
    public ResponseEntity<List<String>> getAllFiles() {
        Path filePath = Paths.get(DOSSIER_STOCKAGE_FICHIER);

        try (Stream<Path> stream = Files.list(filePath)) {
            log.info("Erreur lors de la récupération de la liste de fichiers");
            List<String> listeFichier = stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();

            return ResponseEntity.ok(listeFichier);
        } catch (IOException e) {
            log.error("Erreur lors de la récupération de la liste de fichiers : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList()); // Retourne une liste vide en cas d'erreur
        }
    }

    @GetMapping("/metadata")
    @Operation(summary = "Obtenir les métadonnées d'un fichier", description = "Permet de récupérer des informations sur un fichier spécifique dans le dossier de stockage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Les métadonnées ont été récupérées avec succès"),
            @ApiResponse(responseCode = "404", description = "Le fichier n'existe pas"),
            @ApiResponse(responseCode = "500", description = "Une erreur technique est survenue")
    })
    public ResponseEntity<Object> getFileMetadata(@RequestParam("file") String filename) {
        Path filePath = Paths.get(DOSSIER_STOCKAGE_FICHIER).resolve(filename);

        if (!Files.exists(filePath)) {
            log.warn("Le fichier {} n'existe pas, les métadonnées ne peuvent être extraites.", filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le fichier " + filename + " n'existe pas.");
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("Nom du fichier", filename);
            metadata.put("Taille (octets)", attrs.size());
            metadata.put("Date de création", attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter));
            metadata.put("Dernière modification", attrs.lastModifiedTime().toString());


            log.info("Métadonnées récupérées pour le fichier : {}", filename);
            return ResponseEntity.ok(metadata);
        } catch (IOException e) {
            log.error("Erreur lors de la récupération des métadonnées du fichier {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des métadonnées du fichier.");
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        log.info("Tentative de téléchargement du fichier : {}", filename);

        try {
            Path filePath = Paths.get(DOSSIER_STOCKAGE_FICHIER).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                log.info("Fichier {} trouvé et prêt pour le téléchargement.", filename);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                log.warn("Fichier {} introuvable.", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du fichier {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{filename}")
    @Operation(summary = "suppression de fichier", description = "Permet de supprimer un fichier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier supprimé avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la suppression du fichier"),
            @ApiResponse(responseCode = "404", description = "Le fichier n'existe pas")
    })

    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        Path filePath = Paths.get(DOSSIER_STOCKAGE_FICHIER).resolve(filename);

        // Vérifier si le fichier existe
        if (!Files.exists(filePath)) {
            log.warn("Tentative de suppression d'un fichier inexistant : {}", filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le fichier " + filename + " n'existe pas.");
        }

        try {
            Files.delete(filePath);
            log.info("Fichier supprimé avec succès : {}", filename);
            return ResponseEntity.ok("Fichier " + filename + " supprimé avec succès !");
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier : {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression du fichier.");
        }
    }
}
