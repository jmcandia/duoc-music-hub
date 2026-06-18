package cl.duoc.music_hub.user_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Schema(description = "Fecha y hora del error", example = "2023-01-01T00:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código de estado", example = "404")
    private Integer status;

    @Schema(description = "Tipo de error", example = "Not Found")
    private String error;

    @Schema(description = "Mensaje de error", example = "El recurso no fue encontrado")
    private String message;

    @Schema(description = "Ruta de la solicitud", example = "/api/autores/1")
    private String path;

    @Schema(description = "Lista de errores", example = "[\"El campo 'nombre' es requerido\"]")
    private List<String> errors;
}
