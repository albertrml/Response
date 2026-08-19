Segue o passo a passo para interagir com Gemini

# Passo 1: O Pedido (o seu task.md)
Este é o rascunho da sua ideia. Você cria-o na pasta `.gemini/tasks/`.
- Nome: `001-fluent-api-response.md`
- Ação: Você diz-me: "Gemini, ative a skill grill-me sobre a tarefa em `.gemini/tasks/001-fluent-api-response.md`."
- O que eu faço: Eu leio o arquivo e começo a te "grelhar" com perguntas críticas para garantir que não há buracos na lógica.

# Passo 2: O Contrato (write-a-prd)
Após o debate do Passo 1, consolidamos tudo num PRD (Product Requirements Document).
- Ação: Eu uso a skill write-a-prd e gero um arquivo oficial em `.gemini/docs/prd-001.md`.
- Objetivo: Este documento é a "lei" da funcionalidade. Ele descreve User Stories e restrições técnicas.

# Passo 3: O Plano de Ataque (prd-to-issues)
Aqui quebramos o PRD em tarefas técnicas acionáveis.
- Ação: Eu atualizo o seu `.gemini/roadmap/v0.1.x.md` ou crio uma lista de issues técnicas.
- O que acontece: Transformamos "Como usuário, quero mapear erros" em "Implementar extensão .mapError() no Response.kt".

# Passo 4: A Execução (Implementation)
É aqui que o "suor" acontece usando o TDD.
- Ação: Eu pego a primeira issue do Passo 3 e coloco no `current_task.md`.
- O que eu faço: Sigo o loop RED -> GREEN -> REFACTOR usando Gradle e análise de arquivo.