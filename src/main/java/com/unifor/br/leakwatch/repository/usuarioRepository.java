package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que é um componente de acesso a dados
public interface usuarioRepository extends JpaRepository<usuario, Long> {
}
