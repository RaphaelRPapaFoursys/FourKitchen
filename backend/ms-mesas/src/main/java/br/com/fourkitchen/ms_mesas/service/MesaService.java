package br.com.fourkitchen.ms_mesas.service;

import br.com.fourkitchen.ms_mesas.client.PedidosAtivosClient;
import br.com.fourkitchen.ms_mesas.client.ResumoContaAtendimentoResponse;
import br.com.fourkitchen.ms_mesas.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.ms_mesas.dto.request.CriarMesaRequest;
import br.com.fourkitchen.ms_mesas.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaPaginadaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaOpcaoResponse;
import br.com.fourkitchen.ms_mesas.dto.response.ResumoMesasOperacaoResponse;
import br.com.fourkitchen.ms_mesas.dto.response.SessaoMesaResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;

    private final AtendimentoRepository atendimentoRepository;

    private final HistoricoAtendimentoRepository historicoAtendimentoRepository;

    private final PedidosAtivosClient pedidosAtivosClient;

    private final MesaResponseMapper mesaResponseMapper;

    private final MesaGarcomResponseMapper mesaGarcomResponseMapper;

    private final CriarMesaRequestMapper criarMesaRequestMapper;

    public List<MesaResponse> listarMesas() {
        return mesaRepository.findAll()
                .stream()
                .map(mesaResponseMapper::map)
                .toList();
    }

    public List<MesaOpcaoResponse> listarOpcoes() {
        return mesaRepository.buscarOpcoesOrdenadas()
                .stream()
                .map(mesa -> new MesaOpcaoResponse(mesa.getId(), mesa.getNumero()))
                .toList();
    }

    //DEVOLVE UMA PAGE
    public MesaPaginadaResponse listarMesasPaginadas(Pageable pageable) {
        return MesaPaginadaResponse.from(
                mesaRepository.findAll(pageable)
                        .map(mesaResponseMapper::map)
        );
    }

    public List<MesaGarcomResponse> listarMesasPorGarcom(Integer garcomId) {
        validarGarcomExisteComPerfilGarcom(garcomId);

        return mesaRepository
                .findByDisponivelFalseAndAtendimento_GarcomIdAndAtendimento_DataFechamentoIsNullOrderByNumeroAsc(
                        garcomId
                )
                .stream()
                .map(mesaGarcomResponseMapper::map)
                .toList();
    }

    public ResumoMesasOperacaoResponse buscarResumoOperacao() {
        return new ResumoMesasOperacaoResponse(mesaRepository.countByDisponivelFalse());
    }

    public List<HistoricoAtendimentoResponse> listarHistoricoAtendimentos() {
        return historicoAtendimentoRepository.findAllByOrderByDataFechamentoDescIdDesc()
                .stream()
                .map(this::mapearHistoricoAtendimento)
                .toList();
    }

    @Transactional
    public MesaResponse criarMesa(CriarMesaRequest request) {
        if (mesaRepository.existsByNumero(request.numero())) {
            throw new BaseException(ErrorEnum.NUMERO_MESA_JA_CADASTRADO);
        }

        Mesa mesa = criarMesaRequestMapper.map(request);
        mesa.setDisponivel(true);

        Mesa mesaSalva = mesaRepository.save(mesa);

        return mesaResponseMapper.map(mesaSalva);
    }

    @Transactional
    public MesaResponse abrirMesa(Integer id) {
        Mesa mesa = buscarPorId(id);

        if (!Boolean.TRUE.equals(mesa.getDisponivel())) {
            throw new BaseException(ErrorEnum.MESA_NAO_DISPONIVEL);
        }

        Atendimento atendimento = Atendimento.builder()
                .codigoSessao(gerarCodigoSessaoUnico())
                .mesa(mesa)
                .dataAbertura(LocalDateTime.now())
                .build();

        Atendimento atendimentoSalvo = atendimentoRepository.save(atendimento);

        mesa.setDisponivel(false);
        mesa.setAtendimento(atendimentoSalvo);

        Mesa mesaSalva = mesaRepository.save(mesa);

        return mesaResponseMapper.map(mesaSalva);
    }

    @Transactional
    public MesaResponse fecharMesa(Integer id) {
        Mesa mesa = buscarPorId(id);

        if (Boolean.TRUE.equals(mesa.getDisponivel())) {
            throw new BaseException(ErrorEnum.MESA_NAO_OCUPADA);
        }

        validarMesaSemPedidosAtivos(mesa);

        Atendimento atendimento = buscarAtendimentoAberto(mesa);
        LocalDateTime dataFechamento = LocalDateTime.now();
        atendimento.setDataFechamento(dataFechamento);
        atendimentoRepository.save(atendimento);
        salvarHistoricoAtendimento(mesa, atendimento);

        mesa.setDisponivel(true);
        mesa.setAtendimento(null);

        Mesa mesaSalva = mesaRepository.save(mesa);

        return mesaResponseMapper.map(mesaSalva);
    }

    @Transactional
    public MesaResponse atribuirGarcom(Integer id, AtribuirGarcomRequest request) {
        Mesa mesa = buscarPorId(id);

        if (Boolean.TRUE.equals(mesa.getDisponivel())) {
            throw new BaseException(ErrorEnum.ATENDIMENTO_NAO_ABERTO);
        }

        validarGarcomExisteComPerfilGarcom(request.garcomId());

        Atendimento atendimento = buscarAtendimentoAberto(mesa);
        atendimento.setGarcomId(request.garcomId());
        Atendimento atendimentoSalvo = atendimentoRepository.save(atendimento);
        mesa.setAtendimento(atendimentoSalvo);

        return mesaResponseMapper.map(mesa);
    }

    public SessaoMesaResponse validarSessaoMesa(Integer idMesa, Integer codigoSessao) {
        Mesa mesa = buscarPorId(idMesa);

        if (Boolean.TRUE.equals(mesa.getDisponivel())) {
            throw new BaseException(ErrorEnum.MESA_NAO_OCUPADA);
        }

        Atendimento atendimento = buscarAtendimentoAberto(mesa);

        if (atendimento.getDataFechamento() != null
                || !Objects.equals(codigoSessao, atendimento.getCodigoSessao())) {
            throw new BaseException(ErrorEnum.CODIGO_SESSAO_INVALIDO);
        }

        return new SessaoMesaResponse(
                mesa.getId(),
                atendimento.getId(),
                atendimento.getCodigoSessao(),
                atendimento.getGarcomId(),
                StatusMesa.OCUPADA
        );
    }

    public SessaoMesaResponse buscarAtendimentoAtual(Integer idMesa) {
        Mesa mesa = buscarPorId(idMesa);

        if (Boolean.TRUE.equals(mesa.getDisponivel())) {
            throw new BaseException(ErrorEnum.MESA_NAO_OCUPADA);
        }

        Atendimento atendimento = buscarAtendimentoAberto(mesa);

        return new SessaoMesaResponse(
                mesa.getId(),
                atendimento.getId(),
                atendimento.getCodigoSessao(),
                atendimento.getGarcomId(),
                StatusMesa.OCUPADA
        );
    }

    public SessaoMesaResponse validarMesaAtribuidaGarcom(Integer idMesa, Integer idGarcom) {
        Mesa mesa = buscarPorId(idMesa);

        if (Boolean.TRUE.equals(mesa.getDisponivel())) {
            throw new BaseException(ErrorEnum.MESA_NAO_OCUPADA);
        }

        Atendimento atendimento = buscarAtendimentoAberto(mesa);

        if (atendimento.getDataFechamento() != null) {
            throw new BaseException(ErrorEnum.ATENDIMENTO_NAO_ABERTO);
        }

        if (!Objects.equals(atendimento.getGarcomId(), idGarcom)) {
            throw new BaseException(ErrorEnum.MESA_NAO_ATRIBUIDA_AO_GARCOM);
        }

        return new SessaoMesaResponse(
                mesa.getId(),
                atendimento.getId(),
                atendimento.getCodigoSessao(),
                atendimento.getGarcomId(),
                StatusMesa.OCUPADA
        );
    }

    private Mesa buscarPorId(Integer id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.MESA_NAO_ENCONTRADA));
    }

    private Integer gerarCodigoSessaoUnico() {
        Integer codigoSessao;

        do {
            codigoSessao = ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (atendimentoRepository.existsByCodigoSessao(codigoSessao));

        return codigoSessao;
    }

    private void validarMesaSemPedidosAtivos(Mesa mesa) {
        if (possuiPedidosAtivos(mesa)) {
            throw new BaseException(ErrorEnum.MESA_COM_PEDIDOS_ATIVOS);
        }
    }

    private boolean possuiPedidosAtivos(Mesa mesa) {
        Atendimento atendimento = buscarAtendimentoAberto(mesa);

        return pedidosAtivosClient.possuiPedidosAtivos(atendimento.getId());
    }

    private void salvarHistoricoAtendimento(Mesa mesa, Atendimento atendimento) {
        ResumoContaAtendimentoResponse resumo = pedidosAtivosClient.buscarResumoConta(atendimento.getId());

        HistoricoAtendimento historico = HistoricoAtendimento.builder()
                .idAtendimento(atendimento.getId())
                .codigoSessao(atendimento.getCodigoSessao())
                .idMesa(mesa.getId())
                .numeroMesa(mesa.getNumero())
                .idGarcom(atendimento.getGarcomId())
                .nomeGarcom(null)
                .valorFinal(resumo != null && resumo.valorFinal() != null ? resumo.valorFinal() : BigDecimal.ZERO)
                .totalPedidos(resumo != null && resumo.totalPedidos() != null ? resumo.totalPedidos() : 0)
                .totalItens(resumo != null && resumo.totalItens() != null ? resumo.totalItens() : 0)
                .dataAbertura(dataAberturaHistorico(atendimento))
                .dataFechamento(atendimento.getDataFechamento())
                .duracaoMinutos(calcularDuracaoMinutos(atendimento))
                .build();

        historicoAtendimentoRepository.save(historico);
    }

    private LocalDateTime dataAberturaHistorico(Atendimento atendimento) {
        return atendimento.getDataAbertura() == null
                ? atendimento.getDataFechamento()
                : atendimento.getDataAbertura();
    }

    private Integer calcularDuracaoMinutos(Atendimento atendimento) {
        long minutos = Duration.between(
                dataAberturaHistorico(atendimento),
                atendimento.getDataFechamento()
        ).toMinutes();

        return Math.toIntExact(Math.max(0, minutos));
    }

    private HistoricoAtendimentoResponse mapearHistoricoAtendimento(HistoricoAtendimento historico) {
        return new HistoricoAtendimentoResponse(
                historico.getId(),
                historico.getIdAtendimento(),
                historico.getCodigoSessao(),
                historico.getIdMesa(),
                historico.getNumeroMesa(),
                historico.getIdGarcom(),
                historico.getNomeGarcom(),
                historico.getValorFinal(),
                historico.getTotalPedidos(),
                historico.getTotalItens(),
                historico.getDataAbertura(),
                historico.getDataFechamento(),
                historico.getDuracaoMinutos()
        );
    }

    private Atendimento buscarAtendimentoAberto(Mesa mesa) {
        if (mesa.getAtendimento() == null || mesa.getAtendimento().getId() == null) {
            throw new BaseException(ErrorEnum.ATENDIMENTO_NAO_ABERTO);
        }

        return mesa.getAtendimento();
    }

    private void validarGarcomExisteComPerfilGarcom(Integer garcomId) {
        if (garcomId == null || garcomId <= 0) {
            throw new BaseException(ErrorEnum.GARCOM_INVALIDO);
        }

        // TODO Integrar com ms-usuarios para validar existencia do usuario e perfil GARCOM.
        // A FK em atendimentos.id_garcom garante a existencia no banco, mas o perfil GARCOM
        // ainda precisa ser validado pelo ms-usuarios ou pelo BFF.
    }
}
