package com.semeando.service;

import com.semeando.model.Perfil;
import com.semeando.model.Usuario;
import com.semeando.repo.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Autenticação por JWT: emissão, validação e falhas. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarios;
    AuthService auth;

    @BeforeEach
    void setup() {
        auth = new AuthService(usuarios, "segredo-de-teste-com-mais-de-32-bytes-aqui-ok", 8);
    }

    private Usuario gestor() {
        Usuario u = new Usuario("Coordenação", "gestor@exemplo.com", auth.hash("123456"), Perfil.GESTOR);
        u.setId(1L);
        return u;
    }

    @Test
    void loginValido_emiteTokenQueIdentificaOUsuario() {
        Usuario u = gestor();
        when(usuarios.findByEmailIgnoreCase("gestor@exemplo.com")).thenReturn(Optional.of(u));
        when(usuarios.findById(1L)).thenReturn(Optional.of(u));

        String token = auth.login("gestor@exemplo.com", "123456");

        assertThat(token).isNotBlank();
        assertThat(auth.usuarioPorToken(token).getEmail()).isEqualTo("gestor@exemplo.com");
    }

    @Test
    void senhaErrada_lanca401() {
        when(usuarios.findByEmailIgnoreCase("gestor@exemplo.com")).thenReturn(Optional.of(gestor()));
        assertThatThrownBy(() -> auth.login("gestor@exemplo.com", "errada"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void tokenInvalido_lanca401() {
        assertThatThrownBy(() -> auth.usuarioPorToken("isto-nao-e-um-jwt"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void tokenExpirado_lanca401() {
        // validade negativa -> já nasce expirado
        AuthService expira = new AuthService(usuarios, "segredo-de-teste-com-mais-de-32-bytes-aqui-ok", -1);
        String token = expira.emitirToken(gestor());
        assertThatThrownBy(() -> expira.usuarioPorToken(token))
                .isInstanceOf(ResponseStatusException.class);
    }
}
