package com.unifor.br.leakwatch.model;

import jakarta.persistence.*;
import lombok.Data; // Assumindo que você usa Lombok

@Data
@Entity
@Table(name = "usuario")
public class usuario {

    @Id // Define o campo como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coluna para o nome do usuário
    @Column(nullable = false) // Garante que o campo não pode ser nulo
    private String nome;

    // Coluna para a senha. Em produção, use sempre Hash!
    // Mapeia o campo "senha"
    @Column(nullable = false)
    private String senha;
}