package com.soma.kg.service;

import com.soma.kg.dto.DomainDto;
import com.soma.kg.entity.Domain;
import com.soma.kg.repository.DomainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DomainService {

    private final DomainRepository domainRepository;

    public DomainService(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    public DomainDto.Response create(DomainDto.CreateRequest req) {
        if (domainRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 도메인: " + req.name());
        }
        Domain domain = domainRepository.save(new Domain(req.name(), req.description()));
        return DomainDto.Response.from(domain);
    }

    public List<DomainDto.Response> findAll() {
        return domainRepository.findAll().stream()
                .map(DomainDto.Response::from)
                .toList();
    }

    public Domain getById(Long id) {
        return domainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("도메인 없음: " + id));
    }
}
