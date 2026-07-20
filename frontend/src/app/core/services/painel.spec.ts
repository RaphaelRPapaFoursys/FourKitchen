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

const PAGINA_MESAS_API = {
  content: MESAS_API,
  page: 0,
  size: 12,
  totalElements: 3,
  totalPages: 1,
  first: true,
  last: true,
};

const RESUMO_API = {
  mesasLivres: 1,
  mesasSemGarcom: 0,
  emPreparo: 1,
  prontos: 0,
  problemas: 0,
  ticketMedio: 40,
  cargaGarcons: [
    { id: 7, nome: 'Carlos', mesasAtivas: 1 },
    { id: 8, nome: 'Julia', mesasAtivas: 1 },
  ],
};

describe('PainelService', () => {
  let service: PainelService;
  let httpMock: HttpTestingController;

  function flushCargaInicial(): void {
    flushPainel();
  }

  function flushPainel(historico: unknown[] = []): void {
    httpMock
      .expectOne(request => request.url === `${BASE_URL}/mesas/paginadas`)
      .flush(PAGINA_MESAS_API);
    httpMock.expectOne(`${BASE_URL}/mesas/resumo`).flush(RESUMO_API);
    httpMock.expectOne(`${BASE_URL}/atendimentos/historico`).flush(historico);
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), PainelService],
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

  it('busca pedidos detalhados de um atendimento histórico', async () => {
    const promise = service.buscarPedidosDetalhadosPorAtendimento(80);
    httpMock.expectOne(`${BASE_URL}/atendimentos/80/pedidos`).flush([]);

    expect(await promise).toEqual([]);
  });

  describe('cargaGarcons', () => {
    it('reflete a contagem de mesas ocupadas por garçom a partir da lista de garçons e mesas reais', () => {
      const carga = service.cargaGarcons();

      expect(carga.find(garcom => garcom.nome === 'Carlos')?.mesasAtivas).toBe(1);
      expect(carga.find(garcom => garcom.nome === 'Julia')?.mesasAtivas).toBe(1);
    });
  });

  describe('ultimosPedidos', () => {
    it('reflete os atendimentos finalizados retornados pelo histórico do backend', async () => {
      expect(service.ultimosPedidos()).toHaveLength(0);

      const promise = service.fecharConta(1);
      httpMock.expectOne(`${BASE_URL}/mesas/1/fechar`).flush({});
      for (let i = 0; i < 3; i++) {
        await Promise.resolve();
      }
      flushPainel([
        {
          id: 99,
          idAtendimento: 5,
          codigoSessao: 123,
          idMesa: 1,
          numeroMesa: 3,
          idGarcom: 7,
          nomeGarcom: 'Carlos',
          valorFinal: 80,
          totalPedidos: 2,
          totalItens: 3,
          dataAbertura: '2026-07-03T10:00:00',
          dataFechamento: '2026-07-03T11:00:00',
          duracaoMinutos: 60,
        },
      ]);
      await promise;

      const pedidos = service.ultimosPedidos();
      expect(pedidos).toHaveLength(1);
      expect(pedidos[0].numeroMesa).toBe(3);
      expect(pedidos[0].valor).toBe(80);
      expect(pedidos[0].garcom).toBe('Carlos');
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
      flushPainel();
      await promise;
    });

    it('reatribuirGarcom chama o PATCH de atribuir-garcom com o id do garçom e recarrega as mesas', async () => {
      const promise = service.reatribuirGarcom(1, 8);

      const request = httpMock.expectOne(`${BASE_URL}/mesas/1/atribuir-garcom`);
      expect(request.request.body).toEqual({ garcomId: 8 });
      request.flush({});
      await esperarMicrotarefas();
      flushPainel();
      await promise;
    });

    it('abrirMesa chama abrir e atribuir-garcom em sequência e recarrega as mesas', async () => {
      const promise = service.abrirMesa(3, 7);

      httpMock.expectOne(`${BASE_URL}/mesas/3/abrir`).flush({});
      await esperarMicrotarefas();
      httpMock.expectOne(`${BASE_URL}/mesas/3/atribuir-garcom`).flush({});
      await esperarMicrotarefas();
      flushPainel();
      await promise;
    });

    it('não chama a API quando o expediente está fechado', async () => {
      service.expedienteFechado.set(true);

      await service.fecharConta(1);

      httpMock.expectNone(`${BASE_URL}/mesas/1/fechar`);
    });

    it('não recarrega o histórico quando o fecharConta falha', async () => {
      expect(service.ultimosPedidos()).toHaveLength(0);

      const promise = service.fecharConta(1);
      httpMock
        .expectOne(`${BASE_URL}/mesas/1/fechar`)
        .flush({ msgError: 'Falha ao fechar' }, { status: 500, statusText: 'Server Error' });
      await esperarMicrotarefas();
      await promise;

      // Sem sucesso no PATCH, o painel (e o histórico) não é recarregado e a lista continua vazia.
      expect(service.ultimosPedidos()).toHaveLength(0);
      expect(service.mensagemErro()).not.toBeNull();
    });

    it('abrirMesa recarrega as mesas mesmo quando a atribuição falha', async () => {
      const promise = service.abrirMesa(3, 7);

      httpMock.expectOne(`${BASE_URL}/mesas/3/abrir`).flush({});
      await esperarMicrotarefas();
      httpMock
        .expectOne(`${BASE_URL}/mesas/3/atribuir-garcom`)
        .flush({ msgError: 'Falha ao atribuir' }, { status: 500, statusText: 'Server Error' });
      await esperarMicrotarefas();
      // O finally dispara atualizarMesas mesmo com a atribuição falhando.
      flushPainel();
      await promise;

      expect(service.mensagemErro()).not.toBeNull();
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

    it('abrirNovoExpediente reabre o expediente e persiste o início no localStorage', () => {
      service.expedienteFechado.set(true);

      service.abrirNovoExpediente();

      expect(service.expedienteFechado()).toBe(false);
      expect(localStorage.getItem('fk.expediente.fechado')).toBe('false');
      expect(localStorage.getItem('fk.expediente.inicio')).not.toBeNull();
    });
  });
});

describe('PainelService — cache e prefetch de páginas', () => {
  let service: PainelService;
  let httpMock: HttpTestingController;

  const TOTAL_PAGINAS = 6;

  function paginaComTotal(page: number, size = 12) {
    return {
      content: MESAS_API,
      page,
      size,
      totalElements: TOTAL_PAGINAS * size,
      totalPages: TOTAL_PAGINAS,
      first: page === 0,
      last: page === TOTAL_PAGINAS - 1,
    };
  }

  function consultaPagina(page: number, size = 12) {
    return { page, size, sort: 'criticidade' as const, filtroEstado: null, garcomId: null, busca: '' };
  }

  /** Responde toda `/mesas/paginadas` pendente com a página que ela de fato pediu. */
  function flushPaginadasPendentes(): void {
    httpMock.match(request => request.url === `${BASE_URL}/mesas/paginadas`).forEach(req => {
      const page = Number(req.request.params.get('page') ?? '0');
      const size = Number(req.request.params.get('size') ?? '12');
      req.flush(paginaComTotal(page, size));
    });
  }

  /** Roda os timers escalonados do prefetch e responde cada requisição conforme aparece. */
  async function estabilizar(): Promise<void> {
    for (let i = 0; i < 6; i++) {
      flushPaginadasPendentes();
      await vi.advanceTimersByTimeAsync(450);
    }
    flushPaginadasPendentes();
  }

  beforeEach(async () => {
    localStorage.clear();
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), PainelService],
    });
    service = TestBed.inject(PainelService);
    httpMock = TestBed.inject(HttpTestingController);

    // Load inicial: página 0 + resumo + histórico.
    httpMock.expectOne(request => request.url === `${BASE_URL}/mesas/paginadas`).flush(paginaComTotal(0));
    httpMock.expectOne(`${BASE_URL}/mesas/resumo`).flush(RESUMO_API);
    httpMock.expectOne(`${BASE_URL}/atendimentos/historico`).flush([]);

    // Com 12 mesas por página (raio 2), o prefetch inicial cacheia as páginas 1 e 2.
    await estabilizar();
  });

  afterEach(() => {
    httpMock.verify();
    vi.useRealTimers();
  });

  it('prefetcha 2 páginas à frente: trocar para a página 2 não liga o loading', async () => {
    expect(service.carregandoMesas()).toBe(false);

    void service.atualizarConsulta(consultaPagina(2));
    // Página 2 já está em cache (raio 2): troca instantânea, sem spinner.
    expect(service.carregandoMesas()).toBe(false);
    expect(service.mesas().length).toBeGreaterThan(0);

    await estabilizar();
  });

  it('trocar para uma página fora do raio liga o loading durante o fetch', async () => {
    // Página 5 está fora do cache {0,1,2}.
    void service.atualizarConsulta(consultaPagina(5));
    expect(service.carregandoMesas()).toBe(true);

    await estabilizar();

    expect(service.carregandoMesas()).toBe(false);
  });

  it('trocar um filtro carrega a página atual primeiro e só depois prefetcha as vizinhas', async () => {
    // Muda um filtro (nova base) → o cache é invalidado.
    void service.atualizarConsulta({
      page: 0,
      size: 12,
      sort: 'criticidade',
      filtroEstado: 'PROBLEMAS',
      garcomId: null,
      busca: '',
    });

    // Enquanto a página atual carrega: spinner ligado e apenas ela foi pedida (nenhuma vizinha ainda).
    expect(service.carregandoMesas()).toBe(true);
    const atual = httpMock.expectOne(request => request.url === `${BASE_URL}/mesas/paginadas`);
    expect(atual.request.params.get('page')).toBe('0');
    atual.flush(paginaComTotal(0));

    // Concluída a página atual, as vizinhas começam a ser prefetchadas (de forma escalonada).
    await estabilizar();
    expect(service.carregandoMesas()).toBe(false);
  });

  it('com 30+ mesas por página, o raio cai para 1: a página +2 não fica em cache', async () => {
    // Nova base com size 30 (raio 1): recarrega a página 0 e prefetcha só a página 1.
    void service.atualizarConsulta(consultaPagina(0, 30));
    await estabilizar();

    // A página 2 não foi prefetchada com size grande, então liga o loading.
    void service.atualizarConsulta(consultaPagina(2, 30));
    expect(service.carregandoMesas()).toBe(true);

    await estabilizar();
    expect(service.carregandoMesas()).toBe(false);
  });
});
