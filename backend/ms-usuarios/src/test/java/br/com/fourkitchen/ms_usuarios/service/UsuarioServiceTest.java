package br.com.fourkitchen.ms_usuarios.service;

import br.com.fourkitchen.ms_usuarios.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.exception.BaseException;
import br.com.fourkitchen.ms_usuarios.exception.ErrorEnum;
import br.com.fourkitchen.ms_usuarios.mapper.CriarUsuarioRequestMapper;
import br.com.fourkitchen.ms_usuarios.mapper.UsuarioResponseMapper;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import br.com.fourkitchen.ms_usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioResponseMapper usuarioResponseMapper;

    @Mock
    private CriarUsuarioRequestMapper criarUsuarioRequestMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void buscarUsuariosAtivosDeveRetornarUsuariosMapeados() {
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha", true);
        UsuarioResponse response = new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilUsuario(),
                usuario.getIdMesa(),
                usuario.getAtivo()
        );

        when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of(usuario));
        when(usuarioResponseMapper.map(usuario)).thenReturn(response);

        List<UsuarioResponse> resultado = usuarioService.buscarUsuariosAtivos();

        assertEquals(List.of(response), resultado);
        verify(usuarioRepository).findByAtivoTrue();
        verify(usuarioResponseMapper).map(usuario);
    }

    @Test
    void buscarUsuariosDeveIncluirAtivosEInativos() {
        Usuario ativo = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha", true);
        Usuario inativo = criarUsuario(2, "Bruno", "bruno@email.com", PerfilUsuario.GARCOM, "senha", false);
        UsuarioResponse respostaAtivo = new UsuarioResponse(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, null, true);
        UsuarioResponse respostaInativo = new UsuarioResponse(2, "Bruno", "bruno@email.com", PerfilUsuario.GARCOM, null, false);

        when(usuarioRepository.findAll()).thenReturn(List.of(ativo, inativo));
        when(usuarioResponseMapper.map(ativo)).thenReturn(respostaAtivo);
        when(usuarioResponseMapper.map(inativo)).thenReturn(respostaInativo);

        List<UsuarioResponse> resultado = usuarioService.buscarUsuarios();

        assertEquals(List.of(respostaAtivo, respostaInativo), resultado);
        verify(usuarioRepository).findAll();
    }

    @Test
    void criarUsuarioDeveSalvarUsuarioComSenhaCriptografadaEAtivo() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "LUCAS DA SILVA",
                "lucas@email.com",
                "Senha123",
                PerfilUsuario.ADMIN,
                null
        );
        Usuario usuarioMapeado = criarUsuario(null, "LUCAS DA SILVA", "lucas@email.com", PerfilUsuario.ADMIN, "Senha123", null);
        Usuario usuarioSalvo = criarUsuario(1, "Lucas da silva", "lucas@email.com", PerfilUsuario.ADMIN, "senha-criptografada", true);
        UsuarioResponse response = new UsuarioResponse(1, "Lucas da silva", "lucas@email.com", PerfilUsuario.ADMIN, null, true);

        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(criarUsuarioRequestMapper.map(request)).thenReturn(usuarioMapeado);
        when(passwordEncoder.encode(usuarioMapeado.getSenha())).thenReturn("senha-criptografada");
        when(usuarioRepository.save(usuarioMapeado)).thenReturn(usuarioSalvo);
        when(usuarioResponseMapper.map(usuarioSalvo)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.criarUsuario(request);

        assertSame(response, resultado);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioEnviadoParaSalvar = usuarioCaptor.getValue();
        assertEquals("Lucas da silva", usuarioEnviadoParaSalvar.getNome());
        assertEquals("senha-criptografada", usuarioEnviadoParaSalvar.getSenha());
        assertEquals(true, usuarioEnviadoParaSalvar.getAtivo());
        verify(usuarioRepository).existsByEmailIgnoreCase(request.email());
        verify(criarUsuarioRequestMapper).map(request);
        verify(passwordEncoder).encode("Senha123");
        verify(usuarioResponseMapper).map(usuarioSalvo);
    }

    @Test
    void criarUsuarioDeveLancarExcecaoQuandoEmailJaExiste() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Lucas",
                "lucas@email.com",
                "Senha123",
                PerfilUsuario.ADMIN,
                null
        );

        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.criarUsuario(request));

        assertEquals(ErrorEnum.EMAIL_JA_CADASTRADO, exception.getErrorEnum());

        verify(usuarioRepository).existsByEmailIgnoreCase(request.email());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(criarUsuarioRequestMapper, passwordEncoder, usuarioResponseMapper);
    }

    @Test
    void criarUsuarioMesaDeveExigirIdMesa() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Mesa 1",
                "mesa01@fourkitchen.com",
                "Senha123",
                PerfilUsuario.MESA,
                null
        );

        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);

        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.criarUsuario(request));

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
        verify(usuarioRepository).existsByEmailIgnoreCase(request.email());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(criarUsuarioRequestMapper, passwordEncoder, usuarioResponseMapper);
    }

    @Test
    void criarUsuarioHumanoDeveRejeitarIdMesa() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Lucas",
                "lucas@email.com",
                "Senha123",
                PerfilUsuario.ADMIN,
                1
        );

        when(usuarioRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);

        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.criarUsuario(request));

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
        verify(usuarioRepository).existsByEmailIgnoreCase(request.email());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(criarUsuarioRequestMapper, passwordEncoder, usuarioResponseMapper);
    }

    @Test
    void atualizarUsuarioDeveSalvarDadosSemAlterarSenhaQuandoNaoInformada() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Lucas Atualizado",
                "lucas.atualizado@email.com",
                null,
                PerfilUsuario.GESTOR,
                null
        );
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha-antiga", true);
        UsuarioResponse response = new UsuarioResponse(
                1,
                "Lucas Atualizado",
                "lucas.atualizado@email.com",
                PerfilUsuario.GESTOR,
                null,
                true
        );

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1)).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioResponseMapper.map(usuario)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.atualizarUsuario(1, request);

        assertSame(response, resultado);
        assertEquals("Lucas atualizado", usuario.getNome());
        assertEquals("lucas.atualizado@email.com", usuario.getEmail());
        assertEquals(PerfilUsuario.GESTOR, usuario.getPerfilUsuario());
        assertEquals("senha-antiga", usuario.getSenha());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void atualizarUsuarioDeveCriptografarSenhaQuandoInformada() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Lucas Atualizado",
                "lucas@email.com",
                "NovaSenha123",
                PerfilUsuario.ADMIN,
                null
        );
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha-antiga", true);
        UsuarioResponse response = new UsuarioResponse(1, "Lucas Atualizado", "lucas@email.com", PerfilUsuario.ADMIN, null, true);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1)).thenReturn(false);
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioResponseMapper.map(usuario)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.atualizarUsuario(1, request);

        assertSame(response, resultado);
        assertEquals("senha-criptografada", usuario.getSenha());
        verify(passwordEncoder).encode("NovaSenha123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void atualizarUsuarioDeveBloquearEmailUsadoPorOutroUsuario() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Lucas",
                "duplicado@email.com",
                null,
                PerfilUsuario.ADMIN,
                null
        );
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha", true);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1)).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.atualizarUsuario(1, request));

        assertEquals(ErrorEnum.EMAIL_JA_CADASTRADO, exception.getErrorEnum());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void atualizarUsuarioDeveLancarUsuarioNaoEncontrado() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Lucas",
                "lucas@email.com",
                null,
                PerfilUsuario.ADMIN,
                null
        );

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.atualizarUsuario(99, request));

        assertEquals(ErrorEnum.USUARIO_NAO_ENCONTRADO, exception.getErrorEnum());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void inativarUsuarioDeveAlterarAtivoParaFalse() {
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha", true);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        usuarioService.inativarUsuario(1, 99);

        assertEquals(false, usuario.getAtivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void inativarUsuarioDeveBloquearProprioUsuario() {
        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.inativarUsuario(1, 1));

        assertEquals(ErrorEnum.NAO_PODE_EXCLUIR_PROPRIO_USUARIO, exception.getErrorEnum());
        verify(usuarioRepository, never()).findById(org.mockito.ArgumentMatchers.anyInt());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void inativarUsuarioDeveBloquearUsuarioJaInativo() {
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha", false);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        BaseException exception = assertThrows(BaseException.class, () -> usuarioService.inativarUsuario(1, 99));

        assertEquals(ErrorEnum.USUARIO_JA_INATIVO, exception.getErrorEnum());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ativarUsuarioDeveAlterarAtivoParaTrueERetornarUsuario() {
        Usuario usuario = criarUsuario(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, "senha", false);
        UsuarioResponse response = new UsuarioResponse(1, "Lucas", "lucas@email.com", PerfilUsuario.ADMIN, null, true);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioResponseMapper.map(usuario)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.ativarUsuario(1);

        assertSame(response, resultado);
        assertEquals(true, usuario.getAtivo());
        verify(usuarioRepository).save(usuario);
    }

    private Usuario criarUsuario(
            Integer id,
            String nome,
            String email,
            PerfilUsuario perfilUsuario,
            String senha,
            Boolean ativo
    ) {
        return Usuario.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .perfilUsuario(perfilUsuario)
                .senha(senha)
                .ativo(ativo)
                .build();
    }
}
