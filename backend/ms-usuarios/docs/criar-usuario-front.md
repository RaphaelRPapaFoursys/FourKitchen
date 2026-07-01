# Tela de Criacao de Usuario

Esta documentacao descreve o contrato necessario para a tela de criacao de
usuario.

> Importante: na arquitetura com BFF, o frontend deve chamar o BFF. O contrato
> abaixo representa o payload que hoje o `ms-usuarios` espera. Quando o endpoint
> correspondente existir no BFF, ele deve receber dados equivalentes do frontend
> e repassar/adaptar para o `ms-usuarios`.

## Objetivo da tela

Permitir cadastrar um novo usuario no sistema FourKitchen.

## Endpoint atual no ms-usuarios

```http
POST /api/usuarios
```

## Autenticacao

O endpoint `/api/usuarios` esta protegido no `ms-usuarios`.

Para testar direto no microsservico, enviar:

```http
Authorization: Bearer {token}
```

O token pode ser obtido pelo login:

```http
POST /auth/login
```

## Campos da tela

| Campo | Tipo | Obrigatorio | Regra |
| --- | --- | --- | --- |
| Nome | texto | Sim | Entre 3 e 120 caracteres |
| Email | texto | Sim | Deve ser um email valido |
| Senha | senha | Sim | Minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero |
| Perfil | select | Sim | Um dos valores permitidos |

## Perfis permitidos

O campo enviado para a API se chama `perfilUsuario`.

Valores permitidos:

```text
GARCOM
COZINHA
GESTOR
ADMIN
```

Sugestao de labels para o select:

| Label na tela | Valor enviado |
| --- | --- |
| Garcom | GARCOM |
| Cozinha | COZINHA |
| Gestor | GESTOR |
| Admin | ADMIN |

## Payload de criacao

```json
{
  "nome": "Maria Silva",
  "email": "maria@fourkitchen.com",
  "senha": "Senha123",
  "perfilUsuario": "GARCOM"
}
```

## Regra da senha

A senha deve seguir exatamente esta regra do backend:

```regex
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$
```

Na pratica, precisa ter:

- minimo de 8 caracteres;
- pelo menos uma letra minuscula;
- pelo menos uma letra maiuscula;
- pelo menos um numero.

Exemplo valido:

```text
Senha123
```

Exemplos invalidos:

```text
senha123
SENHA123
Senhaabc
Sen123
```

Nao enviar:

```text
id
ativo
```

Esses campos sao controlados pelo backend.

## Resposta de sucesso

Status:

```http
201 Created
```

Body:

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@fourkitchen.com",
  "perfilUsuario": "GARCOM",
  "ativo": true
}
```

Observacoes:

- A senha nunca volta na resposta.
- O usuario novo sempre volta com `ativo: true`.
- O email deve ser unico.

## Erros esperados

### Dados invalidos

Status:

```http
400 Bad Request
```

Exemplos de mensagens:

```json
{
  "codError": "004",
  "msgError": "Nome deve ter entre 3 e 120 caracteres."
}
```

```json
{
  "codError": "004",
  "msgError": "Email invalido"
}
```

```json
{
  "codError": "004",
  "msgError": "A senha deve conter no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero."
}
```

### Email ja cadastrado

Status:

```http
409 Conflict
```

Body:

```json
{
  "codError": "001",
  "msgError": "Email ja cadastrado"
}
```

### Token ausente, invalido ou expirado

Status:

```http
401 Unauthorized
```

Body:

```json
{
  "codError": "005",
  "msgError": "Token invalido ou expirado"
}
```

### Usuario logado inativo

Status:

```http
403 Forbidden
```

Body:

```json
{
  "codError": "003",
  "msgError": "Usuario inativo"
}
```

## Comportamento sugerido no frontend

- Desabilitar o botao de salvar enquanto a requisicao estiver em andamento.
- Validar nome, email, senha e perfil antes de enviar.
- Mostrar erro de email duplicado abaixo do campo email ou em toast.
- Limpar o campo senha quando a criacao falhar.
- Apos sucesso, redirecionar para a listagem de usuarios ou limpar o formulario.

## Exemplo de requisicao

```bash
curl -X POST "http://localhost:8080/api/usuarios" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "nome": "Maria Silva",
    "email": "maria@fourkitchen.com",
    "senha": "Senha123",
    "perfilUsuario": "GARCOM"
  }'
```

## Checklist para a tela

- Campo de nome.
- Campo de email.
- Campo de senha.
- Select de perfil.
- Botao salvar.
- Estado de loading.
- Tratamento de erro de validacao.
- Tratamento de email ja cadastrado.
- Tratamento de token expirado.
- Mensagem de sucesso.
