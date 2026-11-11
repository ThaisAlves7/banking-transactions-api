package com.desafio.banking_transactions_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UsuarioController {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> validarConexao(){
        return Map.of(
                "status", "UP",
                "application", "banking-transactions-api"
        );
    }
}
