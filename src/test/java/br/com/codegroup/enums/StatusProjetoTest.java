package br.com.codegroup.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regras de negócio:
 *   - Sequência: EM_ANALISE → ANALISE_REALIZADA → ANALISE_APROVADA → INICIADO → PLANEJADO → EM_ANDAMENTO → ENCERRADO
 *   - CANCELADO pode ser aplicado a qualquer momento
 *   - Não é permitido pular etapas nem retroceder
 *   - INICIADO, EM_ANDAMENTO e ENCERRADO impedem exclusão
 */
class StatusProjetoTest {

    // ── sequência válida ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Transição válida: EM_ANALISE → ANALISE_REALIZADA")
    void transicao_emAnalise_para_analiseRealizada() {
        assertThat(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.ANALISE_REALIZADA)).isTrue();
    }

    @Test
    @DisplayName("Transição válida: ANALISE_REALIZADA → ANALISE_APROVADA")
    void transicao_analiseRealizada_para_analiseAprovada() {
        assertThat(StatusProjeto.ANALISE_REALIZADA.podeTransicionarPara(StatusProjeto.ANALISE_APROVADA)).isTrue();
    }

    @Test
    @DisplayName("Transição válida: ANALISE_APROVADA → INICIADO")
    void transicao_analiseAprovada_para_iniciado() {
        assertThat(StatusProjeto.ANALISE_APROVADA.podeTransicionarPara(StatusProjeto.INICIADO)).isTrue();
    }

    @Test
    @DisplayName("Transição válida: INICIADO → PLANEJADO")
    void transicao_iniciado_para_planejado() {
        assertThat(StatusProjeto.INICIADO.podeTransicionarPara(StatusProjeto.PLANEJADO)).isTrue();
    }

    @Test
    @DisplayName("Transição válida: PLANEJADO → EM_ANDAMENTO")
    void transicao_planejado_para_emAndamento() {
        assertThat(StatusProjeto.PLANEJADO.podeTransicionarPara(StatusProjeto.EM_ANDAMENTO)).isTrue();
    }

    @Test
    @DisplayName("Transição válida: EM_ANDAMENTO → ENCERRADO")
    void transicao_emAndamento_para_encerrado() {
        assertThat(StatusProjeto.EM_ANDAMENTO.podeTransicionarPara(StatusProjeto.ENCERRADO)).isTrue();
    }

    // ── pular etapas — bloqueado ──────────────────────────────────────────────

    @Test
    @DisplayName("Bloqueado: EM_ANALISE → ANALISE_APROVADA (pula 1 etapa)")
    void bloqueado_emAnalise_para_analiseAprovada() {
        assertThat(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.ANALISE_APROVADA)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: EM_ANALISE → INICIADO (pula 2 etapas)")
    void bloqueado_emAnalise_para_iniciado() {
        assertThat(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.INICIADO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: EM_ANALISE → PLANEJADO (pula 3 etapas)")
    void bloqueado_emAnalise_para_planejado() {
        assertThat(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.PLANEJADO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: EM_ANALISE → EM_ANDAMENTO (pula 4 etapas)")
    void bloqueado_emAnalise_para_emAndamento() {
        assertThat(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.EM_ANDAMENTO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: EM_ANALISE → ENCERRADO (pula 5 etapas)")
    void bloqueado_emAnalise_para_encerrado() {
        assertThat(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.ENCERRADO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: ANALISE_REALIZADA → INICIADO (pula 1 etapa)")
    void bloqueado_analiseRealizada_para_iniciado() {
        assertThat(StatusProjeto.ANALISE_REALIZADA.podeTransicionarPara(StatusProjeto.INICIADO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: ANALISE_APROVADA → PLANEJADO (pula 1 etapa)")
    void bloqueado_analiseAprovada_para_planejado() {
        assertThat(StatusProjeto.ANALISE_APROVADA.podeTransicionarPara(StatusProjeto.PLANEJADO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: INICIADO → EM_ANDAMENTO (pula 1 etapa)")
    void bloqueado_iniciado_para_emAndamento() {
        assertThat(StatusProjeto.INICIADO.podeTransicionarPara(StatusProjeto.EM_ANDAMENTO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado: PLANEJADO → ENCERRADO (pula 1 etapa)")
    void bloqueado_planejado_para_encerrado() {
        assertThat(StatusProjeto.PLANEJADO.podeTransicionarPara(StatusProjeto.ENCERRADO)).isFalse();
    }

    // ── retrocesso — bloqueado ────────────────────────────────────────────────

    @Test
    @DisplayName("Bloqueado retrocesso: ANALISE_REALIZADA → EM_ANALISE")
    void bloqueado_analiseRealizada_para_emAnalise() {
        assertThat(StatusProjeto.ANALISE_REALIZADA.podeTransicionarPara(StatusProjeto.EM_ANALISE)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado retrocesso: ANALISE_APROVADA → ANALISE_REALIZADA")
    void bloqueado_analiseAprovada_para_analiseRealizada() {
        assertThat(StatusProjeto.ANALISE_APROVADA.podeTransicionarPara(StatusProjeto.ANALISE_REALIZADA)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado retrocesso: INICIADO → ANALISE_APROVADA")
    void bloqueado_iniciado_para_analiseAprovada() {
        assertThat(StatusProjeto.INICIADO.podeTransicionarPara(StatusProjeto.ANALISE_APROVADA)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado retrocesso: EM_ANDAMENTO → PLANEJADO")
    void bloqueado_emAndamento_para_planejado() {
        assertThat(StatusProjeto.EM_ANDAMENTO.podeTransicionarPara(StatusProjeto.PLANEJADO)).isFalse();
    }

    @Test
    @DisplayName("Bloqueado retrocesso: ENCERRADO → EM_ANDAMENTO")
    void bloqueado_encerrado_para_emAndamento() {
        assertThat(StatusProjeto.ENCERRADO.podeTransicionarPara(StatusProjeto.EM_ANDAMENTO)).isFalse();
    }

    // ── impedeDeletar ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("impedeDeletar: INICIADO deve bloquear exclusão")
    void impedeDeletar_iniciado() {
        assertThat(StatusProjeto.INICIADO.impedeDeletar()).isTrue();
    }

    @Test
    @DisplayName("impedeDeletar: EM_ANDAMENTO deve bloquear exclusão")
    void impedeDeletar_emAndamento() {
        assertThat(StatusProjeto.EM_ANDAMENTO.impedeDeletar()).isTrue();
    }

    @Test
    @DisplayName("impedeDeletar: ENCERRADO deve bloquear exclusão")
    void impedeDeletar_encerrado() {
        assertThat(StatusProjeto.ENCERRADO.impedeDeletar()).isTrue();
    }

    @Test
    @DisplayName("impedeDeletar: EM_ANALISE deve permitir exclusão")
    void impedeDeletar_emAnalise() {
        assertThat(StatusProjeto.EM_ANALISE.impedeDeletar()).isFalse();
    }

    @Test
    @DisplayName("impedeDeletar: ANALISE_REALIZADA deve permitir exclusão")
    void impedeDeletar_analiseRealizada() {
        assertThat(StatusProjeto.ANALISE_REALIZADA.impedeDeletar()).isFalse();
    }

    @Test
    @DisplayName("impedeDeletar: ANALISE_APROVADA deve permitir exclusão")
    void impedeDeletar_analiseAprovada() {
        assertThat(StatusProjeto.ANALISE_APROVADA.impedeDeletar()).isFalse();
    }

    @Test
    @DisplayName("impedeDeletar: PLANEJADO deve permitir exclusão")
    void impedeDeletar_planejado() {
        assertThat(StatusProjeto.PLANEJADO.impedeDeletar()).isFalse();
    }

    @Test
    @DisplayName("impedeDeletar: CANCELADO deve permitir exclusão")
    void impedeDeletar_cancelado() {
        assertThat(StatusProjeto.CANCELADO.impedeDeletar()).isFalse();
    }
}