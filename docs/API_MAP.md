# Mapa da API - Response

Este documento provê uma visão geral dos componentes, estados e relacionamentos da Response API.

## 1. Núcleo (Módulo :core)

### Estados (`Response<T>`)
| Estado | Tipo | Descrição |
| :--- | :--- | :--- |
| `Loading<T>` | `data class` | Operação em andamento. Pode conter `previousData` (cache) e `metadata`. |
| `Success<T>` | `data class` | Sucesso. Contém o dado resultante (`result`) e `metadata`. |
| `Failure<T>` | `data class` | Falha. Contém o `error: Throwable`, `reason: ErrorReason`, `previousData` e `metadata`. |

### Metadados e Políticas (`ResponseMetadata.kt`)
A Response API utiliza um sistema de **Políticas (Policies)** para carregar contexto sem poluir seus modelos.
- `ResponseMetadata`: Envelope que carrega o `timestamp`, dados `extra` e uma lista de `policies`.
- `ResponsePolicy`: Interface marcadora para qualquer estratégia (Paginação, Sync, etc).
- `PagePaginationPolicy`: Implementação para paginação baseada em páginas (`currentPage`, `hasMore`).
- `CursorPaginationPolicy`: Estratégia baseada em cursores (`nextCursor`, `hasMore`).
- `SyncPolicy`: Estratégia para sincronização de dados (`lastVersion`, `isDelta`).

### Construtores (`ResponseBuilders.kt`)
- `asResponseFlow { block }`: Cria um `Flow<Response<T>>` que emite `Loading` e depois o resultado.
- `Flow<T>.asResponseFlow()`: Extensão que converte um fluxo comum em um fluxo de `Response`.
- `asResponse { block }`: Função `suspend` que executa o bloco e retorna uma instância única de `Response<T>`.

### Operadores e Extensões
#### Manipulação de Estado (`Response<T>`)
- `.onSuccess { data -> ... }`: Reage ao sucesso.
- `.onFailure { error, reason, cache -> ... }`: Reage à falha com contexto semântico.
- `.onLoading { cache -> ... }`: Reage ao carregamento.
- `.onRecover()`: Tenta recuperar o estado de erro usando o cache (`previousData`).
- `.mapTo { ... }`: Transforma o dado interno (Success) e o cache (Loading/Failure).
- `.mapError { ... }`: Transforma o `Throwable` interno de um erro.
- `.update(MutableStateFlow)`: Utilitário para integração com MVI.

#### Gerenciamento de Fluxo (`Flow<Response<T>>`)
- `.mapSuccess { ... }`: Transforma o conteúdo do `Success` mantendo a estrutura do fluxo.
- `.withCache()`: Engine de **State Recovery** que persiste o último sucesso em estados subsequentes.

---

## 2. Interface de Usuário (Módulo :compose)

### Componentes (`ResponseComposables.kt`)
- `Response<T>.ShowResults(...)`: Composable que observa o estado e renderiza o conteúdo apropriado. Oferece suporte a side-effects disparados uma única vez via `actionOnSuccess` e `actionOnFailure`.

---

## 3. Fluxo de Vida com State Recovery
1. **Initial Trigger**: Chamada a `asResponseFlow`. Emite `Loading(null)`.
2. **Success**: Recebe dados. Emite `Success(data, metadata)`.
3. **Refresh**: Nova chamada com `.withCache()`. Emite `Loading(data, metadata)`.
4. **Failure**: Erro. Emite `Failure(error, Network, data, metadata)`.
5. **Recovery**: Chamada a `.onRecover()`. Emite `Success(data, metadata)`.
