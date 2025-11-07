package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Usuario;
import com.unifor.br.leakwatch.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    private final UsuarioRepository usuarioRepository;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.get("usuario"); // front envia campo "usuario"
        String senha = credenciais.get("senha");

        var usuario = usuarioRepository.findByEmailAndSenha(email, senha);

        if (usuario.isPresent()) {
            return Map.of(
                    "token", "fake-token-" + usuario.get().getId(),
                    "nome", usuario.get().getNome(),
                    "role", usuario.get().getRole()
            );
        } else {
            throw new RuntimeException("Usuário ou senha inválidos");
        }
    }
}
