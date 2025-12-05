package com.unifor.br.leakwatch;

import com.unifor.br.leakwatch.model.Role;
import com.unifor.br.leakwatch.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapRunner implements CommandLineRunner {

    private final UserService userService;

    public BootstrapRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userService.listAll().isEmpty()) {
            userService.createUser("admin", "admin123", Role.ROLE_MASTER);
            System.out.println("✔ Usuário ADMIN criado:");
            System.out.println("   usuário: admin");
            System.out.println("   senha: admin123");
        }
    }
}
