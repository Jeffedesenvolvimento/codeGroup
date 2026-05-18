package br.com.codegroup.dto.resquest;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MembroRequest(@JsonProperty("nome") String nome,
                            @JsonProperty("atribuicao") String atribuicao) {
}
