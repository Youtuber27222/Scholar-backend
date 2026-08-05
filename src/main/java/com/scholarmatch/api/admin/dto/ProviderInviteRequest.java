package com.scholarmatch.api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProviderInviteRequest(
    @NotBlank @Email @Size(max = 255) String email,
    @Size(max = 200) String organizationName) {
}
