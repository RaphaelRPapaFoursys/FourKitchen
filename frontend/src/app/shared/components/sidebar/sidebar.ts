import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { Icon } from '../icon/icon';

/**
 * Sidebar de navegação do painel (navy escura). Item ativo = pill laranja
 * (via routerLinkActive). Decoração de folhas + onda no rodapé
 * (assets/images/flor-e-listra-fourkitchen.png). Emite `sair`.
 */
@Component({
  selector: 'fk-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  readonly sair = output<void>();
}
