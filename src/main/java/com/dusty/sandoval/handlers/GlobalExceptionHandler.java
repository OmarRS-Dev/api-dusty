package com.dusty.sandoval.handlers;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dusty.sandoval.exceptions.AliasIgualAlActualException;
import com.dusty.sandoval.exceptions.AliasYaEnUsoException;
import com.dusty.sandoval.exceptions.ContrasenaReutilizadaException;
import com.dusty.sandoval.exceptions.CredencialesInvalidasException;
import com.dusty.sandoval.exceptions.DatosJugadorInvalidosException;
import com.dusty.sandoval.exceptions.JugadorNoEncontradoException;
import com.dusty.sandoval.exceptions.RefreshTokenInvalidoException;
import com.dusty.sandoval.exceptions.RefreshTokenReutilizadoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail manejarAccesoDenegado(AccessDeniedException exception) {
		return crearProblema(HttpStatus.FORBIDDEN, "Acceso denegado", exception.getMessage());
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ProblemDetail manejarCredencialesInvalidas(CredencialesInvalidasException exception) {
		return crearProblema(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", exception.getMessage());
	}

	@ExceptionHandler({RefreshTokenInvalidoException.class, RefreshTokenReutilizadoException.class})
	public ProblemDetail manejarRefreshTokenInvalido(RuntimeException exception) {
		return crearProblema(HttpStatus.UNAUTHORIZED, "Sesión vencida", exception.getMessage());
	}

	@ExceptionHandler(JugadorNoEncontradoException.class)
	public ProblemDetail manejarJugadorNoEncontrado(JugadorNoEncontradoException exception) {
		return crearProblema(HttpStatus.NOT_FOUND, "Jugador no encontrado", exception.getMessage());
	}

	@ExceptionHandler(AliasYaEnUsoException.class)
	public ProblemDetail manejarAliasYaEnUso(AliasYaEnUsoException exception) {
		return crearProblema(HttpStatus.CONFLICT, "Alias no disponible", exception.getMessage());
	}

	@ExceptionHandler(AliasIgualAlActualException.class)
	public ProblemDetail manejarAliasIgualAlActual(AliasIgualAlActualException exception) {
		return crearProblema(HttpStatus.BAD_REQUEST, "Alias sin cambios", exception.getMessage());
	}

	@ExceptionHandler({ContrasenaReutilizadaException.class, DatosJugadorInvalidosException.class})
	public ProblemDetail manejarSolicitudInvalida(RuntimeException exception) {
		return crearProblema(HttpStatus.BAD_REQUEST, "Solicitud inválida", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail manejarErroresDeValidacion(MethodArgumentNotValidException exception) {
		String detalle = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getDefaultMessage())
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.joining(" "));

		return crearProblema(HttpStatus.BAD_REQUEST, "Error de validación", detalle);
	}

	private ProblemDetail crearProblema(HttpStatus status, String titulo, String detalle) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalle);
		problem.setTitle(titulo);
		return problem;
	}
}
