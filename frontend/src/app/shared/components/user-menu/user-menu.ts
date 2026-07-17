import { ChangeDetectionStrategy, Component, ElementRef, HostListener, computed, inject, input, output, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth';
import { Avatar } from '../avatar/avatar';
import { Icon } from '../icon/icon';

/** Menu de perfil compartilhado nas telas operacionais. */
@Component({
  selector: 'fk-user-menu',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Avatar, Icon],
  template: `
    <div class="user-menu">
      <button
        class="user-menu__trigger"
        type="button"
        [attr.aria-expanded]="estaAberto()"
        aria-haspopup="menu"
        aria-label="Abrir menu do perfil"
        (click)="alternar()"
      >
        <fk-avatar [initial]="initial()" color="var(--fk-navy)" />
        @if (showName()) {
          <span class="user-menu__name">{{ name() }}</span>
          <span class="user-menu__chevron" aria-hidden="true">⌄</span>
        }
      </button>

      @if (estaAberto()) {
        <div class="user-menu__popover" role="menu">
          <button type="button" role="menuitem" (click)="sair()">
            <fk-icon name="sign-out" size="1.1rem" />
            Sair
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    :host {
      display: inline-block;
    }

    .user-menu {
      position: relative;
      z-index: 2;
    }

    .user-menu__trigger {
      display: flex;
      align-items: center;
      gap: .5rem;
      padding: .3rem .4rem;
      border: 0;
      border-radius: var(--fk-radius-control);
      background: transparent;
      color: var(--fk-text);
      font: inherit;
      cursor: pointer;
    }

    .user-menu__trigger:hover {
      background: var(--fk-surface-muted);
    }

    .user-menu__trigger:focus-visible,
    .user-menu__popover button:focus-visible {
      outline: 2px solid var(--fk-blue);
      outline-offset: 2px;
    }

    .user-menu__name {
      font-size: .9rem;
      font-weight: 600;
      white-space: nowrap;
    }

    .user-menu__chevron {
      color: var(--fk-muted);
      font-size: 1.1rem;
      line-height: 1;
    }

    .user-menu__popover {
      position: absolute;
      top: calc(100% + .4rem);
      right: 0;
      z-index: 99999;
      min-width: 9rem;
      padding: .35rem;
      border: 1px solid var(--fk-border);
      border-radius: var(--fk-radius-control);
      background: var(--fk-card);
      box-shadow: 0 12px 32px rgba(0, 0, 0, .18);
    }

    .user-menu__popover button {
      display: flex;
      align-items: center;
      gap: .55rem;
      width: 100%;
      padding: .65rem .75rem;
      border: 0;
      border-radius: calc(var(--fk-radius-control) - .2rem);
      background: transparent;
      color: var(--fk-text-2);
      font: inherit;
      font-weight: 700;
      text-align: left;
      cursor: pointer;
    }

    .user-menu__popover button:hover {
      background: var(--fk-surface-muted);
    }
  `,
})
export class UserMenu {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);

  readonly name = input('Usuário');
  readonly initial = input('?');
  readonly showName = input(true);
  /** Quando informado, o componente pai passa a controlar a abertura do menu. */
  readonly abertoControlado = input<boolean | null>(null);
  readonly alternarSolicitado = output<void>();
  readonly fecharSolicitado = output<void>();
  private readonly abertoLocal = signal(false);
  protected readonly estaAberto = computed(() => this.abertoControlado() ?? this.abertoLocal());

  @HostListener('document:keydown.escape')
  protected fecharComEscape(): void { this.fechar(); }

  @HostListener('document:click', ['$event'])
  protected fecharAoClicarFora(event: MouseEvent): void {
    if (this.abertoControlado() !== null) return;
    if (!this.elementRef.nativeElement.contains(event.target)) this.fechar();
  }

  protected alternar(): void {
    if (this.abertoControlado() !== null) {
      this.alternarSolicitado.emit();
      return;
    }
    this.abertoLocal.update(aberto => !aberto);
  }

  private fechar(): void {
    if (this.abertoControlado() !== null) {
      this.fecharSolicitado.emit();
      return;
    }
    this.abertoLocal.set(false);
  }

  protected sair(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }
}
