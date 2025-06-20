#!/bin/bash

echo "🎵 SonsPhere - Testador Completo 🎵"
echo "=================================="

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${PURPLE}🔧 Compilando aplicativo...${NC}"

# Compilar o app primeiro
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
if ./gradlew assembleDebug; then
    echo -e "${GREEN}✅ Compilação concluída!${NC}"
else
    echo -e "${RED}❌ Erro na compilação!${NC}"
    exit 1
fi

echo -e "${BLUE}📱 Aguardando dispositivo Android...${NC}"

# Loop até detectar dispositivo
while true; do
    DEVICE_COUNT=$(adb devices | grep -v "List of devices attached" | grep -c "device")
    
    if [ "$DEVICE_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✅ Dispositivo detectado!${NC}"
        break
    fi
    
    echo -e "${YELLOW}⏳ Aguardando conexão... (conecte o celular via USB e ative depuração USB)${NC}"
    sleep 3
done

echo -e "${BLUE}📱 Instalando SonsPhere...${NC}"

# Instalar o APK
if adb install -r app/build/outputs/apk/universal/debug/SonsPhere-25.06.1-universal-debug.apk; then
    echo -e "${GREEN}✅ Aplicativo instalado com sucesso!${NC}"
    
    echo -e "${PURPLE}📺 Iniciando espelhamento da tela...${NC}"
    echo -e "${YELLOW}   (Uma janela com a tela do celular irá abrir)${NC}"
    
    # Iniciar scrcpy em background
    scrcpy --window-title "SonsPhere Test" --window-width 400 --window-height 800 &
    SCRCPY_PID=$!
    
    sleep 3
    
    echo -e "${BLUE}🚀 Lançando aplicativo...${NC}"
    
    # Tentar lançar o app
    adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.richard.musicplayer/.MainActivity 2>/dev/null || \
    adb shell monkey -p com.richard.musicplayer 1 2>/dev/null || \
    echo -e "${YELLOW}⚠️  Abra o SonsPhere manualmente no celular${NC}"
    
    echo ""
    echo -e "${GREEN}🎉 TESTE INICIADO!${NC}"
    echo ""
    echo -e "${BLUE}📋 MUDANÇAS PARA TESTAR:${NC}"
    echo -e "  • ${GREEN}Visualizador de áudio compacto${NC} (altura reduzida de 120dp para 40dp)"
    echo -e "  • ${GREEN}Layout mais próximo${NC} (álbum próximo aos controles)" 
    echo -e "  • ${GREEN}Espaçamentos otimizados${NC} (interface mais equilibrada)"
    echo -e "  • ${GREEN}Performance melhorada${NC} (sem crashes do visualizador)"
    echo ""
    echo -e "${PURPLE}🎵 INSTRUÇÕES DE TESTE:${NC}"
    echo -e "  1. Abra uma música no player"
    echo -e "  2. Observe o visualizador compacto"
    echo -e "  3. Veja se o álbum está próximo dos controles"
    echo -e "  4. Teste os controles centralizados"
    echo ""
    echo -e "${YELLOW}⌨️  Pressione CTRL+C para parar o teste${NC}"
    
    # Aguardar interrupção
    wait $SCRCPY_PID
    
else
    echo -e "${RED}❌ Erro na instalação. Verifique a conexão USB.${NC}"
    exit 1
fi 