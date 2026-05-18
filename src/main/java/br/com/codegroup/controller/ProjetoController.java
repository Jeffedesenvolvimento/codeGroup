package br.com.codegroup.controller;

import br.com.codegroup.dto.resquest.ProjetoRequest;
import br.com.codegroup.dto.response.ProjetoResponse;
import br.com.codegroup.enums.ClassificacaoRisco;
import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "CRUD de projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @Operation(summary = "Criar projeto")
    @ApiResponse(responseCode = "200", description = "Projeto criado",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    public ResponseEntity<ProjetoResponse> criar(@RequestBody ProjetoRequest request) {
        return ResponseEntity.ok(projetoService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar projetos")
    @ApiResponse(responseCode = "200", description = "Lista de projetos",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    public ResponseEntity<Page<ProjetoResponse>> listar(
            @RequestParam(required = false) StatusProjeto status,
            @RequestParam(required = false) ClassificacaoRisco classificacaoRisco,
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
            ) {
        return ResponseEntity.ok(projetoService.listar(status, classificacaoRisco, nome, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID")
    @ApiResponse(responseCode = "200", description = "Projeto encontrado",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<ProjetoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(projetoService.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto")
    @ApiResponse(responseCode = "200", description = "Projeto atualizado",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    public ResponseEntity<ProjetoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody ProjetoRequest request) {
        return ResponseEntity.ok(projetoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar projeto")
    @ApiResponse(responseCode = "204", description = "Projeto deletado")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        projetoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}