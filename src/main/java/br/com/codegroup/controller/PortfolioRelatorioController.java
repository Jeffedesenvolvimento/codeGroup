package br.com.codegroup.controller;

import br.com.codegroup.dto.response.PortfolioRelatorioResponse;
import br.com.codegroup.service.PortfolioRelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios/portfolio")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatório resumido do portfólio de projetos")
public class PortfolioRelatorioController {

    private final PortfolioRelatorioService portfolioRelatorioService;

    @GetMapping
    @Operation(
        summary = "Relatório de portfólio",
        description = "Retorna quantidade por status, total orçado, média de duração dos encerrados e total de membros únicos"
    )
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
            content = @Content(schema = @Schema(implementation = PortfolioRelatorioResponse.class)))
    public ResponseEntity<PortfolioRelatorioResponse> gerar() {
        return ResponseEntity.ok(portfolioRelatorioService.gerar());
    }
}
