import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { Garcom } from './garcom';

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
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/pedidos-totem/problemas`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/detalhe`).flush(criarDetalheMesa());
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.match(`${environment.apiUrl}/api/garcom/pedidos-totem/problemas`)
      .forEach(request => request.flush([]));
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('exibe card compacto com o numero da mesa sem confundir pedido e mesa', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    const card = elemento.querySelector('.table-card') as HTMLElement;

    expect(card.classList).toContain('table-card--problem');
    expect(card.textContent).toContain('Mesa 10');
    expect(card.textContent).not.toContain('#100025');
    expect(card.textContent).toContain('Ver detalhes');
  });

  it('separa pedidos ativos, prontos e historico sem duplicar pedidos no modal', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);

    const ativos = elemento.querySelector('.orders-section--active') as HTMLElement;
    const prontos = elemento.querySelector('.orders-section--ready') as HTMLElement;
    const historico = elemento.querySelector('.history-section') as HTMLElement;

    expect(ativos.textContent).toContain('#100025');
    expect(ativos.textContent).not.toContain('#100026');
    expect(prontos.textContent).toContain('#100026');
    expect(prontos.textContent).not.toContain('#100027');
    expect(historico.textContent).toContain('#100027');
    expect(historico.textContent).not.toContain('Sem gelo');
  });

  it('exibe a observacao do item no historico', () => {
    const detalhe: any = criarDetalheMesa();
    detalhe.pedidos[2].itens[0].observacao = 'Sem cobertura';
    (component as any).detalhesPorMesa.set({ 1: detalhe });
    fixture.detectChanges();

    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    (elemento.querySelector('.history-order summary') as HTMLElement).click();
    fixture.detectChanges();

    const historico = elemento.querySelector('.history-section') as HTMLElement;
    expect(historico.textContent).toContain('Observação: Sem cobertura');
  });

  it('destaca os valores de um pedido cancelado no historico', () => {
    const detalhe: any = criarDetalheMesa();
    detalhe.pedidos[2] = { ...detalhe.pedidos[2], status: 'CANCELADO' };
    (component as any).detalhesPorMesa.set({ 1: detalhe });
    fixture.detectChanges();

    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);

    const pedidoCancelado = elemento.querySelector('.history-order--cancelled') as HTMLElement;
    expect(pedidoCancelado).toBeTruthy();
    expect(pedidoCancelado.querySelector('summary > strong')?.textContent).toContain('30,00');
    expect(pedidoCancelado.querySelector('.history-items p > strong')?.textContent).toContain('30,00');
  });

  it('exibe a abertura com dia, mes, ano e horario', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);

    expect(elemento.textContent).toContain('02/07/2026 | 10:00');
  });

  it('nao exibe nem soma item removido em um pedido ativo', () => {
    const detalhe: any = criarDetalheMesa();
    detalhe.pedidos[0] = {
      ...detalhe.pedidos[0],
      itens: [
        ...detalhe.pedidos[0].itens,
        {
          id: 83,
          idProduto: 7,
          nomeProduto: 'Batata removida',
          quantidade: 1,
          precoUnitario: 12,
          observacao: null,
          status: 'REMOVIDO',
        },
      ],
    };
    (component as any).detalhesPorMesa.set({ 1: detalhe });
    fixture.detectChanges();

    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);

    const ativos = elemento.querySelector('.orders-section--active') as HTMLElement;
    expect(ativos.textContent).not.toContain('Batata removida');
    expect((component as any).valorPedido(detalhe.pedidos[0])).toBe(25);
  });

  it('seleciona categoria e produto antes de registrar a substituicao', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    abrirSolicitacao(elemento, fixture);

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush(criarProblemasMesa());
    fixture.detectChanges();

    clicarBotao(elemento, 'Substituir item');
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/cardapio`).flush([{
      categoriaId: 3,
      categoriaNome: 'Salgados',
      produtos: [{ id: 12, nome: 'Coxinha de frango', descricao: 'Disponivel', preco: 10.5 }],
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

    const observacao = elemento.querySelector('textarea') as HTMLTextAreaElement;
    observacao.value = 'Sem pimenta';
    observacao.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    clicarBotao(elemento, 'Confirmar decisão');
    const decisao = httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas/decisao`);
    expect(decisao.request.body).toEqual({
      idPedido: 25,
      idProdutoPedido: 80,
      novoStatusProdutoPedido: 'DISPONIVEL',
      pedidoCancelado: false,
      idNovoProduto: 12,
      observacaoNovoProduto: 'Sem pimenta',
    });
    decisao.flush(null, { status: 204, statusText: 'No Content' });
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([]);
  });

  it('exibe erro da solicitacao e permite tentar novamente', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    abrirSolicitacao(elemento, fixture);

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush(
      { msgError: 'Servico de pedidos indisponivel' },
      { status: 502, statusText: 'Bad Gateway' },
    );
    fixture.detectChanges();

    expect(elemento.querySelector('.decision-modal .modal-error')).not.toBeNull();
    expect(elemento.textContent).toContain('Servico de pedidos indisponivel');
    clicarBotao(elemento, 'Tentar novamente');
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush({
      ...criarProblemasMesa(),
      pedidos: [],
      problemas: [],
    });
  });

  it('confirma o cancelamento permitido pelo fluxo de problema', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    abrirSolicitacao(elemento, fixture);
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas`).flush(criarProblemasMesa());
    fixture.detectChanges();

    clicarBotao(elemento, 'Cancelar pedido');
    fixture.detectChanges();
    clicarBotao(elemento, 'Confirmar decisão');
    fixture.detectChanges();
    expect(elemento.textContent).toContain('Esta ação não poderá ser desfeita');
    expect(elemento.textContent).toContain('#100025');

    clicarBotao(elemento, 'Confirmar cancelamento');
    const decisao = httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/problemas/decisao`);
    expect(decisao.request.body.pedidoCancelado).toBe(true);
    decisao.flush(null, { status: 204, statusText: 'No Content' });
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([]);
  });

  it('cancela diretamente um pedido enviado para a cozinha', () => {
    const detalhe: any = criarDetalheMesa();
    detalhe.pedidos[0] = { ...detalhe.pedidos[0], status: 'ENVIADO_COZINHA' };
    (component as any).detalhesPorMesa.set({ 1: detalhe });
    fixture.detectChanges();

    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    clicarBotao(elemento, 'Cancelar pedido');
    fixture.detectChanges();

    expect(elemento.textContent).toContain('#100025');
    clicarBotao(elemento, 'Confirmar cancelamento');

    const cancelamento = httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/pedidos/25/cancelar`);
    expect(cancelamento.request.method).toBe('PATCH');
    expect(cancelamento.request.body).toEqual({});
    cancelamento.flush(null, { status: 204, statusText: 'No Content' });
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([]);
  });

  it('fecha a conta elegivel a partir do modal de detalhes', () => {
    const mesaElegivel = {
      ...criarMesaComProblema(),
      pedidosAtivos: [{ id: 27, codigo: 100027, canal: 'MESA', status: 'ENTREGUE', idAtendimento: 8 }],
    };
    const detalheElegivel = { ...criarDetalheMesa(), pedidos: [criarPedidoEntregue()] };
    (component as any).mesas.set([mesaElegivel]);
    (component as any).detalhesPorMesa.set({ 1: detalheElegivel });
    fixture.detectChanges();

    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    const fechar = buscarBotao(elemento, 'Fechar mesa');
    expect(fechar.disabled).toBe(false);
    fechar.click();
    fixture.detectChanges();

    clicarBotao(elemento, 'Confirmar fechamento');
    const request = httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/fechar-conta`);
    expect(request.request.method).toBe('PATCH');
    request.flush({
      idMesa: 1,
      numero: 10,
      status: 'DISPONIVEL',
      idAtendimento: 8,
      codigoSessao: 123456,
      dataAbertura: '2026-07-02T10:00:00',
      dataFechamento: '2026-07-02T11:00:00',
      conta: detalheElegivel.conta,
    });
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([]);
  });

  it('permite visualizar a divisao da conta por pessoas antes do fechamento', () => {
    const mesaElegivel = {
      ...criarMesaComProblema(),
      pedidosAtivos: [{ id: 27, codigo: 100027, canal: 'MESA', status: 'ENTREGUE', idAtendimento: 8 }],
    };
    const detalheElegivel = { ...criarDetalheMesa(), pedidos: [criarPedidoEntregue()] };
    (component as any).mesas.set([mesaElegivel]);
    (component as any).detalhesPorMesa.set({ 1: detalheElegivel });
    fixture.detectChanges();

    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);
    clicarBotao(elemento, 'Fechar mesa');
    fixture.detectChanges();
    clicarBotao(elemento, 'Dividir por pessoas');
    fixture.detectChanges();

    expect(elemento.textContent).toContain('Pessoa 1');
    expect(elemento.textContent).toContain('Pessoa 2');
    expect(elemento.textContent).toContain('R$ 35,00');

    (elemento.querySelector('[aria-label="Adicionar pessoa"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(elemento.textContent).toContain('Pessoa 3');
    expect(elemento.textContent).toContain('R$ 23,34');
    expect(elemento.textContent).toContain('R$ 23,33');
  });

  it('permite ao garcom marcar um pedido pronto como entregue', () => {
    const elemento: HTMLElement = fixture.nativeElement;
    abrirDetalhes(elemento, fixture);

    clicarBotao(elemento, 'Marcar entregue');
    const entrega = httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/pedidos/26/entregar`);
    expect(entrega.request.method).toBe('PATCH');
    entrega.flush(null, { status: 204, statusText: 'No Content' });

    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas`).flush([{
      ...criarMesaComProblema(),
      pedidosAtivos: [{ id: 26, codigo: 100026, canal: 'GARCOM', status: 'ENTREGUE', idAtendimento: 8 }],
    }]);
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/pedidos-totem/problemas`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/api/garcom/mesas/1/detalhe`).flush({
      ...criarDetalheMesa(),
      pedidos: [criarPedidoEntregue()],
    });
  });
});

function abrirDetalhes(elemento: HTMLElement, fixture: ComponentFixture<Garcom>): void {
  clicarBotao(elemento, 'Ver detalhes');
  fixture.detectChanges();
}

function abrirSolicitacao(elemento: HTMLElement, fixture: ComponentFixture<Garcom>): void {
  clicarBotao(elemento, 'Ver solicitação da cozinha');
  fixture.detectChanges();
}

function clicarBotao(elemento: HTMLElement, texto: string): void {
  buscarBotao(elemento, texto).click();
}

function buscarBotao(elemento: HTMLElement, texto: string): HTMLButtonElement {
  return Array.from(elemento.querySelectorAll('button'))
    .find(botao => botao.textContent?.includes(texto)) as HTMLButtonElement;
}

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

function criarDetalheMesa() {
  const problema = criarProblemasMesa();
  return {
    mesa: problema.mesa,
    conta: { subtotal: 70, total: 70, totalPedidos: 3, totalItens: 3 },
    pedidos: [problema.pedidos[0], criarPedidoPronto(), criarPedidoEntregue()],
    problemas: problema.problemas,
  };
}

function criarProblemasMesa() {
  return {
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
  };
}

function criarPedidoPronto() {
  return {
    id: 26,
    codigo: 100026,
    canal: 'GARCOM',
    status: 'PRONTO',
    dataCriacao: '2026-07-02T10:35:00',
    dataInicioPreparo: '2026-07-02T10:36:00',
    dataPronto: '2026-07-02T10:45:00',
    itens: [{
      id: 81,
      idProduto: 5,
      nomeProduto: 'Suco',
      quantidade: 1,
      precoUnitario: 15,
      observacao: 'Sem gelo',
      status: 'DISPONIVEL',
    }],
  };
}

function criarPedidoEntregue() {
  return {
    id: 27,
    codigo: 100027,
    canal: 'MESA',
    status: 'ENTREGUE',
    dataCriacao: '2026-07-02T10:10:00',
    dataInicioPreparo: '2026-07-02T10:12:00',
    dataPronto: '2026-07-02T10:20:00',
    itens: [{
      id: 82,
      idProduto: 6,
      nomeProduto: 'Sobremesa',
      quantidade: 1,
      precoUnitario: 30,
      observacao: null,
      status: 'DISPONIVEL',
    }],
  };
}
