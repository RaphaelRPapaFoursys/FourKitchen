import { ChangeDetectionStrategy, Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'fk-language-selector',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="language-selector">
      <button class="language-selector__trigger" type="button" [attr.aria-expanded]="open()" aria-haspopup="menu" aria-label="Selecionar idioma" (click)="open.update(value => !value)">
        @for (language of languages; track language.code) {
          @if (language.code === currentLanguage()) {
            <span aria-hidden="true">{{ language.flag }}</span>
            <span class="language-selector__label">{{ language.label }}</span>
          }
        }
        <span class="language-selector__chevron" aria-hidden="true">⌄</span>
      </button>

      @if (open()) {
        <div class="language-selector__menu" role="menu">
          @for (language of languages; track language.code) {
            <button type="button" role="menuitemradio" [attr.aria-checked]="language.code === currentLanguage()" (click)="select(language.code)">
              <span aria-hidden="true">{{ language.flag }}</span>
              <span>{{ language.label }}</span>
              @if (language.code === currentLanguage()) { <span class="language-selector__check" aria-hidden="true">✓</span> }
            </button>
          }
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: inline-block; }
    .language-selector { position: relative; z-index: 20; }
    .language-selector__trigger { display: inline-flex; align-items: center; gap: .45rem; min-width: 8.5rem; padding: .6rem .85rem; border: 1px solid var(--fk-border); border-radius: var(--fk-radius-pill); background: var(--fk-card); box-shadow: var(--fk-shadow-card); color: var(--fk-text); font: inherit; font-weight: 700; cursor: pointer; }
    .language-selector__chevron { margin-left: auto; color: var(--fk-muted); }
    .language-selector__menu { position: absolute; top: calc(100% + .5rem); left: 0; width: 12.5rem; padding: .35rem; border: 1px solid var(--fk-border); border-radius: var(--fk-radius-control); background: var(--fk-card); box-shadow: 0 12px 32px rgba(0, 0, 0, .18); }
    .language-selector__menu button { display: flex; align-items: center; gap: .65rem; width: 100%; padding: .7rem .75rem; border: 0; border-radius: calc(var(--fk-radius-control) - .2rem); background: transparent; color: var(--fk-text); font: inherit; font-weight: 700; text-align: left; cursor: pointer; }
    .language-selector__menu button:hover { background: var(--fk-surface-muted); }
    .language-selector__check { margin-left: auto; color: var(--fk-green, #32865b); }
    @media (max-width: 720px) { .language-selector__trigger { min-width: auto; } .language-selector__label { display: none; } }
  `,
})
export class LanguageSelector {
  private readonly translate = inject(TranslateService);
  private readonly elementRef = inject(ElementRef);

  protected readonly languages = [
    { code: 'pt-BR', flag: '🇧🇷', label: 'Português' },
    { code: 'en-US', flag: '🇺🇸', label: 'English' },
    { code: 'es-ES', flag: '🇪🇸', label: 'Español' },
  ] as const;
  protected readonly currentLanguage = signal(localStorage.getItem('lang') ?? 'pt-BR');
  protected readonly open = signal(false);

  protected select(language: string): void {
    localStorage.setItem('lang', language);
    this.currentLanguage.set(language);
    this.translate.use(language);
    this.open.set(false);
  }

  @HostListener('document:keydown.escape')
  protected closeOnEscape(): void { this.open.set(false); }

  @HostListener('document:click', ['$event'])
  protected closeOnOutsideClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) this.open.set(false);
  }
}
