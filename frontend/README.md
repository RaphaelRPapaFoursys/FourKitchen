# FourKitchen Frontend

Frontend Angular do FourKitchen, com telas por perfil de usuario e protecao de rotas integrada ao BFF.

## Stack

- Angular 21
- TypeScript
- RxJS
- Vitest para testes

## Como rodar

Instale as dependencias:

```bash
npm install
```

Inicie o servidor local:

```bash
npm run dev
```

A aplicacao fica disponivel em `http://localhost:4200/`.

## Comandos principais

```bash
npm run build
npm run test
npm run watch
```

## Autenticacao e rotas

O login usa o BFF em `/api/auth/login` e o token e enviado nas requisicoes autenticadas pelo interceptor HTTP.

O frontend nao salva o perfil do usuario no `localStorage`. Para validar token e perfil, as rotas protegidas consultam `/api/auth/me` antes de liberar o acesso.

Se o token expirar ou for invalido, o usuario e deslogado e redirecionado para `/login`.

As rotas protegidas e permissoes por perfil ficam em:

```txt
src/app/core/routing/protected-routes.ts
```

A rota padrao de cada perfil fica em:

```txt
src/app/core/utils/profile-redirect.ts
```

A documentacao completa de manutencao das rotas esta em:

```txt
docs/ROUTING_PROTECTION.md
```
