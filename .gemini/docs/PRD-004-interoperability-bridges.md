# PRD-004: Interoperabilidade e Pontes (The Bridge Update)

## 1. Problem Statement
Atualmente, a Response API exige que o desenvolvedor realize o mapeamento manual entre os resultados de bibliotecas de rede populares (Retrofit, Ktor) e os estados da `Response`. Isso gera boilerplate repetitivo nos Repositórios e inconsistência no mapeamento de erros.

## 2. Proposed Solution
Fornecer um conjunto de extensões de "Ponte" (Bridges) que convertam tipos nativos do Kotlin e de frameworks de rede diretamente para a hierarquia da `Response<T>`, mapeando automaticamente códigos HTTP e exceções para `ErrorReason` semânticos.

## 3. User Stories
- **US1 (Standard Bridge)**: Como desenvolvedor, quero converter um `Result<T>` nativo do Kotlin em uma `Response<T>` para aproveitar a DSL fluída da biblioteca.
- **US2 (Retrofit Bridge)**: Como desenvolvedor, quero converter um `retrofit2.Response<T>` diretamente em `Response<T>`, onde códigos 4xx e 5xx sejam automaticamente mapeados para `ErrorReason.Client` e `ErrorReason.Server`.
- **US3 (Ktor Bridge)**: Como desenvolvedor, quero que as chamadas do Ktor (HttpResponse) possam ser encapsuladas em `Response<T>` de forma transparente.
- **US4 (Combined Flow)**: Como desenvolvedor, quero combinar múltiplos fluxos de `Response` (ex: zip ou combine) de forma que o estado resultante reflita a saúde de todos os fluxos envolvidos.

## 4. Technical Requirements & Constraints
- **Multimodule Awareness**: As pontes de Retrofit e Ktor não devem poluir o módulo `:core`. Elas devem ser extensões opcionais ou viver em subpacotes/módulos específicos para evitar dependências pesadas e desnecessárias no núcleo.
- **Pure Kotlin Core**: O núcleo permanece 100% livre de frameworks.
- **Autorreasoning**: O mapeamento de `ErrorReason` deve ser inteligente (ex: detetar falha de conexão e atribuir `ErrorReason.Network`).

## 5. Success Metrics
- Redução de ~60% no boilerplate de Repositórios que usam Retrofit/Ktor com a Response API.
- 100% de cobertura de testes nos conversores.
