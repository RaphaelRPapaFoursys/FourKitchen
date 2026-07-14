import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { Mesa } from './mesa';

describe('Mesa', () => {
  let component: Mesa;
  let fixture: ComponentFixture<Mesa>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mesa],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Mesa);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    httpMock.expectOne(`${environment.apiUrl}/api/auth/me`).flush({
      id: 1,
      nome: 'Mesa 3',
      email: 'mesa3@fourkitchen.com',
      perfil: 'MESA',
      idMesa: 3,
    });
    httpMock.expectOne(`${environment.apiUrl}/api/mesa/atendimento-atual`).flush({
      idMesa: 3,
      idAtendimento: 8,
      codigoAtendimento: 123456,
      status: 'OCUPADA',
    });
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('chama o garcom usando o codigo do atendimento atual e bloqueia novo clique apos sucesso', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    const botao = Array.from(elemento.querySelectorAll('button'))
      .find(item => item.textContent?.includes('Chamar garçom')) as HTMLButtonElement;

    botao.click();
    const request = httpMock.expectOne(`${environment.apiUrl}/api/mesa/chamadas-garcom`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ codigoSessao: 123456 });
    request.flush({ id: 4, tipo: 'CHAMADA_GARCOM', mensagem: 'Cliente solicitou atendimento', destino: 'GARCOM', lida: false, data: '2026-07-02T10:15:30', idMesa: 3, idAtendimento: 8, idGarcom: 7 });
    fixture.detectChanges();

    expect(elemento.textContent).toContain('Chamada enviada');
    expect(botao.disabled).toBe(true);
  });

  it('mostra erro da chamada e libera nova tentativa', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    const botao = Array.from(elemento.querySelectorAll('button'))
      .find(item => item.textContent?.includes('Chamar garçom')) as HTMLButtonElement;

    botao.click();
    httpMock.expectOne(`${environment.apiUrl}/api/mesa/chamadas-garcom`).flush(
      { msgError: 'Mesa sem garcom responsavel' },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();

    expect(elemento.textContent).toContain('Mesa sem garcom responsavel');
    expect(botao.disabled).toBe(false);
  });
});
