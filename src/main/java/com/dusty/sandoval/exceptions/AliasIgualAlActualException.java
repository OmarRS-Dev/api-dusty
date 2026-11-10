package com.dusty.sandoval.exceptions;

public class AliasIgualAlActualException extends RuntimeException {

	public AliasIgualAlActualException() {
		super("El nuevo alias debe ser diferente al alias actual.");
	}
}
