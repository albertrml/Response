#!/bin/bash
# ONCE - Script de Setup Inicial / Sincronização
# Execute uma vez ao clonar o repo ou ao mudar de branch principal.

echo "🚀 Configurando ambiente de desenvolvimento..."

# Garante permissão de execução para o gradlew
chmod +x gradlew

# Sincroniza e baixa dependências
echo "🔄 Sincronizando dependências do Gradle..."
./gradlew help --refresh-dependencies

# Verifica se o diretório de docs existe
if [ ! -d ".gemini/docs" ]; then
    mkdir -p .gemini/docs
    echo "📁 Diretório .gemini/docs criado."
fi

echo "✨ Setup concluído com sucesso!"
