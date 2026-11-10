package com.dusty.sandoval.services.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dusty.sandoval.dtos.PartidaCreateDTO;
import com.dusty.sandoval.dtos.PartidaGuardadaDTO;
import com.dusty.sandoval.dtos.PartidaResponseDTO;
import com.dusty.sandoval.entities.Jugador;
import com.dusty.sandoval.entities.Partida;
import com.dusty.sandoval.exceptions.JugadorNoEncontradoException;
import com.dusty.sandoval.repositories.JugadorRepository;
import com.dusty.sandoval.repositories.PartidaRepository;
import com.dusty.sandoval.services.PartidaService;

@Service
public class PartidaServiceImpl implements PartidaService {

	private static final ZoneId MAZATLAN_ZONE = ZoneId.of("America/Mazatlan");
	private static final int ULTIMAS_PARTIDAS_LIMITE = 10;

	private final JugadorRepository jugadorRepository;
	private final PartidaRepository partidaRepository;

	public PartidaServiceImpl(JugadorRepository jugadorRepository, PartidaRepository partidaRepository) {
		this.jugadorRepository = jugadorRepository;
		this.partidaRepository = partidaRepository;
	}

	@Override
	@Transactional
	public PartidaGuardadaDTO guardar(Long jugadorId, PartidaCreateDTO request) {
		Jugador jugador = jugadorRepository.findByIdForUpdate(jugadorId)
				.orElseThrow(() -> new JugadorNoEncontradoException(jugadorId));
		Long recordAnterior = jugador.getRecord();
		boolean nuevoRecord = recordAnterior == null || request.puntuacion() > recordAnterior;

		Partida partida = new Partida();
		partida.setJugador(jugador);
		partida.setPuntuacion(request.puntuacion());
		partida.setFecha(LocalDateTime.now(MAZATLAN_ZONE));
		Partida partidaGuardada = partidaRepository.save(partida);

		if (nuevoRecord) {
			jugador.setRecord(request.puntuacion());
		}

		PartidaResponseDTO response = new PartidaResponseDTO(
				partidaGuardada.getId(),
				jugador.getAlias(),
				partidaGuardada.getPuntuacion(),
				partidaGuardada.getFecha());
		return new PartidaGuardadaDTO(response, nuevoRecord, recordAnterior, jugador.getRecord());
	}

	@Override
	@Transactional(readOnly = true)
	public List<PartidaResponseDTO> obtenerUltimasDiez(Long jugadorId) {
		if (!jugadorRepository.existsById(jugadorId)) {
			throw new JugadorNoEncontradoException(jugadorId);
		}
		return partidaRepository.findLatestByJugadorId(jugadorId, PageRequest.of(0, ULTIMAS_PARTIDAS_LIMITE));
	}
}
