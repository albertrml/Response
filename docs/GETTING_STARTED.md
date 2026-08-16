# Guia de Início Rápido (Getting Started)

A Response API facilita o gerenciamento de estados assíncronos no Android usando Kotlin Coroutines e Jetpack Compose.

## 1. Instalação

Adicione o repositório do GitHub Packages ao seu `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/albertrml/Response")
            credentials {
                username = "SEU_GITHUB_USER"
                password = "SEU_GITHUB_TOKEN"
            }
        }
    }
}
```

Adicione a dependência no seu módulo `build.gradle.kts`:

```kotlin
dependencies {
    // Para lógica pura (ViewModels/Repositories)
    implementation("br.com.arml.response:response-core:0.1.2")
    
    // Para UI (Jetpack Compose)
    implementation("br.com.arml.response:response-compose:0.1.2")
}
```

## 2. Uso Básico

### No Repositório
Use o builder `asResponseFlow` para transformar chamadas suspensas em fluxos de estado inteligentes:

```kotlin
fun fetchUser() = asResponseFlow {
    api.getUser() // chamada suspend
}
```

### Na ViewModel
Consuma o fluxo, adicione resiliência com `.withCache()` e converta para um estado de UI:

```kotlin
val userState = repository.fetchUser()
    .withCache() // Mantém o cache durante atualizações
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Response.Loading())
```

### Na UI (Compose)
Renderize o resultado e reaja a falhas de forma semântica:

```kotlin
val state by viewModel.userState.collectAsState()

state.ShowResults(
    loadingContent = { cache -> 
        // Se houver cache, você pode mostrar a lista antiga com um loading sutil
        if (cache != null) UserList(cache, isLoading = true) else CircularProgressIndicator()
    },
    successContent = { user -> UserList(user) },
    failureContent = { error, reason, cache -> 
        if (reason is ErrorReason.Network) {
            ShowToast("Sem conexão. Exibindo dados antigos.")
        }
        cache?.let { UserList(it) }
    }
)
```
