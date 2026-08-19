# Quando e Como usar Mocks (Kotlin/MockK)

Use Mocks apenas nas **fronteiras do sistema**.

## ✅ Onde usar Mocks:
- **APIs Externas**: Retrofit services, chamadas de rede.
- **Bancos de Dados**: (Se não for possível usar um banco em memória/Room Test).
- **Tempo e Aleatoriedade**: Providers de `Clock` ou `UUID`.
- **Sensores de Hardware**: GPS, Bluetooth, Câmera.

## ❌ Não use Mocks para:
- **As suas próprias Data Classes**: Crie instâncias reais.
- **Classes Utilitárias**: Use a lógica real.
- **Colaboradores da mesma camada**: Se você está testando um `ViewModel`, não mocke o `Reducer` ou o `State` (use instâncias reais para um teste "Social").

## 🛠️ Design para Testabilidade (Dependency Injection)
Sempre passe as dependências externas no construtor. Isso facilita o uso de Mocks reais ou Fakes.

```kotlin
// BOM: Fácil de testar/mockar
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String) = repository.getUser(id)
}

// RUIM: Impossível de mockar sem "sujar" o código
class GetUserUseCase() {
    private val repository = UserRepositoryImpl() // Acoplamento rígido
    suspend operator fun invoke(id: String) = repository.getUser(id)
}
```

## 🏆 Preferência: Fakes > Mocks
Sempre que possível, prefira criar uma implementação "Fake" em vez de um Mock dinâmico. 
- **Exemplo**: `FakeUserRepository` que armazena itens em um `HashMap` local. É mais rápido, previsível e tipado.
