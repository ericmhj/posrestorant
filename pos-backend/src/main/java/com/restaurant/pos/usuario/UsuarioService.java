package com.restaurant.pos.usuario;

import com.restaurant.pos.auth.PasswordHasher;
import com.restaurant.pos.auth.SessionStore;
import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.common.PaginationParams;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UsuarioService {

    private static final Logger LOG = Logger.getLogger(UsuarioService.class);

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    SessionStore sessionStore;

    public List<UsuarioDTO> findAll(PaginationParams pagination) {
        return Usuario.findAll()
                .page(pagination.getPage(), pagination.getSize())
                .stream()
                .map(u -> UsuarioDTO.from((Usuario) u))
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioDTO create(CreateUsuarioRequest request) {
        // Validate unique username
        if (Usuario.findByUsername(request.username).isPresent()) {
            throw new BusinessException(409, "El username '" + request.username + "' ya está en uso");
        }

        Usuario usuario = new Usuario();
        usuario.nombre = request.nombre;
        usuario.apellido = request.apellido;
        usuario.username = request.username;
        usuario.passwordHash = passwordHasher.hash(request.password);
        usuario.rol = request.rol;
        usuario.activo = true;
        usuario.intentosFallidos = 0;
        usuario.persist();

        LOG.infof("Usuario created: username=%s rol=%s", usuario.username, usuario.rol);
        return UsuarioDTO.from(usuario);
    }

    @Transactional
    public UsuarioDTO update(UUID id, UpdateUsuarioRequest request) {
        Usuario usuario = Usuario.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Usuario no encontrado: " + id));

        if (request.nombre != null) usuario.nombre = request.nombre;
        if (request.apellido != null) usuario.apellido = request.apellido;
        if (request.rol != null) usuario.rol = request.rol;

        LOG.infof("Usuario updated: id=%s", id);
        return UsuarioDTO.from(usuario);
    }

    @Transactional
    public void deactivate(UUID id) {
        Usuario usuario = Usuario.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Usuario no encontrado: " + id));

        usuario.activo = false;
        // Invalidate all active sessions for this user
        sessionStore.invalidateAllForUser(id);

        LOG.infof("Usuario deactivated: id=%s username=%s", id, usuario.username);
    }

    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        Usuario usuario = Usuario.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Usuario no encontrado: " + id));

        usuario.passwordHash = passwordHasher.hash(newPassword);
        usuario.intentosFallidos = 0;
        usuario.bloqueadoHasta = null;

        LOG.infof("Password reset for userId=%s", id);
    }
}
