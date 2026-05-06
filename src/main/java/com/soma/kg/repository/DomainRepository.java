package com.soma.kg.repository;

import com.soma.kg.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findByName(String name);
    boolean existsByName(String name);
}
