package com.dusty.sandoval.exceptions;

public class RefreshTokenReutilizadoException extends RuntimeException {

	public RefreshTokenReutilizadoException() {
		super("Se detectó la reutilización de una sesión de renovación. Inicia sesión nuevamente.");
	}
}
