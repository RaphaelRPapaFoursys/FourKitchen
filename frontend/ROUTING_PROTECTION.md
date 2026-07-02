# Protecao de rotas por perfil

Este documento explica como adicionar, alterar ou remover rotas protegidas de acordo com o perfil do usuario autenticado.

## Arquivos principais

- `src/app/core/routing/protected-routes.ts`: cadastro das rotas protegidas e perfis permitidos.
- `src/app/core/guards/auth.guard.ts`: regra que bloqueia acesso sem login ou sem permissao.
- `src/app/core/utils/profile-redirect.ts`: rota padrao para onde cada perfil e redirecionado apos login ou acesso negado.
- `src/app/app.routes.ts`: importa as rotas publicas e as rotas protegidas.

Na maior parte dos casos, voce vai alterar apenas `protected-routes.ts`.

## Perfis disponiveis

Os perfis aceitos hoje sao:

```ts
'ADMIN' | 'GESTOR' | 'COZINHA' | 'GARCOM' | 'MESA' | 'TOTEM'
```

Essa lista fica no tipo `AppProfile` dentro de `protected-routes.ts`.

## Adicionar uma rota protegida

Para criar uma rota acessivel apenas por um perfil, adicione um novo item em `protectedRoutes`:

```ts
protectedRoute(
  {
    path: 'relatorios',
    loadComponent: () =>
      import('../../features/relatorios/relatorios').then(m => m.Relatorios),
  },
  ['GESTOR'],
)
```

Com isso, apenas usuarios com perfil `GESTOR` conseguem acessar `/relatorios`.

## Liberar uma rota para varios perfis

Informe todos os perfis permitidos no segundo argumento:

```ts
protectedRoute(
  {
    path: 'painel',
    loadComponent: () =>
      import('../../features/painel/painel').then(m => m.Painel),
  },
  ['ADMIN', 'GESTOR'],
)
```

Nesse exemplo, `/painel` fica liberada para `ADMIN` e `GESTOR`.

## Criar uma rota autenticada para qualquer perfil

Se a rota deve exigir login, mas nao deve restringir por perfil, nao passe a lista de perfis:

```ts
protectedRoute({
  path: 'home',
  loadComponent: () =>
    import('../../features/home/home').then(m => m.Home),
})
```

Assim, qualquer usuario autenticado consegue acessar `/home`.

## Adicionar subrotas por perfil

Para liberar todas as subrotas de um caminho para o mesmo perfil, use `children`:

```ts
protectedRoute(
  {
    path: 'totem',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('../../features/totem/totem').then(m => m.Totem),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('../../features/totem-pedidos/totem-pedidos').then(m => m.TotemPedidos),
      },
      {
        path: 'configuracoes',
        loadComponent: () =>
          import('../../features/totem-configuracoes/totem-configuracoes').then(m => m.TotemConfiguracoes),
      },
    ],
  },
  ['TOTEM'],
)
```

Nesse caso, o perfil `TOTEM` consegue acessar:

- `/totem`
- `/totem/pedidos`
- `/totem/configuracoes`

As subrotas herdam a permissao definida no pai.

## Alterar os perfis de uma rota existente

Localize a rota em `protected-routes.ts` e altere a lista de perfis.

Antes:

```ts
['GESTOR']
```

Depois:

```ts
['ADMIN', 'GESTOR']
```

## Remover uma rota protegida

Remova o bloco inteiro da rota dentro de `protectedRoutes`.

Exemplo:

```ts
protectedRoute(
  {
    path: 'rota-antiga',
    loadComponent: () =>
      import('../../features/rota-antiga/rota-antiga').then(m => m.RotaAntiga),
  },
  ['ADMIN'],
)
```

Depois de remover a rota do cadastro, o caminho deixa de existir no roteador.

## Alterar a rota inicial de um perfil

O redirecionamento apos login fica em `src/app/core/utils/profile-redirect.ts`.

Exemplo:

```ts
export const profileRoutes: Record<string, string> = {
  ADMIN: '/gestor',
  GESTOR: '/gestor',
  COZINHA: '/cozinha',
  GARCOM: '/garcom',
  MESA: '/mesa',
  TOTEM: '/totem',
};
```

Para mudar a rota inicial do `TOTEM`, altere o valor:

```ts
TOTEM: '/totem/pedidos',
```

## O que acontece quando o usuario nao tem permissao

- Sem login: o usuario e redirecionado para `/login`.
- Com login, mas sem permissao: o usuario e redirecionado para a rota padrao do proprio perfil.
- Com login e permissao correta: o acesso e liberado.

## Checklist rapido

1. Criar o componente da nova tela em `src/app/features`.
2. Registrar a rota em `src/app/core/routing/protected-routes.ts`.
3. Informar os perfis permitidos.
4. Se for rota inicial de perfil, atualizar `src/app/core/utils/profile-redirect.ts`.
5. Rodar `npm.cmd run build` para validar.
