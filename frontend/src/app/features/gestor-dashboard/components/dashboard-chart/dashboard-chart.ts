import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'fk-dashboard-chart',
  template: '<canvas #canvas></canvas>',
  styles: ':host { display: block; position: relative; width: 100%; height: 100%; min-height: 0; } canvas { max-width: 100%; }',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardChart implements AfterViewInit, OnChanges, OnDestroy {
  @Input({ required: true }) config!: ChartConfiguration;
  @ViewChild('canvas') private canvas?: ElementRef<HTMLCanvasElement>;
  private chart?: Chart;

  ngAfterViewInit(): void { this.renderizar(); }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config'] && this.canvas) this.renderizar();
  }

  ngOnDestroy(): void { this.chart?.destroy(); }

  private renderizar(): void {
    if (!this.canvas || !this.config) return;
    if (typeof navigator !== 'undefined' && navigator.userAgent.toLowerCase().includes('jsdom')) return;
    this.chart?.destroy();
    this.chart = new Chart(this.canvas.nativeElement, this.config);
  }
}
