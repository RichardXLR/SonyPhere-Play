#!/bin/bash

echo "🎵 COMPILAÇÃO DIRETA - SonsPhere"
echo "================================"

# Configurar JAVA_HOME automaticamente
export JAVA_HOME="/opt/jdk-24.0.1"

echo "🔧 Java: $JAVA_HOME"
echo "🚀 Compilando Universal Release..."

# Parar processos
./gradlew --stop 2>/dev/null || true

# Compilar diretamente sem cache para evitar problemas
JAVA_HOME="$JAVA_HOME" ./gradlew assembleUniversalRelease \
    --no-daemon \
    --no-build-cache \
    --rerun-tasks \
    --max-workers=2 \
    -Dorg.gradle.jvmargs="-Xmx3584m -XX:+UseG1GC"

if [ $? -eq 0 ]; then
    echo
    echo "✅ SUCESSO! APK gerado:"
    find app/build/outputs/apk -name "*universal*release*.apk" -type f | head -1 | while read apk; do
        SIZE=$(du -h "$apk" | cut -f1)
        echo "📦 $(basename "$apk") ($SIZE)"
        echo "📍 $apk"
    done
else
    echo "❌ FALHOU!"
fi

./gradlew --stop 2>/dev/null || true 