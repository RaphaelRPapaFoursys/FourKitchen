import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type BadgeVariant = 'neutral' | 'livre' | 'atencao' | 'critico' | 'tempo-real';

/** Pill de status apresentacional. Label vem por projeção de conteúdo; a cor/estilo pela variante. */
@Component({
  selector: 'fk-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="badge" [class]="'badge--' + variant()">
      @if (dot()) {
        <span class="badge__dot" aria-hidden="true"></span>
      }
      <ng-content />
    </span>
  `,
  styles: `
    :host {
      display: inline-flex;
    }

    .badge {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.15rem 0.5rem;
      border-radius: var(--fk-radius-pill);
      font-size: 0.72rem;
      font-weight: 600;
      line-height: 1.4;
    }

    .badge--neutral {
      background: var(--fk-surface-muted);
      color: var(--fk-muted);
    }

    .badge--livre {
      background: var(--fk-green-soft);
      color: var(--fk-green-strong);
    }

    .badge--atencao {
      background: var(--fk-orange-soft);
      color: var(--fk-orange-strong);
    }

    .badge--critico {
      background: var(--fk-red-soft);
      color: var(--fk-red-strong);
    }

    .badge--tempo-real {
      padding: 0.35rem 0.75rem;
      border: 1px solid var(--fk-border);
      background: var(--fk-card);
      color: var(--fk-text-2);
      font-size: 0.8rem;
      font-weight: 500;
    }

    .badge__dot {
      width: 0.45rem;
      height: 0.45rem;
      border-radius: var(--fk-radius-pill);
      background: var(--fk-green);
    }
  `,
})
export class Badge {
  readonly variant = input<BadgeVariant>('neutral');
  readonly dot = input(false);
}
