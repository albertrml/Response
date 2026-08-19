# PRD-005: Testability DSL (The Tester Update)

## 1. Problem Statement
Atualmente, os desenvolvedores que utilizam a Response API em suas ViewModels ou Repositórios precisam realizar casts manuais (`as Response.Success`) ou usar blocos `when` extensos em seus testes unitários para validar os estados. Isso torna o código de teste verboso, difícil de ler e propenso a erros de asserção silenciosos.

## 2. Proposed Solution
Criar um módulo dedicado `:test` que fornece uma DSL de asserções fluídas e utilitários de simulação. O objetivo é permitir que a validação de um fluxo de `Response` seja feita numa única linha legível.

## 3. User Stories
- **US1 (Fluent Assertions)**: Como desenvolvedor, quero fazer `response.assertSuccess { data -> ... }` para validar o conteúdo de um sucesso sem fazer cast manual.
- **US2 (Error Validation)**: Como desenvolvedor, quero validar a razão de uma falha de forma semântica: `response.assertFailure(ErrorReason.Network)`.
- **US3 (State Sequence)**: Como desenvolvedor, quero validar que um `Flow` emitiu a sequência correta de estados (ex: Loading -> Success) de forma simplificada.
- **US4 (Mocking Helpers)**: Como desenvolvedor, quero criar instâncias de `Success` ou `Failure` com metadados padrão rapidamente para usar em mocks de Repositórios.

## 4. Technical Requirements & Constraints
- **Módulo Isolado**: Criar o módulo `:test` (Pure Kotlin) para evitar dependências de teste no código de produção.
- **Integração JUnit**: As asserções devem lançar `AssertionError` compatíveis com JUnit 4/5.
- **Extensibilidade**: A DSL deve permitir validar tanto o dado (`T`) quanto os metadados (`ResponseMetadata`).

## 5. Proposed API Change (Draft)
```kotlin
// Exemplo de uso no teste do usuário
viewModel.state.asResponseFlow().assertSuccess { users ->
    assertEquals(3, users.size)
}

response.assertFailure { error, reason, cache ->
    assertTrue(reason is ErrorReason.Network)
}
```

## 6. Success Metrics
- Cobertura de 100% da própria DSL de teste.
- Redução de ~50% nas linhas de código dedicadas a asserções de Response nos projetos consumidores.
