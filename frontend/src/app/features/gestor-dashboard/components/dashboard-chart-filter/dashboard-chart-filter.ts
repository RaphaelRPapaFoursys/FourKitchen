import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Icon } from '../../../../shared/components/icon/icon';
import {
  CanalPedido,
  FILTROS_DASHBOARD_INICIAIS,
  FiltrosDashboard,
  PeriodoDashboard,
  StatusPedidoDashboard,
} from '../../models/dashboard-graficos.models';

export interface MesaFiltroGrafico {
  id: number;
  numero: number;
}

@Component({
  selector: 'fk-dashboard-chart-filter',
  imports: [FormsModule, Icon],
  templateUrl: './dashboard-chart-filter.html',
  styleUrl: './dashboard-chart-filter.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardChartFilter {
  @Input({ required: true }) filtros!: FiltrosDashboard;
  @Input() mesas: readonly MesaFiltroGrafico[] = [];
  @Output() filtrosChange = new EventEmitter<FiltrosDashboard>();

  protected readonly aberto = signal(false);
  protected readonly rascunho = signal<FiltrosDashboard>({ ...FILTROS_DASHBOARD_INICIAIS });
  protected readonly erro = signal<string | null>(null);

  protected aplicarRapido(periodo: 'HOJE' | 'ULTIMOS_7_DIAS' | 'ULTIMOS_30_DIAS'): void {
    this.filtrosChange.emit({ ...this.filtros, periodo, dataInicial: '', dataFinal: '' });
  }

  protected alternarAvancados(): void {
    if (!this.aberto()) {
      this.rascunho.set({ ...this.filtros });
      this.erro.set(null);
    }
    this.aberto.update(valor => !valor);
  }

  protected atualizarPeriodo(periodo: PeriodoDashboard): void {
    this.rascunho.update(filtros => ({
      ...filtros,
      periodo,
      dataInicial: periodo === 'PERSONALIZADO' ? filtros.dataInicial : '',
      dataFinal: periodo === 'PERSONALIZADO' ? filtros.dataFinal : '',
    }));
  }

  protected atualizarData(campo: 'dataInicial' | 'dataFinal', valor: string): void {
    this.rascunho.update(filtros => ({ ...filtros, [campo]: valor }));
  }

  protected atualizarCanal(canal: CanalPedido | ''): void {
    this.rascunho.update(filtros => ({ ...filtros, canal: canal || null }));
  }

  protected atualizarMesa(idMesa: number | null): void {
    this.rascunho.update(filtros => ({ ...filtros, idMesa: idMesa === null ? null : Number(idMesa) }));
  }

  protected atualizarStatus(status: StatusPedidoDashboard | ''): void {
    this.rascunho.update(filtros => ({ ...filtros, status: status || null }));
  }

  protected aplicarAvancados(): void {
    const filtros = this.rascunho();
    if (filtros.periodo === 'PERSONALIZADO'
      && (!filtros.dataInicial || !filtros.dataFinal || filtros.dataInicial > filtros.dataFinal)) {
      this.erro.set('Informe um intervalo de datas válido.');
      return;
    }
    if (filtros.periodo === 'PERSONALIZADO') {
      const limite = new Date(`${filtros.dataInicial}T00:00:00Z`);
      limite.setUTCFullYear(limite.getUTCFullYear() + 1);
      if (new Date(`${filtros.dataFinal}T00:00:00Z`) > limite) {
        this.erro.set('O intervalo máximo permitido é de um ano.');
        return;
      }
    }
    this.filtrosChange.emit({ ...filtros });
    this.aberto.set(false);
    this.erro.set(null);
  }

  protected limpar(): void {
    const filtros = { ...FILTROS_DASHBOARD_INICIAIS };
    this.rascunho.set(filtros);
    this.filtrosChange.emit(filtros);
    this.aberto.set(false);
    this.erro.set(null);
  }

  protected quantidadeAvancados(): number {
    const periodoAvancado = ['HOJE', 'ULTIMOS_7_DIAS', 'ULTIMOS_30_DIAS'].includes(this.filtros.periodo) ? 0 : 1;
    return periodoAvancado
      + (this.filtros.canal === null ? 0 : 1)
      + (this.filtros.idMesa === null ? 0 : 1)
      + (this.filtros.status === null ? 0 : 1);
  }
}
