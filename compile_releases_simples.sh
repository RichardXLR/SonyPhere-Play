#!/bin/bash

# ========================================
# SCRIPT SIMPLES - TODAS AS VERSÕES RELEASE
# ========================================

# Configuração automática do JAVA_HOME
if [ -d "/opt/jdk-24.0.1" ]; then
    export JAVA_HOME="/opt/jdk-24.0.1"
elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
elif [ -d "/usr/lib/jvm/default-java" ]; then
    export JAVA_HOME="/usr/lib/jvm/default-java"
else
    # Encontrar Java automaticamente
    JAVA_PATH=$(which java)
    if [ -n "$JAVA_PATH" ]; then
        export JAVA_HOME=$(readlink -f "$JAVA_PATH" | sed 's|/bin/java||')
    fi
fi

echo "🔧 JAVA_HOME configurado para: $JAVA_HOME"
echo "☕ Versão do Java:"
java -version

echo
echo "🎵 === COMPILAÇÃO TODAS AS VERSÕES RELEASE ==="
echo "=============================================="

# Limpeza
echo "🧹 Limpando builds anteriores..."
JAVA_HOME="$JAVA_HOME" ./gradlew --stop 2>/dev/null || true
JAVA_HOME="$JAVA_HOME" ./gradlew clean --no-daemon

echo
echo "🚀 Iniciando compilação de todas as versões..."

# Lista de todas as variantes para compilar
VARIANTS=(
    "assembleUniversalRelease"
    "assembleArm64Release" 
    "assembleX86_64Release"
    "assembleUniversalUserdebug"
    "assembleArm64Userdebug"
    "assembleX86_64Userdebug"
)

SUCCESS_COUNT=0
FAILED_BUILDS=()

# Compilar cada variante
for i in "${!VARIANTS[@]}"; do
    VARIANT="${VARIANTS[$i]}"
    CURRENT=$((i + 1))
    TOTAL=${#VARIANTS[@]}
    
    echo
    echo "📊 [$CURRENT/$TOTAL] Compilando: $VARIANT"
    echo "────────────────────────────────────────────"
    
    # Comando de build com configurações seguras
    if JAVA_HOME="$JAVA_HOME" timeout 3600 ./gradlew \
        --no-daemon \
        --max-workers=2 \
        --parallel \
        --build-cache \
        --configure-on-demand \
        -Dorg.gradle.jvmargs="-Xmx3584m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
        -Dkotlin.daemon.jvmargs="-Xmx2048m -XX:+UseG1GC" \
        "$VARIANT"; then
        
        echo "✅ $VARIANT - SUCESSO!"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo "❌ $VARIANT - FALHOU!"
        FAILED_BUILDS+=("$VARIANT")
        
        # Limpeza após falha
        JAVA_HOME="$JAVA_HOME" ./gradlew --stop 2>/dev/null || true
        sleep 2
    fi
done

echo
echo "🏁 COMPILAÇÃO FINALIZADA!"
echo "========================="
echo "✅ Sucessos: $SUCCESS_COUNT de ${#VARIANTS[@]}"

if [ ${#FAILED_BUILDS[@]} -gt 0 ]; then
    echo "❌ Falhas: ${FAILED_BUILDS[*]}"
fi

echo
echo "📱 APKs gerados:"
if [ -d "app/build/outputs/apk" ]; then
    find app/build/outputs/apk -name "*.apk" -type f | while read apk; do
        SIZE=$(du -h "$apk" | cut -f1)
        echo "📦 $(basename "$apk") - $SIZE"
        echo "   📍 $apk"
    done
else
    echo "❌ Nenhum APK encontrado"
fi

# Limpeza final
JAVA_HOME="$JAVA_HOME" ./gradlew --stop 2>/dev/null || true

echo
echo "🎉 Processo finalizado!" 