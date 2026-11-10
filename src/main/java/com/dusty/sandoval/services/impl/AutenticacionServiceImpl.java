package com.dusty.sandoval.services.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dusty.sandoval.dtos.JugadorResponseDTO;
import com.dusty.sandoval.dtos.LoginRequestDTO;
import com.dusty.sandoval.dtos.LoginResponseDTO;
import com.dusty.sandoval.entities.Jugador;
import com.dusty.sandoval.entities.RefreshToken;
import com.dusty.sandoval.exceptions.CredencialesInvalidasException;
import com.dusty.sandoval.exceptions.RefreshTokenInvalidoException;
import com.dusty.sandoval.exceptions.RefreshTokenReutilizadoException;
import com.dusty.sandoval.repositories.JugadorRepository;
import com.dusty.sandoval.repositories.RefreshTokenRepository;
import com.dusty.sandoval.services.AutenticacionService;
import com.dusty.sandoval.services.JwtService;
import com.dusty.sandoval.services.SesionAutenticada;

@Service
public class AutenticacionServiceImpl implements AutenticacionService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int REFRESH_TOKEN_BYTES = 32;

	private final JugadorRepository jugadorRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final long refreshIdleSeconds;
	private final long refreshAbsoluteSeconds;

	public AutenticacionServiceImpl(
			JugadorRepository jugadorRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			@Value("${app.auth.refresh-idle-seconds}") long refreshIdleSeconds,
			@Value("${app.auth.refresh-absolute-seconds}") long refreshAbsoluteSeconds) {
		this.jugadorRepository = jugadorRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshIdleSeconds = refreshIdleSeconds;
		this.refreshAbsoluteSeconds = refreshAbsoluteSeconds;
	}

	@Override
	@Transactional
	public SesionAutenticada iniciarSesion(LoginRequestDTO request) {
		if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new CredencialesInvalidasException();
		}

		Jugador jugador = jugadorRepository.findByAlias(request.alias())
				.filter(cuenta -> passwordEncoder.matches(request.password(), cuenta.getPassword()))
				.orElseThrow(CredencialesInvalidasException::new);

		JugadorResponseDTO perfil = new JugadorResponseDTO(
				jugador.getId(),
				jugador.getAlias(),
				jugador.getRecord(),
				jugador.getFechaRegistro());

		Instant now = Instant.now();
		String familyId = UUID.randomUUID().toString();
		Instant familyExpiresAt = now.plusSeconds(refreshAbsoluteSeconds);
		return crearSesion(jugador, perfil, familyId, familyExpiresAt, now);
	}

	@Override
	@Transactional(noRollbackFor = RefreshTokenReutilizadoException.class)
	public SesionAutenticada renovarSesion(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new RefreshTokenInvalidoException();
		}

		RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(hashToken(rawRefreshToken))
				.orElseThrow(RefreshTokenInvalidoException::new);
		Instant now = Instant.now();

		if (current.getRevokedAt() != null) {
			refreshTokenRepository.revokeActiveByFamilyId(current.getFamilyId(), now);
			throw new RefreshTokenReutilizadoException();
		}
		if (!current.getExpiresAt().isAfter(now) || !current.getFamilyExpiresAt().isAfter(now)) {
			throw new RefreshTokenInvalidoException();
		}

		current.setRevokedAt(now);
		RefreshToken replacement = crearRefreshToken(
				current.getJugador(),
				current.getFamilyId(),
				current.getFamilyExpiresAt(),
				now);
		refreshTokenRepository.save(current);

		Jugador jugador = current.getJugador();
		JugadorResponseDTO perfil = new JugadorResponseDTO(
				jugador.getId(),
				jugador.getAlias(),
				jugador.getRecord(),
				jugador.getFechaRegistro());

		return crearSesionConRefreshToken(jugador, perfil, replacement);
	}

	@Override
	@Transactional
	public void cerrarSesion(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}

		refreshTokenRepository.findByTokenHashForUpdate(hashToken(rawRefreshToken))
				.filter(token -> token.getRevokedAt() == null)
				.ifPresent(token -> token.setRevokedAt(Instant.now()));
	}

	private SesionAutenticada crearSesion(
			Jugador jugador,
			JugadorResponseDTO perfil,
			String familyId,
			Instant familyExpiresAt,
			Instant now) {
		RefreshToken refreshToken = crearRefreshToken(jugador, familyId, familyExpiresAt, now);
		return crearSesionConRefreshToken(jugador, perfil, refreshToken);
	}

	private SesionAutenticada crearSesionConRefreshToken(
			Jugador jugador,
			JugadorResponseDTO perfil,
			RefreshToken refreshToken) {
		String rawToken = crearRawToken();
		refreshToken.setTokenHash(hashToken(rawToken));
		refreshTokenRepository.save(refreshToken);

		LoginResponseDTO response = new LoginResponseDTO(
				jwtService.generarToken(jugador),
				"Bearer",
				jwtService.getExpirationSeconds(),
				perfil);
		return new SesionAutenticada(response, rawToken);
	}

	private RefreshToken crearRefreshToken(
			Jugador jugador,
			String familyId,
			Instant familyExpiresAt,
			Instant now) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setJugador(jugador);
		refreshToken.setFamilyId(familyId);
		Instant idleExpiresAt = now.plusSeconds(refreshIdleSeconds);
		refreshToken.setExpiresAt(idleExpiresAt.isBefore(familyExpiresAt)
				? idleExpiresAt
				: familyExpiresAt);
		refreshToken.setFamilyExpiresAt(familyExpiresAt);
		return refreshToken;
	}

	private String crearRawToken() {
		byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTES];
		SECURE_RANDOM.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}

	private String hashToken(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("No está disponible el algoritmo SHA-256 requerido.", exception);
		}
	}
}
