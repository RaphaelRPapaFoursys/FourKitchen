import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { PainelService } from './painel';

const BASE_URL = `${environment.apiUrl}/api/gestor`;

const MESAS_API = [
  {
    id: 1,
    numero: 3,
    status: 'OCUPADA' as const,
    garcomId: 7,
    garcomNome: 'Carlos',
    codigoSessao: 123,
    dataAbertura: '2026-07-03T10:00:00',
    dataFechamento: null,
    pedidos: [
      { id: 10, status: 'EM_PREPARO', valor: 50, criadoEm: new Date().toISOString(), totalItens: 2 },
    ],
  },
  {
    id: 2,
    numero: 7,
    status: 'OCUPADA' as const,
    garcomId: 8,
    garcomNome: 'Julia',
    codigoSessao: 456,
    dataAbertura: '2026-07-03T09:00:00',
    dataFechamento: null,
    pedidos: [
      { id: 11, status: 'ENTREGUE', valor: 30, criadoEm: new Date().toISOString(), totalItens: 1 },
    ],
  },
  {
    id: 3,
    numero: 8,
    status: 'DISPONIVEL' as const,
    garcomId: null,
    garcomNome: null,
    codigoSessao: null,
    dataAbertura: null,
    dataFechamento: null,
    pedidos: [],
  },
];

const GARCONS_API = [
  { id: 7, nome: 'Carlos', email: 'carlos@fourkitchen.com' },
  { id: 8, nome: 'Julia', email: 'julia@fourkitchen.com' },
];

describe('PainelService', () => {
  let service: PainelService;
  let httpMock: HttpTestingController;

  function flushCargaInicial(): void {
    httpMock.expectOne(`${BASE_URL}/mesas`).flush(MESAS_API);
    httpMock.expectOne(`${BASE_URL}/garcons`).flush(GARCONS_API);
  }

  /** As ações só recarregam as mesas — a lista de garçons não muda por causa delas. */
  function flushMesas(): void {
    httpMock.expectOne(`${BASE_URL}/mesas`).flush(MESAS_API);
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PainelService);
    httpMock = TestBed.inject(HttpTestingController);
    flushCargaInicial();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('mapeia as mesas retornadas pela API, incluindo o status LIVRE para mesas DISPONIVEL', () => {
    const mesas = service.mesas();

    expect(mesas).toHaveLength(3);
    expect(mesas.find(mesa => mesa.numero === 8)?.status).toBe('LIVRE');
    expect(mesas.find(mesa => mesa.numero === 3)?.status).toBe('OCUPADA');
  });

  it('deriva o statusPedido da mesa a partir do status dos pedidos ativos', () => {
    const mesas = service.mesas();

    expect(mesas.find(mesa => mesa.numero === 3)?.statusPedido).toBe('EM_PREPARO');
    expect(mesas.find(mesa => mesa.numero === 7)?.statusPedido).toBe('CONTA_ABERTA');
  });

  describe('cargaGarcons', () => {
    it('reflete a contagem de mesas ocupadas por garçom a partir da lista de garçons e mesas reais', () => {
      const carga = service.cargaGarcons();

      expect(carga.find(garcom => garcom.nome === 'Carlos')?.mesasAtivas).toBe(1);
      expect(carga.find(garcom => garcom.nome === 'Julia')?.mesasAtivas).toBe(1);
    });
  });

  describe('ultimosPedidos', () => {
    it('inclui apenas pedidos de mesas ocupadas com atendimento ativo', () => {
      const numerosDeMesasNaoOcupadas = service
        .mesas()
        .filter(mesa => mesa.status !== 'OCUPADA')
        .map(mesa => mesa.numero);

      for (const pedido of service.ultimosPedidos()) {
        expect(numerosDeMesasNaoOcupadas).not.toContain(pedido.numeroMesa);
      }
    });
  });

  describe('ações que chamam o BFF', () => {
    /** Cada `await` numa promise resolvida via `firstValueFrom` só retoma no próximo microtask. */
    async function esperarMicrotarefas(): Promise<void> {
      for (let i = 0; i < 3; i++) {
        await Promise.resolve();
      }
    }

    it('fecharConta chama o PATCH de fechar e recarrega as mesas', async () => {
      const promise = service.fecharConta(1);

      httpMock.expectOne(`${BASE_URL}/mesas/1/fechar`).flush({});
      await esperarMicrotarefas();
      flushMesas();
      await promise;
    });

    it('marcarEntregue chama o PATCH de marcar-entregue e recarrega as mesas', async () => {
      const promise = service.marcarEntregue(1);

      httpMock.expectOne(`${BASE_URL}/mesas/1/marcar-entregue`).flush({});
      await esperarMicrotarefas();
      flushMesas();
      await promise;
    });

    it('reatribuirGarcom chama o PATCH de atribuir-garcom com o id do garçom e recarrega as mesas', async () => {
      const promise = service.reatribuirGarcom(1, 8);

      const request = httpMock.expectOne(`${BASE_URL}/mesas/1/atribuir-garcom`);
      expect(request.request.body).toEqual({ garcomId: 8 });
      request.flush({});
      await esperarMicrotarefas();
      flushMesas();
      await promise;
    });

    it('abrirMesa chama abrir e atribuir-garcom em sequência e recarrega as mesas', async () => {
      const promise = service.abrirMesa(3, 7);

      httpMock.expectOne(`${BASE_URL}/mesas/3/abrir`).flush({});
      await esperarMicrotarefas();
      httpMock.expectOne(`${BASE_URL}/mesas/3/atribuir-garcom`).flush({});
      await esperarMicrotarefas();
      flushMesas();
      await promise;
    });

    it('não chama a API quando o expediente está fechado', async () => {
      service.expedienteFechado.set(true);

      await service.fecharConta(1);

      httpMock.expectNone(`${BASE_URL}/mesas/1/fechar`);
    });
  });

  describe('fechamento de expediente', () => {
    it('habilita o fechamento quando não há pedidos pendentes de entrega', () => {
      expect(service.pedidosPendentesEntrega()).toBeGreaterThan(0);
      expect(service.podeFecharExpediente()).toBe(false);
    });

    it('fecharExpediente marca expedienteFechado quando não é permitido', () => {
      expect(service.expedienteFechado()).toBe(false);

      service.fecharExpediente();

      expect(service.expedienteFechado()).toBe(false);
    });

    it('acaoPrimaria de uma mesa com conta aberta é fechar conta', () => {
      const mesaComContaAberta = service.mesas().find(mesa => mesa.statusPedido === 'CONTA_ABERTA')!;

      expect(service.acaoPrimaria(mesaComContaAberta).tipo).toBe('FECHAR_CONTA');
    });
  });
});
