import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { chartColor } from '../chart-colors';

@Component({
  selector: 'fk-status-pedidos-chart',
  imports: [DashboardChart],
  templateUrl: './status-pedidos-chart.html',
  styleUrl: './status-pedidos-chart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusPedidosChart {
  @Input() ativos = 0;
  @Input() preparo = 0;
  @Input() prontos = 0;
  @Input() problemas = 0;

  get config(): ChartConfiguration<'bar'> {
    return {
      type: 'bar',
      data: {
        labels: ['Ativos', 'Preparo', 'Prontos', 'Problemas'],
        datasets: [{
          data: [this.ativos, this.preparo, this.prontos, this.problemas],
          backgroundColor: [
            chartColor('--fk-muted', 'rgb(100 116 139)'),
            chartColor('--fk-orange', 'rgb(234 88 12)'),
            chartColor('--fk-green', 'rgb(34 197 94)'),
            chartColor('--fk-red', 'rgb(220 38 38)'),
          ],
          borderRadius: 5,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { precision: 0 } } },
      },
    };
  }
}
