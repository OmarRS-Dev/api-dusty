package com.dusty.sandoval.dtos;

public record PartidaGuardadaDTO(
		PartidaResponseDTO partida,
		boolean nuevoRecord,
		Long recordAnterior,
		Long recordActual) {
}
