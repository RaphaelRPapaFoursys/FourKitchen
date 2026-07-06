import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type AvatarSize = 'sm' | 'md';

/** Avatar circular com inicial. `color` pinta o fundo (texto branco); sem cor usa tom neutro. */
@Component({
  selector: 'fk-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span
      class="avatar"
      [class]="'avatar--' + size()"
      [class.avatar--tinted]="!!color()"
      [style.background]="color()"
      >{{ initial() }}</span
    >
  `,
  styles: `
    :host {
      display: inline-flex;
    }

    .avatar {
      display: grid;
      place-items: center;
      flex-shrink: 0;
      border-radius: var(--fk-radius-pill);
      background: var(--fk-surface-muted);
      color: var(--fk-text);
      font-weight: 700;
    }

    .avatar--tinted {
      color: #fff;
    }

    .avatar--md {
      width: 2rem;
      height: 2rem;
      font-size: 0.85rem;
    }

    .avatar--sm {
      width: 1.75rem;
      height: 1.75rem;
      font-size: 0.75rem;
    }
  `,
})
export class Avatar {
  readonly initial = input('?');
  readonly color = input<string | null>(null);
  readonly size = input<AvatarSize>('md');
}
