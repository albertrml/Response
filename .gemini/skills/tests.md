# Testes de Alta Qualidade (Kotlin/Android)

## ✅ Bons Testes
Focam no **comportamento observável** através de interfaces públicas, não em detalhes de implementação.

```kotlin
// BOM: Testa o comportamento esperado pelo usuário/chamador
@Test
fun `quando adicionar item ao carrinho, o total deve ser atualizado`() {
    val cart = ShoppingCart()
    cart.add(Product("Anel", price = 100.0))
    
    assertEquals(100.0, cart.totalPrice)
}
```

**Características:**
- Descrevem **O QUE** o código faz, não **COMO**.
- Sobrevivem a refatorações internas (se a lógica de cálculo mudar, o teste continua válido).
- Usam apenas a API pública.

## ❌ Maus Testes
Acoplados à **estrutura interna** do código.

```kotlin
// RUIM: Testa se um método privado ou colaborador interno foi chamado
@Test
fun `checkout deve chamar processPayment no serviço de pagamento`() {
    val mockService = mockk<PaymentService>()
    val checkout = Checkout(mockService)
    
    checkout.process(cart)
    
    verify { mockService.processPayment(any()) } // Acoplamento de implementação
}
```

**Sinais de Alerta:**
- Mocking excessivo de colaboradores internos da mesma camada.
- Testar métodos `private`.
- Asserções sobre ordem de chamada ou número de vezes que um método interno foi invocado.
- O teste quebra ao renomear uma variável interna, mesmo que o resultado final seja o mesmo.

## 🧠 Regra de Ouro
Se você mudar a implementação interna de uma função e o teste quebrar (sem que o comportamento final tenha mudado), o seu teste está **mal projetado**. Teste o **contrato**, não o algoritmo.
