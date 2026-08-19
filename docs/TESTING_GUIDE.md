# Guia de Testes (Testing Guide)

A Response API fornece um módulo dedicado `:test` com uma DSL fluída para facilitar a validação de estados em testes unitários.

## 1. Configuração

Adicione o módulo de teste como `testImplementation`:

```kotlin
dependencies {
    testImplementation("br.com.arml.response:response-test:0.1.5")
}
```

## 2. Fluent Assertions

Em vez de realizar casts manuais (`as Response.Success`), use as extensões de asserção para um código mais limpo e mensagens de erro descritivas.

### Validando Sucesso
A função `assertSuccess` valida o estado e fornece acesso direto aos dados e metadados.

```kotlin
@Test
fun `should fetch user successfully`() = runTest {
    val response = repository.getUser().asResponse()
    
    response.assertSuccess { data, metadata ->
        assertEquals("Albert", data.name)
        assertNotNull(metadata?.timestamp)
    }
}
```

### Validando Falhas
A função `assertFailure` permite validar a razão semântica do erro.

```kotlin
@Test
fun `should return network error`() = runTest {
    val response = repository.getUser() // Simula erro de rede
    
    response.assertFailure(expectedReason = ErrorReason.Network) { error, reason, cache, _ ->
        assertTrue(error is IOException)
        assertNull(cache) // Garante que não havia cache
    }
}
```

### Validando Loading
Útil para validar estados iniciais ou fluxos de refresh.

```kotlin
@Test
fun `should start with loading`() {
    val state = Response.Loading(previousData = "Old Data")
    
    state.assertLoading { cache, _ ->
        assertEquals("Old Data", cache)
    }
}
```

## 3. Mock Helpers

Utilize os helpers para criar instâncias de `Response` rapidamente ao configurar os seus mocks (MockK, Mockito, etc.).

```kotlin
// Em seu setup de teste
coEvery { repository.fetchUsers() } returns successResponse(listOf(user1, user2))

coEvery { repository.fetchUsers() } returns failureResponse("API Down", ErrorReason.Server(500))

coEvery { repository.fetchUsers() } returns networkErrorResponse()
```

## 4. Testando Flows (com Turbine)

A Response API funciona perfeitamente com a biblioteca [Turbine](https://github.com/cashapp/turbine) para testar sequências de estados.

```kotlin
@Test
fun `flow should emit loading then success`() = runTest {
    repository.fetchUsersFlow().test {
        awaitItem().assertLoading()
        awaitItem().assertSuccess { users, _ ->
            assertEquals(2, users.size)
        }
        awaitComplete()
    }
}
```
