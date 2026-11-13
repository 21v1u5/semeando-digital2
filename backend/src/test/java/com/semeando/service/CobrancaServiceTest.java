package com.semeando.service;

import com.semeando.model.*;
import com.semeando.repo.InstituicaoRepository;
import com.semeando.repo.ParcelaRepository;
import com.semeando.repo.TabelaPrecoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Regras financeiras (RN01, RN03, RN04): desconto, divergência, multa, juros e baixa. */
@ExtendWith(MockitoExtension.class)
class CobrancaServiceTest {

    @Mock TabelaPrecoRepository tabela;
    @Mock ParcelaRepository parcelas;
    @Mock InstituicaoRepository instituicao;
    @InjectMocks CobrancaService cobranca;

    // ---------- fixtures ----------
    private Contrato contrato(String serie, double desconto, String valorParcela) {
        Turma t = new Turma();
        t.setNome(serie);
        t.setSegmento(Segmento.FUNDAMENTAL);
        Aluno a = new Aluno();
        a.setNome("Aluno Teste");
        a.setTurma(t);
        Contrato c = new Contrato();
        c.setAluno(a);
        c.setDescontoPercentual(desconto);
        c.setNumeroParcelas(11);
        c.setAnoLetivo(2026);
        if (valorParcela != null) c.setValorParcela(new BigDecimal(valorParcela));
        return c;
    }

    private Parcela parcela(String valor, LocalDate vencimento, boolean pago) {
        Parcela p = new Parcela();
        p.setValor(new BigDecimal(valor));
        p.setVencimento(vencimento);
        p.setPago(pago);
        return p;
    }

    private void preco(String serie, String valor) {
        when(tabela.findBySerie(serie)).thenReturn(Optional.of(new TabelaPreco(serie, new BigDecimal(valor))));
    }

    private void instituicaoPadrao() {
        Instituicao i = new Instituicao();
        i.setMultaPercentual(2.0);
        i.setJurosMensalPercentual(1.0);
        when(instituicao.findById(1L)).thenReturn(Optional.of(i));
    }

    // ---------- valor oficial / divergência ----------
    @Test
    void valorOficial_aplicaDesconto() {
        preco("3º ano", "500.00");
        assertThat(cobranca.valorOficial(contrato("3º ano", 10, null))).isEqualByComparingTo("450.00");
    }

    @Test
    void valorOficial_semDesconto_igualTabela() {
        preco("3º ano", "500.00");
        assertThat(cobranca.valorOficial(contrato("3º ano", 0, null))).isEqualByComparingTo("500.00");
    }

    @Test
    void valorOficial_arredondaParaDuasCasas() {
        preco("2º ano", "333.33"); // 333.33 * 0.90 = 299.997 -> 300.00 (HALF_UP)
        assertThat(cobranca.valorOficial(contrato("2º ano", 10, null))).isEqualByComparingTo("300.00");
    }

    @Test
    void divergente_detectaValorForaDaTabela() {
        preco("3º ano", "500.00"); // oficial com 10% = 450
        assertThat(cobranca.divergente(contrato("3º ano", 10, "500.00"))).isTrue();
        assertThat(cobranca.divergente(contrato("3º ano", 10, "450.00"))).isFalse();
    }

    // ---------- multa / juros / total ----------
    @Test
    void multa_semAtraso_eZero() {
        assertThat(cobranca.multa(parcela("500.00", LocalDate.now(), false))).isEqualByComparingTo("0.00");
    }

    @Test
    void multa_comAtraso_aplicaPercentual() {
        instituicaoPadrao();
        assertThat(cobranca.multa(parcela("500.00", LocalDate.now().minusDays(30), false))).isEqualByComparingTo("10.00");
    }

    @Test
    void juros_saoProRataDie() {
        instituicaoPadrao();
        assertThat(cobranca.juros(parcela("500.00", LocalDate.now().minusDays(30), false))).isEqualByComparingTo("5.00");
        assertThat(cobranca.juros(parcela("500.00", LocalDate.now().minusDays(15), false))).isEqualByComparingTo("2.50");
    }

    @Test
    void total_somaValorMultaEJuros() {
        instituicaoPadrao(); // 500 + 2% (10) + 1%/mês por 30d (5) = 515
        assertThat(cobranca.total(parcela("500.00", LocalDate.now().minusDays(30), false))).isEqualByComparingTo("515.00");
    }

    // ---------- situação / baixa ----------
    @Test
    void situacao_refleteEstadoDaParcela() {
        assertThat(cobranca.situacao(parcela("500.00", LocalDate.now().plusDays(10), false))).isEqualTo("ABERTA");
        assertThat(cobranca.situacao(parcela("500.00", LocalDate.now().minusDays(1), false))).isEqualTo("ATRASADA");
        assertThat(cobranca.situacao(parcela("500.00", LocalDate.now().minusDays(1), true))).isEqualTo("PAGA");
    }

    @Test
    void pagar_registraTotalComMultaEJuros() {
        instituicaoPadrao();
        Parcela p = parcela("500.00", LocalDate.now().minusDays(30), false);
        cobranca.pagar(p);
        assertThat(p.isPago()).isTrue();
        assertThat(p.getDataPagamento()).isEqualTo(LocalDate.now());
        assertThat(p.getValorPago()).isEqualByComparingTo("515.00");
    }

    @Test
    void pagar_parcelaJaPaga_lancaConflito() {
        Parcela p = parcela("500.00", LocalDate.now().minusDays(5), true);
        assertThatThrownBy(() -> cobranca.pagar(p)).isInstanceOf(ResponseStatusException.class);
    }

    // ---------- geração de parcelas ----------
    @Test
    void gerarParcelas_valorDivergente_naoEmite() {
        preco("3º ano", "500.00"); // oficial 450, parcela 500 -> diverge
        Contrato c = contrato("3º ano", 10, "500.00");
        assertThatThrownBy(() -> cobranca.gerarParcelas(c)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void gerarParcelas_jaGeradas_lancaConflito() {
        Contrato c = contrato("3º ano", 0, "500.00");
        c.setParcelasGeradas(true);
        assertThatThrownBy(() -> cobranca.gerarParcelas(c)).isInstanceOf(ResponseStatusException.class);
    }
}
