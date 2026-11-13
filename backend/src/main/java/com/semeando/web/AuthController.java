package com.semeando.web;

import com.semeando.model.Usuario;
import com.semeando.service.AuthService;
import com.semeando.service.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService auth, LoginRateLimiter rateLimiter) {
        this.auth = auth;
        this.rateLimiter = rateLimiter;
    }

    public record LoginForm(String email, String senha) {}

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginForm form, HttpServletRequest req) {
        String ip = clientIp(req);
        rateLimiter.verificar(ip);
        try {
            String token = auth.login(form.email(), form.senha());
            rateLimiter.sucesso(ip);
            return Map.of("token", token, "usuario", usuarioSemSenha(auth.usuarioPorToken(token)));
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) rateLimiter.falhou(ip);
            throw e;
        }
    }

    /** IP real do cliente atrás do Cloudflare/Traefik (cabeçalhos de proxy). */
    private static String clientIp(HttpServletRequest req) {
        String cf = req.getHeader("CF-Connecting-IP");
        if (cf != null && !cf.isBlank()) return cf.trim();
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest req) {
        return usuarioSemSenha(auth.usuario(req));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest req) {
        auth.logout(req);
    }

    public record SenhaForm(String senhaAtual, String novaSenha) {}

    @PostMapping("/senha")
    public void trocarSenha(@RequestBody SenhaForm form, HttpServletRequest req) {
        auth.trocarSenha(req, form.senhaAtual(), form.novaSenha());
    }

    private Map<String, Object> usuarioSemSenha(Usuario u) {
        return Map.of("id", u.getId(), "nome", u.getNome(), "email", u.getEmail(), "perfil", u.getPerfil());
    }
}
