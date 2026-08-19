# PRD-002: Ergonomia e Resiliência (The Fluent Update)

## 1. Problem Statement
Atualmente, a `Response API` funciona como um snapshot estático. Quando um fluxo transita de `Success` para um novo `Loading` (refresh) ou para `Failure`, os dados anteriores são descartados. Isso força a UI a exibir estados vazios ou "flashes" de carregamento, degradando a UX. Além disso, a manipulação de erros é baseada em exceções brutas, o que é verboso e acoplado à implementação.

## 2. Proposed Solution
Transformar a `Response` num motor de estados resiliente com suporte a **State Recovery** e **Semantic Error Reasoning**. Introduzir uma DSL fluída para manipulação de dados sem boilerplate.

## 3. User Stories
- **US1 (State Recovery)**: Como desenvolvedor, quero que os estados `Loading` e `Failure` carreguem o `previousData` para manter a UI populada durante atualizações ou falhas temporárias.
- **US2 (Error Reasoning)**: Como desenvolvedor, quero categorizar erros em razões semânticas (`Network`, `Server`, `Business`) para simplificar a tomada de decisão na UI.
- **US3 (Fluent Operators)**: Como desenvolvedor, quero transformar dados (`mapTo`), reagir a estados (`onSuccess`, `onFailure`) e recuperar fluxos (`onRecover`) usando uma API funcional e encadeável.

## 4. Technical Requirements & Constraints
- **Módulo `:core`**: Toda a lógica deve ser Pure Kotlin.
- **Imutabilidade**: Todas as novas propriedades (`previousData`, `reason`) devem ser `val`.
- **Covariância**: Manter o suporte a `Response<out T>`.
- **Compatibilidade Compose**: Garantir que as mudanças não quebrem o `compose-stability.conf`.

## 5. Proposed API Change (Draft)
```kotlin
sealed class Response<out T> {
    data class Loading<out T>(val previousData: T? = null) : Response<T>()
    data class Success<out T>(val data: T) : Response<T>()
    data class Failure<out T>(
        val exception: Exception,
        val reason: ErrorReason = ErrorReason.Unknown,
        val previousData: T? = null
    ) : Response<T>()
}
```

## 6. Success Metrics
- 100% de cobertura de testes nos novos operadores funcionais.
- Zero "recomposições fantasmas" validadas pelo Quality Gate de performance.
