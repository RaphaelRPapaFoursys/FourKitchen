# Graph Report - C:\Users\LucasDoAmaralMartins\Desktop\FourKitchen\FourKitchen  (2026-07-10)

## Corpus Check
- Large corpus: 588 files · ~214,286 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 3325 nodes · 8822 edges · 165 communities (142 shown, 23 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 672 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- BFF Restaurante Produtos Produto Service
- BFF Restaurante Mesas Mesa Service
- BFF Restaurante Usuarios Login Service
- BFF Restaurante Usuario Usuarios Service
- Pedidos MS Pedido Produto Exception
- Cozinha Pedido Models Response Features
- BFF Restaurante Mesas Mesa Service
- Garcom Operacao Mapper Service MS
- Pedidos MS Pedido Service Repository
- Produtos MS Categoria Categorias Service
- Produtos MS Validation Mapper Support
- Pedidos MS Pedido Mapper Enums
- Produtos Produto Utils Component Features
- BFF Restaurante Pedido Garcom Service
- Expediente Pedido Models Response Core
- BFF Restaurante Garcom Mesa Service
- BFF Restaurante Pedido Mesa Controller
- Mesa Garcom Mapper Response MS
- Gestor Garcom Components Component Shared
- BFF Restaurante Pedido Pedidos Enums
- BFF Restaurante Garcom Mesa Controller
- BFF Restaurante Garcom Usuarios Tests
- BFF Restaurante Mesa Garcom Response
- Garcom Expediente Service Core
- Gestor Garcom Component Config Features
- BFF Restaurante Mesa Garcom Exception
- Component Features
- Pedidos MS Pedido Controller Response
- Produtos MS Produto Mapper Response
- BFF Restaurante Pedido Pedidos Mapper
- BFF Restaurante Exception Response
- Mesa Mesas Service Request MS
- Mesa Mesas Controller Response MS
- BFF Restaurante Pedido Mesa Service
- BFF Restaurante Cardapio Produtos Mapper
- Login Auth Models Components Features
- BFF Restaurante Mesa Mesas Service
- Produtos MS Cardapio Produto Mapper
- Mesa Components Service Core
- BFF Restaurante Pedido Pedidos Service
- BFF Restaurante Mesa Gestor Controller
- BFF Restaurante Garcom Auth Service
- Gestor Mesa Components Models Core
- BFF Restaurante Garcom Auth Controller
- Garcom Mesa Models Response Features
- Pedido Pedidos Components Models Features
- BFF Restaurante Mesas Pedidos Service
- BFF Restaurante Notificacoes Garcom Service
- BFF Restaurante Pedido Cozinha Service
- Garcom Notificacoes Service Response MS
- Produtos MS Produto Mapper Request
- Garcom Mesa Models Service Core
- Mesa Mesas Service Response MS
- BFF Restaurante Pedido Auth Controller
- BFF Restaurante Mesa Gestor Controller
- Produtos MS Produto Controller Response
- Usuarios MS Usuario Service Request
- Usuarios MS Exception Response
- Usuarios MS Usuario Auth Repository
- Components Service Features
- BFF Restaurante Cozinha Pedidos Service
- BFF Restaurante Garcom Usuarios Mapper
- Produtos MS Produto Service Request
- Produtos MS Cardapio Categoria Service
- Produtos MS Exception Response
- Historico Mesas Repository Response MS
- Pedidos MS Pedido Produto Exception
- Pedidos MS Exception Response Exceptions
- Usuarios MS Usuario Controller Response
- Produto Produtos Models Components Core
- Component Features
- Pedidos MS Pedido Request Service
- BFF Restaurante Auth Config Service
- BFF Restaurante Cozinha Fila Controller
- BFF Restaurante Pedido Garcom Request
- Pedidos MS Mesa Mesas Repository
- Usuarios MS Usuario Exception
- Auth Routing Utils Core
- BFF Restaurante Pedidos Pedido Client
- BFF Restaurante Controller Response
- BFF Restaurante Mesa Auth Service
- Exception Response MS Notificacoes
- Produtos MS Categoria Produto Exception
- Mesa Pedido Models Response Features
- BFF Restaurante Pedido Pedidos Mapper
- BFF Restaurante Cardapio Categoria Controller
- Operacao Garcom Controller Request MS
- Usuarios MS Usuario Service Request
- Cardapio Pedido Models Components Features
- Auth Login Service Core
- BFF Restaurante Cardapio Mesa Controller
- Pedidos MS Pedido Cozinha Enums
- Usuarios MS Usuario Mapper Response
- Usuarios MS Usuario Mesa Service
- Service Models Core
- Mesa Pedido Component Features
- BFF Restaurante Mesa Garcom Controller
- BFF Restaurante Pedido Garcom Controller
- BFF Restaurante Mesa Auth Controller
- BFF Restaurante Cozinha Fila Response
- Pedido Garcom Enums Request MS
- Pedidos MS Pedido Service
- Auth Components Config Shared
- Mesa Garcom Models Request Core
- BFF Restaurante Auth Usuario Security
- BFF Restaurante Mesa Mesas Service
- BFF Restaurante Gestor Mesa Mapper
- BFF Restaurante Usuario Service Security
- Mesas Mesa Service MS
- Produtos MS Produto Service
- Usuarios MS Auth Request Response
- Mesa Pedido Models Service Core
- BFF Restaurante Auth Response Component
- Mesas Mesa Core
- Mesa Component Features
- BFF Restaurante Cozinha Pedido Controller
- Usuarios MS Config
- Usuarios MS Usuario Garcom Enums
- Usuarios MS Usuario Service Validation
- Pedido Pedidos Client Response MS
- Pedidos MS Pedido Mesa Request
- Usuarios MS Service Security
- Cardapio Produto Components Models Features
- Mesas Mesa Tests MS
- Pedidos MS Pedido Tests
- Produtos MS Cardapio Mesa Controller
- Produtos MS Produto Request Validation
- Produtos MS Produto Tests
- Usuarios MS Usuario Tests
- Components Component Features
- Component Features
- BFF Restaurante Auth Config Exception
- Components Component Features
- Gestor Mesas Features
- BFF Restaurante Config
- Pedidos MS Pedido Response Service
- Mesa Service Core
- Login Components Component Features
- BFF Restaurante Client
- BFF Restaurante Config
- DB Migrations Tests
- Cozinha Tests MS
- Notificacoes Tests MS
- Usuarios MS Config
- Components Component Features
- DB Migrations
- Cozinha MS
- Notificacoes MS
- Core
- Component Features
- Component Features
- Components Component Shared
- BFF Restaurante
- DB Migrations
- Cozinha MS
- Mesas Mesa MS
- Notificacoes MS
- Pedidos MS Pedido
- Produtos MS Produto
- Usuarios MS Usuario

## God Nodes (most connected - your core abstractions)
1. `ErrorEnum` - 87 edges
2. `ErrorEnum` - 66 edges
3. `ErrorEnum` - 65 edges
4. `ErrorEnum` - 65 edges
5. `ErrorEnum` - 63 edges
6. `GestorMesaService` - 62 edges
7. `ErrorEnum` - 60 edges
8. `BaseException` - 53 edges
9. `NotificacaoResponse` - 52 edges
10. `BaseException` - 51 edges

## Surprising Connections (you probably didn't know these)
- `destino()` --references--> `DestinoNotificacao`  [EXTRACTED]
  backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/dto/EventoPedido.java → backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/dto/DestinoNotificacao.java
- `canAccessRoute()` --indirect_call--> `AuthService`  [INFERRED]
  frontend/src/app/core/guards/auth.guard.ts → frontend/src/app/core/services/auth.ts
- `redirectToDefaultRoute()` --indirect_call--> `AuthService`  [INFERRED]
  frontend/src/app/core/guards/auth.guard.ts → frontend/src/app/core/services/auth.ts
- `authInterceptor()` --indirect_call--> `AuthService`  [INFERRED]
  frontend/src/app/core/interceptors/auth.interceptor.ts → frontend/src/app/core/services/auth.ts
- `criar()` --indirect_call--> `ProdutoCadastro`  [INFERRED]
  frontend/src/app/features/produtos/produto-cadastro.spec.ts → frontend/src/app/features/produtos/produto-cadastro.ts

## Import Cycles
- None detected.

## Communities (165 total, 23 thin omitted)

### Community 0 - "BFF Restaurante Produtos Produto Service"
Cohesion: 0.06
Nodes (39): AtualizarProdutoClientRequest, CategoriaClientResponse, CriarCategoriaClientRequest, CriarProdutoClientRequest, ProdutoClientResponse, FeignClient, GetMapping, PatchMapping (+31 more)

### Community 1 - "BFF Restaurante Mesas Mesa Service"
Cohesion: 0.06
Nodes (42): MesaGarcomClientResponse, MesaPaginadaClientResponse, ResumoMesasOperacaoResponse, FeignClient, GetMapping, MesaClient, ResumoNotificacoesOperacaoResponse, FeignClient (+34 more)

### Community 2 - "BFF Restaurante Usuarios Login Service"
Cohesion: 0.06
Nodes (47): UsuarioLoginRequest, UsuarioLoginResponse, FeignClient, PostMapping, UsuarioAuthClient, AuthController, ApiResponses, Authentication (+39 more)

### Community 3 - "BFF Restaurante Usuario Usuarios Service"
Cohesion: 0.07
Nodes (39): AtualizarUsuarioClientRequest, DeleteMapping, FeignClient, GetMapping, PutMapping, UsuarioClient, GestorUsuarioController, ApiResponses (+31 more)

### Community 4 - "Pedidos MS Pedido Produto Exception"
Cohesion: 0.05
Nodes (47): BaseException, Getter, BaseExceptionHandler, ExceptionHandler, MethodArgumentNotValidException, Order, ResponseEntity, RestControllerAdvice (+39 more)

### Community 5 - "Cozinha Pedido Models Response Features"
Cohesion: 0.07
Nodes (17): DecisaoProblemaRequest, ItemFilaCozinhaResponse, PedidoFilaCozinhaResponse, PedidoStatusCozinhaResponse, SinalizarProblemaRequest, SinalizarProblemaResponse, StatusPedidoCozinha, StatusProdutoPedido (+9 more)

### Community 6 - "BFF Restaurante Mesas Mesa Service"
Cohesion: 0.09
Nodes (13): AtribuirGarcomClientRequest, MesaClientResponse, PatchMapping, Schema, PedidoGestorResponse, MesaGestorMapperSource, AlteracaoMesa, GestorMesaService (+5 more)

### Community 7 - "Garcom Operacao Mapper Service MS"
Cohesion: 0.08
Nodes (33): CriarNotificacaoRequest, NotificacaoResponse, DestinoNotificacao, COZINHA, GARCOM, GESTOR, MESA, TOTEM (+25 more)

### Community 8 - "Pedidos MS Pedido Service Repository"
Cohesion: 0.08
Nodes (16): DecisaoProblemaRequest, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+8 more)

### Community 9 - "Produtos MS Categoria Categorias Service"
Cohesion: 0.09
Nodes (31): CategoriaController, GetMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, CriarCategoriaRequest (+23 more)

### Community 10 - "Produtos MS Validation Mapper Support"
Cohesion: 0.09
Nodes (11): BufferedImage, ImagemBase64Utils, ImagemBase64Validator, Override, ImagemBase64MapperTest, Test, ImagemTesteFactory, ImagemBase64ValidatorTest (+3 more)

### Community 11 - "Pedidos MS Pedido Mapper Enums"
Cohesion: 0.06
Nodes (32): AlterarPedidoRequest, PedidoRequest, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+24 more)

### Community 12 - "Produtos Produto Utils Component Features"
Cohesion: 0.06
Nodes (18): base64ParaDataUrl(), carregarImagem(), CompressaoOpcoes, comprimirImagem(), dimensionar(), lerComoDataUrl(), MAGIC_PARA_MIME, BrokenImage (+10 more)

### Community 13 - "BFF Restaurante Pedido Garcom Service"
Cohesion: 0.11
Nodes (22): SessaoMesaResponse, CriarPedidoRequest, ProdutoClient, CriarPedidoGarcomRequest, Schema, ItemPedidoGarcomRequest, Schema, Schema (+14 more)

### Community 14 - "Expediente Pedido Models Response Core"
Cohesion: 0.06
Nodes (37): CargaGarcom, MesaMaisOcupada, Pedido, PedidoRecente, ResumoAtendimento, ResumoExpediente, StatusMesaPainel, StatusPedidoPainel (+29 more)

### Community 15 - "BFF Restaurante Garcom Mesa Service"
Cohesion: 0.10
Nodes (25): ApiResponses, Authentication, Operation, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+17 more)

### Community 16 - "BFF Restaurante Pedido Mesa Controller"
Cohesion: 0.10
Nodes (28): ApiResponses, Authentication, GetMapping, Operation, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+20 more)

### Community 17 - "Mesa Garcom Mapper Response MS"
Cohesion: 0.09
Nodes (26): CriarMesaRequest, MesaGarcomResponse, StatusMesa, DISPONIVEL, OCUPADA, CriarMesaRequestMapper, Component, Override (+18 more)

### Community 18 - "Gestor Garcom Components Component Shared"
Cohesion: 0.07
Nodes (28): NivelCarga, FiltroEstadoPainel, AcaoCritica, ConfirmacaoAcaoEstado, Criticidade, FiltroEstado, ModoSelecaoGarcom, Ordenacao (+20 more)

### Community 19 - "BFF Restaurante Pedido Pedidos Enums"
Cohesion: 0.08
Nodes (25): DecisaoProblemaPedidoRequest, SinalizarProblemaRequest, SinalizarProblemaResponse, DecisaoProblemaRequest, Schema, StatusProdutoPedido, CANCELADO, DISPONIVEL (+17 more)

### Community 20 - "BFF Restaurante Garcom Mesa Controller"
Cohesion: 0.09
Nodes (20): PostMapping, DestinoNotificacao, COZINHA, GARCOM, GESTOR, MESA, TOTEM, Schema (+12 more)

### Community 21 - "BFF Restaurante Garcom Usuarios Tests"
Cohesion: 0.13
Nodes (10): AutoConfigureMockMvc, BffRestauranteApplicationTests, GetMapping, RestController, SpringBootTest, Test, ProtectedRoutesTestController, Import (+2 more)

### Community 22 - "BFF Restaurante Mesa Garcom Response"
Cohesion: 0.11
Nodes (20): ChamadaPendenteMesaResponse, Schema, Schema, MesaGarcomResponse, Schema, PedidoAtivoMesaResponse, MesaGarcomMapperSource, ChamadaPendenteMesaResponse (+12 more)

### Community 23 - "Garcom Expediente Service Core"
Cohesion: 0.12
Nodes (8): nivelCargaGarcom(), baseConsulta(), chaveConsulta(), consultasIguais(), normalizarConsulta(), PainelService, raioPrefetch(), Injectable

### Community 24 - "Gestor Garcom Component Config Features"
Cohesion: 0.06
Nodes (3): OrdenacaoPainel, Gestor, Component

### Community 25 - "BFF Restaurante Mesa Garcom Exception"
Cohesion: 0.06
Nodes (33): ErrorEnum, ACESSO_NEGADO, ATENDIMENTO_NAO_ABERTO, CATEGORIA_NAO_ENCONTRADA, CATEGORIA_NOME_DUPLICADO, CHAMADA_GARCOM_INVALIDA, CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM, CREDENCIAIS_INVALIDAS (+25 more)

### Community 26 - "Component Features"
Cohesion: 0.10
Nodes (4): CustomerHome, Component, ViewChild, HostListener

### Community 27 - "Pedidos MS Pedido Controller Response"
Cohesion: 0.16
Nodes (11): DeleteMapping, GetMapping, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+3 more)

### Community 28 - "Produtos MS Produto Mapper Response"
Cohesion: 0.10
Nodes (20): Mapper, Component, Override, ProdutoDisponibilidadeResponse, ProdutoDisponibilidadeResponseMapper, Component, Override, ProdutoResponseMapper (+12 more)

### Community 29 - "BFF Restaurante Pedido Pedidos Mapper"
Cohesion: 0.13
Nodes (16): ProdutoPedidoRequest, ProdutoDisponibilidadeResponse, ItemPedidoTotemRequest, Schema, ItemPedidoTotemMapperSource, ItemPedidoTotemRequestMapper, Component, Override (+8 more)

### Community 30 - "BFF Restaurante Exception Response"
Cohesion: 0.13
Nodes (18): BaseExceptionHandler, ExceptionHandler, MethodArgumentNotValidException, Order, ResponseEntity, RestControllerAdvice, Slf4j, ErrorObject (+10 more)

### Community 31 - "Mesa Mesas Service Request MS"
Cohesion: 0.18
Nodes (3): AtribuirGarcomRequest, Test, MesaServiceTest

### Community 32 - "Mesa Mesas Controller Response MS"
Cohesion: 0.15
Nodes (14): GetMapping, Pageable, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+6 more)

### Community 33 - "BFF Restaurante Pedido Mesa Service"
Cohesion: 0.17
Nodes (10): Authentication, PedidoMesaResponse, PedidoMesaStatusResponse, MesaPedidoService, Authentication, CriarPedidoMesaRequest, ExtendWith, FeignException (+2 more)

### Community 34 - "BFF Restaurante Cardapio Produtos Mapper"
Cohesion: 0.14
Nodes (15): CategoriaCardapioClientResponse, ProdutoCardapioClientResponse, Schema, ProdutoCardapioResponse, CardapioResponseMapper, CategoriaCardapioResponse, Component, Override (+7 more)

### Community 35 - "Login Auth Models Components Features"
Cohesion: 0.12
Nodes (11): ApiError, LoginFormValue, LoginRequest, LoginResponse, UsuarioAutenticadoResponse, LoginFooterComponent, Component, Login (+3 more)

### Community 36 - "BFF Restaurante Mesa Mesas Service"
Cohesion: 0.24
Nodes (3): GestorMesaServiceTest, PedidoCozinhaResponse, Test

### Community 37 - "Produtos MS Cardapio Produto Mapper"
Cohesion: 0.13
Nodes (13): ProdutoCardapioResponse, CategoriaCardapioMapperSource, Component, Override, ProdutoCardapioResponse, ProdutoCardapioResponseMapper, CardapioMapperTest, Test (+5 more)

### Community 38 - "Mesa Components Service Core"
Cohesion: 0.15
Nodes (8): CartSummary, CustomerContext, CustomerContextService, Injectable, CustomerCartHeaderComponent, CustomerMenuActiveLink, Component, MesaOrdersState

### Community 39 - "BFF Restaurante Pedido Pedidos Service"
Cohesion: 0.19
Nodes (10): CriarPedidoRequest, UsuarioAutenticado, Authentication, Authentication, BeforeEach, CriarPedidoTotemRequest, ExtendWith, FeignException (+2 more)

### Community 40 - "BFF Restaurante Mesa Gestor Controller"
Cohesion: 0.22
Nodes (15): GestorMesaController, ApiResponses, GetMapping, Operation, PatchMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 41 - "BFF Restaurante Garcom Auth Service"
Cohesion: 0.13
Nodes (15): GarcomChamadaServiceTest, Authentication, ExtendWith, NotificacaoResponse, Test, BaseException, Getter, ErrorEnum (+7 more)

### Community 42 - "Gestor Mesa Components Models Core"
Cohesion: 0.11
Nodes (10): Criticidade, LIMIARES_CARGA_GARCOM, LIMIARES_URGENCIA, resolverAcaoPrimaria(), resolverCriticidadeMesa(), AcaoMesaPainel, MesaPainel, MesaCard (+2 more)

### Community 43 - "BFF Restaurante Garcom Auth Controller"
Cohesion: 0.15
Nodes (18): GarcomChamadaController, ApiResponses, Authentication, Operation, PatchMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+10 more)

### Community 44 - "Garcom Mesa Models Response Features"
Cohesion: 0.14
Nodes (8): ChamadaPendenteMesaResponse, MesaGarcomResponse, PedidoAtivoMesaResponse, GarcomMesaService, Injectable, FiltroMesa, Garcom, Component

### Community 45 - "Pedido Pedidos Components Models Features"
Cohesion: 0.10
Nodes (12): ApiErrorObject, PedidoCanal, PedidoResponse, PedidoStatusItemResponse, CartActionsComponent, Component, CartItemCardComponent, Component (+4 more)

### Community 46 - "BFF Restaurante Mesas Pedidos Service"
Cohesion: 0.12
Nodes (9): HistoricoAtendimentoClientResponse, ItemPedidoCozinhaResponse, PedidoCozinhaResponse, HistoricoAtendimentoResponse, Schema, HistoricoAtendimentoResponse, ExtendWith, FeignException (+1 more)

### Community 47 - "BFF Restaurante Notificacoes Garcom Service"
Cohesion: 0.18
Nodes (7): PatchMapping, CriarNotificacaoRequest, ExtendWith, FeignException, NotificacaoResponse, Test, NotificacaoServiceTest

### Community 48 - "BFF Restaurante Pedido Cozinha Service"
Cohesion: 0.13
Nodes (15): destino(), EventoPedido, PEDIDO_COM_FALTA, PEDIDO_COM_PROBLEMA, PEDIDO_EM_PREPARO, PEDIDO_ERRO, PEDIDO_INDISPONIVEL, PEDIDO_PRONTO (+7 more)

### Community 49 - "Garcom Notificacoes Service Response MS"
Cohesion: 0.21
Nodes (3): NotificacaoResponse, Test, NotificacaoServiceTest

### Community 50 - "Produtos MS Produto Mapper Request"
Cohesion: 0.15
Nodes (10): AtualizarProdutoRequest, AtualizarProdutoRequestMapper, Component, CriarProdutoRequestMapper, Component, Override, ImagemBase64Mapper, Component (+2 more)

### Community 51 - "Garcom Mesa Models Service Core"
Cohesion: 0.16
Nodes (10): DestinoNotificacao, NotificacaoResponse, GarcomChamadaService, Injectable, ChamarGarcomRequest, MesaChamadaService, Injectable, NotificacaoService (+2 more)

### Community 52 - "Mesa Mesas Service Response MS"
Cohesion: 0.23
Nodes (6): SessaoMesaResponse, RequiredArgsConstructor, Service, SessaoMesaResponse, Transactional, MesaService

### Community 53 - "BFF Restaurante Pedido Auth Controller"
Cohesion: 0.18
Nodes (15): ApiResponses, Authentication, Operation, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+7 more)

### Community 54 - "BFF Restaurante Mesa Gestor Controller"
Cohesion: 0.19
Nodes (9): AtribuirGarcomRequest, Schema, CargaGarcomResponse, Schema, Schema, ResumoPainelResponse, GestorMesaControllerTest, ExtendWith (+1 more)

### Community 55 - "Produtos MS Produto Controller Response"
Cohesion: 0.22
Nodes (10): GetMapping, PatchMapping, PostMapping, PutMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+2 more)

### Community 56 - "Usuarios MS Usuario Service Request"
Cohesion: 0.25
Nodes (6): AtualizarUsuarioRequest, Schema, ExtendWith, PasswordEncoder, Test, UsuarioServiceTest

### Community 57 - "Usuarios MS Exception Response"
Cohesion: 0.19
Nodes (14): BaseExceptionHandler, ExceptionHandler, MethodArgumentNotValidException, Order, ResponseEntity, RestControllerAdvice, Slf4j, ErrorObject (+6 more)

### Community 58 - "Usuarios MS Usuario Auth Repository"
Cohesion: 0.15
Nodes (15): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, Usuario (+7 more)

### Community 59 - "Components Service Features"
Cohesion: 0.13
Nodes (10): MenuContext, MenuService, Injectable, CustomerFooterComponent, Component, CustomerHeroComponent, Component, CustomerHomeHeaderComponent (+2 more)

### Community 60 - "BFF Restaurante Cozinha Pedidos Service"
Cohesion: 0.20
Nodes (6): CozinhaServiceTest, ExtendWith, FeignException, PedidoCozinhaResponse, PedidoResponse, Test

### Community 61 - "BFF Restaurante Garcom Usuarios Mapper"
Cohesion: 0.18
Nodes (8): UsuarioClientResponse, GarcomResumoResponse, Schema, GarcomResumoResponseMapper, Component, Override, GarcomResumoResponseMapperTest, Test

### Community 62 - "Produtos MS Produto Service Request"
Cohesion: 0.32
Nodes (4): CriarProdutoRequest, ExtendWith, Test, ProdutoServiceTest

### Community 63 - "Produtos MS Cardapio Categoria Service"
Cohesion: 0.19
Nodes (11): CategoriaCardapioResponse, CategoriaCardapioResponseMapper, CategoriaCardapioResponse, Component, Override, CardapioService, RequiredArgsConstructor, Service (+3 more)

### Community 64 - "Produtos MS Exception Response"
Cohesion: 0.20
Nodes (13): BaseExceptionHandler, ExceptionHandler, MethodArgumentNotValidException, Order, ResponseEntity, RestControllerAdvice, Slf4j, ErrorObject (+5 more)

### Community 65 - "Historico Mesas Repository Response MS"
Cohesion: 0.18
Nodes (12): HistoricoAtendimentoResponse, HistoricoAtendimento, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+4 more)

### Community 66 - "Pedidos MS Pedido Produto Exception"
Cohesion: 0.14
Nodes (15): BaseException, Getter, ErrorEnum, DADOS_INVALIDOS, ERRO_INTERNO, PEDIDO_AGUARDANDO_DECISAO, PEDIDO_ENCERRADO, PEDIDO_NAO_ENCONTRADO (+7 more)

### Community 67 - "Pedidos MS Exception Response Exceptions"
Cohesion: 0.27
Nodes (11): BaseExceptionHandler, ExceptionHandler, MethodArgumentNotValidException, Order, ResponseEntity, RestControllerAdvice, Slf4j, ErrorObject (+3 more)

### Community 68 - "Usuarios MS Usuario Controller Response"
Cohesion: 0.20
Nodes (13): ApiResponses, Authentication, DeleteMapping, GetMapping, Operation, PostMapping, PutMapping, RequestMapping (+5 more)

### Community 69 - "Produto Produtos Models Components Core"
Cohesion: 0.24
Nodes (9): CategoriaGestorResponse, CriarCategoriaRequest, CriarProdutoRequest, ProdutoGestorResponse, ProdutoService, Injectable, Badge, BadgeVariant (+1 more)

### Community 71 - "Pedidos MS Pedido Request Service"
Cohesion: 0.18
Nodes (7): AssertTrue, CriarPedidoRequest, ProdutoPedidoRequest, CriarPedidoRequestMapper, Component, Override, CriarPedidoRequest

### Community 72 - "BFF Restaurante Auth Config Service"
Cohesion: 0.24
Nodes (11): AuthenticationException, Bean, Configuration, HttpSecurity, HttpServletRequest, HttpServletResponse, ObjectMapper, RequiredArgsConstructor (+3 more)

### Community 73 - "BFF Restaurante Cozinha Fila Controller"
Cohesion: 0.27
Nodes (11): CozinhaController, ApiResponses, GetMapping, Operation, PatchMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+3 more)

### Community 74 - "BFF Restaurante Pedido Garcom Request"
Cohesion: 0.15
Nodes (15): CriarNotificacaoRequest, Schema, Getter, Schema, TipoNotificacao, ALTERACAO_PEDIDO_SOLICITADA, CHAMADA_GARCOM, CONTA_SOLICITADA (+7 more)

### Community 75 - "Pedidos MS Mesa Mesas Repository"
Cohesion: 0.16
Nodes (8): AtendimentoRepository, Repository, Override, Repository, MesaRepository, ResumoMesasOperacaoResponse, EntityGraph, JpaRepository

### Community 76 - "Usuarios MS Usuario Exception"
Cohesion: 0.15
Nodes (14): BaseException, Getter, ErrorEnum, CREDENCIAIS_INVALIDAS, DADOS_INVALIDOS, EMAIL_JA_CADASTRADO, ERRO_INTERNO, NAO_PODE_EXCLUIR_PROPRIO_USUARIO (+6 more)

### Community 77 - "Auth Routing Utils Core"
Cohesion: 0.24
Nodes (13): routes, authChildGuard(), authGuard(), canAccessRoute(), defaultRedirectGuard(), getAllowedProfiles(), redirectToDefaultRoute(), AppProfile (+5 more)

### Community 78 - "BFF Restaurante Pedidos Pedido Client"
Cohesion: 0.20
Nodes (9): FeignClient, GetMapping, PatchMapping, PedidoCozinhaResponse, PedidoResponse, PostMapping, SinalizarProblemaRequest, SinalizarProblemaResponse (+1 more)

### Community 79 - "BFF Restaurante Controller Response"
Cohesion: 0.27
Nodes (12): ApiResponses, GetMapping, Operation, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+4 more)

### Community 80 - "BFF Restaurante Mesa Auth Service"
Cohesion: 0.23
Nodes (10): Schema, MesaAtendimentoAtualResponse, Authentication, MesaAtendimentoAtualResponse, RequiredArgsConstructor, Service, MesaAtendimentoService, ExtendWith (+2 more)

### Community 81 - "Exception Response MS Notificacoes"
Cohesion: 0.28
Nodes (10): BaseExceptionHandler, ExceptionHandler, MethodArgumentNotValidException, Order, ResponseEntity, RestControllerAdvice, Slf4j, ErrorObject (+2 more)

### Community 82 - "Produtos MS Categoria Produto Exception"
Cohesion: 0.16
Nodes (13): BaseException, Getter, ErrorEnum, CATEGORIA_INATIVA, CATEGORIA_NAO_ENCONTRADA, CATEGORIA_NOME_DUPLICADO, DADOS_INVALIDOS, ERRO_INTERNO (+5 more)

### Community 83 - "Mesa Pedido Models Response Features"
Cohesion: 0.17
Nodes (4): PedidoMesaStatusResponse, PedidoStatus, CustomerOrders, Component

### Community 84 - "BFF Restaurante Pedido Pedidos Mapper"
Cohesion: 0.23
Nodes (9): PedidoResponse, Schema, PedidoTotemResponse, Component, Override, PedidoTotemResponse, PedidoTotemResponseMapper, Test (+1 more)

### Community 85 - "BFF Restaurante Cardapio Categoria Controller"
Cohesion: 0.26
Nodes (8): CategoriaCardapioResponse, Schema, CardapioService, RequiredArgsConstructor, Service, CardapioControllerTest, ExtendWith, Test

### Community 86 - "Operacao Garcom Controller Request MS"
Cohesion: 0.28
Nodes (8): GetMapping, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, NotificacaoController

### Community 87 - "Usuarios MS Usuario Service Request"
Cohesion: 0.26
Nodes (5): CriarUsuarioRequest, Schema, CriarUsuarioRequestMapper, Component, Override

### Community 88 - "Cardapio Pedido Models Components Features"
Cohesion: 0.15
Nodes (7): CategoriaCardapioResponse, ErrorResponse, ItemPedidoMesaRequest, ItemPedidoTotemRequest, ProdutoCardapioResponse, MenuFilterBarComponent, Component

### Community 90 - "BFF Restaurante Cardapio Mesa Controller"
Cohesion: 0.33
Nodes (10): CardapioController, ApiResponses, GetMapping, Operation, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+2 more)

### Community 91 - "Pedidos MS Pedido Cozinha Enums"
Cohesion: 0.21
Nodes (9): PedidoCozinhaResponse, CanaisPedido, GARCOM, MESA, TOTEM, ExtendWith, Test, PedidoControllerTest (+1 more)

### Community 92 - "Usuarios MS Usuario Mapper Response"
Cohesion: 0.21
Nodes (6): Schema, UsuarioResponse, Mapper, Component, Override, UsuarioResponseMapper

### Community 93 - "Usuarios MS Usuario Mesa Service"
Cohesion: 0.27
Nodes (5): BeforeEach, Claims, JwtService, Test, JwtServiceTest

### Community 94 - "Service Models Core"
Cohesion: 0.35
Nodes (3): CartItem, CartService, Injectable

### Community 95 - "Mesa Pedido Component Features"
Cohesion: 0.18
Nodes (5): Mesa, MesaResumo, PedidoMesaAtivo, pedidosAtivos, Component

### Community 96 - "BFF Restaurante Mesa Garcom Controller"
Cohesion: 0.28
Nodes (11): GarcomMesaController, ApiResponses, Authentication, GetMapping, Operation, RequestMapping, RequiredArgsConstructor, ResponseEntity (+3 more)

### Community 97 - "BFF Restaurante Pedido Garcom Controller"
Cohesion: 0.28
Nodes (11): GarcomPedidoController, ApiResponses, Authentication, Operation, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+3 more)

### Community 98 - "BFF Restaurante Mesa Auth Controller"
Cohesion: 0.28
Nodes (11): ApiResponses, Authentication, GetMapping, Operation, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+3 more)

### Community 99 - "BFF Restaurante Cozinha Fila Response"
Cohesion: 0.21
Nodes (8): ItemFilaCozinhaResponse, Schema, Schema, PedidoFilaCozinhaResponse, ItemFilaCozinhaResponse, ItemPedidoCozinhaResponse, PedidoCozinhaResponse, PedidoFilaCozinhaResponse

### Community 100 - "Pedido Garcom Enums Request MS"
Cohesion: 0.17
Nodes (11): Getter, TipoNotificacao, ALTERACAO_PEDIDO_SOLICITADA, CHAMADA_GARCOM, CONTA_SOLICITADA, PEDIDO_CANCELADO, PEDIDO_COM_FALTA, PEDIDO_EM_PREPARO (+3 more)

### Community 101 - "Pedidos MS Pedido Service"
Cohesion: 0.36
Nodes (3): Service, Transactional, PedidoService

### Community 102 - "Auth Components Config Shared"
Cohesion: 0.21
Nodes (6): App, appConfig, Component, authInterceptor(), FloatingCartButton, Component

### Community 103 - "Mesa Garcom Models Request Core"
Cohesion: 0.26
Nodes (6): AtribuirGarcomRequest, CriarMesaRequest, MesaResponse, StatusMesa, MesaService, Injectable

### Community 104 - "BFF Restaurante Auth Usuario Security"
Cohesion: 0.26
Nodes (5): AfterEach, BeforeEach, JwtAuthenticationFilter, Test, JwtAuthenticationFilterTest

### Community 105 - "BFF Restaurante Mesa Mesas Service"
Cohesion: 0.32
Nodes (5): Authentication, ExtendWith, FeignException, Test, MesaAtendimentoServiceTest

### Community 106 - "BFF Restaurante Gestor Mesa Mapper"
Cohesion: 0.20
Nodes (6): Mapper, Component, Override, MesaGestorResponseMapper, Test, MesaGestorResponseMapperTest

### Community 107 - "BFF Restaurante Usuario Service Security"
Cohesion: 0.33
Nodes (4): BeforeEach, JwtService, Test, JwtServiceTest

### Community 108 - "Mesas Mesa Service MS"
Cohesion: 0.32
Nodes (8): Atendimento, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table

### Community 109 - "Produtos MS Produto Service"
Cohesion: 0.32
Nodes (3): RequiredArgsConstructor, Service, ProdutoService

### Community 110 - "Usuarios MS Auth Request Response"
Cohesion: 0.30
Nodes (9): Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, RequiredArgsConstructor, JwtAuthenticationFilter (+1 more)

### Community 111 - "Mesa Pedido Models Service Core"
Cohesion: 0.24
Nodes (7): CriarPedidoMesaRequest, CriarPedidoTotemRequest, MesaAtendimentoAtualResponse, PedidoMesaResponse, PedidoTotemResponse, OrderService, Injectable

### Community 112 - "BFF Restaurante Auth Response Component"
Cohesion: 0.33
Nodes (8): Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, RequiredArgsConstructor, JwtAuthenticationFilter

### Community 113 - "Mesas Mesa Core"
Cohesion: 0.24
Nodes (8): estabilizar(), flushCargaInicial(), flushPaginadasPendentes(), flushPainel(), MESAS_API, PAGINA_MESAS_API, paginaComTotal(), RESUMO_API

### Community 115 - "BFF Restaurante Cozinha Pedido Controller"
Cohesion: 0.36
Nodes (5): Schema, PedidoStatusCozinhaResponse, CozinhaControllerTest, ExtendWith, Test

### Community 116 - "Usuarios MS Config"
Cohesion: 0.36
Nodes (7): Bean, Configuration, HttpSecurity, PasswordEncoder, RequiredArgsConstructor, SecurityFilterChain, SecurityConfig

### Community 117 - "Usuarios MS Usuario Garcom Enums"
Cohesion: 0.22
Nodes (8): Getter, PerfilUsuario, ADMIN, COZINHA, GARCOM, GESTOR, MESA, TOTEM

### Community 118 - "Usuarios MS Usuario Service Validation"
Cohesion: 0.27
Nodes (5): PasswordEncoder, RequiredArgsConstructor, Service, UsuarioService, UsuarioRegex

### Community 119 - "Pedido Pedidos Client Response MS"
Cohesion: 0.28
Nodes (5): Builder, Component, PedidosAtivosClient, ResumoContaAtendimentoResponse, RestClient

### Community 120 - "Pedidos MS Pedido Mesa Request"
Cohesion: 0.42
Nodes (4): CriarPedidoRequestTest, CriarPedidoRequest, Test, Validator

### Community 121 - "Usuarios MS Service Security"
Cohesion: 0.39
Nodes (3): Claims, Service, JwtService

### Community 122 - "Cardapio Produto Components Models Features"
Cohesion: 0.28
Nodes (3): ProdutoCardapioView, ProductGridComponent, Component

### Community 123 - "Mesas Mesa Tests MS"
Cohesion: 0.32
Nodes (4): SpringBootApplication, MsMesasApplication, Test, MsMesasApplicationTests

### Community 124 - "Pedidos MS Pedido Tests"
Cohesion: 0.32
Nodes (4): SpringBootApplication, MsPedidosApplication, Test, MsPedidosApplicationTests

### Community 125 - "Produtos MS Cardapio Mesa Controller"
Cohesion: 0.43
Nodes (6): CardapioController, GetMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController

### Community 126 - "Produtos MS Produto Request Validation"
Cohesion: 0.39
Nodes (5): ImagemBase64, Constraint, Documented, Retention, Target

### Community 127 - "Produtos MS Produto Tests"
Cohesion: 0.32
Nodes (4): SpringBootApplication, MsProdutosApplication, Test, MsProdutosApplicationTests

### Community 128 - "Usuarios MS Usuario Tests"
Cohesion: 0.32
Nodes (4): SpringBootApplication, MsUsuariosApplication, Test, MsUsuariosApplicationTests

### Community 131 - "BFF Restaurante Auth Config Exception"
Cohesion: 0.48
Nodes (3): AccessDeniedException, Test, SecurityConfigTest

### Community 132 - "Components Component Features"
Cohesion: 0.29
Nodes (3): CategoryCarouselComponent, Component, ViewChild

### Community 133 - "Gestor Mesas Features"
Cohesion: 0.33
Nodes (5): flushCargaInicial(), flushPainel(), MESAS_API, PAGINA_MESAS_API, RESUMO_API

### Community 134 - "BFF Restaurante Config"
Cohesion: 0.53
Nodes (4): CorsConfig, Bean, Configuration, CorsConfigurationSource

### Community 137 - "Login Components Component Features"
Cohesion: 0.33
Nodes (3): LoginField, LoginFormComponent, Component

### Community 138 - "BFF Restaurante Client"
Cohesion: 0.60
Nodes (3): BffRestauranteApplication, SpringBootApplication, EnableFeignClients

### Community 139 - "BFF Restaurante Config"
Cohesion: 0.70
Nodes (4): Configuration, OpenAPIDefinition, SecurityScheme, OpenApiConfig

### Community 140 - "DB Migrations Tests"
Cohesion: 0.60
Nodes (3): DbMigrationsApplicationTests, SpringBootTest, Test

### Community 141 - "Cozinha Tests MS"
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, MsCozinhaApplicationTests

### Community 142 - "Notificacoes Tests MS"
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, MsNotificacoesApplicationTests

### Community 143 - "Usuarios MS Config"
Cohesion: 0.70
Nodes (4): Configuration, OpenAPIDefinition, SecurityScheme, OpenApiConfig

## Knowledge Gaps
- **211 isolated node(s):** `br.com.fourkitchen:bff-restaurante`, `GARCOM`, `MESA`, `TOTEM`, `COZINHA` (+206 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ErrorEnum` connect `Usuarios MS Usuario Exception` to `BFF Restaurante Produtos Produto Service`, `BFF Restaurante Mesas Mesa Service`, `BFF Restaurante Usuarios Login Service`, `BFF Restaurante Usuario Usuarios Service`, `BFF Restaurante Auth Config Exception`, `Pedidos MS Pedido Produto Exception`, `BFF Restaurante Mesas Mesa Service`, `Garcom Operacao Mapper Service MS`, `Produtos MS Categoria Categorias Service`, `Produtos MS Validation Mapper Support`, `BFF Restaurante Pedido Garcom Service`, `BFF Restaurante Garcom Mesa Service`, `BFF Restaurante Pedido Mesa Controller`, `Mesa Garcom Mapper Response MS`, `BFF Restaurante Garcom Mesa Controller`, `BFF Restaurante Mesa Garcom Response`, `BFF Restaurante Pedido Pedidos Mapper`, `BFF Restaurante Pedido Mesa Service`, `BFF Restaurante Cardapio Produtos Mapper`, `BFF Restaurante Pedido Pedidos Service`, `BFF Restaurante Garcom Auth Service`, `BFF Restaurante Garcom Auth Controller`, `BFF Restaurante Mesas Pedidos Service`, `BFF Restaurante Notificacoes Garcom Service`, `BFF Restaurante Pedido Cozinha Service`, `Produtos MS Produto Mapper Request`, `Mesa Mesas Service Response MS`, `Usuarios MS Usuario Service Request`, `Usuarios MS Exception Response`, `Usuarios MS Usuario Auth Repository`, `BFF Restaurante Cozinha Pedidos Service`, `Produtos MS Produto Service Request`, `BFF Restaurante Auth Config Service`, `BFF Restaurante Mesa Auth Service`, `BFF Restaurante Cardapio Categoria Controller`, `BFF Restaurante Auth Usuario Security`, `BFF Restaurante Mesa Mesas Service`, `Produtos MS Produto Service`, `Usuarios MS Auth Request Response`, `BFF Restaurante Auth Response Component`, `Usuarios MS Usuario Service Validation`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Why does `ErrorEnum` connect `BFF Restaurante Mesa Garcom Exception` to `BFF Restaurante Produtos Produto Service`, `BFF Restaurante Mesas Mesa Service`, `BFF Restaurante Usuarios Login Service`, `BFF Restaurante Usuario Usuarios Service`, `BFF Restaurante Auth Config Exception`, `Pedidos MS Pedido Produto Exception`, `BFF Restaurante Mesas Mesa Service`, `Garcom Operacao Mapper Service MS`, `Produtos MS Categoria Categorias Service`, `Produtos MS Validation Mapper Support`, `BFF Restaurante Pedido Garcom Service`, `BFF Restaurante Garcom Mesa Service`, `BFF Restaurante Pedido Mesa Controller`, `Mesa Garcom Mapper Response MS`, `BFF Restaurante Garcom Mesa Controller`, `BFF Restaurante Mesa Garcom Response`, `BFF Restaurante Pedido Pedidos Mapper`, `BFF Restaurante Exception Response`, `BFF Restaurante Pedido Mesa Service`, `BFF Restaurante Cardapio Produtos Mapper`, `BFF Restaurante Pedido Pedidos Service`, `BFF Restaurante Garcom Auth Service`, `BFF Restaurante Garcom Auth Controller`, `BFF Restaurante Mesas Pedidos Service`, `BFF Restaurante Notificacoes Garcom Service`, `BFF Restaurante Pedido Cozinha Service`, `Produtos MS Produto Mapper Request`, `Mesa Mesas Service Response MS`, `Usuarios MS Usuario Service Request`, `Usuarios MS Usuario Auth Repository`, `BFF Restaurante Cozinha Pedidos Service`, `Produtos MS Produto Service Request`, `BFF Restaurante Auth Config Service`, `BFF Restaurante Mesa Auth Service`, `BFF Restaurante Cardapio Categoria Controller`, `BFF Restaurante Auth Usuario Security`, `BFF Restaurante Mesa Mesas Service`, `Produtos MS Produto Service`, `Usuarios MS Auth Request Response`, `BFF Restaurante Auth Response Component`, `Usuarios MS Usuario Service Validation`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **Why does `ErrorEnum` connect `Pedidos MS Pedido Produto Exception` to `BFF Restaurante Produtos Produto Service`, `BFF Restaurante Mesas Mesa Service`, `BFF Restaurante Usuarios Login Service`, `BFF Restaurante Usuario Usuarios Service`, `BFF Restaurante Auth Config Exception`, `BFF Restaurante Mesas Mesa Service`, `Garcom Operacao Mapper Service MS`, `Produtos MS Categoria Categorias Service`, `Produtos MS Validation Mapper Support`, `BFF Restaurante Pedido Garcom Service`, `BFF Restaurante Garcom Mesa Service`, `BFF Restaurante Pedido Mesa Controller`, `Mesa Garcom Mapper Response MS`, `BFF Restaurante Garcom Mesa Controller`, `BFF Restaurante Mesa Garcom Response`, `BFF Restaurante Pedido Pedidos Mapper`, `BFF Restaurante Pedido Mesa Service`, `BFF Restaurante Cardapio Produtos Mapper`, `BFF Restaurante Pedido Pedidos Service`, `BFF Restaurante Garcom Auth Service`, `BFF Restaurante Garcom Auth Controller`, `BFF Restaurante Mesas Pedidos Service`, `BFF Restaurante Notificacoes Garcom Service`, `BFF Restaurante Pedido Cozinha Service`, `Produtos MS Produto Mapper Request`, `Mesa Mesas Service Response MS`, `Usuarios MS Usuario Service Request`, `Usuarios MS Usuario Auth Repository`, `BFF Restaurante Cozinha Pedidos Service`, `Produtos MS Produto Service Request`, `BFF Restaurante Auth Config Service`, `BFF Restaurante Mesa Auth Service`, `BFF Restaurante Cardapio Categoria Controller`, `BFF Restaurante Auth Usuario Security`, `BFF Restaurante Mesa Mesas Service`, `Produtos MS Produto Service`, `Usuarios MS Auth Request Response`, `BFF Restaurante Auth Response Component`, `Usuarios MS Usuario Service Validation`?**
  _High betweenness centrality (0.030) - this node is a cross-community bridge._
- **What connects `br.com.fourkitchen:bff-restaurante`, `GARCOM`, `MESA` to the rest of the system?**
  _215 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `BFF Restaurante Produtos Produto Service` be split into smaller, more focused modules?**
  _Cohesion score 0.06007480130902291 - nodes in this community are weakly interconnected._
- **Should `BFF Restaurante Mesas Mesa Service` be split into smaller, more focused modules?**
  _Cohesion score 0.056179775280898875 - nodes in this community are weakly interconnected._
- **Should `BFF Restaurante Usuarios Login Service` be split into smaller, more focused modules?**
  _Cohesion score 0.05995410212277682 - nodes in this community are weakly interconnected._