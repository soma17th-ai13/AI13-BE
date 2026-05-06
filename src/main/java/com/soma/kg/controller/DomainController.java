package com.soma.kg.controller;

import com.soma.kg.dto.DomainDto;
import com.soma.kg.dto.KnowledgeDto;
import com.soma.kg.entity.Domain;
import com.soma.kg.service.DomainService;
import com.soma.kg.service.KnowledgeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domains")
public class DomainController {

    private final DomainService domainService;
    private final KnowledgeService knowledgeService;

    public DomainController(DomainService domainService, KnowledgeService knowledgeService) {
        this.domainService = domainService;
        this.knowledgeService = knowledgeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DomainDto.Response createDomain(@RequestBody DomainDto.CreateRequest req) {
        return domainService.create(req);
    }

    @GetMapping
    public List<DomainDto.Response> listDomains() {
        return domainService.findAll();
    }

    @PostMapping("/{id}/knowledge")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDto.Response addKnowledge(@PathVariable Long id,
                                               @RequestBody KnowledgeDto.AddRequest req) {
        Domain domain = domainService.getById(id);
        return knowledgeService.addKnowledge(domain, req.rawInput());
    }

    @GetMapping("/{id}/knowledge")
    public List<KnowledgeDto.Response> listKnowledge(@PathVariable Long id) {
        return knowledgeService.findByDomain(id);
    }

    @PostMapping("/{id}/chat")
    public KnowledgeDto.ChatResponse chat(@PathVariable Long id,
                                          @RequestBody KnowledgeDto.ChatRequest req) {
        Domain domain = domainService.getById(id);
        return knowledgeService.chat(domain, req.question());
    }
}
