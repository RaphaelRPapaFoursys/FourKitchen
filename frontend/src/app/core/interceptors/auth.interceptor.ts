import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();
  const authRequest = token
    ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : request;

  return next(authRequest).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !request.url.includes('/api/auth/login')) {
        authService.logout();
        void router.navigateByUrl('/login');
      }

      return throwError(() => error);
    }),
  );
};
