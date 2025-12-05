package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    public static record LoginReq(String username, String password) {}
    public static record LoginResp(String token) {}

    @PostMapping("/login")
    public LoginResp login(@RequestBody LoginReq req) {
        var token = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        authManager.authenticate(token);
        String jwt = jwtUtil.generateToken(req.username());
        return new LoginResp(jwt);
    }
}
