import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { ApiError, UsuarioAutenticadoResponse } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth';
import { getRedirectRouteByProfile } from '../../core/utils/profile-redirect';

type LoginField = 'email' | 'password';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  protected passwordVisible = false;
  protected readonly isLoading = signal(false);
  protected readonly authErrorMessage = signal('');

  protected readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected togglePasswordVisibility(): void {
    this.passwordVisible = !this.passwordVisible;
  }

  protected isFieldInvalid(fieldName: LoginField): boolean {
    const field = this.loginForm.controls[fieldName];

    return field.invalid && (field.dirty || field.touched);
  }

  protected onSubmit(): void {
    this.authErrorMessage.set('');

    if (this.loginForm.invalid || this.isLoading()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    this.authService
      .login(this.loginForm.getRawValue())
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => {
          void this.router.navigateByUrl(this.getRedirectRoute(response.usuario));
        },
        error: error => {
          this.authErrorMessage.set(this.getErrorMessage(error));
        },
      });
  }

  private getRedirectRoute(usuario: UsuarioAutenticadoResponse): string {
    return getRedirectRouteByProfile(usuario.perfil);
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const apiError = this.getApiError(error.error);

      if (error.status === 502) {
        return 'Nao foi possivel entrar no sistema. Tente novamente mais tarde.';
      }
      
      if (error.status === 401) {
        return 'E-mail ou senha invalidos.';
      }

      if (apiError?.msgError) {
        return apiError.msgError;
      }

      if (error.status === 400) {
        return 'Dados invalidos. Verifique e tente novamente.';
      }
    }

    return 'Nao foi possivel entrar no sistema. Tente novamente mais tarde.';
  }

  private getApiError(error: unknown): ApiError | null {
    if (!this.isRecord(error)) {
      return null;
    }

    const codError = error['codError'];
    const msgError = error['msgError'];

    if (typeof codError !== 'string' || typeof msgError !== 'string') {
      return null;
    }

    return { codError, msgError };
  }

  private isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
  }
}
