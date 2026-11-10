package com.dusty.sandoval.dtos;

import java.time.LocalDateTime;

public record JugadorResponseDTO(
		Long id,
		String alias,
		Long record,
		LocalDateTime fechaRegistro) {
}
