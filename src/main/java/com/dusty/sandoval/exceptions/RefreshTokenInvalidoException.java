package com.dusty.sandoval.exceptions;

public class RefreshTokenInvalidoException extends RuntimeException {

	public RefreshTokenInvalidoException() {
		super("La sesión de renovación no es válida o expiró. Inicia sesión nuevamente.");
	}
}
