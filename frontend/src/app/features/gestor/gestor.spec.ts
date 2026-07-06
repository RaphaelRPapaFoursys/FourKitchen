import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

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

const GARCONS_API = [
  { id: 7, nome: 'Carlos', email: 'carlos@fourkitchen.com' },
  { id: 8, nome: 'Julia', email: 'julia@fourkitchen.com' },
];

describe('Gestor', () => {
  let component: Gestor;
  let fixture: ComponentFixture<Gestor>;
  let painelService: PainelService;
  let httpMock: HttpTestingController;

  function flushCargaInicial(): void {
    httpMock.expectOne(`${BASE_URL}/mesas`).flush(MESAS_API);
    httpMock.expectOne(`${BASE_URL}/garcons`).flush(GARCONS_API);
  }

  /** As ações só recarregam as mesas — a lista de garçons não muda por causa delas. */
  function flushMesas(): void {
    httpMock.expectOne(`${BASE_URL}/mesas`).flush(MESAS_API);
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Gestor],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    painelService = TestBed.inject(PainelService);
    httpMock = TestBed.inject(HttpTestingController);
    flushCargaInicial();

    fixture = TestBed.createComponent(Gestor);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('lista os últimos pedidos das mesas ocupadas', () => {
    const pedidos = painelService.ultimosPedidos();

    expect(pedidos.length).toBe(2);
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
    flushMesas();
    await promise;
  });

  it('abrirMesa chama abrir seguido de atribuir-garcom', async () => {
    const promise = painelService.abrirMesa(3, 8);

    httpMock.expectOne(`${BASE_URL}/mesas/3/abrir`).flush({});
    await esperarMicrotarefas();
    httpMock.expectOne(`${BASE_URL}/mesas/3/atribuir-garcom`).flush({});
    await esperarMicrotarefas();
    flushMesas();
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

    void painelService.marcarEntregue(1);

    httpMock.expectNone(`${BASE_URL}/mesas/1/marcar-entregue`);
  });

  it('deriva os garçons disponíveis da carga real', () => {
    const resumo = painelService.resumo();
    const disponiveis = painelService.cargaGarcons().filter(garcom => garcom.mesasAtivas <= 2).length;

    expect(resumo.garconsDisponiveis).toBe(disponiveis);
  });
});
