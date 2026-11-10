package com.dusty.sandoval.exceptions;

public class AliasYaEnUsoException extends RuntimeException {

	public AliasYaEnUsoException() {
		super("El alias ya está registrado por otro jugador.");
	}
}
