import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-login-footer',
  templateUrl: './login-footer.html',
  styleUrl: './login-footer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class LoginFooterComponent {}
