
package com.unifor.br.leakwatch.dto;

/**
 * DTO de criação/edição de usuário.
 * Contém senha em texto puro (apenas entrada), que será criptografada.
 */
public record UsuarioCreateDTO(
        String nome,
        String email,
        String senha,
        String role
) {}
