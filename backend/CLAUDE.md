# CLAUDE.md

## Stack
- Java 21, Spring Boot, PostgreSQL, Flyway, JWT
- Build/testes: ./mvnw test

## Padrões de código
- Constructor injection (nunca @Autowired em field)
- DTOs no padrão CriarXRequest/CriarXResponse; nunca expor entity no controller
- Entities: @Getter/@Setter (nunca @Data); IDs nomeados como idCliente, idConta
- Bean Validation (@Valid) nos DTOs
- Exceptions customizadas + @ControllerAdvice
- Código em inglês, mensagens de erro em português
- Métodos curtos; extrair private methods quando crescer

## Domínio
- Simulador de transações ATM seguindo ISO 8583
- Detalhes de MTIs, campos e regras de negócio: ver docs/iso8583-context.md
- Transações: depósito, saque, transferência, PIX — todas com máquina de estados

## Verificação
- Rodar ./mvnw test após cada mudança e corrigir falhas antes de apresentar.