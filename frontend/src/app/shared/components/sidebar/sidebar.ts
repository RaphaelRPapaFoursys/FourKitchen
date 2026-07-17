import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { Icon } from '../icon/icon';

/** Navegação compartilhada das telas do gestor. */
@Component({
  selector: 'fk-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {}
