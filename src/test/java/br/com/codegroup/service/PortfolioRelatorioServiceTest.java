package br.com.codegroup.service;

import br.com.codegroup.dto.response.PortfolioRelatorioResponse;
import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.model.Projeto;
import br.com.codegroup.model.ProjetoMembro;
import br.com.codegroup.repository.ProjetoMembroRepository;
import br.com.codegroup.repository.ProjetoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioRelatorioServiceTest {

    @InjectMocks
    private PortfolioRelatorioService portfolioRelatorioService;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private ProjetoMembroRepository projetoMembroRepository;

    @Test
    @DisplayName("gerar deve retornar relatório com quantidades por status corretas")
    void gerar_deveRetornarQuantidadesPorStatus() {
        Projeto p1 = Projeto.builder().status(StatusProjeto.EM_ANALISE)
                .orcamentoTotal(new BigDecimal("10000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 2, 1)).build();

        Projeto p2 = Projeto.builder().status(StatusProjeto.EM_ANALISE)
                .orcamentoTotal(new BigDecimal("20000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 2, 1)).build();

        Projeto p3 = Projeto.builder().status(StatusProjeto.ENCERRADO)
                .orcamentoTotal(new BigDecimal("50000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataRealTermino(LocalDate.of(2024, 4, 1))
                .previsaoTermino(LocalDate.of(2024, 4, 1)).build();

        when(projetoRepository.findAll()).thenReturn(List.of(p1, p2, p3));
        when(projetoMembroRepository.findAll()).thenReturn(List.of());

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.quantidadePorStatus().get(StatusProjeto.EM_ANALISE)).isEqualTo(2L);
        assertThat(response.quantidadePorStatus().get(StatusProjeto.ENCERRADO)).isEqualTo(1L);
    }

    @Test
    @DisplayName("gerar deve calcular total orçado por status corretamente")
    void gerar_deveCalcularTotalOrcadoPorStatus() {
        Projeto p1 = Projeto.builder().status(StatusProjeto.EM_ANALISE)
                .orcamentoTotal(new BigDecimal("30000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 2, 1)).build();

        Projeto p2 = Projeto.builder().status(StatusProjeto.EM_ANALISE)
                .orcamentoTotal(new BigDecimal("70000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 2, 1)).build();

        when(projetoRepository.findAll()).thenReturn(List.of(p1, p2));
        when(projetoMembroRepository.findAll()).thenReturn(List.of());

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.totalOrcadoPorStatus().get(StatusProjeto.EM_ANALISE))
                .isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("gerar deve calcular média de duração dos projetos encerrados")
    void gerar_deveCalcularMediaDuracaoEncerrados() {
        // Projeto encerrado com 90 dias de duração
        Projeto encerrado = Projeto.builder().status(StatusProjeto.ENCERRADO)
                .orcamentoTotal(new BigDecimal("10000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataRealTermino(LocalDate.of(2024, 4, 1))
                .previsaoTermino(LocalDate.of(2024, 4, 1)).build();

        when(projetoRepository.findAll()).thenReturn(List.of(encerrado));
        when(projetoMembroRepository.findAll()).thenReturn(List.of());

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.mediaDuracaoProjetosEncerradosDias()).isEqualTo(91.0);
    }

    @Test
    @DisplayName("gerar deve retornar média 0 quando não há projetos encerrados")
    void gerar_semProjetosEncerrados_mediaDeveSerZero() {
        Projeto p = Projeto.builder().status(StatusProjeto.EM_ANALISE)
                .orcamentoTotal(new BigDecimal("10000"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .previsaoTermino(LocalDate.of(2024, 2, 1)).build();

        when(projetoRepository.findAll()).thenReturn(List.of(p));
        when(projetoMembroRepository.findAll()).thenReturn(List.of());

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.mediaDuracaoProjetosEncerradosDias()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("gerar deve calcular total de membros únicos corretamente")
    void gerar_deveCalcularMembrosUnicos() {
        ProjetoMembro pm1 = ProjetoMembro.builder().membroId(1L).build();
        ProjetoMembro pm2 = ProjetoMembro.builder().membroId(2L).build();
        ProjetoMembro pm3 = ProjetoMembro.builder().membroId(1L).build(); // duplicado

        when(projetoRepository.findAll()).thenReturn(List.of());
        when(projetoMembroRepository.findAll()).thenReturn(List.of(pm1, pm2, pm3));

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.totalMembrosUnicosAlocados()).isEqualTo(2L);
    }

    @Test
    @DisplayName("gerar deve funcionar com lista vazia de projetos")
    void gerar_semProjetos_deveRetornarRelatorioVazio() {
        when(projetoRepository.findAll()).thenReturn(List.of());
        when(projetoMembroRepository.findAll()).thenReturn(List.of());

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.quantidadePorStatus()).isEmpty();
        assertThat(response.totalOrcadoPorStatus()).isEmpty();
        assertThat(response.mediaDuracaoProjetosEncerradosDias()).isEqualTo(0.0);
        assertThat(response.totalMembrosUnicosAlocados()).isEqualTo(0L);
    }

    @Test
    @DisplayName("gerar deve ignorar projetos encerrados sem dataInicio ou dataRealTermino")
    void gerar_encerradoSemDatas_naoDeveContarNaMedia() {
        Projeto semDatas = Projeto.builder().status(StatusProjeto.ENCERRADO)
                .orcamentoTotal(new BigDecimal("10000"))
                .dataInicio(null)
                .dataRealTermino(null)
                .previsaoTermino(LocalDate.of(2024, 4, 1)).build();

        when(projetoRepository.findAll()).thenReturn(List.of(semDatas));
        when(projetoMembroRepository.findAll()).thenReturn(List.of());

        PortfolioRelatorioResponse response = portfolioRelatorioService.gerar();

        assertThat(response.mediaDuracaoProjetosEncerradosDias()).isEqualTo(0.0);
    }
}
