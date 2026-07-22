import { ChangeDetectionStrategy, Component, ElementRef, HostListener, inject, input, model, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Icon } from '../icon/icon';
import { UserMenu } from '../user-menu/user-menu';

/** Topbar compartilhada das telas de gestão. */
@Component({
  selector: 'fk-topbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, Icon, UserMenu],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Topbar {
  private readonly elementRef = inject(ElementRef);

  readonly busca = model('');
  readonly buscaPlaceholder = input('Pesquisar mesa, pedido ou garçom');
  readonly mostrarBusca = input(true);
  readonly usuarioNome = input('Usuário');
  readonly usuarioInicial = input('?');
  readonly notificacoes = input<number | null>(null);
  /** Um único estado garante que apenas um popover possa estar aberto. */
  protected readonly menuAberto = signal<'avatar' | 'notificacoes' | null>(null);

  protected alternarMenu(menu: 'avatar' | 'notificacoes'): void {
    this.menuAberto.update(aberto => aberto === menu ? null : menu);
  }

  protected fecharMenus(): void {
    this.menuAberto.set(null);
  }

  @HostListener('document:click', ['$event'])
  protected fecharAoClicarFora(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) this.fecharMenus();
  }

  @HostListener('document:keydown.escape')
  protected fecharComEscape(): void {
    this.fecharMenus();
  }
}
