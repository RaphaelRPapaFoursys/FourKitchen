import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const isLoginRequest = request.url.includes('/api/auth/login');
  const isPublicRequest = request.url.includes('/api/painel-retirada/');
  const token = isLoginRequest || isPublicRequest ? null : authService.getToken();
  const authRequest = token
    ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : request;

  return next(authRequest).pipe(
    catchError(error => {
      if (
        error instanceof HttpErrorResponse
        && error.status === 401
        && !isLoginRequest
        && !isPublicRequest
      ) {
        authService.logout();
        void router.navigateByUrl('/login');
      }

      return throwError(() => error);
    }),
  );
};
