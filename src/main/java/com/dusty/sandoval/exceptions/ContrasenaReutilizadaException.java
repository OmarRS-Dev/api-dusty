package com.dusty.sandoval.exceptions;

public class ContrasenaReutilizadaException extends RuntimeException {

	public ContrasenaReutilizadaException() {
		super("La nueva contraseña no puede ser igual a la contraseña actual.");
	}
}
