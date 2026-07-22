import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { EstadoGrafico, FiltrosDashboard, ProblemasCozinhaMotivoResponse } from '../../models/dashboard-graficos.models';
import { DashboardChartFilter, MesaFiltroGrafico } from '../dashboard-chart-filter/dashboard-chart-filter';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { chartColor } from '../chart-colors';

@Component({
  selector: 'fk-problemas-cozinha-chart',
  imports: [DashboardChart, DashboardChartFilter],
  templateUrl: './problemas-cozinha-chart.html',
  styleUrl: './problemas-cozinha-chart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProblemasCozinhaChart {
  @Input({ required: true }) estado!: EstadoGrafico<ProblemasCozinhaMotivoResponse>;
  @Input() filtrosAtivos = false;
  @Input({ required: true }) filtros!: FiltrosDashboard;
  @Input() mesas: readonly MesaFiltroGrafico[] = [];
  @Output() filtrosChange = new EventEmitter<FiltrosDashboard>();
  @Output() tentarNovamente = new EventEmitter<void>();

  config(dados: ProblemasCozinhaMotivoResponse): ChartConfiguration<'bar'> {
    return {
      type: 'bar',
      data: {
        labels: dados.dados.map(item => item.descricao),
        datasets: [{
          label: 'Problemas',
          data: dados.dados.map(item => item.quantidade),
          backgroundColor: dados.dados.map((_, indice) => indice === 0
            ? chartColor('--fk-red', 'rgb(220 38 38)')
            : chartColor('--fk-amber', 'rgb(245 158 11)')),
          borderRadius: 5,
        }],
      },
      options: {
        indexAxis: 'y', responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { callbacks: { label: item => {
          const detalhe = dados.dados[item.dataIndex];
          return `${detalhe.quantidade} (${detalhe.percentual.toFixed(2)}%)`;
        } } } },
        scales: { x: { beginAtZero: true, ticks: { precision: 0 } }, y: { grid: { display: false } } },
      },
    };
  }
}
