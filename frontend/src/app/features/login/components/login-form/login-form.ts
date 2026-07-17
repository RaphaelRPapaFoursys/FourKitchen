import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ReactiveFormsModule, FormGroup } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';

type LoginField = 'email' | 'password';

@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './login-form.html',
  styleUrl: './login-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginFormComponent {
  readonly form = input.required<FormGroup>();
  readonly authErrorMessage = input('');
  readonly isLoading = input(false);
  readonly passwordVisible = input(false);
  readonly fieldInvalid = input.required<(fieldName: LoginField) => boolean>();

  readonly submitted = output<void>();
  readonly passwordVisibilityToggled = output<void>();

  protected submit(): void {
    this.submitted.emit();
  }

  protected togglePasswordVisibility(): void {
    this.passwordVisibilityToggled.emit();
  }
}
