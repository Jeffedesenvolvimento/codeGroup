package br.com.codegroup.service;

import br.com.codegroup.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "minha-chave-secreta-super-segura-para-testes");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600L);
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .usuario("joao")
                .email("joao@email.com")
                .senha("123456")
                .build();
    }

    @Test
    @DisplayName("generateToken deve retornar token não nulo para usuário válido")
    void generateToken_deveRetornarToken() {
        String token = jwtService.generateToken(buildUser());
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername deve retornar o usuário correto")
    void extractUsername_deveRetornarUsuario() {
        String token = jwtService.generateToken(buildUser());
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("joao");
    }

    @Test
    @DisplayName("extractUserId deve retornar o ID correto")
    void extractUserId_deveRetornarId() {
        String token = jwtService.generateToken(buildUser());
        Long userId = jwtService.extractUserId(token);
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("isTokenValid deve retornar true para token válido")
    void isTokenValid_tokenValido_deveRetornarTrue() {
        String token = jwtService.generateToken(buildUser());
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid deve retornar false para token inválido")
    void isTokenValid_tokenInvalido_deveRetornarFalse() {
        assertThat(jwtService.isTokenValid("token.invalido.aqui")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid deve retornar false para token expirado")
    void isTokenValid_tokenExpirado_deveRetornarFalse() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1L);
        String token = jwtService.generateToken(buildUser());
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("extractUsername deve lançar exceção para token com assinatura errada")
    void extractUsername_assinaturaErrada_deveLancarExcecao() {
        String tokenTamperedSignature = jwtService.generateToken(buildUser()) + "tampered";
        assertThatThrownBy(() -> jwtService.extractUsername(tokenTamperedSignature))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("generateToken para dois usuários distintos deve gerar tokens distintos")
    void generateToken_usuariosDistintos_devemGerarTokensDistintos() {
        User user2 = User.builder().id(2L).usuario("maria").email("maria@email.com").senha("abc").build();
        String t1 = jwtService.generateToken(buildUser());
        String t2 = jwtService.generateToken(user2);
        assertThat(t1).isNotEqualTo(t2);
    }
}
