import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { GestorDashboard } from './gestor-dashboard';

const BASE_URL = `${environment.apiUrl}/api/gestor`;

describe('GestorDashboard', () => {
  let fixture: ComponentFixture<GestorDashboard>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestorDashboard],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GestorDashboard);

    httpMock.expectOne(request => request.url === `${BASE_URL}/mesas/paginadas`).flush({
      content: [{
        id: 1,
        numero: 4,
        status: 'OCUPADA',
        garcomId: 7,
        garcomNome: 'Carlos',
        codigoSessao: 123456,
        dataAbertura: '2026-07-17T10:00:00',
        dataFechamento: null,
        pedidos: [],
      }],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    });
    httpMock.expectOne(`${BASE_URL}/mesas/resumo`).flush({
      mesasLivres: 0,
      mesasSemGarcom: 4,
      emPreparo: 0,
      prontos: 0,
      problemas: 1,
      ticketMedio: 50,
      cargaGarcons: [{ id: 7, nome: 'Carlos', mesasAtivas: 1 }],
    });
    httpMock.expectOne(`${BASE_URL}/atendimentos/historico`).flush([{
      id: 30,
      idAtendimento: 80,
      codigoSessao: 987654,
      idMesa: 1,
      numeroMesa: 4,
      idGarcom: 7,
      nomeGarcom: 'Carlos',
      valorFinal: 125,
      totalPedidos: 3,
      totalItens: 6,
      dataAbertura: '2026-07-17T09:00:00',
      dataFechamento: '2026-07-17T10:30:00',
      duracaoMinutos: 90,
    }]);

    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('mantém somente o indicador de problemas clicável', () => {
    const indicadoresClicaveis = fixture.nativeElement.querySelectorAll('.kpi--clicavel');
    expect(indicadoresClicaveis.length).toBe(1);
    expect(indicadoresClicaveis[0].getAttribute('href')).toContain('filtro=PROBLEMAS');
    expect(indicadoresClicaveis[0].classList).toContain('kpi--com-problemas');
  });

  it('direciona a carga do garçom para o filtro correspondente', () => {
    const link: HTMLAnchorElement = fixture.nativeElement.querySelector('.carga li a');
    expect(link.getAttribute('href')).toContain('garcomId=7');
  });

  it('exibe a contagem global de mesas ocupadas sem garçom retornada pelo resumo', () => {
    const alerta = [...fixture.nativeElement.querySelectorAll('.alertas li')]
      .find((item: HTMLElement) => item.textContent?.includes('ocupadas sem garçom'));

    expect(alerta?.textContent).toContain('4 mesas ocupadas sem garçom');
  });

  it('abre os detalhes de um atendimento recente', () => {
    const registro: HTMLButtonElement = fixture.nativeElement.querySelector('.atividade--principal li button');
    registro.click();
    fixture.detectChanges();

    const modal = fixture.nativeElement.querySelector('.modal--detalhe');
    expect(modal).not.toBeNull();
    expect(modal.textContent).toContain('125');
    expect(modal.textContent).toContain('6');
  });
});
