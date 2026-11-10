package com.dusty.sandoval.controllers;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dusty.sandoval.dtos.LoginRequestDTO;
import com.dusty.sandoval.dtos.LoginResponseDTO;
import com.dusty.sandoval.services.AutenticacionService;
import com.dusty.sandoval.services.SesionAutenticada;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

	private static final String REFRESH_COOKIE_NAME = "refresh_token";

	private final AutenticacionService autenticacionService;
	private final boolean secureCookie;
	private final long refreshIdleSeconds;

	public AutenticacionController(
			AutenticacionService autenticacionService,
			@Value("${app.auth.refresh-cookie-secure}") boolean secureCookie,
			@Value("${app.auth.refresh-idle-seconds}") long refreshIdleSeconds) {
		this.autenticacionService = autenticacionService;
		this.secureCookie = secureCookie;
		this.refreshIdleSeconds = refreshIdleSeconds;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request) {
		return respuestaConCookie(autenticacionService.iniciarSesion(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<LoginResponseDTO> renovarSesion(
			@CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		return respuestaConCookie(autenticacionService.renovarSesion(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> cerrarSesion(
			@CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		autenticacionService.cerrarSesion(refreshToken);
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, crearCookie("", Duration.ZERO).toString())
				.build();
	}

	private ResponseEntity<LoginResponseDTO> respuestaConCookie(SesionAutenticada session) {
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, crearCookie(
						session.refreshToken(),
						Duration.ofSeconds(refreshIdleSeconds)).toString())
				.body(session.respuesta());
	}

	private ResponseCookie crearCookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
				.httpOnly(true)
				.secure(secureCookie)
				.sameSite("Strict")
				.path("/api/auth")
				.maxAge(maxAge)
				.build();
	}
}
