package br.com.codegroup.enums;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum ClassificacaoRisco {
    BAIXO, MEDIO, ALTO;

    public static ClassificacaoRisco calcular(BigDecimal orcamento, LocalDate dataInicio, LocalDate previsaoTermino) {
        long meses = ChronoUnit.MONTHS.between(dataInicio, previsaoTermino);

        boolean orcamentoBaixo = orcamento.compareTo(new BigDecimal("100000")) <= 0;
        boolean orcamentoMedio = orcamento.compareTo(new BigDecimal("100001")) >= 0
                && orcamento.compareTo(new BigDecimal("500000")) <= 0;

        boolean prazoBaixo  = meses <= 3;
        boolean prazoMedio  = meses > 3 && meses <= 6;

        if (orcamento.compareTo(new BigDecimal("500000")) > 0 || meses > 6) return ALTO;
        if (orcamentoMedio || prazoMedio) return MEDIO;
        if (orcamentoBaixo && prazoBaixo) return BAIXO;

        return MEDIO; // fallback seguro
    }
}