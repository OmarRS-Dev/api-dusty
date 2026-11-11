package com.dusty.sandoval.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origin-patterns:*}") String allowedOriginPatterns) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.stream(allowedOriginPatterns.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> {
				})
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(
								HttpMethod.POST,
								"/api/jugadores",
								"/api/auth/login",
								"/api/auth/refresh",
								"/api/auth/logout").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> {
				}))
				.build();
	}
}
