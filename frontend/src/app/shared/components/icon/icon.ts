import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/**
 * Ícone monocromático servido de `assets/icons/{name}.svg`, pintado por `currentColor`
 * via CSS mask — herda a cor do contexto (sidebar branca, KPI laranja, etc.).
 * `size` aceita qualquer comprimento CSS (ex.: "1.5rem"); padrão 1em.
 */
@Component({
  selector: 'fk-icon',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: '',
  host: {
    '[style.--fk-icon-mask]': 'mask()',
    '[style.--fk-icon-size]': 'size()',
    role: 'img',
    'aria-hidden': 'true',
  },
  styles: `
    :host {
      display: inline-block;
      width: var(--fk-icon-size, 1em);
      height: var(--fk-icon-size, 1em);
      flex-shrink: 0;
      background-color: currentColor;
      -webkit-mask: var(--fk-icon-mask) center / contain no-repeat;
      mask: var(--fk-icon-mask) center / contain no-repeat;
    }
  `,
})
export class Icon {
  readonly name = input.required<string>();
  readonly size = input<string | null>(null);

  protected readonly mask = computed(() => `url('/assets/icons/${this.name()}.svg')`);
}
