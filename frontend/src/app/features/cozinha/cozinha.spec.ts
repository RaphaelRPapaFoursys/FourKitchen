import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { Cozinha } from './cozinha';
import { environment } from '../../../environments/environment';

describe('Cozinha', () => {
  let component: Cozinha;
  let fixture: ComponentFixture<Cozinha>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cozinha],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Cozinha);
    component = fixture.componentInstance;

    httpMock.expectOne(`${environment.apiUrl}/api/cozinha/fila`).flush([]);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('permite sinalizar problema apenas antes de iniciar o preparo', () => {
    (component as any).pedidos.set([
      criarPedido('ENVIADO_COZINHA'),
      criarPedido('EM_PREPARO'),
    ]);
    fixture.detectChanges();

    const botoesProblema = fixture.nativeElement.querySelectorAll('[aria-label="Sinalizar problema neste item"]');
    expect(botoesProblema).toHaveLength(1);
  });
});

function criarPedido(status: 'ENVIADO_COZINHA' | 'EM_PREPARO') {
  return {
    id: status === 'ENVIADO_COZINHA' ? 1 : 2,
    codigo: status === 'ENVIADO_COZINHA' ? 100001 : 100002,
    canal: 'MESA',
    status,
    idMesa: 1,
    origem: 'Mesa 01',
    idAtendimento: 1,
    dataCriacao: '2026-07-16T10:00:00',
    dataInicioPreparo: status === 'EM_PREPARO' ? '2026-07-16T10:01:00' : null,
    dataPronto: null,
    itens: [{
      id: status === 'ENVIADO_COZINHA' ? 10 : 20,
      idProduto: 1,
      nomeProduto: 'Suco',
      quantidade: 1,
      precoUnitario: 10,
      observacao: null,
      status: 'DISPONIVEL' as const,
    }],
  };
}
