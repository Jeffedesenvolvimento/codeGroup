package br.com.codegroup.dto.resquest;

import br.com.codegroup.enums.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoRequest(
        String nome,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        BigDecimal orcamentoTotal,
        String descricao,
        Long gerenteMembroId,
        StatusProjeto status
) {}