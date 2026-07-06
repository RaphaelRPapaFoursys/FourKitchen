import { MesaPainel } from '../models/painel.models';
import { contarPedidosPendentesEntrega, montarResumoExpediente } from './expediente.util';

let proximoIdPedido = 1;

function mesa(overrides: Partial<MesaPainel>): MesaPainel {
  return {
    id: overrides.numero ?? 1,
    numero: 1,
    status: 'OCUPADA',
    garcomId: 1,
    garcom: 'Carlos',
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
  it('soma valor total, atendimentos e pedidos apenas das mesas com atendimento ativo', () => {
    const mesas = [
      mesa({
        numero: 1,
        garcom: 'Carlos',
        pedidos: [pedido({ valor: 50 }), pedido({ valor: 30 })],
      }),
      mesa({ numero: 2, garcom: 'Julia', pedidos: [pedido({ valor: 20 })] }),
      mesa({ numero: 3, status: 'LIVRE', garcomId: null, garcom: null, statusPedido: null, pedidos: [] }),
      mesa({ numero: 4, status: 'OCUPADA', garcom: 'Roberto', pedidos: [] }),
    ];

    const resumo = montarResumoExpediente(mesas, []);

    expect(resumo.valorTotal).toBeCloseTo(100);
    expect(resumo.atendimentosRealizados).toBe(2);
    expect(resumo.pedidosFeitos).toBe(3);
  });

  it('elege o garçom com mais atendimentos e desempata por ordem alfabética', () => {
    const mesas = [
      mesa({ numero: 1, garcom: 'Julia', pedidos: [pedido({})] }),
      mesa({ numero: 2, garcom: 'Carlos', pedidos: [pedido({})] }),
      mesa({ numero: 3, garcom: 'Carlos', pedidos: [pedido({})] }),
    ];

    expect(montarResumoExpediente(mesas, []).garcomDestaque).toEqual({ nome: 'Carlos', atendimentos: 2 });
  });

  it('desempata o garçom destaque por nome quando a contagem é igual', () => {
    const mesas = [
      mesa({ numero: 1, garcom: 'Julia', pedidos: [pedido({})] }),
      mesa({ numero: 2, garcom: 'Carlos', pedidos: [pedido({})] }),
    ];

    expect(montarResumoExpediente(mesas, []).garcomDestaque).toEqual({ nome: 'Carlos', atendimentos: 1 });
  });

  it('retorna garcomDestaque nulo e tempoMedioPreparoMin nulo quando não há atendimentos', () => {
    const mesas = [mesa({ numero: 1, status: 'LIVRE', garcomId: null, garcom: null, statusPedido: null, pedidos: [] })];

    const resumo = montarResumoExpediente(mesas, []);

    expect(resumo.garcomDestaque).toBeNull();
    expect(resumo.tempoMedioPreparoMin).toBeNull();
  });

  it('calcula o tempo médio de preparo ignorando pedidos sem o dado', () => {
    const mesas = [
      mesa({
        numero: 1,
        pedidos: [
          pedido({ tempoPreparoMinutos: 10 }),
          pedido({ tempoPreparoMinutos: 20 }),
          pedido({ tempoPreparoMinutos: null }),
        ],
      }),
    ];

    expect(montarResumoExpediente(mesas, []).tempoMedioPreparoMin).toBe(15);
  });

  it('lista o top 5 de mesas mais ocupadas, cortando o restante', () => {
    const mesas = Array.from({ length: 7 }, (_, index) =>
      mesa({
        numero: index + 1,
        garcom: 'Carlos',
        pedidos: Array.from({ length: 7 - index }, () => pedido({})),
      }),
    );

    const mesasMaisOcupadas = montarResumoExpediente(mesas, []).mesasMaisOcupadas;

    expect(mesasMaisOcupadas).toHaveLength(5);
    expect(mesasMaisOcupadas.map(item => item.numeroMesa)).toEqual([1, 2, 3, 4, 5]);
  });

  it('desempata mesas com o mesmo número de pedidos pelo número da mesa', () => {
    const mesas = [
      mesa({ numero: 9, garcom: 'Carlos', pedidos: [pedido({}), pedido({})] }),
      mesa({ numero: 2, garcom: 'Julia', pedidos: [pedido({}), pedido({})] }),
      mesa({ numero: 5, garcom: 'Roberto', pedidos: [pedido({})] }),
    ];

    const mesasMaisOcupadas = montarResumoExpediente(mesas, []).mesasMaisOcupadas;

    expect(mesasMaisOcupadas.map(item => item.numeroMesa)).toEqual([2, 9, 5]);
  });

  it('retorna lista vazia de mesas mais ocupadas quando não há atendimentos ativos', () => {
    const mesas = [mesa({ numero: 1, status: 'LIVRE', garcomId: null, garcom: null, statusPedido: null, pedidos: [] })];

    expect(montarResumoExpediente(mesas, []).mesasMaisOcupadas).toEqual([]);
  });
});
