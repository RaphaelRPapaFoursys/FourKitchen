import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { FloatingCartButton } from './shared/components/floating-cart-button/floating-cart-button';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FloatingCartButton],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {

  constructor(private translate: TranslateService) {
    const idioma = localStorage.getItem('lang') ?? 'pt-BR';

    this.translate.setFallbackLang('pt-BR');
    this.translate.use(idioma);
  }

  protected readonly title = signal('frontend');

}
