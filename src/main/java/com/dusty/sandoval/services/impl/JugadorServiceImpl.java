package com.dusty.sandoval.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dusty.sandoval.dtos.JugadorCreateDTO;
import com.dusty.sandoval.dtos.JugadorResponseDTO;
import com.dusty.sandoval.entities.Jugador;
import com.dusty.sandoval.exceptions.AliasIgualAlActualException;
import com.dusty.sandoval.exceptions.AliasYaEnUsoException;
import com.dusty.sandoval.exceptions.ContrasenaReutilizadaException;
import com.dusty.sandoval.exceptions.DatosJugadorInvalidosException;
import com.dusty.sandoval.exceptions.JugadorNoEncontradoException;
import com.dusty.sandoval.repositories.JugadorRepository;
import com.dusty.sandoval.repositories.RefreshTokenRepository;
import com.dusty.sandoval.services.JugadorService;

@Service
public class JugadorServiceImpl implements JugadorService {

	private static final ZoneId MAZATLAN_ZONE = ZoneId.of("America/Mazatlan");
	private static final int ALIAS_MAX_LENGTH = 80;
	private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

	private final JugadorRepository jugadorRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;

	public JugadorServiceImpl(
			JugadorRepository jugadorRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder) {
		this.jugadorRepository = jugadorRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public JugadorResponseDTO registrar(JugadorCreateDTO jugadorCreateDTO) {
		validarLongitudBcrypt(jugadorCreateDTO.password());
		if (jugadorRepository.existsByAlias(jugadorCreateDTO.alias())) {
			throw new AliasYaEnUsoException();
		}

		Jugador jugador = new Jugador();
		jugador.setAlias(jugadorCreateDTO.alias());
		jugador.setPassword(passwordEncoder.encode(jugadorCreateDTO.password()));
		jugador.setFechaRegistro(LocalDateTime.now(MAZATLAN_ZONE));

		return toResponse(jugadorRepository.save(jugador));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<JugadorResponseDTO> listarRanking(Pageable pageable) {
		return jugadorRepository.findRanking(pageable).map(this::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public JugadorResponseDTO obtenerPorId(Long jugadorId) {
		validarIdentificador(jugadorId);
		return toResponse(obtenerJugador(jugadorId));
	}

	@Override
	@Transactional
	public JugadorResponseDTO cambiarAlias(Long jugadorId, String nuevoAlias) {
		validarIdentificador(jugadorId);
		validarAlias(nuevoAlias);

		Jugador jugador = obtenerJugador(jugadorId);
		if (jugador.getAlias().equals(nuevoAlias)) {
			throw new AliasIgualAlActualException();
		}
		
		if (jugadorRepository.existsByAliasAndIdNot(nuevoAlias, jugadorId)) {
			throw new AliasYaEnUsoException();
		}

		jugador.setAlias(nuevoAlias);
		return toResponse(jugador);
	}

	@Override
	@Transactional
	public JugadorResponseDTO cambiarContrasena(Long jugadorId, String nuevaContrasena) {
		validarIdentificador(jugadorId);
		validarContrasena(nuevaContrasena);

		Jugador jugador = obtenerJugador(jugadorId);
		if (passwordEncoder.matches(nuevaContrasena, jugador.getPassword())) {
			throw new ContrasenaReutilizadaException();
		}

		jugador.setPassword(passwordEncoder.encode(nuevaContrasena));
		refreshTokenRepository.revokeAllActiveByJugadorId(jugadorId, Instant.now());
		return toResponse(jugador);
	}

	private Jugador obtenerJugador(Long jugadorId) {
		return jugadorRepository.findById(jugadorId)
				.orElseThrow(() -> new JugadorNoEncontradoException(jugadorId));
	}

	private void validarIdentificador(Long jugadorId) {
		if (jugadorId == null || jugadorId <= 0) {
			throw new DatosJugadorInvalidosException("El identificador del jugador debe ser mayor que cero.");
		}
	}

	private void validarAlias(String alias) {
		if (alias == null || alias.isBlank()) {
			throw new DatosJugadorInvalidosException("El alias es obligatorio y no puede estar vacío.");
		}
		if (alias.length() > ALIAS_MAX_LENGTH) {
			throw new DatosJugadorInvalidosException("El alias no puede superar los 80 caracteres.");
		}
	}

	private void validarContrasena(String contrasena) {
		if (contrasena == null || contrasena.isBlank()) {
			throw new DatosJugadorInvalidosException("La contraseña es obligatoria y no puede estar vacía.");
		}
		validarLongitudBcrypt(contrasena);
	}

	private void validarLongitudBcrypt(String contrasena) {
		if (contrasena.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
			throw new DatosJugadorInvalidosException(
					"La contraseña no puede superar los 72 bytes en UTF-8 por el límite de BCrypt.");
		}
	}

	private JugadorResponseDTO toResponse(Jugador jugador) {
		return new JugadorResponseDTO(
				jugador.getId(),
				jugador.getAlias(),
				jugador.getRecord(),
				jugador.getFechaRegistro());
	}
}
