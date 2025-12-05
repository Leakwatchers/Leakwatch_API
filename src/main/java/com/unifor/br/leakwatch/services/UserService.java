package com.unifor.br.leakwatch.services;

import com.unifor.br.leakwatch.model.Role;
import com.unifor.br.leakwatch.model.User;
import com.unifor.br.leakwatch.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // Criar usuário (MASTER)
    public User createUser(String username, String rawPassword, Role role) {

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));  // <-- SENHA CORRETA!
        u.setRole(role);
        u.setActive(true);

        return repo.save(u);
    }

    // Listar todos (MASTER)
    public List<User> listAll() {
        return repo.findAll();
    }

    // Remover usuário (MASTER)
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
