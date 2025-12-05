
package com.unifor.br.leakwatch.dto;

/**
 * DTO de resposta: não contém o campo senha (nem hash).
 */
public record UsuarioViewDTO(
        Long id,
        String nome,
        String email,
        String role
) {}
