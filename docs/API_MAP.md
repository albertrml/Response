# Mapa da API - Response

Este documento provê uma visão geral dos componentes, estados e relacionamentos da Response API.

## 1. Núcleo (Módulo :core)

### Estados (`Response<T>`)
| Estado | Tipo | Descrição |
| :--- | :--- | :--- |
| `Loading` | `data object` | Representa uma operação assíncrona em andamento. |
| `Success<T>` | `data class` | Contém o dado resultante (`result`) de uma operação bem-sucedida. |
| `Failure` | `data class` | Contém a exceção (`exception`) resultante de uma falha. |

### Construtores (`ResponseBuilders.kt`)
- `asResponse { suspend block }`: Cria um `Flow<Response<T>>` que emite `Loading` e depois o resultado.
- `runAsResponse { suspend block }`: Executa o bloco e retorna uma instância única de `Response<T>`.

### Extensões de Fluxo (`ResponseExtension.kt`)
- `Flow<T>.toResponseFlow()`: Converte um Flow comum em um Flow de Response.
- `Flow<Response<T>>.mapSuccess()`: Transforma o conteúdo do `Success` mantendo outros estados.
- `Response<T>.update(MutableStateFlow)`: Utilitário para atualizar estados MVI em ViewModels.

---

## 2. Interface de Usuário (Módulo :compose)

### Componentes (`ResponseComposables.kt`)
- `Response<T>.ShowResults(...)`: Composable que observa o estado e renderiza o conteúdo apropriado (`successContent`, `loadingContent`, `failureContent`). Oferece suporte a side-effects via `actionOnSuccess` e `actionOnFailure`.

---

## 3. Fluxo de Vida Típico
1. **Trigger**: Repositório ou UseCase inicia uma chamada (`asResponse`).
2. **Loading**: UI recebe `Loading` e exibe um shimmer ou progress bar.
3. **Completion**: O bloco suspend termina.
4. **Outcome**:
    - Sucesso -> UI recebe `Success` e renderiza os dados.
    - Erro -> UI recebe `Failure` e exibe mensagem de erro ou retry.
