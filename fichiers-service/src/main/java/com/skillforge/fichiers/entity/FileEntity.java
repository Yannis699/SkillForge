package com.skillforge.fichiers.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String filename;

    @Column(nullable = false)
    private Long size;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    public FileEntity(String filename, Long size, String fileType, String filePath, LocalDateTime uploadedAt) {
        this.filename = filename;
        this.size = size;
        this.fileType = fileType;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }
}
