---
name: architecture
description: Padrões de arquitetura para a API Response.
---

# Manual de Arquitetura (Elite Android)

Toda a implementação deve seguir estes dogmas arquiteturais.

## 1. Módulos e Isolamento
- **`:core`**: PURE KOTLIN. Proibido `import android.*`. Sem dependências de UI. Contém o motor da `Response<T>`.
- **`:compose`**: UI Framework. Foco em estabilidade do Compose (`@Stable`, `@Immutable`) e componentes de UI que consomem a `Response`.
- **`Models`**: Devem ser imutáveis e preferencialmente `data classes`.

## 2. Jetpack Compose (Regras de Ouro)
- **Hoisting**: O estado deve subir, os eventos devem descer.
- **Stability**: Toda a classe de estado deve ser anotada com `@Stable` ou `@Immutable`.
- **Derived State**: Use `derivedStateOf` para cálculos que dependem de outros estados (como paginação e progresso de scroll).
- **Skippable**: Use a ferramenta `task` para gerar relatórios de métricas do compilador e garantir que os componentes são skippables.

## 3. Fluxo de Dados (MVI)
- **Unidirecional**: State -> UI -> Event -> Reducer -> State.
- **Efeitos de Lado**: Use `Channels` ou `SharedFlow` para efeitos únicos (Toasts, Navegação).
- **Response**: Use o wrapper `Response<T>` para gerenciar o ciclo de vida de toda a carga assíncrona.

## 4. Naming Convention
- **Interfaces de Estado**: `Response`, `State`.
- **Implementações**: `Success`, `Loading`, `Failure`.
- **Extensões**: `mapTo()`, `fold()`, `asUiState()`.
