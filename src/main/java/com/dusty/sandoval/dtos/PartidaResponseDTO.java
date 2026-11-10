package com.dusty.sandoval.dtos;

import java.time.LocalDateTime;

public record PartidaResponseDTO(
		Long id,
		String nombreJugador,
		Long puntuacion,
		LocalDateTime fecha) {
}
