package com.dusty.sandoval.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JugadorCambiarAliasDTO(
		@NotBlank(message = "El alias es obligatorio y no puede estar vacío.")
		@Size(max = 80, message = "El alias no puede superar los 80 caracteres.")
		String alias) {
}
