---
name: write-a-prd
description: Gera um PRD estruturado a partir de uma ideia ou discussão.
---

# Protocolo PRD (Gemini OS)

Use este protocolo para formalizar uma ideia após a fase de 'grill-me'.

## 📝 Processo de Escrita
1.  **Contexto**: Leia o arquivo de task original em `.gemini/tasks/` e o histórico da discussão.
2.  **Geração**: Crie o arquivo em `.gemini/docs/PRD-[ID].md`.
3.  **Estrutura Obrigatória**:
    - **Problem**: Qual a dor do desenvolvedor?
    - **Solution**: Como essa feature resolve?
    - **User Stories**: "Como [persona], eu quero [feature] para [benefício]".
    - **Technical Constraints**: Ex: "Pure Kotlin", "Sem dependências de terceiros".
    - **Success Metrics**: Ex: "100% de cobertura de testes na lógica de mapeamento".

## 🛠️ Próximo Passo Automático
Após escrever o PRD, sugira ao usuário a quebra em tarefas técnicas para o Roadmap.
