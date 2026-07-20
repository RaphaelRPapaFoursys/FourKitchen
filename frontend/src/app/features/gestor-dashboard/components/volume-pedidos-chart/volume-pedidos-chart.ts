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

  config(dados: VolumePedidosHorarioResponse): ChartConfiguration<'line'> {
    const laranja = chartColor('--fk-orange', 'rgb(234 88 12)');
    return {
      type: 'line',
      data: {
        labels: dados.dados.map(item => item.horario),
        datasets: [{
          label: 'Pedidos',
          data: dados.dados.map(item => item.quantidade),
          borderColor: laranja,
          backgroundColor: chartColor('--fk-orange-soft', 'rgb(255 237 213)'),
          pointBackgroundColor: laranja,
          pointRadius: 4,
          pointHoverRadius: 6,
          fill: true,
          tension: .3,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { intersect: false, mode: 'index' },
        plugins: { legend: { display: false }, tooltip: { callbacks: { label: item => `${item.parsed.y} pedidos` } } },
        scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { precision: 0 } } },
      },
    };
  }
}
