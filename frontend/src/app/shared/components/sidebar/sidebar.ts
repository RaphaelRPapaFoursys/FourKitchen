import { ChangeDetectionStrategy, Component, output } from '@angular/core';

import { Icon } from '../icon/icon';

/**
 * Sidebar de navegação do painel (navy escura). Item ativo = pill laranja.
 * Decoração de folhas + onda no rodapé (assets/images/flor-e-listra-fourkitchen.png).
 * Apresentacional: emite `sair`; a navegação entre abas ainda não tem rota própria.
 */
@Component({
  selector: 'fk-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  readonly sair = output<void>();
}
