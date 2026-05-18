package br.com.codegroup.service;

import br.com.codegroup.client.MembroClient;
import br.com.codegroup.dto.response.MembroResponse;
import br.com.codegroup.dto.response.ProjetoResponse;
import br.com.codegroup.dto.resquest.ProjetoRequest;
import br.com.codegroup.enums.ClassificacaoRisco;
import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.exception.BusinessException;
import br.com.codegroup.exception.ExternalIntegrationException;
import br.com.codegroup.exception.NotFoundException;
import br.com.codegroup.model.Projeto;
import br.com.codegroup.repository.ProjetoRepository;
import br.com.codegroup.repository.spec.ProjetoSpecification;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final MembroClient membroClient;
    private final ProjetoMembroService projetoMembroService;

    @Transactional
    public ProjetoResponse criar(ProjetoRequest request) {
        validarGerente(request.gerenteMembroId());

        Projeto projeto = Projeto.builder()
                .nome(request.nome())
                .dataInicio(request.dataInicio())
                .previsaoTermino(request.previsaoTermino())
                .dataRealTermino(request.dataRealTermino())
                .orcamentoTotal(request.orcamentoTotal())
                .descricao(request.descricao())
                .gerenteMembroId(request.gerenteMembroId())
                .status(request.status())
                .build();

        projeto = projetoRepository.save(projeto);

        if (request.gerenteMembroId() != null) {
            projetoMembroService.adicionarMembro(projeto.getId(), request.gerenteMembroId());
        }

        return toResponse(projeto);
    }

    @Transactional(readOnly = true)
    public Page<ProjetoResponse> listar(StatusProjeto status, ClassificacaoRisco classificacaoRisco, String nome, Pageable pageable) {
        Specification<Projeto> spec = ProjetoSpecification.filtrar(status, nome);

        Page<Projeto> pagina = projetoRepository.findAll(spec, pageable);

        if (classificacaoRisco != null) {
            List<ProjetoResponse> filtrados = pagina.getContent()
                    .stream()
                    .map(this::toResponse)
                    .filter(p -> p.classificacaoRisco() == classificacaoRisco)
                    .toList();

            return new PageImpl<>(filtrados, pageable, filtrados.size());
        }

        return pagina.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscar(Long id) {
        return projetoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Projeto com ID " + id + " não encontrado"));
    }

    @Transactional
    public ProjetoResponse atualizar(Long id, ProjetoRequest request) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Projeto com ID " + id + " não encontrado"));

        if (request.status() != null && !projeto.getStatus().podeTransicionarPara(request.status())) {
            throw new BusinessException(
                    "Transição de status inválida: " + projeto.getStatus() + " → " + request.status()
            );
        }

        validarGerente(request.gerenteMembroId());

        projeto.setNome(request.nome());
        projeto.setDataInicio(request.dataInicio());
        projeto.setPrevisaoTermino(request.previsaoTermino());
        projeto.setDataRealTermino(request.dataRealTermino());
        projeto.setOrcamentoTotal(request.orcamentoTotal());
        projeto.setDescricao(request.descricao());
        projeto.setGerenteMembroId(request.gerenteMembroId());
        projeto.setStatus(request.status());

        projeto = projetoRepository.save(projeto);

        // ✅ chama a validação de alocação
        if (request.gerenteMembroId() != null) {
            projetoMembroService.adicionarMembro(projeto.getId(), request.gerenteMembroId());
        }

        return toResponse(projeto);
    }

    @Transactional
    public void deletar(Long id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Projeto com ID " + id + " não encontrado"));

        if (projeto.getStatus().impedeDeletar()) {
            throw new BusinessException(
                    "Projeto com status '" + projeto.getStatus() + "' não pode ser excluído"
            );
        }

        projetoRepository.deleteById(id);
    }

    private void validarGerente(Long gerenteMembroId) {
        if (gerenteMembroId == null) return;
        try {
            membroClient.buscarMembro(gerenteMembroId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("Gerente com ID " + gerenteMembroId + " não encontrado na API externa");
            }
            throw new ExternalIntegrationException("Falha ao consultar API externa de membros");
        }
    }

    private ProjetoResponse toResponse(Projeto projeto) {
        MembroResponse gerente = null;
        if (projeto.getGerenteMembroId() != null) {
            try {
                gerente = membroClient.buscarMembro(projeto.getGerenteMembroId());
            } catch (FeignException.NotFound e) {
                gerente = new MembroResponse(projeto.getGerenteMembroId(), "Não encontrado", "-");
            } catch (FeignException e) {
                throw new ExternalIntegrationException("Falha ao buscar gerente na API externa");
            }
        }

        List<MembroResponse> membros = projeto.getMembros().stream()
                .map(pm -> {
                    try {
                        return membroClient.buscarMembro(pm.getMembroId());
                    } catch (FeignException.NotFound e) {
                        return new MembroResponse(pm.getMembroId(), "Não encontrado", "-");
                    } catch (FeignException e) {
                        throw new ExternalIntegrationException("Falha ao buscar membro ID " + pm.getMembroId() + " na API externa");
                    }
                })
                .toList();

        ClassificacaoRisco risco = ClassificacaoRisco.calcular(
                projeto.getOrcamentoTotal(),
                projeto.getDataInicio(),
                projeto.getPrevisaoTermino()
        );

        return new ProjetoResponse(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDataInicio(),
                projeto.getPrevisaoTermino(),
                projeto.getDataRealTermino(),
                projeto.getOrcamentoTotal(),
                projeto.getDescricao(),
                gerente,
                projeto.getStatus(),
                risco,
                membros
        );
    }
}
