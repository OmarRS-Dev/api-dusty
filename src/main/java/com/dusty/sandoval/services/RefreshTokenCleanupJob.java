package com.dusty.sandoval.services;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dusty.sandoval.repositories.RefreshTokenRepository;

@Component
public class RefreshTokenCleanupJob {

	private final RefreshTokenRepository refreshTokenRepository;

	public RefreshTokenCleanupJob(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Scheduled(fixedDelayString = "86400000")
	@Transactional
	public void eliminarSesionesExpiradas() {
		refreshTokenRepository.deleteExpiredFamilies(Instant.now());
	}
}
