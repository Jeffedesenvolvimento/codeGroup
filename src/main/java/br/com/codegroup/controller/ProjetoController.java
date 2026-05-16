package br.com.codegroup.controller;

import br.com.codegroup.dto.resquest.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjetoController {

   @Autowired
   private AuthenticationManager authenticationManager;

    @GetMapping("/teste")
    public String teste() {
       return "ok";
    }
}
