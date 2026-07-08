# Guia de backend: expediente persistido, tempo de preparo e "problemas" no resumo

## Contexto

O fechamento de expediente do painel do gestor já foi migrado no frontend para consumir o
histórico real de atendimentos (`GET /api/gestor/atendimentos/historico`). Sobraram três lacunas
que só o backend pode fechar (hoje são paliativos ou valores fixos no frontend):

1. **Expediente não é persistido** — o estado aberto/fechado e o marco de início vivem no
   `localStorage` do navegador (`frontend/src/app/core/services/painel.ts`), então não é
   compartilhado entre gestores nem sobrevive a troca de máquina.
2. **`tempoMedioPreparoMin` é sempre `null`** — o backend não registra quando o pedido entra em
   preparo nem quando fica pronto (`frontend/src/app/core/services/expediente.util.ts` mantém TODO).
3. **`problemas` é sempre `0`** — nunca houve regra definida no resumo do expediente.

Este guia descreve a implementação no backend (3 microsserviços + BFF + migrations) e o passo final
de integração no frontend. O banco é único e compartilhado; migrations ficam em `db-migrations`
(última versão hoje: `V13`).

### Decisões de produto (já tomadas)

- **Fechar expediente** exige **nenhuma mesa ocupada** (todas `disponivel = true`).
- **"Problema"** de um atendimento = teve **pelo menos um pedido sinalizado** (fluxo
  `sinalizar-problema` → `AGUARDANDO_DECISAO`) **OU** o atendimento **estourou um limiar de tempo**
  (atrasado). `problemas` no resumo = nº de atendimentos do expediente marcados com problema.

---

## Parte A — Expediente persistido (ms-mesas + BFF)

Conceito novo. O "expediente atual" é o único registro com status `ABERTO` (invariante: no máximo um
aberto). O histórico de atendimentos passa a referenciar o expediente em que foi fechado, o que
substitui o filtro por data usado hoje no frontend.

### ms-mesas

- **Model** `model/Expediente.java` (`@Table("expedientes")`): `id`, `status`
  (enum `StatusExpediente { ABERTO, FECHADO }`, `@Enumerated(STRING)`), `dataAbertura`,
  `dataFechamento`, `idGestorAbertura`, `idGestorFechamento` (ambos nullable — o id do gestor é
  opcional até a pendência de JWT/idUsuário estar resolvida). Espelhar o padrão de
  `model/Atendimento.java`.
- **Enum** `enums/StatusExpediente.java`.
- **Repository** `repository/ExpedienteRepository.java`:
  `Optional<Expediente> findFirstByStatusOrderByDataAberturaDesc(StatusExpediente status)`.
- **Service** `service/ExpedienteService.java` (ou métodos em `MesaService`):
  - `buscarExpedienteAtual()` → o `ABERTO`; se nenhum existir, tratar como "sem expediente"
    (retornar 404 ou um estado default — ver contrato).
  - `fecharExpediente(idGestor)` → valida `mesaRepository.countByDisponivelFalse() == 0` (reusar o
    contador já usado em `buscarResumoOperacao`, `MesaService.java:82-84`); senão lança
    `BaseException(ErrorEnum.EXPEDIENTE_COM_MESAS_OCUPADAS)` (novo enum em `exception/ErrorEnum.java`).
    Seta `status=FECHADO`, `dataFechamento=now`.
  - `abrirExpediente(idGestor)` → se já houver `ABERTO`, erro (ou idempotente); senão cria novo
    `ABERTO` com `dataAbertura=now`.
- **Controller** `controller/MesaController.java` (novas rotas sob `/api/mesas/expediente`):
  - `GET /api/mesas/expediente/atual` → `ExpedienteResponse { id, status, dataAbertura, dataFechamento }`.
  - `POST /api/mesas/expediente/fechar`
  - `POST /api/mesas/expediente/abrir`
- **Vincular histórico ao expediente**: em `MesaService.salvarHistoricoAtendimento`
  (`MesaService.java:249`), preencher `idExpediente` com o expediente `ABERTO` atual. Estender
  `HistoricoAtendimento` (model + migration) e o `HistoricoAtendimentoResponse` com `idExpediente`.
- **Filtrar histórico por expediente**: `listarHistoricoAtendimentos` (`MesaService.java:86`) passa a
  aceitar `idExpediente` opcional; adicionar `findAllByIdExpedienteOrderByDataFechamentoDescIdDesc`
  em `HistoricoAtendimentoRepository`. Endpoint:
  `GET /api/mesas/atendimentos/historico?idExpediente=` (default: expediente atual).

### bff-restaurante

- **Client** `client/mesas/MesaClient.java`: adicionar `buscarExpedienteAtual()`,
  `fecharExpediente()`, `abrirExpediente()`, e o param `idExpediente` em
  `listarHistoricoAtendimentos`. DTOs em `client/mesas/dto/`.
- **Service** `service/GestorMesaService.java`: métodos passthrough; em `fechar`/`abrir` capturar o id
  do gestor do token quando disponível (nullable por ora).
- **Controller** `controller/GestorMesaController.java` (`/api/gestor`):
  - `GET /api/gestor/expediente`
  - `POST /api/gestor/expediente/fechar`
  - `POST /api/gestor/expediente/abrir`
  - `GET /api/gestor/atendimentos/historico?idExpediente=` (estender o existente,
    `GestorMesaController.java:161`).

---

## Parte B — Tempo de preparo real (ms-pedidos → ms-mesas)

### ms-pedidos

- **Entity** `entities/Pedido.java`: adicionar `dataInicioPreparo` e `dataPronto`
  (`LocalDateTime`, nullable), colunas `data_inicio_preparo` / `data_pronto`.
- **Service** `service/PedidoService.java`:
  - `iniciarPreparo` (`:225`): `pedido.setDataInicioPreparo(LocalDateTime.now())`.
  - `finalizarPreparo` (`:237`): `pedido.setDataPronto(LocalDateTime.now())`.
- **Expor no resumo de conta**: estender `dto/response/ResumoContaAtendimentoResponse.java` com
  `tempoMedioPreparoMinutos` (nullable). Em `buscarResumoContaAtendimento` (`:166`), calcular a média,
  sobre os pedidos do atendimento que têm ambos os timestamps, de
  `Duration.between(dataInicioPreparo, dataPronto).toMinutes()`; `null` quando nenhum tiver o dado.

### ms-mesas

- Espelhar o campo em `client/ResumoContaAtendimentoResponse.java` (o record consumido via
  `PedidosAtivosClient.buscarResumoConta`).
- Em `salvarHistoricoAtendimento` (`MesaService.java:249`), gravar `tempoMedioPreparoMinutos` no
  `HistoricoAtendimento` (novo campo/coluna). Expor em `HistoricoAtendimentoResponse`.

---

## Parte C — "Problema" por atendimento (ms-pedidos → ms-mesas)

### ms-pedidos

- **Entity** `entities/Pedido.java`: adicionar `teveProblema` (boolean, default `false`, coluna
  `teve_problema`).
- **Service** `service/PedidoService.java` → `sinalizarProblema` (`:355`): setar
  `pedido.setTeveProblema(true)` (não reverter na decisão, para preservar o histórico do incidente).
- **Resumo de conta**: adicionar `pedidosComProblema` (int) a `ResumoContaAtendimentoResponse` = nº de
  pedidos do atendimento com `teveProblema == true`.

### ms-mesas

- Espelhar `pedidosComProblema` em `client/ResumoContaAtendimentoResponse.java`.
- Nova constante `LIMIAR_ATENDIMENTO_ATRASADO_MINUTOS` (referência: `LIMIAR_PREPARO_ATENCAO_MINUTOS = 14`
  no BFF; sugerir ~30 min de duração total — ajustar com o time).
- Em `salvarHistoricoAtendimento` calcular
  `teveProblema = pedidosComProblema > 0 || duracaoMinutos > LIMIAR_ATENDIMENTO_ATRASADO_MINUTOS` e
  gravar boolean `teve_problema` em `HistoricoAtendimento`. Expor em `HistoricoAtendimentoResponse`.

---

## Migrations (db-migrations)

`backend/db-migrations/src/main/resources/db/migration/`

- **`V14__create_expedientes_e_ajusta_historico.sql`**:
  - `CREATE TABLE expedientes (id, status VARCHAR, data_abertura, data_fechamento,
    id_gestor_abertura, id_gestor_fechamento, criado_em)`.
  - Seed: um expediente `ABERTO` com `data_abertura = CURRENT_TIMESTAMP` (para não quebrar dados
    existentes).
  - `ALTER TABLE historico_atendimentos ADD COLUMN id_expediente INTEGER` + FK para `expedientes(id)`
    (nullable); backfill dos registros existentes para o expediente semeado.
  - `ALTER TABLE historico_atendimentos ADD COLUMN tempo_medio_preparo_minutos INTEGER NULL`.
  - `ALTER TABLE historico_atendimentos ADD COLUMN teve_problema BOOLEAN NOT NULL DEFAULT FALSE`.
- **`V15__add_preparo_e_problema_em_pedidos.sql`**:
  - `ALTER TABLE pedidos ADD COLUMN data_inicio_preparo TIMESTAMP NULL,
    ADD COLUMN data_pronto TIMESTAMP NULL, ADD COLUMN teve_problema BOOLEAN NOT NULL DEFAULT FALSE`.

Seguir o estilo de `V13__create_historico_atendimentos.sql` (constraints e índices nomeados). Índice
em `historico_atendimentos(id_expediente)`.

---

## Passo final — Integração no frontend (fecha os paliativos)

Depois que o backend estiver no ar:

- `frontend/src/app/core/services/painel.ts`:
  - Buscar `GET /api/gestor/expediente` → alimentar `expedienteFechado` (por `status`) e guardar
    `idExpediente`.
  - `fecharExpediente()` → `POST /api/gestor/expediente/fechar`; `abrirNovoExpediente()` →
    `POST .../abrir`; recarregar o painel após cada um.
  - Histórico: chamar `GET /api/gestor/atendimentos/historico?idExpediente=<atual>` (o backend já
    escopa por expediente).
  - **Remover** `STORAGE_EXPEDIENTE_FECHADO`, `STORAGE_EXPEDIENTE_INICIO`, `lerExpedienteFechado`,
    `lerInicioExpediente`, `inicioExpedienteSignal` e o filtro por data em `historicoExpedienteAtual`.
- `frontend/src/app/core/services/expediente.util.ts`:
  - Incluir `teveProblema` e `tempoMedioPreparoMinutos` em `HistoricoAtendimentoExpediente`.
  - `montarResumoExpediente`: `problemas` = nº de registros com `teveProblema`; `tempoMedioPreparoMin`
    = média dos `tempoMedioPreparoMinutos` não nulos (documentar que é média de médias por
    atendimento). Remover os dois TODOs.

---

## Verificação

1. **Migrations**: subir os serviços com `start-all.ps1` (raiz do workspace) e conferir que o Flyway
   aplica `V14`/`V15` sem erro; validar que o expediente semeado existe
   (`GET /api/mesas/expediente/atual`).
2. **Testes de unidade** (espelhar os já existentes):
   - ms-pedidos `PedidoServiceTest`: `iniciarPreparo`/`finalizarPreparo` gravam timestamps;
     `sinalizarProblema` marca `teveProblema`; `buscarResumoContaAtendimento` calcula
     `tempoMedioPreparoMinutos` e `pedidosComProblema`.
   - ms-mesas `MesaServiceTest`: `salvarHistoricoAtendimento` vincula `idExpediente`, calcula
     `teveProblema` (por sinalização e por atraso) e grava tempo médio; `fecharExpediente` bloqueia
     com mesa ocupada e permite com todas disponíveis.
   - bff `GestorMesaServiceTest`/`GestorMesaControllerTest`: rotas de expediente e histórico filtrado.
3. **Fluxo ponta a ponta** no painel do gestor:
   - abrir mesa → criar pedido → iniciar preparo (aguardar) → finalizar preparo → entregar →
     fechar conta; conferir no `GET /api/gestor/atendimentos/historico` que o registro tem
     `tempoMedioPreparoMinutos` preenchido e `idExpediente` do atual.
   - sinalizar problema em um pedido e fechar a conta desse atendimento → o registro sai com
     `teveProblema = true` e entra na contagem de `problemas` do resumo.
   - tentar fechar expediente com mesa ocupada → erro; liberar todas → fechar OK; `GET expediente`
     reflete `FECHADO`; abrir novo → `ABERTO` e o resumo zera (histórico do expediente anterior deixa
     de ser contado).

## Fora de escopo

- Preencher `idGestorAbertura/Fechamento` a partir do usuário autenticado depende da pendência de
  JWT/idUsuário (`docs/backend-integration-pendencias.md` item 1); por ora ficam nullable.
- Ajuste fino do `LIMIAR_ATENDIMENTO_ATRASADO_MINUTOS` com o time de produto.
