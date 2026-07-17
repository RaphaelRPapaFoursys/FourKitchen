import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { PainelService } from '../../core/services/painel';
import { Gestor } from './gestor';

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
    pedidos: [{ id: 10, status: 'ENTREGUE', valor: 90, criadoEm: new Date().toISOString(), totalItens: 2 }],
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
    pedidos: [{ id: 11, status: 'ENTREGUE', valor: 40, criadoEm: new Date().toISOString(), totalItens: 1 }],
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
  size: 10,
  totalElements: 3,
  totalPages: 1,
  first: true,
  last: true,
};

const RESUMO_API = {
  mesasLivres: 1,
  emPreparo: 0,
  prontos: 0,
  problemas: 0,
  ticketMedio: 65,
  cargaGarcons: [
    { id: 7, nome: 'Carlos', mesasAtivas: 1 },
    { id: 8, nome: 'Julia', mesasAtivas: 1 },
  ],
};

describe('Gestor', () => {
  let component: Gestor;
  let fixture: ComponentFixture<Gestor>;
  let painelService: PainelService;
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

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Gestor],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);

    fixture = TestBed.createComponent(Gestor);
    component = fixture.componentInstance;
    painelService = fixture.debugElement.injector.get(PainelService);
    flushCargaInicial();
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock?.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('reflete os atendimentos finalizados retornados pelo histórico do backend', async () => {
    expect(painelService.ultimosPedidos().length).toBe(0);

    const promise = painelService.fecharConta(1);
    httpMock.expectOne(`${BASE_URL}/mesas/1/fechar`).flush({});
    await esperarMicrotarefas();
    flushPainel([
      {
        id: 99,
        idAtendimento: 5,
        codigoSessao: 123,
        idMesa: 1,
        numeroMesa: 3,
        idGarcom: 7,
        nomeGarcom: 'Carlos',
        valorFinal: 90,
        totalPedidos: 1,
        totalItens: 2,
        dataAbertura: '2026-07-03T10:00:00',
        dataFechamento: '2026-07-03T11:00:00',
        duracaoMinutos: 60,
      },
    ]);
    await promise;

    const pedidos = painelService.ultimosPedidos();
    expect(pedidos.length).toBe(1);
    expect(pedidos[0].numeroMesa).toBe(3);
  });

  /** Cada `await` numa promise resolvida via `firstValueFrom` só retoma no próximo microtask. */
  async function esperarMicrotarefas(): Promise<void> {
    for (let i = 0; i < 3; i++) {
      await Promise.resolve();
    }
  }

  it('fecharConta chama o BFF e recarrega as mesas', async () => {
    const promise = painelService.fecharConta(1);

    httpMock.expectOne(`${BASE_URL}/mesas/1/fechar`).flush({});
    await esperarMicrotarefas();
    flushPainel();
    await promise;
  });

  it('abrirMesa chama abrir seguido de atribuir-garcom', async () => {
    const promise = painelService.abrirMesa(3, 8);

    httpMock.expectOne(`${BASE_URL}/mesas/3/abrir`).flush({});
    await esperarMicrotarefas();
    httpMock.expectOne(`${BASE_URL}/mesas/3/atribuir-garcom`).flush({});
    await esperarMicrotarefas();
    flushPainel();
    await promise;
  });

  it('bloqueia o fechamento do expediente enquanto houver contas abertas', () => {
    expect(painelService.mesasComContaAberta()).toBeGreaterThan(0);
    expect(painelService.podeFecharExpediente()).toBeFalsy();

    painelService.fecharExpediente();
    expect(painelService.expedienteFechado()).toBeFalsy();
  });

  it('não chama a API de ações quando o expediente está fechado', () => {
    painelService.expedienteFechado.set(true);

    void painelService.fecharConta(1);

    httpMock.expectNone(`${BASE_URL}/mesas/1/fechar`);
  });

  it('deriva os garçons disponíveis da carga real', () => {
    const resumo = painelService.resumo();
    const disponiveis = painelService.cargaGarcons().filter(garcom => garcom.mesasAtivas <= 2).length;

    expect(resumo.garconsDisponiveis).toBe(disponiveis);
  });

  it('abre os detalhes do pedido com itens, observações, progresso e valor', async () => {
    fixture.detectChanges();
    const elemento = fixture.nativeElement as HTMLElement;
    const botoes = Array.from(elemento.querySelectorAll<HTMLButtonElement>('.mesa-card__acao'));
    const verPedido = botoes.find(botao => botao.textContent?.includes('Ver pedido'));

    expect(verPedido).toBeTruthy();
    verPedido?.click();
    fixture.detectChanges();

    httpMock.expectOne(`${BASE_URL}/mesas/1/pedidos`).flush([
      {
        id: 10,
        codigo: 100010,
        canal: 'MESA',
        status: 'ENTREGUE',
        dataCriacao: '2026-07-03T10:15:00',
        dataInicioPreparo: '2026-07-03T10:20:00',
        dataPronto: '2026-07-03T10:35:00',
        itens: [{
          id: 1,
          idProduto: 20,
          nomeProduto: 'Risoto de cogumelos',
          quantidade: 2,
          precoUnitario: 45,
          observacao: 'Sem queijo',
          status: 'DISPONIVEL',
        }],
      },
      {
        id: 11,
        codigo: 100011,
        canal: 'MESA',
        status: 'EM_PREPARO',
        dataCriacao: '2026-07-03T10:45:00',
        dataInicioPreparo: '2026-07-03T10:48:00',
        dataPronto: null,
        itens: [{
          id: 2,
          idProduto: 21,
          nomeProduto: 'Suco de laranja',
          quantidade: 1,
          precoUnitario: 10,
          observacao: null,
          status: 'DISPONIVEL',
        }],
      },
    ]);
    await esperarMicrotarefas();
    fixture.detectChanges();

    const modal: HTMLElement | null = fixture.nativeElement.querySelector('.modal--detalhes');
    expect(modal).toBeTruthy();
    expect(modal?.textContent).toContain('Risoto de cogumelos');
    expect(modal?.textContent).toContain('Sem queijo');
    expect(modal?.textContent).toContain('Recebido');
    expect(modal?.textContent).toContain('Entregue');
    expect(modal?.textContent).toContain('Valor total da conta');
    expect(modal?.textContent).toContain('R$100.00');

    const pedidos = Array.from(modal?.querySelectorAll<HTMLElement>('.detalhe-pedido') ?? []);
    expect(pedidos[0].textContent).toContain('#100011');
    expect(pedidos[1].textContent).toContain('#100010');
    expect(modal?.querySelector('.detalhes-totais')).toBeTruthy();
  });
});
