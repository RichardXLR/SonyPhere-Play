#!/bin/bash

echo "📱 COMPILAÇÃO ARM64 - SonsPhere para Celular"
echo "============================================"

# Configurar JAVA_HOME
export JAVA_HOME="/opt/jdk-24.0.1"
echo "🔧 Java: $JAVA_HOME"
echo "🎯 Target: ARM64 Release (otimizado para celulares)"

# Parar processos
./gradlew --stop 2>/dev/null || true

echo
echo "🚀 Compilando versão ARM64..."

# Compilar ARM64 Release
if JAVA_HOME="$JAVA_HOME" ./gradlew assembleArm64Release \
    --no-daemon \
    --max-workers=3 \
    --parallel \
    -Dorg.gradle.jvmargs="-Xmx4096m -XX:+UseG1GC"; then
    
    echo
    echo "✅ ARM64 Release compilado com SUCESSO!"
    
    # Encontrar e mostrar o APK
    ARM64_APK=$(find app/build/outputs/apk -name "*arm64*release*.apk" -type f | head -1)
    
    if [ -n "$ARM64_APK" ]; then
        SIZE=$(du -h "$ARM64_APK" | cut -f1)
        echo
        echo "📦 APK ARM64 gerado:"
        echo "   📱 $(basename "$ARM64_APK") ($SIZE)"
        echo "   📍 $ARM64_APK"
        
        # Verificar se adb está disponível
        if command -v adb >/dev/null 2>&1; then
            echo
            echo "📲 Instalação no celular:"
            echo "   1️⃣  Conecte o celular via USB"
            echo "   2️⃣  Ative 'Depuração USB' nas opções do desenvolvedor"
            echo "   3️⃣  Execute: adb install \"$ARM64_APK\""
            echo
            echo "🔗 Ou execute agora:"
            echo "   ./instalar_celular.sh"
        else
            echo
            echo "📲 Para instalar no celular:"
            echo "   1️⃣  Transfira o APK para o celular"
            echo "   2️⃣  Habilite 'Fontes desconhecidas' nas configurações"
            echo "   3️⃣  Toque no APK para instalar"
        fi
        
        # Criar script de instalação
        cat > instalar_celular.sh << EOF
#!/bin/bash
echo "📱 Instalando SonsPhere no celular..."
echo "🔌 Verificando conexão ADB..."

if ! command -v adb >/dev/null 2>&1; then
    echo "❌ ADB não encontrado. Instale: sudo apt install android-tools-adb"
    exit 1
fi

if ! adb devices | grep -q "device$"; then
    echo "❌ Nenhum dispositivo conectado."
    echo "   1️⃣  Conecte o celular via USB"
    echo "   2️⃣  Ative 'Depuração USB' nas opções do desenvolvedor"
    echo "   3️⃣  Autorize o computador no celular"
    exit 1
fi

echo "📲 Instalando APK..."
adb install -r "$ARM64_APK"

if [ \$? -eq 0 ]; then
    echo "✅ SonsPhere instalado com sucesso!"
    echo "🎵 Abra o app no celular!"
else
    echo "❌ Erro na instalação"
fi
EOF
        chmod +x instalar_celular.sh
        
    else
        echo "❌ APK ARM64 não encontrado!"
    fi
    
else
    echo "❌ Erro na compilação ARM64!"
fi

./gradlew --stop 2>/dev/null || true
echo "🏁 Processo finalizado!" 