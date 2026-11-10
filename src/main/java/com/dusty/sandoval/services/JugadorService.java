package com.dusty.sandoval.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dusty.sandoval.dtos.JugadorCreateDTO;
import com.dusty.sandoval.dtos.JugadorResponseDTO;

public interface JugadorService {

	JugadorResponseDTO registrar(JugadorCreateDTO jugadorCreateDTO);

	Page<JugadorResponseDTO> listarRanking(Pageable pageable);

	JugadorResponseDTO obtenerPorId(Long jugadorId);

	JugadorResponseDTO cambiarAlias(Long jugadorId, String nuevoAlias);

	JugadorResponseDTO cambiarContrasena(Long jugadorId, String nuevaContrasena);
}
