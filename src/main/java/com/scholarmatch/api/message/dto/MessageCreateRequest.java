package com.scholarmatch.api.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageCreateRequest(@NotBlank @Size(max = 4000) String body) {
}
