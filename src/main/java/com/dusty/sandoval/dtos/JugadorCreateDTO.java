package com.dusty.sandoval.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JugadorCreateDTO(
		@NotBlank(message = "El alias es obligatorio y no puede estar vacío.")
		@Size(max = 80, message = "El alias no puede superar los 80 caracteres.")
		String alias,

		@NotBlank(message = "La contraseña es obligatoria y no puede estar vacía.")
		@Size(max = 255, message = "La contraseña no puede superar los 255 caracteres.")
		String password) {
}
