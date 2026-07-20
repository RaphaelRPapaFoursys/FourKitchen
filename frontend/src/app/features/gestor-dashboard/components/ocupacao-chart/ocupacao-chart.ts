import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { chartColor } from '../chart-colors';

@Component({
  selector: 'fk-ocupacao-chart',
  imports: [DashboardChart],
  templateUrl: './ocupacao-chart.html',
  styleUrl: './ocupacao-chart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OcupacaoChart {
  @Input() ocupadas = 0;
  @Input() livres = 0;
  @Input() percentual = 0;

  get config(): ChartConfiguration<'doughnut'> {
    return {
      type: 'doughnut',
      data: {
        labels: ['Ocupadas', 'Disponíveis'],
        datasets: [{
          data: [this.ocupadas, this.livres],
          backgroundColor: [chartColor('--fk-orange', 'rgb(234 88 12)'), chartColor('--fk-green', 'rgb(34 197 94)')],
          borderWidth: 0,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false, cutout: '72%', plugins: { legend: { display: false } } },
    };
  }
}
