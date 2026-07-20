import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../../environments/environment';
import { FILTROS_DASHBOARD_INICIAIS } from '../models/dashboard-graficos.models';
import { DashboardGraficosService } from './dashboard-graficos.service';

describe('DashboardGraficosService', () => {
  let service: DashboardGraficosService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/api/gestor/dashboard`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DashboardGraficosService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DashboardGraficosService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('compartilha a chamada e publica loading seguido dos dados', async () => {
    const estadosA: string[] = [];
    const estadosB: string[] = [];
    const subscriptionA = service.volume$.subscribe(estado => estadosA.push(estado.status));
    const subscriptionB = service.volume$.subscribe(estado => estadosB.push(estado.status));
    await proximoCiclo();

    const request = httpMock.expectOne(req => req.url === `${baseUrl}/pedidos-por-horario`);
    expect(request.request.params.get('periodo')).toBe('HOJE');
    request.flush({ periodo: 'HOJE', totalPedidos: 1, horarioPico: '12:00', quantidadeNoPico: 1, dados: [{ horario: '12:00', quantidade: 1 }] });

    expect(estadosA).toEqual(['carregando', 'sucesso']);
    expect(estadosB.at(-1)).toBe('sucesso');
    subscriptionA.unsubscribe();
    subscriptionB.unsubscribe();
  });

  it('substitui a consulta ao mudar o período', async () => {
    const subscription = service.canais$.subscribe();
    await proximoCiclo();
    httpMock.expectOne(req => req.url === `${baseUrl}/pedidos-por-canal`).flush({ periodo: 'HOJE', totalPedidos: 0, dados: [] });

    service.atualizarFiltrosCanais({
      ...FILTROS_DASHBOARD_INICIAIS,
      periodo: 'PERSONALIZADO',
      dataInicial: '2026-07-01',
      dataFinal: '2026-07-20',
      canal: 'MESA',
      idMesa: 4,
      status: 'ENTREGUE',
    });
    const request = httpMock.expectOne(req => req.url === `${baseUrl}/pedidos-por-canal` && req.params.get('periodo') === 'PERSONALIZADO');
    expect(request.request.params.get('dataInicial')).toBe('2026-07-01');
    expect(request.request.params.get('dataFinal')).toBe('2026-07-20');
    expect(request.request.params.get('canal')).toBe('MESA');
    expect(request.request.params.get('idMesa')).toBe('4');
    expect(request.request.params.get('status')).toBe('ENTREGUE');
    request.flush({ periodo: 'ULTIMA_HORA', totalPedidos: 0, dados: [] });
    subscription.unsubscribe();
  });

  it('isola erro e permite nova tentativa', async () => {
    const estados: string[] = [];
    const subscription = service.problemas$.subscribe(estado => estados.push(estado.status));
    await proximoCiclo();
    httpMock.expectOne(req => req.url === `${baseUrl}/problemas-por-motivo`).flush({}, { status: 503, statusText: 'Unavailable' });
    expect(estados.at(-1)).toBe('erro');

    service.repetirProblemas();
    httpMock.expectOne(req => req.url === `${baseUrl}/problemas-por-motivo`).flush({ periodo: 'HOJE', totalProblemas: 0, motivoMaisFrequente: null, dados: [] });
    expect(estados.at(-1)).toBe('sucesso');
    subscription.unsubscribe();
  });

  it('atualiza o ranking ao trocar seu período independente', async () => {
    const subscription = service.rankingProdutos$.subscribe();
    await proximoCiclo();
    httpMock.expectOne(req => req.url === `${baseUrl}/ranking-produtos` && req.params.get('periodo') === 'ULTIMOS_30_DIAS')
      .flush({ periodo: 'ULTIMOS_30_DIAS', dados: [] });

    service.atualizarPeriodoRanking('ULTIMOS_7_DIAS');
    httpMock.expectOne(req => req.url === `${baseUrl}/ranking-produtos` && req.params.get('periodo') === 'ULTIMOS_7_DIAS')
      .flush({ periodo: 'ULTIMOS_7_DIAS', dados: [] });
    subscription.unsubscribe();
  });

  it('mantém os filtros independentes entre os gráficos', async () => {
    const volume = service.volume$.subscribe();
    const canais = service.canais$.subscribe();
    await proximoCiclo();
    httpMock.expectOne(req => req.url === `${baseUrl}/pedidos-por-horario`).flush({ periodo: 'HOJE', totalPedidos: 0, horarioPico: null, quantidadeNoPico: 0, dados: [] });
    httpMock.expectOne(req => req.url === `${baseUrl}/pedidos-por-canal`).flush({ periodo: 'HOJE', totalPedidos: 0, dados: [] });

    service.atualizarFiltrosCanais({ ...FILTROS_DASHBOARD_INICIAIS, periodo: 'ULTIMOS_7_DIAS' });
    httpMock.expectOne(req => req.url === `${baseUrl}/pedidos-por-canal` && req.params.get('periodo') === 'ULTIMOS_7_DIAS')
      .flush({ periodo: 'ULTIMOS_7_DIAS', totalPedidos: 0, dados: [] });
    httpMock.expectNone(req => req.url === `${baseUrl}/pedidos-por-horario` && req.params.get('periodo') === 'ULTIMOS_7_DIAS');
    volume.unsubscribe();
    canais.unsubscribe();
  });

  function proximoCiclo(): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, 0));
  }
});
