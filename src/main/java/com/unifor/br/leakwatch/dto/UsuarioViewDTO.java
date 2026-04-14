
package com.unifor.br.leakwatch.dto;

public record UsuarioViewDTO(
        Long id,
        String nome,
        String email,
        String role
) {}
