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
adb install -r "app/build/outputs/apk/arm64/release/SonsPhere-25.06.1-arm64-release.apk"

if [ $? -eq 0 ]; then
    echo "✅ SonsPhere instalado com sucesso!"
    echo "🎵 Abra o app no celular!"
else
    echo "❌ Erro na instalação"
fi
