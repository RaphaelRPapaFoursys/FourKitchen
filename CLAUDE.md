# FourKitchen

Sistema de restaurante: frontend Angular (`frontend/`) + backend Java Spring Boot em microserviços (`backend/`): `bff-restaurante` (BFF, único exposto ao front), `ms-produtos`, `ms-pedidos`, `ms-mesas`, `ms-usuarios`, `ms-notificacoes`, `ms-cozinha`.

## Knowledge graph (graphify) — use para economizar tokens

O código INTEIRO deste repositório está indexado pelo graphify em `graphify-out/` (3k+ nós, 8k+ arestas). **Antes de grepar ou ler arquivos para entender estrutura, callers/callees ou arquitetura, consulte o grafo** — é muito mais barato em tokens:

- `graphify query "<pergunta ou símbolo>"` — retorna o subgrafo relevante (nós com arquivo/linha + arestas). Ex.: `graphify query "GestorProdutoService callers"`.
- `graphify path <A> <B>` — traça como dois símbolos se conectam (ex.: componente do front até endpoint do MS).
- `graphify-out/GRAPH_REPORT.md` — visão geral das comunidades/hubs do código; leia só as seções relevantes.

Rodar os comandos a partir da raiz do repo. Use Read direto no arquivo somente depois de localizar o alvo via grafo. Não leia `graphify-out/graph.json` (7 MB) nem os `.html`.

Após mudanças estruturais grandes, reindexe com `graphify` (regenera `graphify-out/`).

## Convenções

- Commits: sem Co-Authored-By; referenciar task com `(#N)` no título e `Refs task N` no corpo.
- Reiniciar serviços com `start-all.ps1` (pós-pull, processos ficam com build antigo → 500/404).
- Usuários seed usam senha `Senha123`.
