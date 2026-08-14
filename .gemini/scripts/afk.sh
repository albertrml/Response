#!/bin/bash
# AFK (Away From Keyboard) - Script de Verificação de Saúde do Projeto
# Use este script para garantir que o projeto está estável antes de grandes mudanças ou após implementações.

echo "🔍 Iniciando Verificação de Saúde (Health Check)..."

# 1. Limpeza básica (opcional, remova se o build estiver lento)
# ./gradlew clean

# 2. Build e Teste do Projeto
echo "📦 Verificando projeto Response..."
./gradlew test assemble
if [ $? -ne 0 ]; then
    echo "❌ Erro no build ou testes. Abortando."
    exit 1
fi

echo "✅ Projeto saudável e pronto para o trabalho!"
