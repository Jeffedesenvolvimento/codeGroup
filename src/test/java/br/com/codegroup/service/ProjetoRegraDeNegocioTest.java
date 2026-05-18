package br.com.codegroup.service;

import br.com.codegroup.client.MembroClient;
import br.com.codegroup.dto.response.MembroResponse;
import br.com.codegroup.dto.resquest.ProjetoRequest;
import br.com.codegroup.enums.ClassificacaoRisco;
import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.exception.BusinessException;
import br.com.codegroup.exception.NotFoundException;
import br.com.codegroup.model.Projeto;
import br.com.codegroup.repository.ProjetoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regras de negócio cobertas:
 *   RN-05: Exclusão bloqueada para projetos INICIADO, EM_ANDAMENTO e ENCERRADO
 *   RN-06: Transição de status deve respeitar a sequência lógica
 *   RN-07: Classificação de risco calculada dinamicamente e persistida no projeto
 */
@ExtendWith(MockitoExtension.class)
class ProjetoRegraDeNegocioTest {

    @InjectMocks
    private ProjetoService projetoService;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroClient membroClient;

    @Mock
    private ProjetoMembroService projetoMembroService;

    private Projeto buildProjetoComStatus(StatusProjeto status) {
        return Projeto.builder()
                .id(1L)
                .nome("Projeto Teste")
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 3, 1))
                .orcamentoTotal(new BigDecimal("50000"))
                .gerenteMembroId(10L)
                .status(status)
                .build();
    }

    private ProjetoRequest requestComStatus(StatusProjeto status) {
        return new ProjetoRequest(
                "Projeto Teste",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1),
                null,
                new BigDecimal("50000"),
                "Descrição",
                10L,
                status
        );
    }

    // ── RN-05: exclusão bloqueada ─────────────────────────────────────────────

    @Test
    @DisplayName("RN-05: deletar projeto INICIADO deve lançar BusinessException")
    void rn05_deletar_iniciado_deveBloquear() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjetoComStatus(StatusProjeto.INICIADO)));

        assertThatThrownBy(() -> projetoService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não pode ser excluído");
        verify(projetoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("RN-05: deletar projeto EM_ANDAMENTO deve lançar BusinessException")
    void rn05_deletar_emAndamento_deveBloquear() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjetoComStatus(StatusProjeto.EM_ANDAMENTO)));

        assertThatThrownBy(() -> projetoService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não pode ser excluído");
        verify(projetoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("RN-05: deletar projeto ENCERRADO deve lançar BusinessException")
    void rn05_deletar_encerrado_deveBloquear() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjetoComStatus(StatusProjeto.ENCERRADO)));

        assertThatThrownBy(() -> projetoService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não pode ser excluído");
        verify(projetoRepository, never()).deleteById(any());
    }

    @ParameterizedTest(name = "RN-05: deletar projeto {0} deve ser permitido")
    @EnumSource(value = StatusProjeto.class,
            names = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "PLANEJADO", "CANCELADO"})
    @DisplayName("RN-05: status que permitem exclusão")
    void rn05_deletar_statusPermitidos_deveExcluir(StatusProjeto status) {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjetoComStatus(status)));

        assertThatNoException().isThrownBy(() -> projetoService.deletar(1L));
        verify(projetoRepository).deleteById(1L);
    }

    // ── RN-06: transição de status ────────────────────────────────────────────

    @Test
    @DisplayName("RN-06: atualizar com próximo status válido deve ser permitido")
    void rn06_atualizarStatusValido_devePermitir() {
        Projeto projeto = buildProjetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any())).thenReturn(projeto);

        assertThatNoException().isThrownBy(
                () -> projetoService.atualizar(1L, requestComStatus(StatusProjeto.ANALISE_REALIZADA))
        );
    }

    @Test
    @DisplayName("RN-06: pular etapa de status deve lançar BusinessException")
    void rn06_pularEtapaStatus_deveLancarBusinessException() {
        Projeto projeto = buildProjetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> projetoService.atualizar(1L, requestComStatus(StatusProjeto.ANALISE_APROVADA)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("RN-06: retroceder status deve lançar BusinessException")
    void rn06_retrocederStatus_deveLancarBusinessException() {
        Projeto projeto = buildProjetoComStatus(StatusProjeto.ANALISE_REALIZADA);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> projetoService.atualizar(1L, requestComStatus(StatusProjeto.EM_ANALISE)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("RN-06: atualizar para CANCELADO de qualquer status deve ser permitido")
    void rn06_atualizarParaCancelado_semprePermitido() {
        for (StatusProjeto statusAtual : StatusProjeto.values()) {
            if (statusAtual == StatusProjeto.CANCELADO) continue;

            Projeto projeto = buildProjetoComStatus(statusAtual);
            when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
            when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
            when(projetoRepository.save(any())).thenReturn(projeto);

            assertThatNoException().isThrownBy(
                    () -> projetoService.atualizar(1L, requestComStatus(StatusProjeto.CANCELADO))
            );
        }
    }

    @Test
    @DisplayName("RN-06: manter o mesmo status deve ser permitido (sem alteração)")
    void rn06_manterMesmoStatus_devePermitir() {
        Projeto projeto = buildProjetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any())).thenReturn(projeto);

        assertThatNoException().isThrownBy(
                () -> projetoService.atualizar(1L, requestComStatus(StatusProjeto.EM_ANALISE))
        );
    }

    // ── RN-07: classificação de risco calculada e persistida ──────────────────

    @Test
    @DisplayName("RN-07: criar projeto baixo risco deve persistir ClassificacaoRisco.BAIXO")
    void rn07_criarProjetoBaixoRisco_devePersistirClassificacaoBaixo() {
        // orçamento 50k, prazo 2 meses → BAIXO
        ProjetoRequest request = new ProjetoRequest(
                "Projeto Baixo Risco",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1),
                null,
                new BigDecimal("50000"),
                "desc",
                10L,
                StatusProjeto.EM_ANALISE
        );

        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        projetoService.criar(request);

        verify(projetoRepository).save(argThat(p ->
                p.getRisco() == ClassificacaoRisco.BAIXO
        ));
    }

    @Test
    @DisplayName("RN-07: criar projeto alto risco (orçamento > 500k) deve persistir ClassificacaoRisco.ALTO")
    void rn07_criarProjetoAltoRisco_orcamento_devePersistirClassificacaoAlto() {
        ProjetoRequest request = new ProjetoRequest(
                "Projeto Alto Risco",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1),
                null,
                new BigDecimal("600000"),
                "desc",
                10L,
                StatusProjeto.EM_ANALISE
        );

        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        projetoService.criar(request);

        verify(projetoRepository).save(argThat(p ->
                p.getRisco() == ClassificacaoRisco.ALTO
        ));
    }

    @Test
    @DisplayName("RN-07: criar projeto médio risco (prazo 4 meses) deve persistir ClassificacaoRisco.MEDIO")
    void rn07_criarProjetoMedioRisco_prazo_devePersistirClassificacaoMedio() {
        ProjetoRequest request = new ProjetoRequest(
                "Projeto Médio Risco",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 5, 1),  // 4 meses → MEDIO
                null,
                new BigDecimal("50000"),
                "desc",
                10L,
                StatusProjeto.EM_ANALISE
        );

        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        projetoService.criar(request);

        verify(projetoRepository).save(argThat(p ->
                p.getRisco() == ClassificacaoRisco.MEDIO
        ));
    }

    @Test
    @DisplayName("RN-07: atualizar orçamento de projeto deve recalcular classificação de risco")
    void rn07_atualizarOrcamento_deveRecalcularRisco() {
        Projeto projetoExistente = buildProjetoComStatus(StatusProjeto.EM_ANALISE);
        projetoExistente.setRisco(ClassificacaoRisco.BAIXO);

        // mudando o orçamento para > 500k → deve virar ALTO
        ProjetoRequest requestAtualizado = new ProjetoRequest(
                "Projeto Teste",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1),
                null,
                new BigDecimal("700000"),
                "Descrição",
                10L,
                StatusProjeto.EM_ANALISE
        );

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoExistente));
        when(membroClient.buscarMembro(10L)).thenReturn(new MembroResponse(10L, "João", "funcionario"));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        projetoService.atualizar(1L, requestAtualizado);

        verify(projetoRepository).save(argThat(p ->
                p.getRisco() == ClassificacaoRisco.ALTO
        ));
    }
}