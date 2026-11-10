package com.dusty.sandoval.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dusty.sandoval.entities.Jugador;

import jakarta.persistence.LockModeType;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {

	boolean existsByAlias(String alias);

	boolean existsByAliasAndIdNot(String alias, Long id);

	Optional<Jugador> findByAlias(String alias);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select jugador from Jugador jugador where jugador.id = :jugadorId")
	Optional<Jugador> findByIdForUpdate(@Param("jugadorId") Long jugadorId);

	@Query(value = """
			select jugador from Jugador jugador
			order by case when jugador.record is null then 1 else 0 end,
				jugador.record desc, jugador.id asc
			""",
			countQuery = "select count(jugador) from Jugador jugador")
	Page<Jugador> findRanking(Pageable pageable);
}
