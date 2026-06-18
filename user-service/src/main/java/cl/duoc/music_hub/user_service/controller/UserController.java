package cl.duoc.music_hub.user_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.music_hub.user_service.dto.ApiErrorResponse;
import cl.duoc.music_hub.user_service.dto.UserRequest;
import cl.duoc.music_hub.user_service.dto.UserResponse;
import cl.duoc.music_hub.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints para el ciclo de vida y gestión de usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista con todos los usuarios registrados e incluye enlaces HATEOAS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CollectionModel<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll().stream().map(user -> {
            addHateoasLinks(user);
            return user;
        }).collect(Collectors.toList());
        CollectionModel<UserResponse> collectionModel = CollectionModel.of(users);
        collectionModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Retorna un único usuario basado en su ID de base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        addHateoasLinks(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar usuario por username", description = "Permite filtrar y encontrar un usuario exacto mediante su parámetro de cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> getUserByUsername(@RequestParam String username) {
        UserResponse user = userService.findByUsername(username);
        addHateoasLinks(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un usuario en el sistema. Valida campos obligatorios y formato de correo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida debido a errores de validación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de datos, como username o email ya existente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse createdUser = userService.create(request);
        addHateoasLinks(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario existente", description = "Modifica los datos de un perfil. Valida conflictos de duplicados con otras cuentas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida debido a errores de validación o conflictos de datos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado para actualizar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de datos, como username o email ya existente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        UserResponse updatedUser = userService.update(id, request);
        addHateoasLinks(updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Remueve físicamente el registro del usuario del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado para eliminar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    private void addHateoasLinks(UserResponse response) {
        // Enlace al recurso individual (self)
        response.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel());

        // Enlace al recurso de actualización (update)
        response.add(linkTo(methodOn(UserController.class).updateUser(response.getId(), null)).withRel("update"));

        // Enlace al recurso de eliminación (delete)
        response.add(linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete"));

        // Enlace relacional hacia el listado completo (all-users)
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
    }
}
