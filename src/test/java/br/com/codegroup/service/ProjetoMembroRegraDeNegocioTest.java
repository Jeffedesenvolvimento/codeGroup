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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Regras de negócio cobertas:
 *   RN-01: Apenas funcionários podem ser associados a projetos
 *   RN-02: Mínimo 1 e máximo 10 membros por projeto
 *   RN-03: Membro não pode estar em mais de 3 projetos ativos (status ≠ ENCERRADO e ≠ CANCELADO)
 *   RN-04: Membro não pode ser adicionado duas vezes ao mesmo projeto
 */
@ExtendWith(MockitoExtension.class)
class ProjetoMembroRegraDeNegocioTest {

    @InjectMocks
    private ProjetoMembroService projetoMembroService;

    @Mock
    private MembroClient membroClient;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private ProjetoMembroRepository projetoMembroRepository;

    private Projeto projetoAtivo() {
        return Projeto.builder()
                .id(1L)
                .nome("Projeto X")
                .status(StatusProjeto.EM_ANALISE)
                .build();
    }

    private MembroResponse funcionario(Long id, String nome) {
        return new MembroResponse(id, nome, "funcionario");
    }

    private MembroResponse naoFuncionario(Long id, String nome) {
        return new MembroResponse(id, nome, "gerente");
    }

    // ── RN-01: apenas funcionário ─────────────────────────────────────────────

    @Test
    @DisplayName("RN-01: gerente não pode ser associado a projeto")
    void rn01_gerente_naoDeveSerAssociado() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(5L)).thenReturn(naoFuncionario(5L, "Carlos Gerente"));

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("funcionário");
    }

    @Test
    @DisplayName("RN-01: atribuição nula não deve ser associada")
    void rn01_atribuicaoNula_naoDeveSerAssociada() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Sem Atribuição", null));

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("funcionário");
    }

    @Test
    @DisplayName("RN-01: funcionário pode ser associado sem exceção")
    void rn01_funcionario_deveSerAssociado() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(5L)).thenReturn(funcionario(5L, "Ana"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(0L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(0L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 5L)).thenReturn(false);
        when(projetoMembroRepository.save(any())).thenReturn(new ProjetoMembro());

        assertThatNoException().isThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L));
    }

    // ── RN-02: mínimo 1 / máximo 10 membros ──────────────────────────────────

    @Test
    @DisplayName("RN-02: adicionar 1º membro (mínimo) deve ter sucesso")
    void rn02_primeiroMembro_deveSerAdicionado() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(1L)).thenReturn(funcionario(1L, "Membro 1"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(0L); // nenhum membro ainda
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(1L), anyList())).thenReturn(0L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 1L)).thenReturn(false);
        when(projetoMembroRepository.save(any())).thenReturn(new ProjetoMembro());

        assertThatNoException().isThrownBy(() -> projetoMembroService.adicionarMembro(1L, 1L));
    }

    @Test
    @DisplayName("RN-02: adicionar 10º membro (máximo exato) deve ter sucesso")
    void rn02_decimoMembro_limiteMaximo_deveSerAdicionado() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(10L)).thenReturn(funcionario(10L, "Membro 10"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(9L); // 9 membros → vai para 10
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(10L), anyList())).thenReturn(0L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 10L)).thenReturn(false);
        when(projetoMembroRepository.save(any())).thenReturn(new ProjetoMembro());

        assertThatNoException().isThrownBy(() -> projetoMembroService.adicionarMembro(1L, 10L));
    }

    @Test
    @DisplayName("RN-02: adicionar 11º membro deve lançar BusinessException")
    void rn02_onzavoMembro_deveRejeitarComBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(11L)).thenReturn(funcionario(11L, "Membro 11"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(10L); // já atingiu o máximo

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 11L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("10");
    }

    // ── RN-03: máximo 3 projetos ativos por membro ────────────────────────────

    @Test
    @DisplayName("RN-03: membro em 2 projetos ativos pode ser adicionado (abaixo do limite)")
    void rn03_doisProjetosAtivos_devePermitir() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(5L)).thenReturn(funcionario(5L, "Ana"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(2L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 5L)).thenReturn(false);
        when(projetoMembroRepository.save(any())).thenReturn(new ProjetoMembro());

        assertThatNoException().isThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L));
    }

    @Test
    @DisplayName("RN-03: membro exatamente em 3 projetos ativos não pode ser adicionado")
    void rn03_tresProjetosAtivos_deveRejeitar() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(5L)).thenReturn(funcionario(5L, "Ana"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(3L);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("3");
    }

    // ── RN-04: membro duplicado ───────────────────────────────────────────────

    @Test
    @DisplayName("RN-04: adicionar membro já vinculado deve lançar BusinessException")
    void rn04_membroDuplicado_deveRejeitar() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(5L)).thenReturn(funcionario(5L, "Ana"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(1L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está associado");
    }

    @Test
    @DisplayName("RN-04: adicionar membro diferente ao mesmo projeto não deve lançar exceção")
    void rn04_membroNovo_devePermitir() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(6L)).thenReturn(funcionario(6L, "Pedro"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(6L), anyList())).thenReturn(1L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 6L)).thenReturn(false);
        when(projetoMembroRepository.save(any())).thenReturn(new ProjetoMembro());

        assertThatNoException().isThrownBy(() -> projetoMembroService.adicionarMembro(1L, 6L));
    }

    // ── membro não encontrado na API externa ──────────────────────────────────

    @Test
    @DisplayName("Membro não encontrado na API deve lançar BusinessException")
    void membroNaoEncontradoNaApi_deveLancarBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoAtivo()));
        when(membroClient.buscarMembro(99L)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    // ── projeto não encontrado ────────────────────────────────────────────────

    @Test
    @DisplayName("Projeto não encontrado deve lançar BusinessException antes de verificar membro")
    void projetoNaoEncontrado_deveLancarBusinessExceptionSemChamarApi() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(99L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Projeto");

        verifyNoInteractions(membroClient);
    }
}