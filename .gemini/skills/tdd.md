---
name: tdd
description: Test-driven development focado em Android/Kotlin. Use para construir features via loop Red-Green-Refactor usando Gradle.
---

# TDD Operacional (Gemini OS)

Este protocolo define como o agente deve executar o loop de TDD usando as ferramentas do Android Studio.

## 🔄 O Loop de Execução

### 🔴 Passo 1: RED (Falha Real)
1.  Identifique a "Seam" (Interface pública) a ser testada.
2.  Crie o arquivo de teste em `src/test` (Unitário) ou `src/androidTest` (Instrumentado).
3.  **Obrigatório**: Execute `./gradlew :module:test` usando `gradle_build`.
4.  **Ação**: Leia o resultado. O loop só avança se o teste FALHAR por razões lógicas (AssertionError), não por erro de compilação (a menos que a interface ainda não exista).

### 🟢 Passo 2: GREEN (Implementação Mínima)
1.  Escreva o código estritamente necessário para passar o teste.
2.  Use `analyze_file` para garantir que não há erros de sintaxe.
3.  Execute o build novamente. Se passar, marque como `[x]` no `current_task.md`.

### 🔵 Passo 3: REFACTOR (Limpeza)
1.  Melhore o código sem alterar o comportamento.
2.  Execute os testes novamente para garantir que nada quebrou.
3.  **Cobertura**: Verifique se a cobertura de testes da nova funcionalidade atinge pelo menos 95%.

## 🛠️ Ferramentas Obrigatórias
- **`gradle_build`**: Sempre use para validar o estado. Nunca assuma que o código funciona.
- **`read_logcat`**: Use se o teste instrumentado falhar sem mensagem clara.
- **`current_task.md`**: Atualize o checklist a cada mudança de estado (Red -> Green).

## 🚫 Proibições
- Não escreva mais de um teste por vez.
- Não implemente lógica que não foi pedida pelo teste atual.
- Não ignore Warnings do `analyze_file` durante o Refactor.
