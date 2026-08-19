---
name: prd-to-issues
description: Quebra um PRD em tarefas técnicas acionáveis dentro do Roadmap e Task Atual.
---

# PRD para Tasks Técnicas

Este protocolo define como transformar um documento de requisitos (PRD) num plano de ataque técnico.

## 🔄 O Processo

### 1. Localização
Localize o PRD em `.gemini/docs/PRD-[ID].md`. Se não existir, use a skill `write-a-prd` primeiro.

### 2. Decomposição em "Tracer Bullets"
Quebre o PRD em fatias verticais. Cada fatia deve ser:
- **Independente**: Pode ser testada e validada sozinha.
- **Completa**: Atravessa todas as camadas (Data -> ViewModel -> UI).
- **Pequena**: Deve ser realizável numa única sessão de codificação.

### 3. Atualização do Ecossistema .gemini
Para cada fatia identificada:
1.  **Roadmap**: Adicione os itens na versão correspondente em `.gemini/roadmap/`.
2.  **Current Task**: Popule o arquivo `.gemini/tasks/current_task.md` com a primeira fatia da lista para iniciarmos o TDD.

## 📋 Formato da Task Técnica
Toda task gerada deve seguir este padrão:
- `[ ] [Módulo] Título Curto da Tarefa`
- Link para a User Story relacionada.
- Critério de Aceite: "O teste X deve passar com o cenário Y".

## 🚫 Regras
- Não crie tarefas puramente de UI sem lógica de estado.
- Não crie tarefas gigantes ("Implementar a busca inteira"). Quebre em "Criar motor de normalização", depois "Integrar busca no State", etc.
