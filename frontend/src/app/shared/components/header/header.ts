import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Avatar } from '../avatar/avatar';
import { Icon } from '../icon/icon';

/**
 * Topbar do painel: busca (pill), sino com badge e chip do usuário.
 * Transparente sobre o fundo da página (a decoração de onda fica no container).
 * `busca` é two-way (model); `notificacoes` nulo mostra só o ponto de alerta.
 */
@Component({
  selector: 'fk-topbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, Avatar, Icon],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Topbar {
  readonly busca = model('');
  readonly buscaPlaceholder = input('Pesquisar mesa, pedido ou garçom');
  readonly usuarioNome = input('Usuário');
  readonly usuarioInicial = input('?');
  readonly notificacoes = input<number | null>(null);
}
