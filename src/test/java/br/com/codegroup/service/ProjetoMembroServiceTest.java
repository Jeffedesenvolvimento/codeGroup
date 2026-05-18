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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoMembroServiceTest {

    @InjectMocks
    private ProjetoMembroService projetoMembroService;

    @Mock
    private MembroClient membroClient;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private ProjetoMembroRepository projetoMembroRepository;

    private Projeto buildProjeto() {
        return Projeto.builder()
                .id(1L)
                .nome("Projeto Teste")
                .status(StatusProjeto.EM_ANALISE)
                .build();
    }

    // ─── adicionarMembro ──────────────────────────────────────────────────────

    @Test
    @DisplayName("adicionarMembro deve vincular membro funcionário com sucesso")
    void adicionarMembro_funcionario_deveVincular() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjeto()));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Ana", "funcionario"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(1L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 5L)).thenReturn(false);
        when(projetoMembroRepository.save(any())).thenReturn(new ProjetoMembro());

        MembroResponse result = projetoMembroService.adicionarMembro(1L, 5L);

        assertThat(result.nome()).isEqualTo("Ana");
        verify(projetoMembroRepository).save(any(ProjetoMembro.class));
    }

    @Test
    @DisplayName("adicionarMembro deve lançar BusinessException quando projeto não existe")
    void adicionarMembro_projetoInexistente_deveLancarBusinessException() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(99L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Projeto não encontrado");
    }

    @Test
    @DisplayName("adicionarMembro deve lançar BusinessException quando membro não existe na API")
    void adicionarMembro_membroNaoEncontrado_deveLancarBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjeto()));
        when(membroClient.buscarMembro(5L)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Membro com ID 5");
    }

    @Test
    @DisplayName("adicionarMembro deve lançar BusinessException quando atribuição não é funcionário")
    void adicionarMembro_atribuicaoNaoFuncionario_deveLancarBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjeto()));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Carlos", "gerente"));

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("funcionário");
    }

    @Test
    @DisplayName("adicionarMembro deve lançar BusinessException quando projeto atinge 10 membros")
    void adicionarMembro_limiteAtingido_deveLancarBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjeto()));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Ana", "funcionario"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(10L);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("limite máximo de 10");
    }

    @Test
    @DisplayName("adicionarMembro deve lançar BusinessException quando membro já está em 3 projetos ativos")
    void adicionarMembro_membroEm3ProjetosAtivos_deveLancarBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjeto()));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Ana", "funcionario"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(3L);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("3 projetos ativos");
    }

    @Test
    @DisplayName("adicionarMembro deve lançar BusinessException quando membro já está no projeto")
    void adicionarMembro_duplicado_deveLancarBusinessException() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(buildProjeto()));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Ana", "funcionario"));
        when(projetoMembroRepository.countByProjetoId(1L)).thenReturn(2L);
        when(projetoMembroRepository.countProjetosAtivosByMembroId(eq(5L), anyList())).thenReturn(1L);
        when(projetoMembroRepository.existsByProjetoIdAndMembroId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> projetoMembroService.adicionarMembro(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está associado");
    }

    // ─── listarMembros ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarMembros deve retornar lista de membros do projeto")
    void listarMembros_deveRetornarLista() {
        ProjetoMembro pm = ProjetoMembro.builder().membroId(5L).build();
        when(projetoMembroRepository.findByProjetoId(1L)).thenReturn(List.of(pm));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Ana", "funcionario"));

        List<MembroResponse> result = projetoMembroService.listarMembros(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("listarMembros deve retornar placeholder quando membro não existe na API")
    void listarMembros_membroNaoEncontrado_deveRetornarPlaceholder() {
        ProjetoMembro pm = ProjetoMembro.builder().membroId(99L).build();
        when(projetoMembroRepository.findByProjetoId(1L)).thenReturn(List.of(pm));
        when(membroClient.buscarMembro(99L)).thenThrow(FeignException.NotFound.class);

        List<MembroResponse> result = projetoMembroService.listarMembros(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Não encontrado");
    }

    // ─── buscarMembro ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarMembro deve retornar membro quando vinculado ao projeto")
    void buscarMembro_vinculado_deveRetornar() {
        ProjetoMembro pm = ProjetoMembro.builder().membroId(5L).build();
        when(projetoMembroRepository.findByProjetoIdAndMembroId(1L, 5L)).thenReturn(Optional.of(pm));
        when(membroClient.buscarMembro(5L)).thenReturn(new MembroResponse(5L, "Ana", "funcionario"));

        MembroResponse result = projetoMembroService.buscarMembro(1L, 5L);

        assertThat(result.id()).isEqualTo(5L);
    }

    @Test
    @DisplayName("buscarMembro deve lançar BusinessException quando membro não está no projeto")
    void buscarMembro_naoVinculado_deveLancarBusinessException() {
        when(projetoMembroRepository.findByProjetoIdAndMembroId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoMembroService.buscarMembro(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Membro não encontrado no projeto");
    }
}
