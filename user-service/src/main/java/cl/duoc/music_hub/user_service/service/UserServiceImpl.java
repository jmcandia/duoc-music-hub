package cl.duoc.music_hub.user_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.music_hub.user_service.dto.UserRequest;
import cl.duoc.music_hub.user_service.dto.UserResponse;
import cl.duoc.music_hub.user_service.exception.ResourceConflictException;
import cl.duoc.music_hub.user_service.exception.ResourceNotFoundException;
import cl.duoc.music_hub.user_service.mapper.UserMapper;
import cl.duoc.music_hub.user_service.model.User;
import cl.duoc.music_hub.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.info("Solicitando listado completo de usuarios");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.info("Buscando usuario con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        log.info("Buscando usuario con username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el username: " + username));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        log.info("Iniciando creación de usuario con username: {}", request.getUsername());

        // 1. Validar conflicto de username en creación
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceConflictException(
                    "El nombre de usuario '" + request.getUsername() + "' ya está registrado.");
        }

        // 2. Validar conflicto de email en creación
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceConflictException("El correo '" + request.getEmail() + "' ya está en uso.");
        }

        // 3. Transformar DTO a Entidad y guardar
        User newUser = userMapper.toEntity(request);
        User savedUser = userRepository.save(newUser);

        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        log.info("Iniciando actualización de usuario con ID: {}", id);

        // 1. Verificar existencia del recurso
        User userExisting = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Imposible actualizar. Usuario no encontrado con ID: " + id));

        // 2. Validar conflicto de username (que no pertenezca a OTRO usuario)
        userRepository.findByUsername(request.getUsername()).ifPresent(userWithUsername -> {
            if (!userWithUsername.getId().equals(id)) {
                throw new ResourceConflictException(
                        "El nombre de usuario '" + request.getUsername() + "' ya está siendo usado por otro perfil.");
            }
        });

        // 3. Validar conflicto de email (que no pertenezca a OTRO usuario)
        userRepository.findByEmail(request.getEmail()).ifPresent(userWithEmail -> {
            if (!userWithEmail.getId().equals(id)) {
                throw new ResourceConflictException(
                        "El correo '" + request.getEmail() + "' ya está registrado en otra cuenta.");
            }
        });

        // 4. Modificar el estado de la entidad existente de forma directa
        userExisting.setUsername(request.getUsername());
        userExisting.setEmail(request.getEmail());
        userExisting.setFullName(request.getFullName());

        // 5. Guardar cambios y retornar respuesta transformada
        User updatedUser = userRepository.save(userExisting);

        log.info("Usuario con ID: {} actualizado correctamente", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Iniciando eliminación de usuario con ID: {}", id);

        // Verificar existencia antes de borrar para lanzar el error correcto
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Imposible eliminar. Usuario no encontrado con ID: " + id));

        userRepository.delete(user);
        log.info("Usuario con ID: {} eliminado exitosamente", id);
    }
}
