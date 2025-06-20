#!/bin/bash

echo "🎵 SonsPhere - Instalador Automático 🎵"
echo "========================================"

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}Aguardando dispositivo Android...${NC}"

# Loop até detectar dispositivo
while true; do
    DEVICE_COUNT=$(adb devices | grep -v "List of devices attached" | grep -c "device")
    
    if [ "$DEVICE_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✅ Dispositivo detectado!${NC}"
        break
    fi
    
    echo -e "${YELLOW}⏳ Aguardando conexão... (conecte o celular via USB)${NC}"
    sleep 3
done

echo -e "${BLUE}📱 Instalando SonsPhere...${NC}"

# Instalar o APK
if adb install -r app/build/outputs/apk/universal/debug/SonsPhere-25.06.1-universal-debug.apk; then
    echo -e "${GREEN}✅ Aplicativo instalado com sucesso!${NC}"
    
    echo -e "${BLUE}🚀 Lançando aplicativo...${NC}"
    
    # Tentar lançar o app
    adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.richard.musicplayer/.MainActivity 2>/dev/null || \
    adb shell monkey -p com.richard.musicplayer 1 2>/dev/null || \
    echo -e "${YELLOW}⚠️  Abra o SonsPhere manualmente no celular${NC}"
    
    echo ""
    echo -e "${GREEN}🎉 INSTALAÇÃO CONCLUÍDA!${NC}"
    echo ""
    echo -e "${BLUE}📋 MUDANÇAS IMPLEMENTADAS:${NC}"
    echo -e "  • ${GREEN}Visualizador de áudio compacto${NC} (altura reduzida 67%)"
    echo -e "  • ${GREEN}Layout mais próximo${NC} (álbum próximo aos controles)" 
    echo -e "  • ${GREEN}Espaçamentos otimizados${NC} (interface mais equilibrada)"
    echo -e "  • ${GREEN}Performance melhorada${NC} (sem crashes do visualizador)"
    echo ""
    echo -e "${YELLOW}🎵 Teste o player de música e veja as melhorias!${NC}"
    
else
    echo -e "${RED}❌ Erro na instalação. Verifique a conexão USB.${NC}"
    exit 1
fi 