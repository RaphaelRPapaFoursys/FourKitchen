import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { Garcom } from './garcom';
import { environment } from '../../../environments/environment';

describe('Garcom', () => {
  let component: Garcom;
  let fixture: ComponentFixture<Garcom>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Garcom],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Garcom);
    component = fixture.componentInstance;

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([criarMesaComProblema()]);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('destaca a mesa e exibe o resumo do pedido que aguarda decisao', () => {
    const elemento: HTMLElement = fixture.nativeElement;

    expect(elemento.querySelector('.table-card--problem')).not.toBeNull();
    expect(elemento.textContent).toContain('#100025');
    expect(elemento.textContent).toContain('Aguardando decisao');
  });

  it('seleciona categoria e produto pelo nome antes de registrar a substituicao', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    const botaoSolicitacao = Array.from(elemento.querySelectorAll('button'))
      .find(botao => botao.textContent?.includes('Ver solicitacao')) as HTMLButtonElement;

    botaoSolicitacao.click();

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush({
      mesa: {
        idMesa: 1,
        numero: 10,
        status: 'OCUPADA',
        idAtendimento: 8,
        codigoSessao: 123456,
        dataAbertura: '2026-07-02T10:00:00',
      },
      pedidos: [{
        id: 25,
        codigo: 100025,
        canal: 'MESA',
        status: 'AGUARDANDO_DECISAO',
        dataCriacao: '2026-07-02T10:30:00',
        dataInicioPreparo: null,
        dataPronto: null,
        itens: [{
          id: 80,
          idProduto: 4,
          nomeProduto: 'Hamburguer',
          quantidade: 1,
          precoUnitario: 25,
          observacao: null,
          status: 'FALTA_PRODUTO',
        }],
      }],
      problemas: [{
        idPedido: 25,
        idProdutoPedido: 80,
        tipo: 'FALTA_PRODUTO',
        mensagem: 'Item com falta de produto',
      }],
    });
    fixture.detectChanges();

    const botaoSubstituir = Array.from(elemento.querySelectorAll('button'))
      .find(botao => botao.textContent?.includes('Substituir item')) as HTMLButtonElement;
    botaoSubstituir.click();
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/cardapio`).flush([{
      categoriaId: 3,
      categoriaNome: 'Salgados',
      produtos: [{
        id: 12,
        nome: 'Coxinha de frango',
        descricao: 'Coxinha disponivel',
        preco: 10.5,
      }],
    }]);
    fixture.detectChanges();

    let seletores = elemento.querySelectorAll('select');
    const categoria = seletores.item(0);
    categoria.value = '3';
    categoria.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    seletores = elemento.querySelectorAll('select');
    const produto = seletores.item(1);
    expect(produto.textContent).toContain('Coxinha de frango');
    produto.value = '12';
    produto.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    const botaoConfirmar = Array.from(elemento.querySelectorAll('button'))
      .find(botao => botao.textContent?.includes('Confirmar decisao')) as HTMLButtonElement;
    botaoConfirmar.click();

    const decisao = httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas/decisao`);
    expect(decisao.request.method).toBe('PATCH');
    expect(decisao.request.body).toEqual({
      idPedido: 25,
      idProdutoPedido: 80,
      novoStatusProdutoPedido: 'DISPONIVEL',
      pedidoCancelado: false,
      idNovoProduto: 12,
    });
    decisao.flush(null, { status: 204, statusText: 'No Content' });
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([]);
  });

  it('exibe o erro dentro do modal e permite tentar novamente', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    const botaoSolicitacao = Array.from(elemento.querySelectorAll('button'))
      .find(botao => botao.textContent?.includes('Ver solicitacao')) as HTMLButtonElement;
    botaoSolicitacao.click();

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush(
      { msgError: 'Servico de pedidos indisponivel' },
      { status: 502, statusText: 'Bad Gateway' },
    );
    fixture.detectChanges();

    expect(elemento.querySelector('.modal-error')).not.toBeNull();
    expect(elemento.textContent).toContain('Servico de pedidos indisponivel');

    const tentarNovamente = Array.from(elemento.querySelectorAll('button'))
      .find(botao => botao.textContent?.includes('Tentar novamente')) as HTMLButtonElement;
    tentarNovamente.click();

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush({
      mesa: {
        idMesa: 1,
        numero: 10,
        status: 'OCUPADA',
        idAtendimento: 8,
        codigoSessao: 123456,
        dataAbertura: '2026-07-02T10:00:00',
      },
      pedidos: [],
      problemas: [],
    });
  });
});

function criarMesaComProblema() {
  return {
    idMesa: 1,
    numero: 10,
    status: 'OCUPADA',
    idAtendimento: 8,
    codigoSessao: 123456,
    idGarcom: 7,
    dataAbertura: '2026-07-02T10:00:00',
    pedidosAtivos: [{
      id: 25,
      codigo: 100025,
      canal: 'MESA',
      status: 'AGUARDANDO_DECISAO',
      idAtendimento: 8,
    }],
    chamadasPendentes: [],
    possuiChamadaPendente: false,
  };
}
