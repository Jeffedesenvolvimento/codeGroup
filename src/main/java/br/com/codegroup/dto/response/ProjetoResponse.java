package br.com.codegroup.dto.response;

import br.com.codegroup.enums.ClassificacaoRisco;
import br.com.codegroup.enums.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjetoResponse(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        BigDecimal orcamentoTotal,
        String descricao,
        MembroResponse gerente,
        StatusProjeto status,
        ClassificacaoRisco classificacaoRisco,  // ← calculado dinamicamente
        List<MembroResponse> membros
) {}