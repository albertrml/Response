# Task Atual: Início do Marco da Ergonomia (v0.1.2)
**Status**: Planejamento
**Objetivo**: Implementar operadores funcionais fluídos (map, mapError, onSuccess, onFailure) e tipagem de erro aprimorada.

## Checklist
- [x] Refinar design dos operadores funcionais (Grill-me)
- [x] Criar PRD da v0.1.2 (docs/PRD-002-fluent-api-ergonomics.md)
- [x] Definir estrutura do ErrorReason (Network, Server, Business, Unknown)
- [x] Implementar State Recovery e Mudança de Assinatura (Response<T>)
- [x] Implementar Operadores Fluídos (map, mapError, onSuccess, onFailure, onRecover)
- [x] Atualizar Builders (asResponse, asResponseFlow) com suporte a cache
- [x] Implementar extensão de fluxo withCache() para State Recovery automático
- [x] Polimento e refatoração de design (Nomes simétricos e uso de when)
