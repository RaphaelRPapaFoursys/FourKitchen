import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChildFn, CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, catchError, map, of } from 'rxjs';

import { AuthService } from '../services/auth';
import { getRedirectRouteByProfile, normalizePerfil } from '../utils/profile-redirect';

export const authGuard: CanActivateFn = route => canAccessRoute(route);

export const authChildGuard: CanActivateChildFn = childRoute => canAccessRoute(childRoute);

export const defaultRedirectGuard: CanActivateFn = () => redirectToDefaultRoute();

function canAccessRoute(route: ActivatedRouteSnapshot): boolean | UrlTree | Observable<boolean | UrlTree> {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  return authService.me().pipe(
    map(usuario => {
      const allowedProfiles = getAllowedProfiles(route);

      if (!Array.isArray(allowedProfiles)) {
        return true;
      }

      const perfil = normalizePerfil(usuario.perfil);
      const normalizedAllowedProfiles = allowedProfiles
        .filter((allowedProfile): allowedProfile is string => typeof allowedProfile === 'string')
        .map(normalizePerfil);

      if (normalizedAllowedProfiles.includes(perfil)) {
        return true;
      }

      return router.parseUrl(getRedirectRouteByProfile(usuario.perfil));
    }),
    catchError(() => {
      authService.logout();
      return of(router.parseUrl('/login'));
    }),
  );
}

function redirectToDefaultRoute(): UrlTree | Observable<UrlTree> {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  return authService.me().pipe(
    map(usuario => router.parseUrl(getRedirectRouteByProfile(usuario.perfil))),
    catchError(() => {
      authService.logout();
      return of(router.parseUrl('/login'));
    }),
  );
}
function getAllowedProfiles(route: ActivatedRouteSnapshot): unknown {
  for (let index = route.pathFromRoot.length - 1; index >= 0; index -= 1) {
    const allowedProfiles = route.pathFromRoot[index].data['allowedProfiles'];

    if (allowedProfiles) {
      return allowedProfiles;
    }
  }

  return undefined;
}

