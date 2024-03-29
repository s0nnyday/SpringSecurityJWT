package com.example.securityjwt.controller;

import com.example.securityjwt.dto.JoinDTO;
import com.example.securityjwt.service.JoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {

        this.joinService = joinService;
    }

    @PostMapping("/join")
    public String joinProcess(@RequestBody JoinDTO joinDTO) {

        log.info("[Slf4j]username: "+joinDTO.getUsername());
        log.info("[Slf4j]password: "+joinDTO.getPassword());
        joinService.joinProcess(joinDTO);

        return "ok";
    }
}