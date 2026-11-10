package com.dusty.sandoval.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dusty.sandoval.dtos.PartidaResponseDTO;
import com.dusty.sandoval.entities.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

	@Query("""
			select new com.dusty.sandoval.dtos.PartidaResponseDTO(
				partida.id, jugador.alias, partida.puntuacion, partida.fecha)
			from Partida partida
			join partida.jugador jugador
			where jugador.id = :jugadorId
			order by partida.fecha desc, partida.id desc
			""")
	List<PartidaResponseDTO> findLatestByJugadorId(
			@Param("jugadorId") Long jugadorId,
			Pageable pageable);
}
