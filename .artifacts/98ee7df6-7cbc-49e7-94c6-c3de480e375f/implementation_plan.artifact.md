# Refatoração Multi-modular da API Response (v0.1.0)

Este plano detalha a transição da API Response de um projeto de módulo único para uma estrutura multi-modular profissional, utilizando Gradle Version Catalog e separando as responsabilidades de Core (Pure Kotlin) e UI (Compose).

## User Review Required

> [!IMPORTANT]
> **Renomeação de Pacotes:** Para evitar "Split Packages", os pacotes serão renomeados de `br.com.arml.core.response` para:
> - `br.com.arml.response.core` no módulo `:core`.
> - `br.com.arml.response.compose` no módulo `:compose`.

> [!NOTE]
> **Version Catalog:** Centralizaremos todas as dependências em `gradle/libs.versions.toml`.

## Proposed Changes

### Infraestrutura Gradle

#### [NEW] [libs.versions.toml](file:///E:/Github/Android/api/Response/gradle/libs.versions.toml)
Criação do catálogo central de versões.

#### [MODIFY] [settings.gradle.kts](file:///E:/Github/Android/api/Response/settings.gradle.kts)
Inclusão dos novos módulos `:core` e `:compose`.

#### [MODIFY] [build.gradle.kts](file:///E:/Github/Android/api/Response/build.gradle.kts)
Transformação em projeto raiz (container) e remoção de código fonte do diretório raiz.

---

### Módulo Core (:core)
*Pure Kotlin, focado na lógica da Response.*

#### [NEW] [build.gradle.kts](file:///E:/Github/Android/api/Response/core/build.gradle.kts)
Configuração do módulo Kotlin puro.

#### [NEW] [Response.kt](file:///E:/Github/Android/api/Response/core/src/main/kotlin/br/com/arml/response/core/Response.kt)
Migração da classe base com o novo pacote.

---

### Módulo Compose (:compose)
*Integração com Jetpack Compose.*

#### [NEW] [build.gradle.kts](file:///E:/Github/Android/api/Response/compose/build.gradle.kts)
Configuração do módulo com dependência do Compose e do módulo `:core`.

#### [NEW] [compose-stability.conf](file:///E:/Github/Android/api/Response/compose/compose-stability.conf)
Configuração do compilador do Compose para tratar a `Response` do core como estável.

#### [NEW] [ResponseComposables.kt](file:///E:/Github/Android/api/Response/compose/src/main/kotlin/br/com/arml/response/compose/ResponseComposables.kt)
Migração dos composables com o novo pacote.

---

### Limpeza
#### [DELETE] pasta `src/` da raiz.

## Verification Plan

### Automated Tests
1.  Executar `./gradlew :core:test` para validar a lógica de estados.
2.  Executar `./gradlew :compose:assemble` para validar a integração com Compose.
3.  Verificar se o build de publicação (`publishToMavenLocal`) funciona para ambos os artefatos.

### Manual Verification
- Validar via IDE que não existem erros de importação após a renomeação dos pacotes.
