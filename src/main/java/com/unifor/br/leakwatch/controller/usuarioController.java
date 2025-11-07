package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Usuario;
import com.unifor.br.leakwatch.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // Importação chave
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios") // Usando 'usuarios' conforme a correção
public class usuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario novoUsuario) {

        // 1. Criptografa a senha recebida
        String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());

        // 2. Define a senha criptografada na entidade antes de salvar
        novoUsuario.setSenha(senhaCriptografada);

        // 3. Salva a entidade no banco
        Usuario usuarioSalvo = repository.save(novoUsuario);

        // Retorna o objeto salvo (que agora tem a senha criptografada)
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }

    // Método para LISTAR (apenas para teste)
    @GetMapping
    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

}