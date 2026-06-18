package cl.duoc.music_hub.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UserRequest {

    @Schema(description = "Nombre de usuario", example = "johndoe")
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no puede exceder los 50 caracteres")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "john.doe@duocuc.cl")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 150, message = "El correo electrónico no puede exceder los 150 caracteres")
    private String email;

    @Schema(description = "Nombre del usuario", example = "John Doe")
    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Size(max = 200, message = "El nombre del usuario no puede exceder los 200 caracteres")
    private String fullName;
}
