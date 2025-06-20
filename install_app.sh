#!/bin/bash

echo "🚀 Compilando e instalando SonsPhere..."

# Configurar Java
export JAVA_HOME=/opt/jdk-24.0.1

# Parar daemon anterior
./gradlew --stop

echo "📱 Compilando APK..."
# Compilar apenas debug universal (mais rápido)
./gradlew assembleUniversalDebug --no-daemon

# Verificar se compilou com sucesso
if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
    
    # Verificar dispositivos conectados
    echo "📱 Verificando dispositivos conectados..."
    DEVICES=$(adb devices | grep "device$" | wc -l)
    
    if [ $DEVICES -gt 0 ]; then
        echo "📱 Dispositivo encontrado! Instalando..."
        # Instalar APK universal
        adb install -r "app/build/outputs/apk/universal/debug/SonsPhere-25.06.1-universal-debug.apk"
        
        if [ $? -eq 0 ]; then
            echo "🎉 APP INSTALADO COM SUCESSO!"
            echo "📱 SonsPhere está pronto para usar!"
        else
            echo "❌ Erro na instalação"
        fi
    else
        echo "❌ Nenhum dispositivo conectado via USB"
        echo "💡 Conecte seu celular e ative a Depuração USB"
    fi
else
    echo "❌ Erro na compilação"
fi 