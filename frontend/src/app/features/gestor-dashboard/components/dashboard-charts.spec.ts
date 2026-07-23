import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Type } from '@angular/core';

import { PedidosCanalChart } from './pedidos-canal-chart/pedidos-canal-chart';
import { ProblemasCozinhaChart } from './problemas-cozinha-chart/problemas-cozinha-chart';
import { VolumePedidosChart } from './volume-pedidos-chart/volume-pedidos-chart';
import { FILTROS_DASHBOARD_INICIAIS } from '../models/dashboard-graficos.models';

describe('gráficos analíticos do dashboard', () => {
  it('renderiza histograma e consolida o volume pela hora do dia', async () => {
    const fixture = await criar(VolumePedidosChart);
    fixture.componentRef.setInput('estado', {
      status: 'sucesso',
      dados: {
        periodo: 'ULTIMOS_7_DIAS', totalPedidos: 10, horarioPico: '13:00', quantidadeNoPico: 5,
        dados: [{ horario: '12:00', quantidade: 2 }, { horario: '13:00', quantidade: 5 }, { horario: '12:00', quantidade: 3 }],
      },
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Maior volume às 12:00');
    const config = fixture.componentInstance.config(fixture.componentInstance.estado.dados!);
    expect(config.type).toBe('bar');
    expect(config.data.labels).toEqual(['12:00', '13:00']);
    expect(config.data.datasets[0].data).toEqual([5, 5]);
    expect(fixture.nativeElement.querySelector('fk-dashboard-chart-filter')).not.toBeNull();
  });

  it('diferencia vazio e erro e permite tentar novamente', async () => {
    const fixture = await criar(ProblemasCozinhaChart);
    fixture.componentRef.setInput('estado', {
      status: 'sucesso', dados: { periodo: 'HOJE', totalProblemas: 0, motivoMaisFrequente: null, dados: [] },
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Nenhum problema foi registrado');

    fixture.componentRef.setInput('filtrosAtivos', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Nenhum problema corresponde aos filtros aplicados');

    let repeticoes = 0;
    fixture.componentInstance.tentarNovamente.subscribe(() => repeticoes++);
    fixture.componentRef.setInput('estado', { status: 'erro', dados: null, mensagem: 'Não foi possível carregar este gráfico.' });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.estado--erro button').click();
    expect(repeticoes).toBe(1);
  });

  it('permite alterar individualmente o período de um gráfico', async () => {
    const fixture = await criar(VolumePedidosChart);
    fixture.componentRef.setInput('estado', {
      status: 'sucesso', dados: { periodo: 'HOJE', totalPedidos: 0, horarioPico: null, quantidadeNoPico: 0, dados: [] },
    });
    let periodo = '';
    fixture.componentInstance.filtrosChange.subscribe(filtros => periodo = filtros.periodo);
    fixture.detectChanges();

    const botao = [...fixture.nativeElement.querySelectorAll('.filtros-grafico__rapidos button')]
      .find((item: HTMLButtonElement) => item.textContent?.includes('7 dias')) as HTMLButtonElement;
    botao.click();

    expect(periodo).toBe('ULTIMOS_7_DIAS');
  });

  it('mantém legenda tipada dos canais sem depender da ordem', async () => {
    const fixture = await criar(PedidosCanalChart);
    fixture.componentRef.setInput('estado', {
      status: 'sucesso',
      dados: {
        periodo: 'HOJE', totalPedidos: 10,
        dados: [
          { canal: 'GARCOM', descricao: 'Garçom', quantidade: 2, percentual: 20 },
          { canal: 'TOTEM', descricao: 'Totem', quantidade: 8, percentual: 80 },
          { canal: 'MESA', descricao: 'Tablet da mesa', quantidade: 0, percentual: 0 },
        ],
      },
    });
    fixture.detectChanges();

    const legenda = fixture.nativeElement.textContent;
    expect(legenda).toContain('Garçom');
    expect(legenda).toContain('80.00%');
    expect(fixture.componentInstance.config(fixture.componentInstance.estado.dados!).data.datasets[0].data).toEqual([2, 8, 0]);
  });

  async function criar<T>(component: Type<T>): Promise<ComponentFixture<T>> {
    await TestBed.configureTestingModule({ imports: [component] }).compileComponents();
    const fixture = TestBed.createComponent(component);
    fixture.componentRef.setInput('filtros', { ...FILTROS_DASHBOARD_INICIAIS });
    return fixture;
  }
});
