package br.com.codegroup.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ClassificacaoRiscoTest {

    @Test
    @DisplayName("calcular deve retornar BAIXO para orçamento <= 100k e prazo <= 3 meses")
    void calcular_orcamentoBaixoPrazoBaixo_deveRetornarBaixo() {
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("50000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1) // 2 meses
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.BAIXO);
    }

    @Test
    @DisplayName("calcular deve retornar MEDIO para orçamento entre 100k e 500k")
    void calcular_orcamentoMedio_deveRetornarMedio() {
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("300000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1) // 2 meses
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.MEDIO);
    }

    @Test
    @DisplayName("calcular deve retornar MEDIO para prazo entre 3 e 6 meses")
    void calcular_prazoMedio_deveRetornarMedio() {
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("50000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 5, 1) // 4 meses
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.MEDIO);
    }

    @Test
    @DisplayName("calcular deve retornar ALTO para orçamento > 500k")
    void calcular_orcamentoAlto_deveRetornarAlto() {
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("600000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1)
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.ALTO);
    }

    @Test
    @DisplayName("calcular deve retornar ALTO para prazo > 6 meses")
    void calcular_prazoLongo_deveRetornarAlto() {
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("50000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 9, 1) // 8 meses
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.ALTO);
    }

    @Test
    @DisplayName("calcular deve retornar ALTO para orçamento alto E prazo longo")
    void calcular_orcamentoAltoPrazoLongo_deveRetornarAlto() {
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("1000000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1) // 12 meses
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.ALTO);
    }

    @Test
    @DisplayName("calcular deve retornar MEDIO para orçamento exatamente em 100k e prazo médio")
    void calcular_limiteOrcamento_deveRetornarCorreto() {
        // exatamente 100k → orcamentoBaixo=true, prazo 4 meses → prazoMedio=true → MEDIO
        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                new BigDecimal("100000"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 5, 1)
        );
        assertThat(risco).isEqualTo(ClassificacaoRisco.MEDIO);
    }
}