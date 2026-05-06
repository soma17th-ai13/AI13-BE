package com.soma.kg.controller;

import com.soma.kg.dto.DebateDto;
import com.soma.kg.service.DebateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debate")
public class DebateController {

    private final DebateService debateService;

    public DebateController(DebateService debateService) {
        this.debateService = debateService;
    }

    @PostMapping
    public DebateDto.Response startDebate() {
        return new DebateDto.Response(debateService.runDebate());
    }
}
