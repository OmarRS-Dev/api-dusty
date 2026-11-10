package com.dusty.sandoval.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dusty.sandoval.dtos.JugadorCambiarAliasDTO;
import com.dusty.sandoval.dtos.JugadorCambiarContrasenaDTO;
import com.dusty.sandoval.dtos.JugadorCreateDTO;
import com.dusty.sandoval.dtos.JugadorResponseDTO;
import com.dusty.sandoval.dtos.PartidaResponseDTO;
import com.dusty.sandoval.exceptions.DatosJugadorInvalidosException;
import com.dusty.sandoval.services.JugadorService;
import com.dusty.sandoval.services.PartidaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

	private final JugadorService jugadorService;
	private final PartidaService partidaService;

	public JugadorController(JugadorService jugadorService, PartidaService partidaService) {
		this.jugadorService = jugadorService;
		this.partidaService = partidaService;
	}

	@PostMapping
	public ResponseEntity<JugadorResponseDTO> registrar(@Valid @RequestBody JugadorCreateDTO jugadorCreateDTO) {
		JugadorResponseDTO jugador = jugadorService.registrar(jugadorCreateDTO);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(jugador.id())
				.toUri();

		return ResponseEntity.created(location).body(jugador);
	}

	@GetMapping
	public ResponseEntity<Page<JugadorResponseDTO>> listarRanking(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1) {
			throw new DatosJugadorInvalidosException("La página debe ser mayor o igual a cero y el tamaño mayor que cero.");
		}

		return ResponseEntity.ok(jugadorService.listarRanking(PageRequest.of(page, Math.min(size, 50))));
	}

	@GetMapping("/{id}")
	public ResponseEntity<JugadorResponseDTO> obtenerPorId(@PathVariable Long id) {
		return ResponseEntity.ok(jugadorService.obtenerPorId(id));
	}

	@GetMapping("/{id}/partidas")
	public ResponseEntity<List<PartidaResponseDTO>> obtenerUltimasPartidas(@PathVariable Long id) {
		return ResponseEntity.ok(partidaService.obtenerUltimasDiez(id));
	}

	@PatchMapping("/{id}/alias")
	public ResponseEntity<JugadorResponseDTO> cambiarAlias(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody JugadorCambiarAliasDTO request) {
		validarPropietario(id, jwt);
		return ResponseEntity.ok(jugadorService.cambiarAlias(id, request.alias()));
	}

	@PatchMapping("/{id}/contrasena")
	public ResponseEntity<JugadorResponseDTO> cambiarContrasena(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody JugadorCambiarContrasenaDTO request) {
		validarPropietario(id, jwt);
		return ResponseEntity.ok(jugadorService.cambiarContrasena(id, request.contrasena()));
	}

	private void validarPropietario(Long jugadorId, Jwt jwt) {
		if (!jugadorId.toString().equals(jwt.getSubject())) {
			throw new AccessDeniedException("Solo puedes acceder a tu propia cuenta.");
		}
	}
}
