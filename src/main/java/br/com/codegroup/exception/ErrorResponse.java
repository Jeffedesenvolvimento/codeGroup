package br.com.codegroup.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp,
        List<String> detalhes
) {
    public ErrorResponse(int status, String erro, String mensagem) {
        this(status, erro, mensagem, LocalDateTime.now(), null);
    }

    public ErrorResponse(int status, String erro, String mensagem, List<String> detalhes) {
        this(status, erro, mensagem, LocalDateTime.now(), detalhes);
    }
}
