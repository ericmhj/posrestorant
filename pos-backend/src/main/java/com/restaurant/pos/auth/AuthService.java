package com.restaurant.pos.auth;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.usuario.AuditoriaAcceso;
import com.restaurant.pos.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    SessionStore sessionStore;

    @ConfigProperty(name = "pos.jwt.expiration-hours", defaultValue = "8")
    int expirationHours;

    @Transactional
    public AuthLoginResponse login(AuthLoginRequest request, String ipAddress) {
        String username = request.username();
        String password = request.password();
        Usuario usuario = Usuario.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "Credenciales inválidas"));

        // Check if account is locked
        if (usuario.bloqueadoHasta != null && LocalDateTime.now().isBefore(usuario.bloqueadoHasta)) {
            registrarAuditoria(usuario, "LOGIN", "BLOQUEADO");
            throw new BusinessException(401, "Cuenta bloqueada temporalmente. Intente en " + LOCKOUT_MINUTES + " minutos.");
        }

        // Check if account is active
        if (!Boolean.TRUE.equals(usuario.activo)) {
            registrarAuditoria(usuario, "LOGIN", "INACTIVO");
            throw new BusinessException(401, "Cuenta desactivada");
        }

        // Verify password
        if (!passwordHasher.verify(password, usuario.passwordHash)) {
            usuario.intentosFallidos = (usuario.intentosFallidos == null ? 0 : usuario.intentosFallidos) + 1;
            if (usuario.intentosFallidos >= MAX_FAILED_ATTEMPTS) {
                usuario.bloqueadoHasta = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
                LOG.warnf("Account locked for user=%s after %d failed attempts", username, MAX_FAILED_ATTEMPTS);
            }
            registrarAuditoria(usuario, "LOGIN", "FALLIDO");
            throw new BusinessException(401, "Credenciales inválidas");
        }

        // Success — reset failed attempts
        usuario.intentosFallidos = 0;
        usuario.bloqueadoHasta = null;

        // Generate token (Base64 encoded UUID)
        String token = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        Instant expiresAt = Instant.now().plusSeconds(expirationHours * 3600L);
        sessionStore.register(token, usuario.id);

        registrarAuditoria(usuario, "LOGIN", "EXITOSO");
        LOG.infof("User logged in: username=%s role=%s", username, usuario.rol);

        return new AuthLoginResponse(
                token,
                new AuthLoginResponse.UsuarioInfo(usuario.id, usuario.nombre, usuario.apellido, usuario.rol.name()),
                expiresAt
        );
    }

    @Transactional
    public void logout(String token) {
        sessionStore.invalidate(token);
        LOG.infof("User logged out via token");
    }

    @Transactional
    public void logout(String token, UUID userId) {
        sessionStore.invalidate(token);
        Usuario usuario = Usuario.findById(userId).orElse(null);
        if (usuario != null) {
            registrarAuditoria(usuario, "LOGOUT", "EXITOSO");
        }
        LOG.infof("User logged out: userId=%s", userId);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        Usuario usuario = Usuario.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "Usuario no encontrado"));

        if (!passwordHasher.verify(request.currentPassword(), usuario.passwordHash)) {
            throw new com.restaurant.pos.common.ValidationException("currentPassword", "La contraseña actual es incorrecta");
        }

        usuario.passwordHash = passwordHasher.hash(request.newPassword());
        LOG.infof("Password changed for userId=%s", userId);
    }

    private void registrarAuditoria(Usuario usuario, String accion, String resultado) {
        AuditoriaAcceso auditoria = new AuditoriaAcceso();
        auditoria.usuario = usuario;
        auditoria.accion = accion;
        auditoria.resultado = resultado;
        auditoria.fechaHora = LocalDateTime.now();
        auditoria.persist();
    }
}
