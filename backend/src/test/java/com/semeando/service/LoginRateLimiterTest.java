package com.semeando.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Proteção a força bruta: bloqueio após muitas falhas e reset no sucesso. */
class LoginRateLimiterTest {

    @Test
    void bloqueiaAposCincoFalhas() {
        LoginRateLimiter rl = new LoginRateLimiter();
        assertThatCode(() -> rl.verificar("ip-1")).doesNotThrowAnyException();
        for (int i = 0; i < 5; i++) rl.falhou("ip-1");
        assertThatThrownBy(() -> rl.verificar("ip-1")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void sucessoLimpaAsTentativas() {
        LoginRateLimiter rl = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) rl.falhou("ip-2");
        rl.sucesso("ip-2");
        assertThatCode(() -> rl.verificar("ip-2")).doesNotThrowAnyException();
    }

    @Test
    void chavesDiferentesNaoSeAfetam() {
        LoginRateLimiter rl = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) rl.falhou("ip-3");
        assertThatCode(() -> rl.verificar("ip-4")).doesNotThrowAnyException();
    }
}
