package br.com.codegroup.dto.response;

import br.com.codegroup.enums.StatusProjeto;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioRelatorioResponse(Map<StatusProjeto, Long> quantidadePorStatus,
                                         Map<StatusProjeto, BigDecimal> totalOrcadoPorStatus,
                                         Double mediaDuracaoProjetosEncerradosDias,
                                         Long totalMembrosUnicosAlocados) {
}
