#!/bin/bash

# ========================================
# COMPILAÇÃO APENAS VERSÕES ESSENCIAIS - MAIS RÁPIDO
# ========================================

# Auto configurar JAVA_HOME
if [ -d "/opt/jdk-24.0.1" ]; then
    export JAVA_HOME="/opt/jdk-24.0.1"
elif [ -d "/usr/lib/jvm/default-java" ]; then
    export JAVA_HOME="/usr/lib/jvm/default-java"
else
    JAVA_PATH=$(which java)
    if [ -n "$JAVA_PATH" ]; then
        export JAVA_HOME=$(readlink -f "$JAVA_PATH" | sed 's|/bin/java||')
    fi
fi

echo "🔧 JAVA_HOME: $JAVA_HOME"

echo
echo "🎵 === COMPILAÇÃO RÁPIDA - VERSÕES ESSENCIAIS ==="
echo "=============================================="

# Limpeza rápida
echo "🧹 Limpeza..."
JAVA_HOME="$JAVA_HOME" ./gradlew --stop 2>/dev/null || true

echo
echo "🚀 Compilando apenas versões Release essenciais..."
echo "   📱 Universal Release (todas as arquiteturas)"
echo "   📱 ARM64 Release (apenas ARM64)"

# Lista das versões essenciais
BUILDS=(
    "assembleUniversalRelease:Universal Release"
    "assembleArm64Release:ARM64 Release"
)

SUCCESS=0
TOTAL=${#BUILDS[@]}

for build_info in "${BUILDS[@]}"; do
    TASK=${build_info%%:*}
    NAME=${build_info##*:}
    
    echo
    echo "⚡ Compilando: $NAME"
    echo "────────────────────────────────────"
    
    if JAVA_HOME="$JAVA_HOME" timeout 2400 ./gradlew \
        --no-daemon \
        --max-workers=3 \
        --parallel \
        --build-cache \
        -Dorg.gradle.jvmargs="-Xmx4096m -XX:+UseG1GC" \
        "$TASK"; then
        
        echo "✅ $NAME - SUCESSO!"
        SUCCESS=$((SUCCESS + 1))
    else
        echo "❌ $NAME - FALHOU!"
        JAVA_HOME="$JAVA_HOME" ./gradlew --stop 2>/dev/null || true
    fi
done

echo
echo "🏁 RESULTADO FINAL:"
echo "=================="
echo "✅ Sucessos: $SUCCESS de $TOTAL"

if [ $SUCCESS -gt 0 ]; then
    echo
    echo "📱 APKs gerados:"
    if [ -d "app/build/outputs/apk" ]; then
        find app/build/outputs/apk -name "*Release*.apk" -type f | while read apk; do
            SIZE=$(du -h "$apk" | cut -f1)
            echo "📦 $(basename "$apk") ($SIZE)"
        done
    fi
fi

JAVA_HOME="$JAVA_HOME" ./gradlew --stop 2>/dev/null || true
echo "🎉 Processo finalizado!" 