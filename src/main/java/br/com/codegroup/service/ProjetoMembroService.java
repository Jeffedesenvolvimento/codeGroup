package br.com.codegroup.service;

import br.com.codegroup.client.MembroClient;
import br.com.codegroup.dto.response.MembroResponse;
import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.exception.BusinessException;
import br.com.codegroup.model.Projeto;
import br.com.codegroup.model.ProjetoMembro;
import br.com.codegroup.repository.ProjetoMembroRepository;
import br.com.codegroup.repository.ProjetoRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoMembroService {

    private final MembroClient membroClient;
    private final ProjetoRepository projetoRepository;
    private final ProjetoMembroRepository projetoMembroRepository;

    @Transactional
    public MembroResponse adicionarMembro(Long projetoId, Long membroId) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new BusinessException("Projeto não encontrado"));

        // busca membro na API externa
        MembroResponse membro;
        try {
            membro = membroClient.buscarMembro(membroId);
        } catch (FeignException.NotFound e) {
            throw new BusinessException("Membro com ID " + membroId + " não encontrado");
        }

        // apenas funcionários podem ser associados
        if (!"funcionario".equalsIgnoreCase(membro.atribuicao())) {
            throw new BusinessException("Apenas membros com atribuição 'funcionário' podem ser associados");
        }

        // máximo 10 membros por projeto
        long totalMembros = projetoMembroRepository.countByProjetoId(projetoId);
        if (totalMembros >= 10) {
            throw new BusinessException("Projeto já atingiu o limite máximo de 10 membros");
        }

        // membro não pode estar em mais de 3 projetos ativos simultaneamente
        long projetosAtivos = projetoMembroRepository.countProjetosAtivosByMembroId(
                membroId,
                List.of(StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO)
        );
        if (projetosAtivos >= 3) {
            throw new BusinessException("Membro já está alocado em 3 projetos ativos");
        }

        // evita duplicata
        boolean jaVinculado = projetoMembroRepository.existsByProjetoIdAndMembroId(projetoId, membroId);
        if (jaVinculado) {
            throw new BusinessException("Membro já está associado a este projeto");
        }

        ProjetoMembro vinculo = ProjetoMembro.builder()
                .projeto(projeto)
                .membroId(membroId)
                .build();

        projetoMembroRepository.save(vinculo);
        return membro;
    }

    @Transactional(readOnly = true)
    public List<MembroResponse> listarMembros(Long projetoId) {
        return projetoMembroRepository.findByProjetoId(projetoId)
                .stream()
                .map(pm -> {
                    try {
                        return membroClient.buscarMembro(pm.getMembroId());
                    } catch (FeignException.NotFound e) {
                        return new MembroResponse(pm.getMembroId(), "Não encontrado", "-");
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public MembroResponse buscarMembro(Long projetoId, Long membroId) {
        projetoMembroRepository.findByProjetoIdAndMembroId(projetoId, membroId)
                .orElseThrow(() -> new BusinessException("Membro não encontrado no projeto"));
        return membroClient.buscarMembro(membroId);
    }
}