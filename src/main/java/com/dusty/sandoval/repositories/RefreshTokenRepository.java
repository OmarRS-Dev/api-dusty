package com.dusty.sandoval.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dusty.sandoval.entities.RefreshToken;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
	Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

	@Modifying
	@Query("""
			update RefreshToken token
			set token.revokedAt = :revokedAt
			where token.familyId = :familyId and token.revokedAt is null
			""")
	int revokeActiveByFamilyId(@Param("familyId") String familyId, @Param("revokedAt") Instant revokedAt);

	@Modifying
	@Query("""
			update RefreshToken token
			set token.revokedAt = :revokedAt
			where token.jugador.id = :jugadorId and token.revokedAt is null
			""")
	int revokeAllActiveByJugadorId(@Param("jugadorId") Long jugadorId, @Param("revokedAt") Instant revokedAt);

	@Modifying
	@Query("delete from RefreshToken token where token.familyExpiresAt < :now")
	int deleteExpiredFamilies(@Param("now") Instant now);
}
