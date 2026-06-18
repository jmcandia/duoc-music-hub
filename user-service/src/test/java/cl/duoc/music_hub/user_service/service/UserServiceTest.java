package cl.duoc.music_hub.user_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import cl.duoc.music_hub.user_service.dto.UserRequest;
import cl.duoc.music_hub.user_service.dto.UserResponse;
import cl.duoc.music_hub.user_service.exception.ResourceConflictException;
import cl.duoc.music_hub.user_service.exception.ResourceNotFoundException;
import cl.duoc.music_hub.user_service.mapper.UserMapper;
import cl.duoc.music_hub.user_service.model.User;
import cl.duoc.music_hub.user_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - UserService")
@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private User sampleUser2;
    private UserResponse sampleUserResponse;
    private UserRequest sampleUserRequest;

    @BeforeEach
    public void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("highball")
                .email("hal.jordan@duocuc.cl")
                .fullName("Hal Jodan")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sampleUser2 = User.builder()
                .id(2L)
                .username("barry")
                .email("barry.allen@duocuc.cl")
                .fullName("Bartholomew Henry Allen")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleUserResponse = UserResponse.builder()
                .id(1L)
                .username("highball")
                .email("hal.jordan@duocuc.cl")
                .fullName("High Ball")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleUserRequest = UserRequest.builder()
                .username("highball")
                .email("hal.jordan@duocuc.cl")
                .fullName("Hal Jodan")
                .build();
    }

    @Test
    @DisplayName("TC-01 - Retorna todos los usuarios")
    public void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser, sampleUser2));
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        List<UserResponse> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals(sampleUserResponse, result.get(0));
    }

    @Test
    @DisplayName("TC-02 - Retorna un usuario por ID")
    public void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        UserResponse result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(sampleUserResponse.getId());
    }

    @Test
    @DisplayName("TC-03 - Retorna un usuario por ID no existente")
    public void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado con ID: " + 99L);
    }

    @Test
    @DisplayName("TC-04 - Crea un nuevo usuario")
    public void testCreateUser() {
        when(userMapper.toEntity(sampleUserRequest)).thenReturn(sampleUser);
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        UserResponse result = userService.create(sampleUserRequest);
        assertThat(result.getUsername()).isEqualTo(sampleUserResponse.getUsername());
        assertThat(result.getEmail()).isEqualTo(sampleUserResponse.getEmail());
        assertThat(result.getFullName()).isEqualTo(sampleUserResponse.getFullName());
    }

    @Test
    @DisplayName("TC-05 - Crea un nuevo usuario con username duplicado")
    public void testCreateUserWithDuplicateUsername() {
        when(userRepository.findByUsername(sampleUserRequest.getUsername())).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> userService.create(sampleUserRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("El nombre de usuario '" + sampleUserRequest.getUsername() + "' ya está registrado.");
    }

    @Test
    @DisplayName("TC-06 - Crea un nuevo usuario con email duplicado")
    public void testCreateUserWithDuplicateEmail() {
        when(userRepository.findByEmail(sampleUserRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        assertThatThrownBy(() -> userService.create(sampleUserRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("El correo '" + sampleUserRequest.getEmail() + "' ya está en uso.");
    }

    @Test
    @DisplayName("TC-07 - Actualiza un usuario existente")
    public void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);
        UserResponse result = userService.update(1L, sampleUserRequest);
        assertThat(result.getUsername()).isEqualTo(sampleUserResponse.getUsername());
        assertThat(result.getEmail()).isEqualTo(sampleUserResponse.getEmail());
        assertThat(result.getFullName()).isEqualTo(sampleUserResponse.getFullName());
    }

    @Test
    @DisplayName("TC-08 - Elimina un usuario por ID")
    public void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        userService.delete(1L);
        verify(userRepository).delete(sampleUser);
    }
}