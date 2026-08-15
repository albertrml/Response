# API Design Guidelines - Response API

Este documento descreve os princípios de Engenharia de Software e padrões de design aplicados na construção da Response API.

## 1. Imutabilidade por Padrão
Toda estrutura de dados na API deve ser imutável.
- Use `sealed class` para estados e `data class` ou `data object` para implementações.
- Propriedades devem ser declaradas com `val`.
- **Por que?** Garante previsibilidade em ambientes multi-thread e facilita a integração com o motor de recomposição do Jetpack Compose.

## 2. Tipagem Genérica e Covariância
A classe `Response<out T>` utiliza o modificador `out` para permitir covariância.
- **Exemplo:** `Response<String>` pode ser atribuído a uma variável do tipo `Response<Any>`.
- **Por que?** Aumenta a flexibilidade da biblioteca ao lidar com hierarquias de modelos.

## 3. Abordagem Funcional
A API prioriza operadores funcionais em vez de verificações de tipo manuais (`is Success`).
- Use `fold` para processar todos os estados de forma exaustiva.
- Use `mapTo` para transformações de dados sem sair do fluxo da `Response`.
- **Por que?** Reduz boilerplate e evita erros de lógica (como esquecer o estado de erro).

## 4. Integração com Coroutines e Flow
A biblioteca foi desenhada para ser o elo entre a camada de Dados (Data) e a UI.
- Extensões como `toResponseFlow()` facilitam a conversão de fluxos brutos.
- `asResponse { ... }` gerencia automaticamente o ciclo de vida (Loading -> Success/Failure).

## 5. Separação de Preocupações (SoC)
- O motor lógico reside no módulo `:core` (Pure Kotlin).
- Componentes visuais residem no módulo `:compose`.
- **Regra de Ouro:** O módulo `:core` nunca deve depender de frameworks de UI.

## 6. Nomenclatura Semântica
- Implementações de estado devem ser curtas e diretas: `Success`, `Failure`, `Loading`.
- Operadores que transformam o dado interno devem seguir o padrão `map...` (ex: `mapTo`, `mapSuccess`).
