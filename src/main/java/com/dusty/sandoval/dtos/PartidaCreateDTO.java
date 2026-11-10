package com.dusty.sandoval.dtos;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PartidaCreateDTO(
		@NotNull(message = "La puntuación es obligatoria.")
		@PositiveOrZero(message = "La puntuación no puede ser negativa.")
		Long puntuacion) {
}
