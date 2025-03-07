package com.skillforge.fichiers.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formats de fichiers supportés")
public enum FileFormat {
    @Schema(description = "Convertir en PDF")
    PDF,

    @Schema(description = "Convertir en CSV")
    CSV;
}