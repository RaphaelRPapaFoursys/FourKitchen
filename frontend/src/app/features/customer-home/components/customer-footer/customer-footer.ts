import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-customer-footer',
  templateUrl: './customer-footer.html',
  styleUrl: './customer-footer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerFooterComponent {}
