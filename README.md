# Response API

![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/SeuUsuario/NomeDoRepositorio/publish.yml?branch=main)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/SeuUsuario/NomeDoRepositorio)
![License](https://img.shields.io/github/license/SeuUsuario/NomeDoRepositorio)

## 🚀 Visão Geral

A **Response API** é uma biblioteca Kotlin leve e flexível projetada para simplificar o gerenciamento de estados assíncronos (carregamento, sucesso, falha) em aplicações Kotlin Multiplatform (JVM, Android, etc.). Inspirada em padrões comuns de UI e arquitetura, ela oferece uma maneira robusta e expressiva de lidar com operações que podem resultar em dados, erros ou um estado de carregamento contínuo.

Com esta API, você pode:
- Representar claramente o estado de uma operação assíncrona.
- Transformar `Flow`s comuns em `Flow`s de `Response`.
- Mapear e reagir a diferentes estados de forma segura e concisa.
- Integrar facilmente com Jetpack Compose para uma experiência de UI reativa.

## ✨ Recursos

- **`Response<T>` Sealed Class**: Uma classe selada que encapsula os estados `Success<T>`, `Failure(Exception)` e `Loading`.
- **`asResponse()` Builder**: Converte uma função `suspend` em um `Flow<Response<T>>`, emitindo `Loading` antes da execução e `Success` ou `Failure` após.
- **`runAsResponse()` Helper**: Executa uma função `suspend` e retorna um `Response<T>` diretamente, ideal para operações únicas.
- **`toResponseFlow()` Extension**: Transforma qualquer `Flow<T>` em um `Flow<Response<T>>`, adicionando automaticamente os estados de `Loading` e `Failure`.
- **`mapSuccess()` Extension**: Permite transformar o valor de `Success` dentro de um `Flow<Response<T>>` sem afetar os estados de `Loading` ou `Failure`.
- **`update()` Extension**: Facilita a atualização de um `MutableStateFlow` da UI com base no estado atual de um `Response`.
- **`until()` Flow Operator**: Um operador de `Flow` que emite valores até que um predicado seja satisfeito, incluindo o valor que satisfez o predicado, e então cancela a coleta.
- **`ResponseComposables.ShowResults()`**: Um `@Composable` para ‘Jetpack’ Compose que reage aos estados de `Response`, exibindo diferentes conteúdos para `Loading`, `Success` e `Failure`, e permitindo ações de efeito colateral.

## 📦 Instalação

A **Response API** pode ser consumida via **GitHub Packages**.

### 1. Configurar o GitHub Packages no seu `settings.gradle.kts`

Adicione o repositório do GitHub Packages ao seu `settings.gradle.kts` (ou `build.gradle.kts` do módulo raiz):

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SeuUsuario/NomeDoRepositorio")
            credentials {
                // Para repositórios públicos, você ainda pode precisar de um ‘token’
                // se o Gradle não conseguir autenticar automaticamente.
                // Para repositórios privados, um PAT com permissão 'read:packages' é obrigatório.
                username = System.getenv("GITHUB_ACTOR") // Ou project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") // Ou project.findProperty("gpr.key") as String?
            }
        }
    }
}
```
**Importante**: Substitua `SeuUsuario` e `NomeDoRepositorio` pelos seus dados reais. Para repositórios privados, você precisará de um Personal Access Token (PAT) do GitHub com permissão `read:packages`.

### 2. Adicionar a dependência no seu `build.gradle.kts`

No `build.gradle.kts` do seu módulo (app ou library):

```kotlin
// build.gradle.kts (do seu módulo)
dependencies {
    implementation("br.com.arml.core:response:1.0.0") // Verifique a versão mais recente
}
```

## 💡 Casos de Uso

### 1. Operações de Rede (Single-shot)

```kotlin
// Exemplo de uma função de API
suspend fun fetchUserData(): User {
    delay(1000) // Simula uma chamada de rede
    if (System.currentTimeMillis() % 2 == 0) {
        return User("Alice", 30)
    } else {
        throw IOException("Network error")
    }
}

// Usando runAsResponse em um ViewModel ou Presenter
suspend fun loadUser() {
    val userResponse = runAsResponse { fetchUserData() }
    when (userResponse) {
        is Response.Loading -> println("Loading user...") // Não será emitido em runAsResponse
        is Response.Success -> println("User loaded: ${userResponse.result.name}")
        is Response.Failure -> println("Failed to load user: ${userResponse.exception.message}")
    }
}

data class User(val name: String, val age: Int)
```

### 2. Operações de Rede (Fluxo Contínuo)

```kotlin
// Exemplo de um Flow que busca dados
fun observeUserUpdates(): Flow<User> = flow {
    while (true) {
        delay(2000)
        emit(User("Bob", (0..100).random()))
    }
}

// Usando toResponseFlow em um ViewModel
class MyViewModel : ViewModel() {
    private val _userState = MutableStateFlow<Response<User>>(Response.Loading)
    val userState: StateFlow<Response<User>> = _userState

    init {
        viewModelScope.launch {
            observeUserUpdates()
                .toResponseFlow()
                .collect { response ->
                    _userState.value = response
                }
        }
    }
}
```

### 3. Reagindo na UI com Jetpack Compose

```kotlin
@Composable
fun UserScreen(viewModel: MyViewModel) {
    val userResponse by viewModel.userState.collectAsState()

    userResponse.ShowResults(
        loadingContent = { CircularProgressIndicator() },
        successContent = { user -> Text("Welcome, ${user.name}!") },
        failureContent = { exception -> Text("Error: ${exception.message}") },
        actionOnSuccess = { user -> /* Log analytics, navigate */ },
        actionOnFailure = { exception -> /* Show a SnackBar */ }
    )
}
```

### 4. Transformando Dados de Sucesso

```kotlin
// Suponha que você tenha um Flow<Response<User>>
val userResponseFlow: Flow<Response<User>> = /* ... */

// Mapeando para um Flow<Response<String>> (apenas o nome do usuário)
val userNameResponseFlow: Flow<Response<String>> = userResponseFlow.mapSuccess { user ->
    user.name.uppercase()
}

// Coletando
userNameResponseFlow.collect { response ->
    when (response) {
        is Response.Success -> println("User name: ${response.result}")
        is Response.Loading -> println("Loading name...")
        is Response.Failure -> println("Failed to get name: ${response.exception.message}")
    }
}
```

## 🤝 Contribuição

Contribuições são bem-vindas! Se você encontrar um bug ou tiver uma ideia para uma nova funcionalidade, por favor, abra uma issue ou envie um Pull Request.

## 📄 Licença

Este projeto está licenciado sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---
**Nota**: Lembre-se de substituir `SeuUsuario` e `NomeDoRepositorio` nos badges e URLs de instalação pelos valores corretos do seu repositório GitHub. Crie também o arquivo `LICENSE` na raiz do projeto com o texto da licença MIT.
