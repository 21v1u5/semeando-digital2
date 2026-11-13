package com.semeando.service;

import com.semeando.model.*;
import com.semeando.repo.AlunoRepository;
import com.semeando.repo.ContratoRepository;
import com.semeando.repo.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Regras de matrícula (RN02 capacidade, RN06 pré-requisitos, RN01 tipo de documento). */
@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock AlunoRepository alunos;
    @Mock ContratoRepository contratos;
    @Mock TurmaRepository turmas;
    @Mock CobrancaService cobranca;
    @InjectMocks MatriculaService matricula;

    private Turma turma() {
        Turma t = new Turma();
        t.setId(1L);
        t.setNome("3º ano");
        t.setSegmento(Segmento.FUNDAMENTAL);
        return t;
    }

    /** Aluno aprovado, sem pendências e com turma — apto a efetivar. */
    private Aluno alunoApto(Turma t) {
        Aluno a = new Aluno();
        a.setId(7L);
        a.setNome("Aluno Teste");
        a.setTurma(t);
        a.setStatus(StatusMatricula.APROVADA);
        a.setDocCertidaoNascimento(true);
        a.setDocComprovanteResidencia(true);
        a.setDocCartaoVacina(true);
        a.setDocFoto(true);
        a.setDocDocumentoResponsavel(true);
        a.setTaxaMatriculaPaga(true);
        a.setConsentimentoLgpd(true);
        a.setHospitalReferencia("Hospital de Referência");
        a.setContatoEmergencia("(98) 99999-9999");
        return a;
    }

    @Test
    void vagas_descontaMatriculadosDaCapacidade() {
        Turma t = turma();
        when(alunos.countByTurmaAndStatus(t, StatusMatricula.MATRICULADO)).thenReturn(25L);
        assertThat(matricula.vagas(t)).isEqualTo(5); // 30 - 25
    }

    @Test
    void efetivar_naoAprovado_lanca() {
        Aluno a = alunoApto(turma());
        a.setStatus(StatusMatricula.PRE_MATRICULA);
        assertThatThrownBy(() -> matricula.efetivar(a)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void efetivar_comPendencias_lanca() {
        Aluno a = alunoApto(turma());
        a.setTaxaMatriculaPaga(false); // gera pendência
        assertThatThrownBy(() -> matricula.efetivar(a)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void efetivar_semTurma_lanca() {
        Aluno a = alunoApto(turma());
        a.setTurma(null);
        assertThatThrownBy(() -> matricula.efetivar(a)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void efetivar_turmaCheia_lanca() {
        Turma t = turma();
        Aluno a = alunoApto(t);
        when(alunos.countByTurmaAndStatus(t, StatusMatricula.MATRICULADO)).thenReturn(30L); // 0 vagas
        assertThatThrownBy(() -> matricula.efetivar(a)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void efetivar_comVaga_matriculaEEmiteContrato() {
        Turma t = turma();
        Aluno a = alunoApto(t);
        when(alunos.countByTurmaAndStatus(t, StatusMatricula.MATRICULADO)).thenReturn(10L); // 20 vagas
        when(cobranca.valorTabela("3º ano")).thenReturn(new BigDecimal("500.00"));
        when(contratos.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));

        Contrato c = matricula.efetivar(a);

        assertThat(a.getStatus()).isEqualTo(StatusMatricula.MATRICULADO);
        assertThat(a.getMatricula()).isNotNull();
        assertThat(c.getNumeroParcelas()).isEqualTo(11);
        assertThat(c.getValorParcela()).isEqualByComparingTo("500.00");
        assertThat(c.getTipo()).isEqualTo("Contrato de prestação de serviços"); // FUNDAMENTAL
    }
}
