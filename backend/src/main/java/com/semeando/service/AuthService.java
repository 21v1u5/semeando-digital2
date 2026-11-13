package com.semeando.service;

import com.semeando.model.Perfil;
import com.semeando.model.Usuario;
import com.semeando.repo.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

/** Autenticação stateless por JWT assinado (HS256). O token carrega o id e o perfil
 *  do usuário e expira sozinho — não há estado de sessão no servidor. */
@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecretKey chave;
    private final long expiracaoMs;

    public AuthService(UsuarioRepository usuarios,
                       @Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-hours:8}") long horas) {
        this.usuarios = usuarios;
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = horas * 3600_000L;
    }

    public String hash(String senha) {
        return encoder.encode(senha);
    }

    public String login(String email, String senha) {
        Usuario u = usuarios.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .filter(x -> senha != null && x.getSenha() != null && encoder.matches(senha, x.getSenha()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));
        return emitirToken(u);
    }

    /** Emite um JWT para um usuário já autenticado (login por senha ou outro meio). */
    public String emitirToken(Usuario u) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("perfil", u.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(expiracaoMs)))
                .signWith(chave)
                .compact();
    }

    /** Stateless: o logout é feito no cliente descartando o token; ele expira sozinho. */
    public void logout(HttpServletRequest req) {
        // sem estado de sessão no servidor
    }

    public Usuario usuario(HttpServletRequest req) {
        Long id = idDoToken(token(req));
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));
    }

    public Usuario usuarioPorToken(String token) {
        return usuarios.findById(idDoToken(token)).orElseThrow();
    }

    /** RF36 / RNF01 - controle de acesso por perfil. */
    public Usuario exigir(HttpServletRequest req, Perfil... perfis) {
        Usuario u = usuario(req);
        if (Arrays.stream(perfis).noneMatch(p -> p == u.getPerfil()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso não permitido para o seu perfil");
        return u;
    }

    @Transactional
    public void trocarSenha(HttpServletRequest req, String senhaAtual, String novaSenha) {
        Usuario u = usuario(req);
        if (senhaAtual == null || u.getSenha() == null || !encoder.matches(senhaAtual, u.getSenha()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        if (novaSenha == null || novaSenha.trim().length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nova senha deve ter ao menos 6 caracteres");
        u.setSenha(encoder.encode(novaSenha.trim()));
        usuarios.save(u);
    }

    private Long idDoToken(String token) {
        if (token == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada, faça login novamente");
        try {
            Claims c = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Long.valueOf(c.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada, faça login novamente");
        }
    }

    private String token(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return h != null && h.startsWith("Bearer ") ? h.substring(7) : null;
    }
}
