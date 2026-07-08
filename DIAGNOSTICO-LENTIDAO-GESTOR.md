# Diagnóstico da lentidão (~30s) no painel do gestor

## Resumo

A lentidão não está no frontend (o cache/prefetch já implementados funcionam) e sim no custo de **cada** requisição ao backend. Quatro fatores se multiplicam — o dominante é a combinação de banco remoto + N+1.

## Causas encontradas

### 1. Banco de dados em Oregon (us-west-2)
Todos os 7 serviços apontam para `aws-1-us-west-2.pooler.supabase.com` (visto nos `.env` de cada ms). Do Brasil, cada round-trip de query custa **~180–200ms**. Esse é o multiplicador de tudo abaixo.

### 2. N+1 no ms-mesas
`Mesa.atendimento` é `@ManyToOne` (EAGER por padrão — `ms-mesas/.../model/Mesa.java:36`). `mesaRepository.findAll()` — usado por `listarMesas`, que o BFF chama em **toda** requisição do gestor — dispara 1 query + 1 query **por mesa ocupada**.

> Com 30 mesas ocupadas: 30 × ~190ms ≈ **6 segundos só nessa chamada**.

### 3. Fan-out sequencial e paginação em memória no BFF
`GestorMesaService.listarMesasPaginadas` (bff-restaurante) carrega, **em série**:
1. Todas as mesas (ms-mesas → N+1 acima)
2. Todos os garçons (ms-usuarios, pool de só 2 conexões)
3. Todos os pedidos ativos detalhados (ms-pedidos)

...e só então filtra/ordena/pagina **em memória**. Ou seja: cada página, cada filtro e o resumo do painel pagam o pipeline inteiro (~7–10s cada com o banco em Oregon). Não existe nenhum cache no BFF.

### 4. Amplificação do frontend + pools minúsculos
Uma troca de página no front dispara: página atual + até 4 páginas de prefetch + resumo = **~6 pipelines completos concorrentes**. Os pools Hikari têm 2–4 conexões com:
- `max-lifetime: 60000` → toda conexão é descartada a cada 60s, forçando reconexão TLS constante a Oregon (~1s cada)
- `connection-timeout: 10000` → requisições concorrentes ficam em fila até 10s esperando conexão

Resultado: os ~30 segundos percebidos.

## Soluções propostas (em ordem de impacto)

### 1. Cache de snapshot no BFF (maior ganho de código)
Em `GestorMesaService`:
- Cachear o resultado de `carregarMesasPainel` e de `buscarGarcons` por **~5s** (TTL curto, compatível com o polling de 10s do front). Implementação simples com campo + timestamp + lock, sem dependência nova.
- `listarMesasPaginadas`, `buscarResumoPainel` e `listarGarcons` usam o snapshot → as ~6 requisições de uma troca de página colapsam em **1 fan-out** downstream.
- **Invalidar** o cache nas mutações (`abrirMesa`, `fecharMesa`, `atribuirGarcom`, `marcarEntregue`) para o efeito aparecer imediatamente.

### 2. Eliminar o N+1 no ms-mesas
Em `MesaRepository`: sobrescrever `findAll()` com `@EntityGraph(attributePaths = "atendimento")` (ou `join fetch`) → 1 única query em vez de 1+N.

### 3. Paralelizar o fan-out do BFF
Em `carregarMesasPainel`: depois de buscar as mesas, buscar garçons e pedidos **em paralelo** (`CompletableFuture`). Corta um leg do pipeline.

### 4. Ajustar Hikari em todos os ms-*
- `max-lifetime: 60000` → `1800000` (30 min)
- `idle-timeout: 20000` → `600000` e `minimum-idle` = `maximum-pool-size` (conexões sempre quentes)
- Manter os tamanhos de pool (limite do Supabase free tier)

### 5. Infra (maior ganho estrutural — decisão sua)
**Migrar o projeto Supabase para `sa-east-1` (São Paulo)**: reduz o RTT de ~190ms para ~20ms (**~10×** em cada query). Requer criar projeto novo, migrar os dados e atualizar os `.env`.

## Resultado esperado após as correções de código (1–4)

| Cenário | Antes | Depois |
|---|---|---|
| Primeira carga do gestor | ~30s | ~2–4s |
| Troca de página/filtro (janela de 5s do cache) | ~30s | <300ms |
| Ação (abrir/fechar mesa) | lenta | reflete imediatamente (cache invalidado) |

Com a migração do banco para São Paulo (item 5), a primeira carga cai para bem menos de 1s.
