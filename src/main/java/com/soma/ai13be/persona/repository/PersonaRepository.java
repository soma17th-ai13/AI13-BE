package com.soma.ai13be.persona.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soma.ai13be.persona.entity.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

	Optional<Persona> findByDomainName(String domainName);

	boolean existsByDomainName(String domainName);
}
