package com.scholarmatch.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteRequest(
    @NotBlank @Size(min = 8, max = 200) String password,
    @NotBlank @Size(min = 2, max = 120) String fullName) {
}
