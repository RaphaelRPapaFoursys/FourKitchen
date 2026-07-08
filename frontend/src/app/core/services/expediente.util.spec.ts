import { MesaPainel } from '../models/painel.models';
import {
  contarPedidosPendentesEntrega,
  HistoricoAtendimentoExpediente,
  montarResumoExpediente,
} from './expediente.util';

function historico(overrides: Partial<HistoricoAtendimentoExpediente>): HistoricoAtendimentoExpediente {
  return {
    numeroMesa: 1,
    nomeGarcom: 'Carlos',
    valorFinal: 100,
    totalPedidos: 1,
    ...overrides,
  };
}

let proximoIdPedido = 1;

function mesa(overrides: Partial<MesaPainel>): MesaPainel {
  return {
    id: overrides.numero ?? 1,
    numero: 1,
    status: 'OCUPADA',
    garcomId: 1,
    garcom: 'Carlos',
    abertaEm: null,
    statusPedido: 'CONTA_ABERTA',
    tempoLabel: null,
    tempoMinutos: null,
    etapaAtual: null,
    totalEtapas: null,
    pedidos: [],
    ...overrides,
  };
}

function pedido(overrides: Partial<MesaPainel['pedidos'][number]>) {
  return {
    id: proximoIdPedido++,
    status: 'ENTREGUE',
    valor: 100,
    criadoMinutosAtras: 10,
    tempoPreparoMinutos: 15,
    totalItens: 1,
    ...overrides,
  };
}

describe('contarPedidosPendentesEntrega', () => {
  it('retorna 0 quando todas as mesas ocupadas já estão com conta aberta', () => {
    const mesas = [
      mesa({ numero: 1, statusPedido: 'CONTA_ABERTA', pedidos: [pedido({})] }),
      mesa({ numero: 2, status: 'LIVRE', garcomId: null, garcom: null, statusPedido: null }),
    ];

    expect(contarPedidosPendentesEntrega(mesas)).toBe(0);
  });

  it('conta os pedidos de mesas em preparo e prontas para entrega', () => {
    const mesas = [
      mesa({ numero: 1, statusPedido: 'EM_PREPARO', pedidos: [pedido({}), pedido({})] }),
      mesa({ numero: 2, statusPedido: 'PRONTO_ENTREGA', pedidos: [pedido({})] }),
      mesa({ numero: 3, statusPedido: 'CONTA_ABERTA', pedidos: [pedido({})] }),
    ];

    // 2 (mesa 1) + 1 (mesa 2) = 3; mesa 3 (conta aberta) não conta.
    expect(contarPedidosPendentesEntrega(mesas)).toBe(3);
  });

  it('ignora mesas livres', () => {
    const mesas = [mesa({ numero: 1, status: 'LIVRE', garcomId: null, garcom: null, statusPedido: null })];

    expect(contarPedidosPendentesEntrega(mesas)).toBe(0);
  });
});

describe('montarResumoExpediente', () => {
  it('soma valor total, atendimentos e pedidos dos atendimentos finalizados', () => {
    const atendimentos = [
      historico({ numeroMesa: 1, nomeGarcom: 'Carlos', valorFinal: 80, totalPedidos: 2 }),
      historico({ numeroMesa: 2, nomeGarcom: 'Julia', valorFinal: 20, totalPedidos: 1 }),
    ];

    const resumo = montarResumoExpediente(atendimentos);

    expect(resumo.valorTotal).toBeCloseTo(100);
    expect(resumo.atendimentosRealizados).toBe(2);
    expect(resumo.pedidosFeitos).toBe(3);
  });

  it('elege o garçom com mais atendimentos e desempata por ordem alfabética', () => {
    const atendimentos = [
      historico({ numeroMesa: 1, nomeGarcom: 'Julia' }),
      historico({ numeroMesa: 2, nomeGarcom: 'Carlos' }),
      historico({ numeroMesa: 3, nomeGarcom: 'Carlos' }),
    ];

    expect(montarResumoExpediente(atendimentos).garcomDestaque).toEqual({ nome: 'Carlos', atendimentos: 2 });
  });

  it('desempata o garçom destaque por nome quando a contagem é igual', () => {
    const atendimentos = [
      historico({ numeroMesa: 1, nomeGarcom: 'Julia' }),
      historico({ numeroMesa: 2, nomeGarcom: 'Carlos' }),
    ];

    expect(montarResumoExpediente(atendimentos).garcomDestaque).toEqual({ nome: 'Carlos', atendimentos: 1 });
  });

  it('agrupa atendimentos sem garçom sob o rótulo "—"', () => {
    const atendimentos = [historico({ numeroMesa: 1, nomeGarcom: null })];

    expect(montarResumoExpediente(atendimentos).garcomDestaque).toEqual({ nome: '—', atendimentos: 1 });
  });

  it('retorna garcomDestaque nulo e tempoMedioPreparoMin nulo quando não há atendimentos', () => {
    const resumo = montarResumoExpediente([]);

    expect(resumo.garcomDestaque).toBeNull();
    expect(resumo.tempoMedioPreparoMin).toBeNull();
  });

  it('mantém tempoMedioPreparoMin nulo (backend ainda não registra tempo de preparo)', () => {
    const atendimentos = [historico({ numeroMesa: 1, totalPedidos: 3 })];

    expect(montarResumoExpediente(atendimentos).tempoMedioPreparoMin).toBeNull();
  });

  it('lista o top 5 de mesas mais ocupadas, cortando o restante', () => {
    const atendimentos = Array.from({ length: 7 }, (_, index) =>
      historico({ numeroMesa: index + 1, totalPedidos: 7 - index }),
    );

    const mesasMaisOcupadas = montarResumoExpediente(atendimentos).mesasMaisOcupadas;

    expect(mesasMaisOcupadas).toHaveLength(5);
    expect(mesasMaisOcupadas.map(item => item.numeroMesa)).toEqual([1, 2, 3, 4, 5]);
  });

  it('soma os pedidos por mesa quando há mais de um atendimento na mesma mesa', () => {
    const atendimentos = [
      historico({ numeroMesa: 4, totalPedidos: 2 }),
      historico({ numeroMesa: 4, totalPedidos: 3 }),
      historico({ numeroMesa: 7, totalPedidos: 1 }),
    ];

    const mesasMaisOcupadas = montarResumoExpediente(atendimentos).mesasMaisOcupadas;

    expect(mesasMaisOcupadas[0]).toEqual({ numeroMesa: 4, pedidos: 5 });
  });

  it('desempata mesas com o mesmo número de pedidos pelo número da mesa', () => {
    const atendimentos = [
      historico({ numeroMesa: 9, totalPedidos: 2 }),
      historico({ numeroMesa: 2, totalPedidos: 2 }),
      historico({ numeroMesa: 5, totalPedidos: 1 }),
    ];

    const mesasMaisOcupadas = montarResumoExpediente(atendimentos).mesasMaisOcupadas;

    expect(mesasMaisOcupadas.map(item => item.numeroMesa)).toEqual([2, 9, 5]);
  });

  it('retorna lista vazia de mesas mais ocupadas quando não há atendimentos', () => {
    expect(montarResumoExpediente([]).mesasMaisOcupadas).toEqual([]);
  });
});
