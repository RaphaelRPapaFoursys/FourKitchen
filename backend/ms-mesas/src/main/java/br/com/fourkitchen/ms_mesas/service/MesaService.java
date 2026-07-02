package br.com.fourkitchen.ms_mesas.service;

import br.com.fourkitchen.ms_mesas.client.PedidosAtivosClient;
import br.com.fourkitchen.ms_mesas.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.ms_mesas.dto.request.CriarMesaRequest;
import br.com.fourkitchen.ms_mesas.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.SessaoMesaResponse;
import br.com.fourkitchen.ms_mesas.enums.StatusMesa;
import br.com.fourkitchen.ms_mesas.exception.BaseException;
import br.com.fourkitchen.ms_mesas.exception.ErrorEnum;
import br.com.fourkitchen.ms_mesas.mapper.CriarMesaRequestMapper;
import br.com.fourkitchen.ms_mesas.mapper.MesaGarcomResponseMapper;
import br.com.fourkitchen.ms_mesas.mapper.MesaResponseMapper;
import br.com.fourkitchen.ms_mesas.model.Atendimento;
import br.com.fourkitchen.ms_mesas.model.Mesa;
import br.com.fourkitchen.ms_mesas.repository.AtendimentoRepository;
import br.com.fourkitchen.ms_mesas.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;

    private final AtendimentoRepository atendimentoRepository;

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
        atendimento.setDataFechamento(LocalDateTime.now());
        atendimentoRepository.save(atendimento);

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
