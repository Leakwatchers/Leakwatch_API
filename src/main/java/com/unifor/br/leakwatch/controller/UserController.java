package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Role;
import com.unifor.br.leakwatch.model.User;
import com.unifor.br.leakwatch.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService svc;

    public UserController(UserService svc) {
        this.svc = svc;
    }

    public static record CreateUserReq(String username, String password, String role) {}

    // MASTER cria usuário
    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public User createUser(@RequestBody CreateUserReq req) {
        Role role = Role.valueOf("ROLE_" + req.role().toUpperCase());  // MASTER / VIEW
        return svc.createUser(req.username(), req.password(), role);
    }

    // MASTER lista usuários
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public List<User> listUsers() {
        return svc.listAll();
    }

    // MASTER remove
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public void deleteUser(@PathVariable Long id) {
        svc.delete(id);
    }
}
