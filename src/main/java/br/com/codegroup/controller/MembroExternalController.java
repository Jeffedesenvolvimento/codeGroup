package br.com.codegroup.controller;

import br.com.codegroup.dto.response.MembroResponse;
import br.com.codegroup.dto.resquest.MembroRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping(value = "/external/membros", produces = "application/json")
@Tag(name = "External Mock", description = "API externa mockada de membros")
public class MembroExternalController {

    private final Map<Long, MembroResponse> storage = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

   /* public MembroExternalController() {
        criar(new MembroRequest("Ana Lima", "Gerente"));
        criar(new MembroRequest("Carlos Souza", "Desenvolvedor"));
        criar(new MembroRequest("Beatriz Rocha", "Analista"));
    }*/

    @PostMapping
    @Operation(summary = "Criar membro [MOCK]",
            description = "Cria um novo membro em memória durante execução")
    @ApiResponse(responseCode = "200", description = "Membro criado com sucesso",
            content = @Content(schema = @Schema(implementation = MembroResponse.class)))
    public ResponseEntity<MembroResponse> criar(@RequestBody MembroRequest request) {
        Long id = idCounter.getAndIncrement();
        MembroResponse response = new MembroResponse(id, request.nome(), request.atribuicao());
        storage.put(id, response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar membros [MOCK]",
            description = "Retorna todos os membros em memória")
    @ApiResponse(responseCode = "200", description = "Lista de membros",
            content = @Content(schema = @Schema(implementation = MembroResponse.class)))
    public ResponseEntity<List<MembroResponse>> listar() {
        return ResponseEntity.ok(List.copyOf(storage.values()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar membro por ID [MOCK]",
            description = "Retorna um membro específico pelo ID")
    @ApiResponse(responseCode = "200", description = "Membro encontrado",
            content = @Content(schema = @Schema(implementation = MembroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    public ResponseEntity<MembroResponse> buscar(@PathVariable Long id) {
        MembroResponse membro = storage.get(id);
        return membro != null
                ? ResponseEntity.ok(membro)
                : ResponseEntity.notFound().build();
    }
}