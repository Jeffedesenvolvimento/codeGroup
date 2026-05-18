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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @InjectMocks
    private ProjetoService projetoService;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroClient membroClient;

    @Mock
    private ProjetoMembroService projetoMembroService;

    private Projeto buildProjeto() {
        return Projeto.builder()
                .id(1L)
                .nome("Projeto Alpha")
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 3, 1))
                .orcamentoTotal(new BigDecimal("50000"))
                .descricao("Descrição do projeto")
                .gerenteMembroId(10L)
                .status(StatusProjeto.EM_ANALISE)
                .build();
    }

    private ProjetoRequest buildRequest(StatusProjeto status) {
        return new ProjetoRequest(
                "Projeto Alpha",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1),
                null,
                new BigDecimal("50000"),
                "Descrição",
                10L,
                status
        );
    }

    // ─── criar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criar deve salvar projeto e retornar response")
    void criar_deveRetornarProjetoResponse() {
        ProjetoRequest request = buildRequest(StatusProjeto.EM_ANALISE);
        Projeto salvo = buildProjeto();

        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any())).thenReturn(salvo);
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));

        ProjetoResponse response = projetoService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Projeto Alpha");
        verify(projetoRepository).save(any(Projeto.class));
        verify(projetoMembroService).adicionarMembro(1L, 10L);
    }

    @Test
    @DisplayName("criar deve lançar NotFoundException quando gerente não existe na API externa")
    void criar_gerenteNaoEncontrado_deveLancarNotFoundException() {
        ProjetoRequest request = buildRequest(StatusProjeto.EM_ANALISE);

        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(notFound.status()).thenReturn(404);
        when(membroClient.buscarMembro(10L)).thenThrow(notFound);

        assertThatThrownBy(() -> projetoService.criar(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Gerente com ID 10");
    }

    @Test
    @DisplayName("criar deve lançar ExternalIntegrationException quando API externa falha")
    void criar_falhaApiExterna_deveLancarExternalIntegrationException() {
        ProjetoRequest request = buildRequest(StatusProjeto.EM_ANALISE);

        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(500);
        when(membroClient.buscarMembro(10L)).thenThrow(feignEx);

        assertThatThrownBy(() -> projetoService.criar(request))
                .isInstanceOf(ExternalIntegrationException.class);
    }

    @Test
    @DisplayName("criar sem gerenteMembroId não deve chamar adicionarMembro")
    void criar_semGerente_naoDeveAdicionarMembro() {
        ProjetoRequest request = new ProjetoRequest(
                "Projeto Sem Gerente",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1),
                null,
                new BigDecimal("50000"),
                "desc",
                null,
                StatusProjeto.EM_ANALISE
        );
        Projeto salvo = Projeto.builder()
                .id(2L).nome("Projeto Sem Gerente")
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 3, 1))
                .orcamentoTotal(new BigDecimal("50000"))
                .status(StatusProjeto.EM_ANALISE).build();

        when(projetoRepository.save(any())).thenReturn(salvo);

        projetoService.criar(request);

        verifyNoInteractions(projetoMembroService);
    }

    // ─── buscar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscar deve retornar projeto quando ID existe")
    void buscar_idExistente_deveRetornarResponse() {
        Projeto projeto = buildProjeto();
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));

        ProjetoResponse response = projetoService.buscar(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Projeto Alpha");
    }

    @Test
    @DisplayName("buscar deve lançar NotFoundException quando ID não existe")
    void buscar_idInexistente_deveLancarNotFoundException() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoService.buscar(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── listar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listar sem filtro de risco deve retornar página completa")
    void listar_semFiltroRisco_deveRetornarPagina() {
        Projeto projeto = buildProjeto();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projeto> page = new PageImpl<>(List.of(projeto));

        when(projetoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));

        Page<ProjetoResponse> result = projetoService.listar(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listar com filtro de classificação de risco deve filtrar resultados")
    void listar_comFiltroRisco_deveFiltrar() {
        Projeto projeto = buildProjeto(); // orcamento 50k, prazo 2 meses → BAIXO
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projeto> page = new PageImpl<>(List.of(projeto));

        when(projetoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));

        // filtrando por ALTO, projeto BAIXO deve ser excluído
        Page<ProjetoResponse> result = projetoService.listar(null, ClassificacaoRisco.ALTO, null, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // ─── atualizar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("atualizar deve persistir alterações e retornar response")
    void atualizar_deveAtualizarProjeto() {
        Projeto existente = buildProjeto(); // status EM_ANALISE
        ProjetoRequest request = buildRequest(StatusProjeto.ANALISE_REALIZADA);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any())).thenReturn(existente);

        ProjetoResponse response = projetoService.atualizar(1L, request);

        assertThat(response).isNotNull();
        verify(projetoRepository).save(existente);
    }

    @Test
    @DisplayName("atualizar deve lançar NotFoundException quando ID não existe")
    void atualizar_idInexistente_deveLancarNotFoundException() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoService.atualizar(99L, buildRequest(StatusProjeto.EM_ANALISE)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("atualizar deve lançar BusinessException para transição de status inválida")
    void atualizar_transicaoStatusInvalida_deveLancarBusinessException() {
        Projeto existente = buildProjeto(); // status EM_ANALISE
        // tentando ir para EM_ANDAMENTO (pula etapas)
        ProjetoRequest request = buildRequest(StatusProjeto.EM_ANDAMENTO);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> projetoService.atualizar(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("atualizar deve permitir transição para CANCELADO de qualquer status")
    void atualizar_transicaoParaCancelado_devePermitir() {
        Projeto existente = buildProjeto(); // status EM_ANALISE
        ProjetoRequest request = buildRequest(StatusProjeto.CANCELADO);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any())).thenReturn(existente);

        assertThatNoException().isThrownBy(() -> projetoService.atualizar(1L, request));
    }

    // ─── deletar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletar deve remover projeto com status permitido")
    void deletar_statusPermitido_deveDeletar() {
        Projeto projeto = buildProjeto(); // EM_ANALISE → pode deletar
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        projetoService.deletar(1L);

        verify(projetoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletar deve lançar NotFoundException quando ID não existe")
    void deletar_idInexistente_deveLancarNotFoundException() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoService.deletar(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deletar deve lançar BusinessException para projeto INICIADO")
    void deletar_statusIniciado_deveLancarBusinessException() {
        Projeto projeto = buildProjeto();
        projeto.setStatus(StatusProjeto.INICIADO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> projetoService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não pode ser excluído");
    }

    @Test
    @DisplayName("deletar deve lançar BusinessException para projeto EM_ANDAMENTO")
    void deletar_statusEmAndamento_deveLancarBusinessException() {
        Projeto projeto = buildProjeto();
        projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> projetoService.deletar(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("deletar deve lançar BusinessException para projeto ENCERRADO")
    void deletar_statusEncerrado_deveLancarBusinessException() {
        Projeto projeto = buildProjeto();
        projeto.setStatus(StatusProjeto.ENCERRADO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> projetoService.deletar(1L))
                .isInstanceOf(BusinessException.class);
    }
}
