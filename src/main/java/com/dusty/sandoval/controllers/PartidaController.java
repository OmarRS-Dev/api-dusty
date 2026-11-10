package com.dusty.sandoval.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dusty.sandoval.dtos.PartidaCreateDTO;
import com.dusty.sandoval.dtos.PartidaGuardadaDTO;
import com.dusty.sandoval.services.PartidaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

	private final PartidaService partidaService;

	public PartidaController(PartidaService partidaService) {
		this.partidaService = partidaService;
	}

	@PostMapping
	public ResponseEntity<PartidaGuardadaDTO> guardar(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody PartidaCreateDTO request) {
		Long jugadorId = Long.valueOf(jwt.getSubject());
		return ResponseEntity.ok(partidaService.guardar(jugadorId, request));
	}
}
