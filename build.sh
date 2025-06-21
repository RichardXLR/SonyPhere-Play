#!/bin/bash

# Script de build otimizado para evitar travamentos
# Configurações de ambiente

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Configurações de memória otimizadas
export GRADLE_OPTS="-Xmx4096m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
export KOTLIN_DAEMON_OPTS="-Xmx2048m"

echo "🚀 Iniciando build otimizado do StreamTune..."
echo "📦 JAVA_HOME: $JAVA_HOME"
echo "💾 Configurações de memória aplicadas"

# Limpar cache se necessário
if [ "$1" == "clean" ]; then
    echo "🧹 Limpando cache..."
    ./gradlew clean
fi

# Build do APK
echo "🔨 Compilando APK..."
./gradlew assembleUniversalRelease

if [ $? -eq 0 ]; then
    echo "✅ Build concluído com sucesso!"
    echo "📱 APK gerado em: app/build/outputs/apk/"
    ls -la app/build/outputs/apk/universal/release/
else
    echo "❌ Erro no build. Verifique os logs acima."
    exit 1
fi 