import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChildFn, CanActivateFn, Router, UrlTree } from '@angular/router';

import { AuthService } from '../services/auth';
import { getRedirectRouteByProfile, normalizePerfil } from '../utils/profile-redirect';

export const authGuard: CanActivateFn = route => canAccessRoute(route);

export const authChildGuard: CanActivateChildFn = childRoute => canAccessRoute(childRoute);

function canAccessRoute(route: ActivatedRouteSnapshot): boolean | UrlTree {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  const usuario = authService.getCurrentUser();

  if (!usuario) {
    authService.logout();
    return router.parseUrl('/login');
  }

  const allowedProfiles = getAllowedProfiles(route);

  if (!Array.isArray(allowedProfiles)) {
    return true;
  }

  const perfil = normalizePerfil(usuario.perfil);

  if (allowedProfiles.includes(perfil)) {
    return true;
  }

  return router.parseUrl(getRedirectRouteByProfile(usuario.perfil));
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
