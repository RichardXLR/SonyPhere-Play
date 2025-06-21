#!/bin/bash

# ========================================
# SCRIPT DE BUILD RELEASE SEGURO - 8GB RAM
# ========================================

echo "🚀 Iniciando build release seguro para sistema com 8GB RAM..."

# Verificar memória disponível
echo "📊 Verificando memória disponível..."
free -h

# Limpeza preventiva
echo "🧹 Limpando builds anteriores..."
./gradlew clean --no-daemon --max-workers=2

# Aguardar um pouco para liberar memória
sleep 3

# Verificar se há processos Gradle rodando
echo "🔍 Verificando processos Gradle..."
ps aux | grep gradle | grep -v grep || echo "Nenhum processo Gradle encontrado"

# Build release com configurações conservadoras
echo "🔨 Compilando versão release..."
./gradlew assembleRelease \
    --no-daemon \
    --max-workers=2 \
    --parallel \
    --build-cache \
    --configure-on-demand \
    --no-configuration-cache \
    --info \
    -Dorg.gradle.jvmargs="-Xmx3072M -Xms1024M -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
    -Dkotlin.daemon.jvmargs="-Xmx1536M -Xms512M -XX:+UseG1GC"

BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
    echo "✅ Build release concluído com sucesso!"
    echo "📱 APK gerado em: app/build/outputs/apk/release/"
    ls -la app/build/outputs/apk/release/
    
    # Mostrar tamanho do APK
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        APK_SIZE=$(du -h app/build/outputs/apk/release/app-release.apk | cut -f1)
        echo "📦 Tamanho do APK: $APK_SIZE"
    fi
else
    echo "❌ Erro no build release!"
    echo "💡 Dicas:"
    echo "   - Feche outros aplicativos para liberar memória"
    echo "   - Execute: ./gradlew --stop para parar daemons"
    echo "   - Tente novamente após alguns minutos"
fi

# Limpeza final dos daemons
echo "🧹 Parando daemons Gradle..."
./gradlew --stop

echo "🏁 Script finalizado!"
exit $BUILD_RESULT 