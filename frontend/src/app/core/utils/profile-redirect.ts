export const profileRoutes: Record<string, string> = {
  ADMIN: '/gestor',
  GESTOR: '/gestor',
  COZINHA: '/cozinha',
  GARCOM: '/garcom',
  MESA: '/mesa',
  TOTEM: '/totem',
};

export function normalizePerfil(perfil: string): string {
  return perfil.trim().toUpperCase().replace(/\s+/g, '_');
}

export function getRedirectRouteByProfile(perfil: string): string {
  return profileRoutes[normalizePerfil(perfil)] ?? '/home';
}
