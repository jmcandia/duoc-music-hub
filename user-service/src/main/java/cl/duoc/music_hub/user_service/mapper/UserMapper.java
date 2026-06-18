package cl.duoc.music_hub.user_service.mapper;

import org.springframework.stereotype.Component;

import cl.duoc.music_hub.user_service.dto.UserRequest;
import cl.duoc.music_hub.user_service.dto.UserResponse;
import cl.duoc.music_hub.user_service.model.User;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
