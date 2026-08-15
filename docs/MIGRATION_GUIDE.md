# Guia de Migração

Este documento auxilia na atualização de versões legadas ou pré-lançamentos para a versão atual da API.

## v0.1.0 (Refatoração Multi-modular)

A versão 0.1.0 introduziu uma mudança estrutural significativa. Se você usava a API antes da separação em módulos, siga estes passos:

### 1. Mudança de Artefatos
Antigamente existia apenas um artefato. Agora você deve escolher entre o núcleo e a UI:
- **De:** `br.com.arml.core:response:x.y.z`
- **Para:**
    - `br.com.arml.response:response-core:0.1.0` (Lógica)
    - `br.com.arml.response:response-compose:0.1.0` (UI)

### 2. Renomeação de Pacotes
Os pacotes foram renomeados para evitar conflitos técnicos:

| Classe/Objeto | Antigo Pacote | Novo Pacote |
| :--- | :--- | :--- |
| `Response` | `br.com.arml.core.response` | `br.com.arml.response.core` |
| `ShowResults` | `br.com.arml.core.response.ui` | `br.com.arml.response.compose` |

### 3. Gradle (Version Catalog)
Se o seu projeto utiliza Gradle 7.4+, recomendamos usar o **Version Catalog** para gerenciar as dependências da Response API, garantindo que as versões do Core e Compose estejam sempre sincronizadas.
