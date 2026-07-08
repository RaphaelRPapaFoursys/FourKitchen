import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { FloatingCartButton } from './shared/components/floating-cart-button/floating-cart-button';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FloatingCartButton],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}
