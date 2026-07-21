import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { GestorDashboard } from './gestor-dashboard';

const BASE_URL = `${environment.apiUrl}/api/gestor`;
const HOJE = dataLocal(new Date());
const ONTEM = dataLocal(new Date(Date.now() - 86_400_000));

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
      cargaGarcons: [
        { id: 7, nome: 'Carlos', mesasAtivas: 1 },
        { id: 8, nome: 'Joana Nova', mesasAtivas: 0 },
      ],
    });
    httpMock.expectOne(`${BASE_URL}/atendimentos/historico`).flush([
      {
        id: 29,
        idAtendimento: 79,
        codigoSessao: 111222,
        idMesa: 2,
        numeroMesa: 14,
        idGarcom: 7,
        nomeGarcom: 'Carlos',
        valorFinal: 80,
        totalPedidos: 1,
        totalItens: 2,
        dataAbertura: `${ONTEM}T09:00:00`,
        dataFechamento: `${ONTEM}T10:00:00`,
        duracaoMinutos: 60,
      },
      {
        id: 30,
        idAtendimento: 80,
        codigoSessao: 987654,
        idMesa: 1,
        numeroMesa: 4,
        idGarcom: 7,
        nomeGarcom: 'Carlos Atualizado',
        valorFinal: 125,
        totalPedidos: 3,
        totalItens: 6,
        dataAbertura: `${HOJE}T00:30:00`,
        dataFechamento: `${HOJE}T01:30:00`,
        duracaoMinutos: 90,
      },
    ]);
    httpMock.expectOne(`${BASE_URL}/mesas/opcoes`).flush([
      { id: 1, numero: 4 },
      { id: 2, numero: 14 },
      { id: 3, numero: 22 },
    ]);
    await fixture.whenStable();
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 0));

    httpMock.expectOne(request => request.url === `${BASE_URL}/dashboard/pedidos-por-horario`).flush({
      periodo: 'HOJE', totalPedidos: 0, horarioPico: null, quantidadeNoPico: 0, dados: [],
    });
    httpMock.expectOne(request => request.url === `${BASE_URL}/dashboard/problemas-por-motivo`).flush({
      periodo: 'HOJE', totalProblemas: 0, motivoMaisFrequente: null, dados: [],
    });
    httpMock.expectOne(request => request.url === `${BASE_URL}/dashboard/pedidos-por-canal`).flush({
      periodo: 'HOJE', totalPedidos: 0, dados: [
        { canal: 'TOTEM', descricao: 'Totem', quantidade: 0, percentual: 0 },
        { canal: 'MESA', descricao: 'Tablet da mesa', quantidade: 0, percentual: 0 },
        { canal: 'GARCOM', descricao: 'Garçom', quantidade: 0, percentual: 0 },
      ],
    });
    httpMock.expectOne(request => request.url === `${BASE_URL}/dashboard/ranking-produtos` && request.params.get('periodo') === 'ULTIMOS_30_DIAS').flush({
      periodo: 'ULTIMOS_30_DIAS',
      dados: [
        { idProduto: 10, nomeProduto: 'Risoto', quantidade: 14 },
        { idProduto: 11, nomeProduto: 'Limonada', quantidade: 9 },
      ],
    });

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

  it('exibe o ranking de produtos abaixo da carga dos garçons', () => {
    const ranking: HTMLElement = fixture.nativeElement.querySelector('.ranking-produtos');
    expect(ranking.textContent).toContain('Produtos mais pedidos');
    expect(ranking.textContent).toContain('1º');
    expect(ranking.textContent).toContain('Risoto');
    expect(ranking.textContent).toContain('14');
    expect(ranking.nextElementSibling?.classList).toContain('atividade--lateral');
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

  it('ordena o histórico recente do mais novo para o mais antigo', () => {
    const primeiroRegistro = fixture.nativeElement.querySelector('.atividade--principal li button');
    expect(primeiroRegistro.textContent).toContain('Mesa 04');
  });

  it('busca o número da mesa de forma exata', async () => {
    const abrirHistorico: HTMLButtonElement = fixture.nativeElement.querySelector('.atividade__rodape button');
    abrirHistorico.click();
    fixture.detectChanges();

    const busca: HTMLInputElement = fixture.nativeElement.querySelector('.historico-filtros__busca input');
    busca.value = '4';
    busca.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    fixture.detectChanges();

    const registros = fixture.nativeElement.querySelectorAll('.historico-registro');
    expect(registros.length).toBe(1);
    expect(registros[0].textContent).toContain('Mesa 04');
    expect(registros[0].textContent).not.toContain('Mesa 14');
  });

  it('atualiza o dropdown com garçons ativos mesmo sem histórico finalizado', () => {
    const abrirHistorico: HTMLButtonElement = fixture.nativeElement.querySelector('.atividade__rodape button');
    abrirHistorico.click();
    fixture.detectChanges();
    const abrirFiltros: HTMLButtonElement = fixture.nativeElement.querySelector('.historico-filtros__botao');
    abrirFiltros.click();
    fixture.detectChanges();

    const seletor: HTMLSelectElement = fixture.nativeElement.querySelector('.historico-filtros select');
    const opcoes = [...seletor.options].map(opcao => opcao.textContent?.trim());
    expect(opcoes).toContain('Joana Nova');
  });

  it('filtra o histórico por intervalo personalizado de datas', async () => {
    const abrirHistorico: HTMLButtonElement = fixture.nativeElement.querySelector('.atividade__rodape button');
    abrirHistorico.click();
    fixture.detectChanges();
    const abrirFiltros: HTMLButtonElement = fixture.nativeElement.querySelector('.historico-filtros__botao');
    abrirFiltros.click();
    fixture.detectChanges();

    const datas: NodeListOf<HTMLInputElement> = fixture.nativeElement.querySelectorAll('.historico-filtros input[type="date"]');
    for (const input of datas) {
      input.value = HOJE;
      input.dispatchEvent(new Event('input'));
    }
    await fixture.whenStable();
    fixture.detectChanges();

    const registros = fixture.nativeElement.querySelectorAll('.historico-registro');
    expect(registros.length).toBe(1);
    expect(registros[0].textContent).toContain('Mesa 04');
  });

  it('abre o resumo dos pedidos a partir do total do atendimento', async () => {
    const registro: HTMLButtonElement = fixture.nativeElement.querySelector('.atividade--principal li button');
    registro.click();
    fixture.detectChanges();

    const abrirResumo: HTMLButtonElement = fixture.nativeElement.querySelector('.modal__resumo-pedido');
    abrirResumo.click();
    httpMock.expectOne(`${BASE_URL}/atendimentos/80/pedidos`).flush([{
      id: 20,
      codigo: 100020,
      canal: 'MESA',
      status: 'ENTREGUE',
      dataCriacao: '2026-07-17T09:30:00',
      dataInicioPreparo: '2026-07-17T09:35:00',
      dataPronto: '2026-07-17T09:50:00',
      itens: [{ id: 1, idProduto: 10, nomeProduto: 'Risoto', quantidade: 2, precoUnitario: 30, observacao: null, status: 'ATIVO' }],
    }]);
    await fixture.whenStable();
    fixture.detectChanges();

    const modal = fixture.nativeElement.querySelector('.modal--resumo-pedidos');
    expect(modal.textContent).toContain('Risoto');
    expect(modal.textContent).toContain('2');
  });
});

function dataLocal(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}
