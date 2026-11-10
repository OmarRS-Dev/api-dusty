package com.dusty.sandoval.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.dusty.sandoval.entities.Jugador;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final long expirationSeconds;

	public JwtService(
			JwtEncoder jwtEncoder,
			@Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
		this.jwtEncoder = jwtEncoder;
		this.expirationSeconds = expirationSeconds;
	}

	public String generarToken(Jugador jugador) {
		Instant issuedAt = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("sandoval-api")
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(expirationSeconds))
				.subject(jugador.getId().toString())
				.claim("alias", jugador.getAlias())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}
}
