package br.com.codegroup.enums;

import java.util.List;

public enum StatusProjeto {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    private static final List<StatusProjeto> SEQUENCIA = List.of(
            EM_ANALISE,
            ANALISE_REALIZADA,
            ANALISE_APROVADA,
            INICIADO,
            PLANEJADO,
            EM_ANDAMENTO,
            ENCERRADO
    );

    public boolean podeTransicionarPara(StatusProjeto proximo) {
        if (proximo == CANCELADO) return true;

        int atual = SEQUENCIA.indexOf(this);
        int destino = SEQUENCIA.indexOf(proximo);

        if (atual == -1 || destino == -1) return false;
        return destino == atual + 1;
    }

    public boolean impedeDeletar() {
        return this == INICIADO || this == EM_ANDAMENTO || this == ENCERRADO;
    }
}