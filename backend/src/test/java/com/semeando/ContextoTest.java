package com.semeando;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integração: sobe o contexto completo contra um PostgreSQL real. O Flyway
 * aplica as migrations (db/migration) num banco limpo e o Hibernate valida o
 * schema (ddl-auto=validate) — rede de proteção das migrations.
 *
 * Só roda no CI (variável CI=true), onde há um Postgres de serviço; localmente,
 * sem banco, é ignorado para não quebrar um `mvn test` comum.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class ContextoTest {

    @Test
    void contextoSobeComFlywayEValidacaoDeSchema() {
        // Se o contexto carregar, Flyway + validate + seed funcionaram.
    }
}
