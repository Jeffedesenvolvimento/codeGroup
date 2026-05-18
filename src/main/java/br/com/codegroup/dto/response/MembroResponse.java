package br.com.codegroup.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MembroResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("nome")
        String nome,
        @JsonProperty("atribuicao")
        String atribuicao) {
}
