package br.com.codegroup.security;

import br.com.codegroup.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @InjectMocks
    private SecurityFilter securityFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Rota pública /login deve passar sem verificar token")
    void rotaPublica_login_devePassarSemToken() throws Exception {
        when(request.getServletPath()).thenReturn("/login");
        securityFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Rota pública /swagger-ui deve passar sem verificar token")
    void rotaPublica_swagger_devePassarSemToken() throws Exception {
        when(request.getServletPath()).thenReturn("/swagger-ui/index.html");
        securityFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Rota pública /v3/api-docs deve passar sem verificar token")
    void rotaPublica_apiDocs_devePassarSemToken() throws Exception {
        when(request.getServletPath()).thenReturn("/v3/api-docs");
        securityFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Requisição sem header Authorization deve retornar 401")
    void semAuthHeader_deveRetornar401() throws Exception {
        when(request.getServletPath()).thenReturn("/api/projetos");
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Header Authorization sem Bearer deve retornar 401")
    void authHeaderSemBearer_deveRetornar401() throws Exception {
        when(request.getServletPath()).thenReturn("/api/projetos");
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Token válido deve autenticar e chamar filterChain")
    void tokenValido_deveAutenticarEContinuar() throws Exception {
        when(request.getServletPath()).thenReturn("/api/projetos");
        when(request.getHeader("Authorization")).thenReturn("Bearer token.valido.aqui");
        when(jwtService.extractUsername("token.valido.aqui")).thenReturn("joao");

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo("joao");
    }

    @Test
    @DisplayName("Token inválido deve retornar 401")
    void tokenInvalido_deveRetornar401() throws Exception {
        when(request.getServletPath()).thenReturn("/api/projetos");
        when(request.getHeader("Authorization")).thenReturn("Bearer token.invalido");
        when(jwtService.extractUsername("token.invalido")).thenThrow(new RuntimeException("JWT inválido"));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Não deve sobrescrever autenticação já existente no contexto")
    void autenticacaoJaExistente_naoDeveSubstituir() throws Exception {
        // Seta autenticação manualmente no contexto
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "usuario-existente", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getServletPath()).thenReturn("/api/projetos");
        when(request.getHeader("Authorization")).thenReturn("Bearer token.valido");
        when(jwtService.extractUsername("token.valido")).thenReturn("novo-usuario");

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // autenticação original deve permanecer
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo("usuario-existente");
    }
}
