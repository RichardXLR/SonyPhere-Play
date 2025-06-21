#!/bin/bash

echo "🎵 COMPILAÇÃO COMPLETA - TODAS AS VERSÕES RELEASE"
echo "================================================"
echo "📱 SonsPhere v25.06.1"
echo

# Configurar JAVA_HOME
export JAVA_HOME="/opt/jdk-24.0.1"
echo "🔧 Java: $JAVA_HOME"

# Lista de todas as versões para compilar
VERSIONS=(
    "assembleUniversalRelease:Universal Release (todas arquiteturas)"
    "assembleArm64Release:ARM64 Release (apenas ARM64)"
    "assembleX86_64Release:x86_64 Release (apenas x86_64)"
    "assembleUniversalUserdebug:Universal UserDebug (todas arquiteturas)"
    "assembleArm64Userdebug:ARM64 UserDebug (apenas ARM64)"
    "assembleX86_64Userdebug:x86_64 UserDebug (apenas x86_64)"
)

SUCCESS_COUNT=0
FAILED_BUILDS=()
START_TIME=$(date +%s)

echo "🚀 Iniciando compilação de ${#VERSIONS[@]} versões..."
echo

# Parar processos
./gradlew --stop 2>/dev/null || true

# Compilar cada versão
for i in "${!VERSIONS[@]}"; do
    VERSION="${VERSIONS[$i]}"
    TASK="${VERSION%%:*}"
    NAME="${VERSION##*:}"
    CURRENT=$((i + 1))
    TOTAL=${#VERSIONS[@]}
    
    echo "[$CURRENT/$TOTAL] 🔨 Compilando: $NAME"
    echo "────────────────────────────────────────────────────────"
    
    START_BUILD=$(date +%s)
    
    if JAVA_HOME="$JAVA_HOME" timeout 3600 ./gradlew "$TASK" \
        --no-daemon \
        --no-build-cache \
        --max-workers=2 \
        -Dorg.gradle.jvmargs="-Xmx3584m -XX:+UseG1GC" \
        --quiet; then
        
        END_BUILD=$(date +%s)
        BUILD_TIME=$((END_BUILD - START_BUILD))
        
        echo "✅ $NAME - SUCESSO! (${BUILD_TIME}s)"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo "❌ $NAME - FALHOU!"
        FAILED_BUILDS+=("$NAME")
        
        # Limpar após falha
        ./gradlew --stop 2>/dev/null || true
        sleep 2
    fi
    
    echo
done

# Estatísticas finais
END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
MINUTES=$((TOTAL_TIME / 60))
SECONDS=$((TOTAL_TIME % 60))

echo "🏁 COMPILAÇÃO FINALIZADA!"
echo "========================="
echo "⏱️  Tempo total: ${MINUTES}m ${SECONDS}s"
echo "✅ Sucessos: $SUCCESS_COUNT de ${#VERSIONS[@]}"

if [ ${#FAILED_BUILDS[@]} -gt 0 ]; then
    echo "❌ Falhas: ${FAILED_BUILDS[*]}"
fi

echo
echo "📱 APKs gerados:"
if [ -d "app/build/outputs/apk" ]; then
    echo
    find app/build/outputs/apk -name "*.apk" -type f | sort | while read apk; do
        SIZE=$(du -h "$apk" | cut -f1)
        BASENAME=$(basename "$apk")
        echo "📦 $BASENAME ($SIZE)"
        echo "   📍 $apk"
        echo
    done
    
    # Calcular tamanho total
    TOTAL_SIZE=$(find app/build/outputs/apk -name "*.apk" -type f -exec du -b {} + | awk '{sum += $1} END {printf "%.1f MB", sum/1024/1024}')
    echo "📊 Tamanho total: $TOTAL_SIZE"
else
    echo "❌ Nenhum APK encontrado"
fi

# Limpeza final
./gradlew --stop 2>/dev/null || true

echo
if [ $SUCCESS_COUNT -eq ${#VERSIONS[@]} ]; then
    echo "🎉 TODAS AS VERSÕES COMPILADAS COM SUCESSO!"
else
    echo "⚠️  Algumas versões falharam, mas você tem $SUCCESS_COUNT APKs prontos!"
fi

echo "📂 Localização: app/build/outputs/apk/" 