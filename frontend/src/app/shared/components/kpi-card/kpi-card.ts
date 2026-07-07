import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export type KpiSize = 'md' | 'lg';

/** Card de KPI/resumo: ícone opcional (projetado com atributo `icon`) + número grande + rótulo.
 *  O próprio :host é o card; quando `clickable`, vira um botão acessível (role/tabindex/teclado).
 *  Um único slot de projeção evita o bug de conteúdo duplicado entre ramos. */
@Component({
  selector: 'fk-kpi-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'card',
    '[class.card--clickable]': 'clickable()',
    '[class.card--alert]': 'alert()',
    '[class.card--active]': 'active()',
    '[class.card--tint-orange]': "tint() === 'orange'",
    '[class.card--tint-green]': "tint() === 'green'",
    '[class.card--tint-red]': "tint() === 'red'",
    '[attr.role]': "clickable() ? 'button' : null",
    '[attr.tabindex]': 'clickable() ? 0 : null',
    '(click)': 'acionar()',
    '(keydown.enter)': 'acionar($event)',
    '(keydown.space)': 'acionar($event)',
  },
  template: `
    <ng-content select="[icon]" />
    <div class="body" [class]="'body--' + size()">
      <strong>{{ value() }}</strong>
      <span>{{ label() }}</span>
    </div>
    @if (clickable()) {
      <svg
        class="card__chevron"
        [class.card__chevron--aberto]="active()"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        aria-hidden="true"
      >
        <path d="m9 6 6 6-6 6" />
      </svg>
    }
  `,
  styles: `
    :host {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      padding: 0.6rem 0.8rem;
      border: 1px solid var(--fk-border);
      border-radius: var(--fk-radius-card);
      background: var(--fk-card);
      box-shadow: var(--fk-shadow-card);
      color: inherit;
      text-align: left;
    }

    :host(.card--tint-orange) {
      background: var(--fk-tint-orange);
    }

    :host(.card--tint-green) {
      background: var(--fk-tint-green);
    }

    :host(.card--tint-red) {
      background: var(--fk-tint-red);
    }

    .card__chevron {
      width: 1.1rem;
      height: 1.1rem;
      margin-left: auto;
      flex-shrink: 0;
      color: var(--fk-muted);
      transition: transform 150ms ease;
    }

    .card__chevron--aberto {
      transform: rotate(90deg);
    }

    :host(.card--clickable) {
      cursor: pointer;
      transition:
        border-color 120ms ease,
        background 120ms ease;
    }

    :host(.card--clickable:hover) {
      border-color: var(--fk-border-strong);
    }

    :host(.card--clickable:focus-visible) {
      outline: 2px solid var(--fk-blue);
      outline-offset: 2px;
    }

    :host(.card--active) {
      border-color: var(--fk-orange);
      background: var(--fk-orange-soft);
    }

    :host(.card--active.card--tint-orange) {
      border-color: var(--fk-orange);
      background: var(--fk-orange-soft);
    }

    :host(.card--active.card--tint-green) {
      border-color: var(--fk-green);
      background: var(--fk-green-soft);
    }

    :host(.card--active.card--tint-red) {
      border-color: var(--fk-red);
      background: var(--fk-red-soft);
    }

    .body {
      min-width: 0;
    }

    .body strong {
      display: block;
      font-weight: 700;
      line-height: 1.1;
      color: var(--fk-text);
    }

    .body--lg strong {
      font-size: 1.4rem;
    }

    .body--md strong {
      font-size: 1.25rem;
    }

    .body span {
      color: var(--fk-muted);
      font-size: 0.8rem;
    }

    :host(.card--alert) .body strong {
      color: var(--fk-red);
    }
  `,
})
export class KpiCard {
  readonly value = input('');
  readonly label = input('');
  readonly size = input<KpiSize>('lg');
  readonly tint = input<'neutral' | 'orange' | 'green' | 'red'>('neutral');
  readonly alert = input(false);
  readonly active = input(false);
  readonly clickable = input(false);

  readonly select = output<void>();

  protected acionar(event?: Event): void {
    if (!this.clickable()) return;
    event?.preventDefault();
    this.select.emit();
  }
}
