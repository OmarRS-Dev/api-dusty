package com.dusty.sandoval.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jugadores")
@Getter
@Setter
@NoArgsConstructor
public class Jugador {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "alias", nullable = false, length = 80, unique = true)
	private String alias;

	@Column(name = "password", nullable = false, length = 255)
	private String password;

	@Column(name = "fecha_registro", nullable = false)
	private LocalDateTime fechaRegistro;

	@Column(name = "record")
	private Long record;

	@OneToMany(mappedBy = "jugador", fetch = FetchType.LAZY)
	private List<Partida> partidas;
}
