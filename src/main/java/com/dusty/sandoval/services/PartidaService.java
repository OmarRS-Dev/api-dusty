package com.dusty.sandoval.services;

import java.util.List;

import com.dusty.sandoval.dtos.PartidaCreateDTO;
import com.dusty.sandoval.dtos.PartidaGuardadaDTO;
import com.dusty.sandoval.dtos.PartidaResponseDTO;

public interface PartidaService {

	PartidaGuardadaDTO guardar(Long jugadorId, PartidaCreateDTO request);

	List<PartidaResponseDTO> obtenerUltimasDiez(Long jugadorId);
}
