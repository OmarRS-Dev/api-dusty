package com.dusty.sandoval.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
		@NotBlank(message = "El alias es obligatorio.")
		String alias,

		@NotBlank(message = "La contraseña es obligatoria.")
		String password) {
}
