package com.dusty.sandoval.exceptions;

public class JugadorNoEncontradoException extends RuntimeException {

	public JugadorNoEncontradoException(Long jugadorId) {
		super("No se encontró un jugador con el identificador " + jugadorId + ".");
	}
}
