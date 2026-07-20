import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { EstadoGrafico, FiltrosDashboard, PedidosCanalResponse } from '../../models/dashboard-graficos.models';
import { DashboardChartFilter, MesaFiltroGrafico } from '../dashboard-chart-filter/dashboard-chart-filter';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { chartColor } from '../chart-colors';

@Component({
  selector: 'fk-pedidos-canal-chart',
  imports: [DashboardChart, DashboardChartFilter],
  templateUrl: './pedidos-canal-chart.html',
  styleUrl: './pedidos-canal-chart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PedidosCanalChart {
  @Input({ required: true }) estado!: EstadoGrafico<PedidosCanalResponse>;
  @Input() filtrosAtivos = false;
  @Input({ required: true }) filtros!: FiltrosDashboard;
  @Input() mesas: readonly MesaFiltroGrafico[] = [];
  @Output() filtrosChange = new EventEmitter<FiltrosDashboard>();
  @Output() tentarNovamente = new EventEmitter<void>();

  config(dados: PedidosCanalResponse): ChartConfiguration<'doughnut'> {
    const cores = {
      TOTEM: chartColor('--fk-orange', 'rgb(234 88 12)'),
      MESA: chartColor('--fk-blue', 'rgb(37 99 235)'),
      GARCOM: chartColor('--fk-green', 'rgb(34 197 94)'),
    };
    return {
      type: 'doughnut',
      data: {
        labels: dados.dados.map(item => item.descricao),
        datasets: [{ data: dados.dados.map(item => item.quantidade), backgroundColor: dados.dados.map(item => cores[item.canal]), borderWidth: 0 }],
      },
      options: {
        responsive: true, maintainAspectRatio: false, cutout: '68%',
        plugins: { legend: { display: false }, tooltip: { callbacks: { label: item => {
          const detalhe = dados.dados[item.dataIndex];
          return `${detalhe.descricao}: ${detalhe.quantidade} (${detalhe.percentual.toFixed(2)}%)`;
        } } } },
      },
    };
  }
}
