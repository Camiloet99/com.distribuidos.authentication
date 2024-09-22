package com.distribuidos.authentication.controllers;

import com.distribuidos.authentication.models.LoginRequest;
import com.distribuidos.authentication.models.ResponseBody;
import com.distribuidos.authentication.services.facades.users.models.UserEntity;
import com.distribuidos.authentication.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    
    private final AuthService service;
    
    @PostMapping("/register")
    public Mono<ResponseEntity<ResponseBody<Boolean>>> register(@RequestBody UserEntity user) {
        
        // implement validations

        return service.authRegisterUser(user)
                .map(ControllerUtils::created);
    }
    
    @PostMapping("/login")
    public Mono<ResponseEntity<ResponseBody<UserEntity>>> login(@RequestBody LoginRequest loginRequest) {
        
        // implement validations
        
        return service.authLoginUser(loginRequest)
                .map(ControllerUtils::ok);
    }
    
}
