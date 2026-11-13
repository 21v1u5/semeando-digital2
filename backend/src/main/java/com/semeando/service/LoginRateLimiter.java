package com.semeando.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador simples de tentativas de login (proteção a força bruta).
 * Bloqueia uma chave (ex.: IP) após MAX falhas dentro da JANELA de tempo.
 * Em memória — adequado a uma instância; para múltiplas, usar um store compartilhado.
 */
@Component
public class LoginRateLimiter {
    private static final int MAX = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();

    /** Lança 429 se a chave já estourou o limite na janela atual. */
    public void verificar(String chave) {
        Deque<Instant> q = falhas.get(chave);
        if (q == null) return;
        synchronized (q) {
            limpar(q);
            if (q.size() >= MAX)
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Muitas tentativas de login. Aguarde alguns minutos e tente de novo.");
        }
    }

    public void falhou(String chave) {
        Deque<Instant> q = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (q) {
            limpar(q);
            q.addLast(Instant.now());
        }
    }

    public void sucesso(String chave) {
        falhas.remove(chave);
    }

    private void limpar(Deque<Instant> q) {
        Instant limite = Instant.now().minus(JANELA);
        while (!q.isEmpty() && q.peekFirst().isBefore(limite)) q.pollFirst();
    }
}
