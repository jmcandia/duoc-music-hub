package cl.duoc.music_hub.user_service.dto;

import java.time.LocalDateTime;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse extends RepresentationModel<UserResponse> {

    @Schema(description = "ID del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario", example = "johndoe")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "john.doe@duocuc.cl")
    private String email;

    @Schema(description = "Nombre del usuario", example = "John Doe")
    private String fullName;

    @Schema(description = "Fecha de creación del usuario", example = "2023-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de actualización del usuario", example = "2023-01-01T00:00:00")
    private LocalDateTime updatedAt;
}
