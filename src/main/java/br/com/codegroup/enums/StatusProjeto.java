package br.com.codegroup.enums;

import java.util.List;
import java.util.Map;

public enum StatusProjeto {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    // Mapa de transições válidas
    private static final Map<StatusProjeto, List<StatusProjeto>> TRANSICOES_VALIDAS = Map.of(
            EM_ANALISE, List.of(ANALISE_REALIZADA),
            ANALISE_REALIZADA, List.of(ANALISE_APROVADA),
            ANALISE_APROVADA, List.of(INICIADO),
            INICIADO, List.of(PLANEJADO),
            PLANEJADO, List.of(EM_ANDAMENTO),
            EM_ANDAMENTO, List.of(ENCERRADO, CANCELADO),
            ENCERRADO, List.of(),
            CANCELADO, List.of()
    );

    public boolean podeTransicionarPara(StatusProjeto proximo) {
        return TRANSICOES_VALIDAS.getOrDefault(this, List.of()).contains(proximo);
    }

    public boolean impedeDeletar() {
        return this == INICIADO || this == EM_ANDAMENTO || this == ENCERRADO;
    }
}
