package br.com.codegroup.client;


import br.com.codegroup.dto.response.MembroResponse;
import br.com.codegroup.dto.resquest.MembroRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "membroClient", url = "http://localhost:8080/external/membros")
public interface MembroClient {

    @PostMapping
    MembroResponse criarMembro(@RequestBody MembroRequest request);

    @GetMapping
    List<MembroResponse> listarMembros();

    @GetMapping("/{id}")
    MembroResponse buscarMembro(@PathVariable Long id);
}

