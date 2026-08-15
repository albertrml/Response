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
    implementation("br.com.arml.response:response-core:0.1.0")
    
    // Para UI (Jetpack Compose)
    implementation("br.com.arml.response:response-compose:0.1.0")
}
```

## 2. Uso Básico

### No Repositório
Use o builder `asResponse` para transformar chamadas suspensas em fluxos de estado:

```kotlin
fun fetchUser() = asResponse {
    api.getUser() // chamada suspend
}
```

### Na ViewModel
Consuma o fluxo e converta para um estado de UI:

```kotlin
val userState = repository.fetchUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Response.Loading)
```

### Na UI (Compose)
Renderize o resultado de forma simples com `ShowResults`:

```kotlin
val state by viewModel.userState.collectAsState()

state.ShowResults(
    loadingContent = { CircularProgressIndicator() },
    successContent = { user -> Text("Olá, ${user.name}") },
    failureContent = { error -> Text("Erro: ${error.message}") }
)
```
