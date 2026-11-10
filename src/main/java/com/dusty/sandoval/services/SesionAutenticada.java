package com.dusty.sandoval.services;

import com.dusty.sandoval.dtos.LoginResponseDTO;

public record SesionAutenticada(LoginResponseDTO respuesta, String refreshToken) {
}
