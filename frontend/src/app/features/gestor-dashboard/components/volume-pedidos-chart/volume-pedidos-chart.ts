import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { EstadoGrafico, FiltrosDashboard, VolumePedidosHorarioResponse } from '../../models/dashboard-graficos.models';
import { DashboardChartFilter, MesaFiltroGrafico } from '../dashboard-chart-filter/dashboard-chart-filter';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { chartColor } from '../chart-colors';

@Component({
  selector: 'fk-volume-pedidos-chart',
  imports: [DashboardChart, DashboardChartFilter],
  templateUrl: './volume-pedidos-chart.html',
  styleUrl: './volume-pedidos-chart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VolumePedidosChart {
  @Input({ required: true }) estado!: EstadoGrafico<VolumePedidosHorarioResponse>;
  @Input() filtrosAtivos = false;
  @Input({ required: true }) filtros!: FiltrosDashboard;
  @Input() mesas: readonly MesaFiltroGrafico[] = [];
  @Output() filtrosChange = new EventEmitter<FiltrosDashboard>();
  @Output() tentarNovamente = new EventEmitter<void>();

  config(dados: VolumePedidosHorarioResponse): ChartConfiguration<'bar'> {
    const laranja = chartColor('--fk-orange', 'rgb(234 88 12)');
    const histograma = this.histograma(dados);
    return {
      type: 'bar',
      data: {
        labels: histograma.labels,
        datasets: [{
          label: 'Pedidos',
          data: histograma.quantidades,
          backgroundColor: laranja,
          borderRadius: 4,
          borderSkipped: false,
          maxBarThickness: 30,
          categoryPercentage: .86,
          barPercentage: .92,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { intersect: false, mode: 'index' },
        plugins: { legend: { display: false }, tooltip: { callbacks: { label: item => `${item.parsed.y} pedidos` } } },
        scales: {
          x: { grid: { display: false }, ticks: { autoSkip: true, maxRotation: 0, maxTicksLimit: 12 } },
          y: { beginAtZero: true, ticks: { precision: 0 } },
        },
      },
    };
  }

  histograma(dados: VolumePedidosHorarioResponse): {
    labels: string[];
    quantidades: number[];
    horarioPico: string | null;
    quantidadePico: number;
  } {
    const porHorario = new Map<string, number>();
    for (const item of dados.dados) {
      porHorario.set(item.horario, (porHorario.get(item.horario) ?? 0) + item.quantidade);
    }
    const itens = [...porHorario.entries()].sort(([horarioA], [horarioB]) => horarioA.localeCompare(horarioB));
    const pico = itens.reduce<[string, number] | null>(
      (maior, atual) => maior === null || atual[1] > maior[1] ? atual : maior,
      null,
    );
    return {
      labels: itens.map(([horario]) => horario),
      quantidades: itens.map(([, quantidade]) => quantidade),
      horarioPico: pico?.[0] ?? null,
      quantidadePico: pico?.[1] ?? 0,
    };
  }
}
