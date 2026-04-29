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

    public User createUser(String username, String rawPassword, Role role, String telefone) { // <-- ALTERADO

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(role);
        u.setActive(true);
        u.setTelefone(telefone); // <-- ADICIONADO

        return repo.save(u);
    }

    public List<User> listAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}