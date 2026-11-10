package com.dusty.sandoval.dtos;

public record LoginResponseDTO(
		String accessToken,
		String tokenType,
		long expiresIn,
		JugadorResponseDTO jugador) {
}
