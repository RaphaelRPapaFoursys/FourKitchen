# Review das Mudancas

## Veredito

A mudanca esta boa como feature de cadastro de produtos: tem recorte claro, integra frontend, BFF e `ms-produtos`, respeita a autorizacao de gestor/admin e veio com uma cobertura de testes bem razoavel.

Eu avaliaria como **7/10**. O fluxo principal parece bem encaminhado, mas ainda existem alguns pontos de contrato, UX e robustez que valem corrigir antes de chamar a feature de madura.

## Achados Principais

### Medio - Erros de categoria viram erro de produto no BFF

O `ms-produtos` diferencia `CATEGORIA_NAO_ENCONTRADA` e `CATEGORIA_INATIVA`, mas o BFF mapeia qualquer `404` vindo do `ms-produtos` como `PRODUTO_NAO_ENCONTRADO`.

Arquivos:

- `backend/ms-produtos/src/main/java/br/com/fourkitchen/ms_produtos/service/ProdutoService.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/service/GestorProdutoService.java`

Impacto: a UI pode mostrar "Produto nao encontrado" quando o problema real e a categoria usada no cadastro/edicao.

Sugestao: preservar o erro original do `ms-produtos` quando possivel, ou separar o tratamento por operacao: produto vs categoria.

### Medio - Validacao de imagem depende demais do frontend

A tela limita imagens a 5 MB, mas o BFF aceita `String imagem` sem limite, e o `ms-produtos` valida basicamente se o conteudo e Base64.

Arquivos:

- `frontend/src/app/features/produtos/produto-cadastro.ts`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/dto/request/CriarProdutoRequest.java`
- `backend/ms-produtos/src/main/java/br/com/fourkitchen/ms_produtos/validation/ImagemBase64Validator.java`

Impacto: um cliente autenticado pode chamar a API diretamente com um payload grande demais, pressionando memoria, rede e banco.

Sugestao: colocar limite server-side de tamanho do Base64/imagem decodificada e, se possivel, validar mime/magic bytes reais.

### Baixo/Medio - Criar categoria pode disparar submit do produto

O campo "Nova categoria" fica dentro do form principal, e o `keyup.enter` chama `salvarCategoria()` sem impedir o submit do produto.

Arquivo:

- `frontend/src/app/features/produtos/produto-cadastro.html`

Impacto: ao pressionar Enter para criar uma categoria, o usuario pode acabar acionando a validacao do produto e vendo erros que nao esperava.

Sugestao: usar `$event.preventDefault()` no Enter, tirar a criacao de categoria do form principal ou tratar o bloco como um subform isolado.

### Baixo/Medio - Mensagem generica errada ao falhar categoria

`salvarCategoria()` usa a mesma funcao de mensagem do cadastro de produto. Se o backend nao mandar `msgError`, o fallback fica "Nao foi possivel cadastrar o produto".

Arquivo:

- `frontend/src/app/features/produtos/produto-cadastro.ts`

Impacto: erro confuso em falha de criacao de categoria.

Sugestao: ter fallback especifico por acao: produto, categoria, imagem etc.

### Baixo - Dropzone tem botao dentro de label clicavel

O dropzone usa um `<label>` como area clicavel e coloca o botao "Remover" dentro dele.

Arquivo:

- `frontend/src/app/features/produtos/produto-cadastro.html`

Impacto: HTML/acessibilidade fragil; pode haver comportamento estranho entre remover imagem e abrir seletor de arquivo.

Sugestao: transformar o dropzone em container com input associado por `for/id`, ou separar o botao de remover da area clicavel.

### Baixo - Assets decorativos pesados

Os PNGs decorativos usados no painel estao grandes para imagens de layout.

Arquivos:

- `frontend/src/assets/images/flor-e-listra-fourkitchen.png`
- `frontend/src/assets/images/listra-fourkitchen.png`
- `frontend/src/app/shared/components/sidebar/sidebar.html`
- `frontend/src/app/features/produtos/produto-cadastro.html`

Impacto: aumenta tempo de carregamento sem trazer ganho funcional proporcional.

Sugestao: recomprimir, gerar WebP/AVIF ou reduzir dimensoes.

### Baixo - API de produto esta mais completa que a UI

O BFF expoe listar, editar, ativar e desativar produtos, mas o frontend implementa basicamente cadastrar produto/categoria e desativar no service.

Arquivos:

- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/controller/GestorProdutoController.java`
- `frontend/src/app/core/services/produto.ts`

Impacto: a entrega fica com cara de fundacao incompleta para "gestao de produtos".

Sugestao: adicionar tela/listagem de produtos com editar, ativar/desativar, ou reduzir o escopo exposto no frontend ate haver uso real.

### Baixo - Comentario dos models esta desatualizado

O comentario diz que `CriarProdutoRequest` nao tem campo novo e lista os campos sem `disponivel`, mas a interface ja inclui `disponivel`.

Arquivo:

- `frontend/src/app/core/models/produto.models.ts`

Impacto: pequena confusao para quem mantiver o codigo.

Sugestao: atualizar o comentario ou remover a documentacao duplicada.

### Baixo - Falta teste para criar produto indisponivel

A regra nova permite criar produto com `disponivel = false`, mas o teste principal cobre apenas o default `null -> true`.

Arquivos:

- `backend/ms-produtos/src/main/java/br/com/fourkitchen/ms_produtos/service/ProdutoService.java`
- `backend/ms-produtos/src/test/java/br/com/fourkitchen/ms_produtos/service/ProdutoServiceTest.java`

Impacto: risco baixo, porque a implementacao esta simples, mas o comportamento novo mais importante nao fica travado por teste.

Sugestao: adicionar teste para `CriarProdutoRequest(..., disponivel=false)` garantindo que o produto salvo fica indisponivel.

## Pontos Positivos

- O recorte por `/api/gestor/**` esta coerente com a autorizacao do BFF.
- A rota frontend foi protegida para `ADMIN` e `GESTOR`.
- A tela cobre validacao de nome, categoria, preco, descricao, imagem e disponibilidade.
- A criacao inline de categoria melhora bastante a experiencia do gestor.
- Os testes de frontend cobrem fluxo feliz, erros, imagem, DOM e interacoes principais.
- O ajuste de `disponivel` no `ms-produtos` manteve compatibilidade com clientes antigos usando `null -> true`.

## Prioridade Recomendada

1. Corrigir mapeamento de erros de categoria no BFF.
2. Adicionar limite server-side para imagem.
3. Ajustar o Enter/subform de nova categoria.
4. Separar fallback de erro de produto e categoria.
5. Comprimir assets decorativos.
6. Adicionar teste para produto criado como indisponivel.
7. Atualizar comentarios e alinhar escopo API/UI.

## Observacao Geral

Essas mudancas melhoram a gestao de produtos, mas nao resolvem riscos maiores ja identificados em outros fluxos da aplicacao, como preco de pedido vindo do cliente, carrinho ainda sem integracao real e bordas de decisao de problema em pedidos.
