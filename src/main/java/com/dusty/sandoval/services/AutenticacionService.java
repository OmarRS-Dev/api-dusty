package com.dusty.sandoval.services;

import com.dusty.sandoval.dtos.LoginRequestDTO;

public interface AutenticacionService {

	SesionAutenticada iniciarSesion(LoginRequestDTO request);

	SesionAutenticada renovarSesion(String refreshToken);

	void cerrarSesion(String refreshToken);
}
