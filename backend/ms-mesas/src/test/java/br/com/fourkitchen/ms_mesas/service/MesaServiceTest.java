package br.com.fourkitchen.ms_mesas.service;

import br.com.fourkitchen.ms_mesas.client.PedidosAtivosClient;
import br.com.fourkitchen.ms_mesas.client.ResumoContaAtendimentoResponse;
import br.com.fourkitchen.ms_mesas.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.ms_mesas.dto.request.CriarMesaRequest;
import br.com.fourkitchen.ms_mesas.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaPaginadaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.ResumoMesasOperacaoResponse;
import br.com.fourkitchen.ms_mesas.enums.StatusMesa;
import br.com.fourkitchen.ms_mesas.exception.BaseException;
import br.com.fourkitchen.ms_mesas.exception.ErrorEnum;
import br.com.fourkitchen.ms_mesas.mapper.CriarMesaRequestMapper;
import br.com.fourkitchen.ms_mesas.mapper.MesaGarcomResponseMapper;
import br.com.fourkitchen.ms_mesas.mapper.MesaResponseMapper;
import br.com.fourkitchen.ms_mesas.model.Atendimento;
import br.com.fourkitchen.ms_mesas.model.HistoricoAtendimento;
import br.com.fourkitchen.ms_mesas.model.Mesa;
import br.com.fourkitchen.ms_mesas.repository.AtendimentoRepository;
import br.com.fourkitchen.ms_mesas.repository.HistoricoAtendimentoRepository;
import br.com.fourkitchen.ms_mesas.repository.MesaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private HistoricoAtendimentoRepository historicoAtendimentoRepository;

    @Mock
    private PedidosAtivosClient pedidosAtivosClient;

    @Mock
    private MesaResponseMapper mesaResponseMapper;

    @Mock
    private MesaGarcomResponseMapper mesaGarcomResponseMapper;

    @Mock
    private CriarMesaRequestMapper criarMesaRequestMapper;

    @InjectMocks
    private MesaService mesaService;

    @Test
    void listarMesasDeveRetornarMesasMapeadas() {
        Mesa mesa = criarMesa(1, 10, true, null);
        MesaResponse response = new MesaResponse(1, 10, StatusMesa.DISPONIVEL, null, null, null, null, null);

        when(mesaRepository.findAll()).thenReturn(List.of(mesa));
        when(mesaResponseMapper.map(mesa)).thenReturn(response);

        List<MesaResponse> resultado = mesaService.listarMesas();

        assertEquals(List.of(response), resultado);
        verify(mesaRepository).findAll();
        verify(mesaResponseMapper).map(mesa);
    }

    @Test
    void listarMesasPaginadasDeveRetornarPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 10);
        Mesa mesa = criarMesa(1, 10, true, null);
        MesaResponse response = new MesaResponse(1, 10, StatusMesa.DISPONIVEL, null, null, null, null, null);

        when(mesaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(mesa), pageable, 1));
        when(mesaResponseMapper.map(mesa)).thenReturn(response);

        MesaPaginadaResponse resultado = mesaService.listarMesasPaginadas(pageable);

        assertEquals(List.of(response), resultado.content());
        assertEquals(0, resultado.page());
        assertEquals(10, resultado.size());
        assertEquals(1L, resultado.totalElements());
        assertEquals(1, resultado.totalPages());
        assertEquals(true, resultado.first());
        assertEquals(true, resultado.last());
        verify(mesaRepository).findAll(pageable);
        verify(mesaResponseMapper).map(mesa);
    }

    @Test
    void listarMesasPorGarcomDeveRetornarSomenteMesasAtribuidasAoGarcom() {
        Atendimento atendimento = criarAtendimento(8, 123456);
        atendimento.setGarcomId(7);
        Mesa mesa = criarMesa(1, 10, false, atendimento);
        MesaGarcomResponse response = new MesaGarcomResponse(
                1,
                10,
                StatusMesa.OCUPADA,
                8,
                123456,
                7,
                null
        );

        when(mesaRepository.findByDisponivelFalseAndAtendimento_GarcomIdAndAtendimento_DataFechamentoIsNullOrderByNumeroAsc(7))
                .thenReturn(List.of(mesa));
        when(mesaGarcomResponseMapper.map(mesa)).thenReturn(response);

        List<MesaGarcomResponse> resultado = mesaService.listarMesasPorGarcom(7);

        assertEquals(List.of(response), resultado);
        verify(mesaRepository)
                .findByDisponivelFalseAndAtendimento_GarcomIdAndAtendimento_DataFechamentoIsNullOrderByNumeroAsc(7);
        verify(mesaGarcomResponseMapper).map(mesa);
        verifyNoInteractions(mesaResponseMapper, atendimentoRepository, pedidosAtivosClient);
    }

    @Test
    void criarMesaDeveSalvarMesaDisponivel() {
        CriarMesaRequest request = new CriarMesaRequest(10);
        Mesa mesaMapeada = criarMesa(null, 10, null, null);
        Mesa mesaSalva = criarMesa(1, 10, true, null);
        MesaResponse response = new MesaResponse(1, 10, StatusMesa.DISPONIVEL, null, null, null, null, null);

        when(mesaRepository.existsByNumero(10)).thenReturn(false);
        when(criarMesaRequestMapper.map(request)).thenReturn(mesaMapeada);
        when(mesaRepository.save(mesaMapeada)).thenReturn(mesaSalva);
        when(mesaResponseMapper.map(mesaSalva)).thenReturn(response);

        MesaResponse resultado = mesaService.criarMesa(request);

        assertSame(response, resultado);

        ArgumentCaptor<Mesa> mesaCaptor = ArgumentCaptor.forClass(Mesa.class);
        verify(mesaRepository).save(mesaCaptor.capture());

        Mesa mesaEnviadaParaSalvar = mesaCaptor.getValue();
        assertEquals(true, mesaEnviadaParaSalvar.getDisponivel());
        verify(mesaRepository).existsByNumero(10);
        verify(criarMesaRequestMapper).map(request);
        verify(mesaResponseMapper).map(mesaSalva);
    }

    @Test
    void criarMesaDeveBloquearNumeroDuplicado() {
        CriarMesaRequest request = new CriarMesaRequest(10);

        when(mesaRepository.existsByNumero(10)).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.criarMesa(request));

        assertEquals(ErrorEnum.NUMERO_MESA_JA_CADASTRADO, exception.getErrorEnum());
        verify(mesaRepository).existsByNumero(10);
        verify(mesaRepository, never()).save(any());
        verifyNoInteractions(criarMesaRequestMapper, mesaResponseMapper, atendimentoRepository, pedidosAtivosClient);
    }

    @Test
    void abrirMesaDeveCriarAtendimentoEOcuparMesa() {
        Mesa mesa = criarMesa(1, 10, true, null);
        Atendimento atendimentoSalvo = criarAtendimento(1, 123456);
        Mesa mesaSalva = criarMesa(1, 10, false, atendimentoSalvo);
        MesaResponse response = new MesaResponse(1, 10, StatusMesa.OCUPADA, null, 123456, null, null, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));
        when(atendimentoRepository.existsByCodigoSessao(anyInt())).thenReturn(false);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimentoSalvo);
        when(mesaRepository.save(mesa)).thenReturn(mesaSalva);
        when(mesaResponseMapper.map(mesaSalva)).thenReturn(response);

        MesaResponse resultado = mesaService.abrirMesa(1);

        assertSame(response, resultado);

        ArgumentCaptor<Atendimento> atendimentoCaptor = ArgumentCaptor.forClass(Atendimento.class);
        verify(atendimentoRepository).save(atendimentoCaptor.capture());

        Atendimento atendimentoEnviadoParaSalvar = atendimentoCaptor.getValue();
        assertNotNull(atendimentoEnviadoParaSalvar.getCodigoSessao());
        assertSame(mesa, atendimentoEnviadoParaSalvar.getMesa());
        assertNotNull(atendimentoEnviadoParaSalvar.getDataAbertura());
        assertEquals(false, mesa.getDisponivel());
        assertSame(atendimentoSalvo, mesa.getAtendimento());
        verify(mesaRepository).findById(1);
        verify(mesaRepository).save(mesa);
        verify(mesaResponseMapper).map(mesaSalva);
    }

    @Test
    void abrirMesaDeveLancarExcecaoQuandoMesaNaoEstiverDisponivel() {
        Atendimento atendimento = criarAtendimento(1, 123456);
        Mesa mesa = criarMesa(1, 10, false, atendimento);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.abrirMesa(1));

        assertEquals(ErrorEnum.MESA_NAO_DISPONIVEL, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verify(mesaRepository, never()).save(any());
        verifyNoInteractions(atendimentoRepository, mesaResponseMapper, pedidosAtivosClient);
    }

    @Test
    void fecharMesaDeveDisponibilizarMesaELimparAtendimento() {
        Atendimento atendimento = criarAtendimento(1, 123456);
        atendimento.setDataAbertura(LocalDateTime.of(2026, 7, 2, 10, 0));
        atendimento.setGarcomId(7);
        Mesa mesa = criarMesa(1, 10, false, atendimento);
        Mesa mesaSalva = criarMesa(1, 10, true, null);
        MesaResponse response = new MesaResponse(1, 10, StatusMesa.DISPONIVEL, null, null, null, null, null);
        ResumoContaAtendimentoResponse resumo = new ResumoContaAtendimentoResponse(
                1,
                new BigDecimal("149.70"),
                3,
                7
        );

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));
        when(pedidosAtivosClient.possuiPedidosAtivos(1)).thenReturn(false);
        when(pedidosAtivosClient.buscarResumoConta(1)).thenReturn(resumo);
        when(atendimentoRepository.save(atendimento)).thenReturn(atendimento);
        when(mesaRepository.save(mesa)).thenReturn(mesaSalva);
        when(mesaResponseMapper.map(mesaSalva)).thenReturn(response);

        MesaResponse resultado = mesaService.fecharMesa(1);

        assertSame(response, resultado);
        assertEquals(true, mesa.getDisponivel());
        assertEquals(null, mesa.getAtendimento());
        assertNotNull(atendimento.getDataFechamento());
        ArgumentCaptor<HistoricoAtendimento> historicoCaptor = ArgumentCaptor.forClass(HistoricoAtendimento.class);
        verify(mesaRepository).findById(1);
        verify(pedidosAtivosClient).possuiPedidosAtivos(1);
        verify(pedidosAtivosClient).buscarResumoConta(1);
        verify(atendimentoRepository).save(atendimento);
        verify(historicoAtendimentoRepository).save(historicoCaptor.capture());
        verify(mesaRepository).save(mesa);
        verify(mesaResponseMapper).map(mesaSalva);

        HistoricoAtendimento historico = historicoCaptor.getValue();
        assertEquals(1, historico.getIdAtendimento());
        assertEquals(123456, historico.getCodigoSessao());
        assertEquals(1, historico.getIdMesa());
        assertEquals(10, historico.getNumeroMesa());
        assertEquals(7, historico.getIdGarcom());
        assertEquals(new BigDecimal("149.70"), historico.getValorFinal());
        assertEquals(3, historico.getTotalPedidos());
        assertEquals(7, historico.getTotalItens());
        assertEquals(LocalDateTime.of(2026, 7, 2, 10, 0), historico.getDataAbertura());
        assertNotNull(historico.getDataFechamento());
        assertNotNull(historico.getDuracaoMinutos());
    }

    @Test
    void listarHistoricoAtendimentosDeveRetornarHistoricoOrdenadoDoRepositorio() {
        HistoricoAtendimento historico = HistoricoAtendimento.builder()
                .id(1)
                .idAtendimento(8)
                .codigoSessao(123456)
                .idMesa(1)
                .numeroMesa(10)
                .idGarcom(7)
                .nomeGarcom("Amanda Souza")
                .valorFinal(new BigDecimal("149.70"))
                .totalPedidos(3)
                .totalItens(7)
                .dataAbertura(LocalDateTime.of(2026, 7, 2, 10, 0))
                .dataFechamento(LocalDateTime.of(2026, 7, 2, 11, 20))
                .duracaoMinutos(80)
                .build();

        when(historicoAtendimentoRepository.findAllByOrderByDataFechamentoDescIdDesc())
                .thenReturn(List.of(historico));

        List<HistoricoAtendimentoResponse> resultado = mesaService.listarHistoricoAtendimentos();

        assertEquals(1, resultado.size());
        assertEquals(8, resultado.getFirst().idAtendimento());
        assertEquals(10, resultado.getFirst().numeroMesa());
        assertEquals("Amanda Souza", resultado.getFirst().nomeGarcom());
        assertEquals(new BigDecimal("149.70"), resultado.getFirst().valorFinal());
        assertEquals(80, resultado.getFirst().duracaoMinutos());
        verify(historicoAtendimentoRepository).findAllByOrderByDataFechamentoDescIdDesc();
    }

    @Test
    void fecharMesaDeveLancarExcecaoQuandoExistiremPedidosAtivos() {
        Atendimento atendimento = criarAtendimento(1, 123456);
        Mesa mesa = criarMesa(1, 10, false, atendimento);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));
        when(pedidosAtivosClient.possuiPedidosAtivos(1)).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.fecharMesa(1));

        assertEquals(ErrorEnum.MESA_COM_PEDIDOS_ATIVOS, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verify(pedidosAtivosClient).possuiPedidosAtivos(1);
        verify(mesaRepository, never()).save(any());
        verifyNoInteractions(atendimentoRepository, mesaResponseMapper);
    }

    @Test
    void fecharMesaDeveLancarExcecaoQuandoMesaJaEstiverDisponivel() {
        Mesa mesa = criarMesa(1, 10, true, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.fecharMesa(1));

        assertEquals(ErrorEnum.MESA_NAO_OCUPADA, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verify(mesaRepository, never()).save(any());
        verifyNoInteractions(mesaResponseMapper, atendimentoRepository, pedidosAtivosClient);
    }

    @Test
    void fecharMesaDeveLancarExcecaoQuandoNaoExistirAtendimentoAberto() {
        Mesa mesa = criarMesa(1, 10, false, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.fecharMesa(1));

        assertEquals(ErrorEnum.ATENDIMENTO_NAO_ABERTO, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verify(mesaRepository, never()).save(any());
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void atribuirGarcomDeveAtualizarAtendimentoDaMesa() {
        AtribuirGarcomRequest request = new AtribuirGarcomRequest(7);
        Atendimento atendimento = criarAtendimento(1, 123456);
        Mesa mesa = criarMesa(1, 10, false, atendimento);
        Atendimento atendimentoSalvo = criarAtendimento(1, 123456);
        atendimentoSalvo.setGarcomId(7);
        MesaResponse response = new MesaResponse(1, 10, StatusMesa.OCUPADA, 7, 123456, null, null, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));
        when(atendimentoRepository.save(atendimento)).thenReturn(atendimentoSalvo);
        when(mesaResponseMapper.map(mesa)).thenReturn(response);

        MesaResponse resultado = mesaService.atribuirGarcom(1, request);

        assertSame(response, resultado);
        assertEquals(7, atendimento.getGarcomId());
        assertSame(atendimentoSalvo, mesa.getAtendimento());
        verify(mesaRepository).findById(1);
        verify(atendimentoRepository).save(atendimento);
        verify(mesaResponseMapper).map(mesa);
        verifyNoInteractions(pedidosAtivosClient);
    }

    @Test
    void atribuirGarcomDeveLancarExcecaoQuandoMesaNaoPossuirAtendimentoAberto() {
        AtribuirGarcomRequest request = new AtribuirGarcomRequest(7);
        Mesa mesa = criarMesa(1, 10, true, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.atribuirGarcom(1, request));

        assertEquals(ErrorEnum.ATENDIMENTO_NAO_ABERTO, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void atribuirGarcomDeveLancarExcecaoQuandoGarcomForInvalido() {
        AtribuirGarcomRequest request = new AtribuirGarcomRequest(0);
        Atendimento atendimento = criarAtendimento(1, 123456);
        Mesa mesa = criarMesa(1, 10, false, atendimento);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.atribuirGarcom(1, request));

        assertEquals(ErrorEnum.GARCOM_INVALIDO, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void atribuirGarcomDeveLancarExcecaoQuandoNaoExistirAtendimentoAberto() {
        AtribuirGarcomRequest request = new AtribuirGarcomRequest(7);
        Mesa mesa = criarMesa(1, 10, false, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.atribuirGarcom(1, request));

        assertEquals(ErrorEnum.ATENDIMENTO_NAO_ABERTO, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void validarMesaAtribuidaGarcomDeveRetornarSessaoQuandoMesaOcupadaForDoGarcom() {
        Atendimento atendimento = criarAtendimento(8, 123456);
        atendimento.setGarcomId(7);
        Mesa mesa = criarMesa(1, 10, false, atendimento);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        var response = mesaService.validarMesaAtribuidaGarcom(1, 7);

        assertEquals(1, response.idMesa());
        assertEquals(8, response.idAtendimento());
        assertEquals(123456, response.codigoSessao());
        assertEquals(7, response.idGarcom());
        assertEquals(StatusMesa.OCUPADA, response.status());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void buscarAtendimentoAtualDeveRetornarSessaoDaMesaOcupada() {
        Atendimento atendimento = criarAtendimento(8, 123456);
        atendimento.setGarcomId(7);
        Mesa mesa = criarMesa(1, 10, false, atendimento);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        var response = mesaService.buscarAtendimentoAtual(1);

        assertEquals(1, response.idMesa());
        assertEquals(8, response.idAtendimento());
        assertEquals(123456, response.codigoSessao());
        assertEquals(7, response.idGarcom());
        assertEquals(StatusMesa.OCUPADA, response.status());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void buscarAtendimentoAtualDeveBloquearMesaDisponivel() {
        Mesa mesa = criarMesa(1, 10, true, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.buscarAtendimentoAtual(1));

        assertEquals(ErrorEnum.MESA_NAO_OCUPADA, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void validarMesaAtribuidaGarcomDeveBloquearMesaDeOutroGarcom() {
        Atendimento atendimento = criarAtendimento(8, 123456);
        atendimento.setGarcomId(9);
        Mesa mesa = criarMesa(1, 10, false, atendimento);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.validarMesaAtribuidaGarcom(1, 7));

        assertEquals(ErrorEnum.MESA_NAO_ATRIBUIDA_AO_GARCOM, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void validarMesaAtribuidaGarcomDeveBloquearMesaDisponivel() {
        Mesa mesa = criarMesa(1, 10, true, null);

        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.validarMesaAtribuidaGarcom(1, 7));

        assertEquals(ErrorEnum.MESA_NAO_OCUPADA, exception.getErrorEnum());
        verify(mesaRepository).findById(1);
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void abrirMesaDeveLancarExcecaoQuandoMesaNaoExistir() {
        when(mesaRepository.findById(99)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> mesaService.abrirMesa(99));

        assertEquals(ErrorEnum.MESA_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(mesaRepository).findById(99);
        verify(mesaRepository, never()).save(any());
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    @Test
    void buscarResumoOperacaoDeveContarMesasOcupadas() {
        when(mesaRepository.countByDisponivelFalse()).thenReturn(8L);

        ResumoMesasOperacaoResponse resultado = mesaService.buscarResumoOperacao();

        assertEquals(8L, resultado.mesasOcupadas());
        verify(mesaRepository).countByDisponivelFalse();
        verifyNoInteractions(atendimentoRepository, pedidosAtivosClient, mesaResponseMapper);
    }

    private Mesa criarMesa(Integer id, Integer numero, Boolean disponivel, Atendimento atendimento) {
        return Mesa.builder()
                .id(id)
                .numero(numero)
                .disponivel(disponivel)
                .atendimento(atendimento)
                .build();
    }

    private Atendimento criarAtendimento(Integer id, Integer codigoSessao) {
        return Atendimento.builder()
                .id(id)
                .codigoSessao(codigoSessao)
                .build();
    }
}
