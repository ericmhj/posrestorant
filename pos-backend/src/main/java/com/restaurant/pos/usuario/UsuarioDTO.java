package com.restaurant.pos.usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public class UsuarioDTO {

    public UUID id;
    public String nombre;
    public String apellido;
    public String username;
    public String rol;
    public Boolean activo;
    public LocalDateTime createdAt;

    public static UsuarioDTO from(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.id = u.id;
        dto.nombre = u.nombre;
        dto.apellido = u.apellido;
        dto.username = u.username;
        dto.rol = u.rol != null ? u.rol.name() : null;
        dto.activo = u.activo;
        dto.createdAt = u.createdAt;
        return dto;
    }
}
