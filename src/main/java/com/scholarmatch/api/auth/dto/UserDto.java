package com.scholarmatch.api.auth.dto;

import com.scholarmatch.api.common.Role;
import java.util.List;
import java.util.UUID;

public record UserDto(UUID id, String email, List<Role> roles, boolean emailVerified) {
}
