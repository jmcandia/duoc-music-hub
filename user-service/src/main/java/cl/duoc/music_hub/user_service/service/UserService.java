package cl.duoc.music_hub.user_service.service;

import java.util.List;

import cl.duoc.music_hub.user_service.dto.UserRequest;
import cl.duoc.music_hub.user_service.dto.UserResponse;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);
}
