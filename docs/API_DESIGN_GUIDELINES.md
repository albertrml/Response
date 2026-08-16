# API Design Guidelines - Response API

Este documento descreve os princípios de Engenharia de Software e padrões de design aplicados na construção da Response API.

## 1. Imutabilidade e Sealed Hierarchy
Toda estrutura de dados na API deve ser imutável para garantir previsibilidade e performance no Compose.
- `Success`, `Loading` e `Failure` são `data classes`.
- **Por que?** Facilita o uso do método `.copy()` e garante que o compilador do Compose identifique mudanças de estado com precisão.

## 2. State Recovery (Princípio da Continuidade)
Diferente de wrappers comuns, a Response API foca na continuidade da experiência do usuário.
- Estados de transição (`Loading`) e erro (`Failure`) carregam o `previousData`.
- **Regra:** Nunca descarte um dado válido a menos que um novo dado de sucesso chegue.

## 3. Semantic Error Reasoning
Evitamos o acoplamento da UI com tipos de `Exception`.
- A API mapeia falhas para `ErrorReason` (`Network`, `Server`, `Business`, etc).
- **Por que?** Permite que a camada de UI tome decisões de negócio (ex: retry, fallback) sem precisar conhecer bibliotecas de rede ou banco de dados.

## 4. Robustez com Throwable
A biblioteca utiliza `Throwable` como base para todos os erros.
- Capturamos e propagamos a causa raiz de qualquer falha técnica ou lógica.
- **Vantagem:** Compatibilidade total com a Standard Library do Kotlin e Coroutines.

## 5. Abordagem Funcional e Fluída
Priorizamos operadores funcionais que evitam o "Pyramid of Doom" de blocos `when`.
- Operadores encadeáveis: `.onSuccess`, `.onFailure`, `.onRecover`.
- Transformações inteligentes: `.mapTo` transforma o sucesso e o cache simultaneamente.

## 6. Nomenclatura Simétrica
Seguimos os padrões do Kotlin para facilitar a descoberta:
- `asResponse`: Operação única (One-shot).
- `asResponseFlow`: Operação contínua (Stream).
- `withCache`: Ativação de memória no fluxo.
