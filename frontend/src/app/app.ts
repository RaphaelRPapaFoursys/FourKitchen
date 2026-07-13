import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { FloatingCartButton } from './shared/components/floating-cart-button/floating-cart-button';
import { DeviceIndicatorComponent } from './shared/components/device-indicator/device-indicator';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FloatingCartButton, DeviceIndicatorComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}
