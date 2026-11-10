package com.dusty.sandoval.exceptions;

public class CredencialesInvalidasException extends RuntimeException {

	public CredencialesInvalidasException() {
		super("El alias o la contraseña son incorrectos.");
	}
}
