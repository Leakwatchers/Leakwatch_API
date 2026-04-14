
package com.unifor.br.leakwatch.dto;

public record UsuarioCreateDTO(
        String nome,
        String email,
        String senha,
        String role
) {}
