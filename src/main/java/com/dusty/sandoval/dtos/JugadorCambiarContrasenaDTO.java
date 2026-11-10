package com.dusty.sandoval.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JugadorCambiarContrasenaDTO(
		@NotBlank(message = "La contraseña es obligatoria y no puede estar vacía.")
		@Size(max = 255, message = "La contraseña no puede superar los 255 caracteres.")
		String contrasena) {
}
