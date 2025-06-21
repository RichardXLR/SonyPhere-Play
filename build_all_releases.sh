#!/bin/bash

# ========================================
# SCRIPT COMPLETO - TODAS AS VERSÕES RELEASE
# ========================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

# Função para imprimir com cores
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_building() {
    echo -e "${PURPLE}[BUILDING]${NC} $1"
}

# Verificar memória disponível
check_memory() {
    local mem_total=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    local mem_available=$(grep MemAvailable /proc/meminfo | awk '{print $2}')
    local mem_total_gb=$((mem_total / 1024 / 1024))
    local mem_available_gb=$((mem_available / 1024 / 1024))
    
    print_status "💾 Memória total: ${mem_total_gb}GB"
    print_status "🆓 Memória disponível: ${mem_available_gb}GB"
    
    if [ $mem_available_gb -lt 4 ]; then
        print_warning "⚠️  Pouca memória disponível (${mem_available_gb}GB). Recomenda-se pelo menos 4GB livres."
        print_warning "🔧 Usando configurações conservadoras..."
        return 1
    fi
    return 0
}

# Limpeza antes do build
cleanup_system() {
    print_status "🧹 Limpando sistema antes dos builds..."
    
    # Parar todos os daemons Gradle
    ./gradlew --stop || true
    
    # Aguardar daemons pararem
    sleep 2
    
    # Matar processos Gradle órfãos
    pkill -f "GradleDaemon" 2>/dev/null || true
    pkill -f "KotlinCompileDaemon" 2>/dev/null || true
    
    # Limpar builds anteriores
    ./gradlew clean --no-daemon --max-workers=1
    
    print_success "✅ Sistema limpo!"
}

# Configurar ambiente otimizado
setup_environment() {
    local has_enough_memory=$1
    
    if [ $has_enough_memory -eq 0 ]; then
        # Sistema com memória suficiente
        export GRADLE_OPTS="-Xmx4096m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        export KOTLIN_DAEMON_OPTS="-Xmx2048m -XX:+UseG1GC"
        MAX_WORKERS=3
        print_status "🚀 Configuração normal ativada (memória suficiente)"
    else
        # Sistema com pouca memória
        export GRADLE_OPTS="-Xmx3072m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        export KOTLIN_DAEMON_OPTS="-Xmx1536m -XX:+UseG1GC"
        MAX_WORKERS=2
        print_status "⚡ Configuração conservadora ativada (pouca memória)"
    fi
}

# Build individual de uma variante
build_variant() {
    local variant_name=$1
    local build_type=$2
    local full_variant="${variant_name}${build_type}"
    
    print_building "🔨 Compilando: $full_variant"
    
    # Limpar daemons antes de cada build para controlar memória
    ./gradlew --stop 2>/dev/null || true
    sleep 1
    
    # Comando de build otimizado
    local build_cmd="./gradlew"
    build_cmd+=" --no-daemon"
    build_cmd+=" --max-workers=$MAX_WORKERS"
    build_cmd+=" --parallel"
    build_cmd+=" --configure-on-demand"
    build_cmd+=" --build-cache"
    build_cmd+=" assemble$full_variant"
    
    # Executar com timeout de 45 minutos
    if timeout 2700 $build_cmd; then
        print_success "✅ $full_variant compilado com sucesso!"
        return 0
    else
        print_error "❌ Falha ao compilar $full_variant"
        return 1
    fi
}

# Mostrar informações dos APKs gerados
show_build_results() {
    local apk_dir="app/build/outputs/apk"
    
    echo
    print_status "📱 RESUMO DOS BUILDS GERADOS:"
    print_status "================================"
    
    if [ -d "$apk_dir" ]; then
        local total_size=0
        local apk_count=0
        
        find "$apk_dir" -name "*.apk" -type f | while read apk_file; do
            local size=$(stat -c%s "$apk_file")
            local size_mb=$((size / 1024 / 1024))
            local basename=$(basename "$apk_file")
            
            echo -e "${GREEN}📦${NC} $basename (${size_mb}MB)"
            
            # Mostrar localização completa
            echo -e "   📍 $apk_file"
            
            # Mostrar hash para verificação
            local hash=$(sha256sum "$apk_file" | cut -d' ' -f1 | cut -c1-16)
            echo -e "   🔐 SHA256: $hash..."
            echo
        done
        
        echo
        print_success "🎉 Todos os builds concluídos!"
        print_status "📂 APKs disponíveis em: $apk_dir"
        
    else
        print_error "❌ Diretório de APKs não encontrado!"
    fi
}

# Menu de seleção
show_menu() {
    echo
    print_status "🎵 === BUILD TODAS AS VERSÕES RELEASE - SonsPhere ==="
    echo "1️⃣  Compilar TODOS os releases (6 variantes)"
    echo "2️⃣  Compilar apenas Release builds (3 variantes)"
    echo "3️⃣  Compilar apenas UserDebug builds (3 variantes)"
    echo "4️⃣  Compilar apenas Universal (release + userdebug)"
    echo "5️⃣  Compilar apenas ARM64 (release + userdebug)"
    echo "6️⃣  Compilar apenas x86_64 (release + userdebug)"
    echo "7️⃣  Verificar status do sistema"
    echo "8️⃣  Limpeza completa e sair"
    echo "0️⃣  Sair"
    echo
}

# Compilar todas as variantes
build_all_variants() {
    local build_types=("$@")
    local flavors=("Universal" "Arm64" "X86_64")
    local success_count=0
    local total_count=0
    local failed_builds=()
    
    print_status "🚀 Iniciando compilação de todas as variantes..."
    
    for build_type in "${build_types[@]}"; do
        for flavor in "${flavors[@]}"; do
            total_count=$((total_count + 1))
            
            print_status "📊 Progresso: $total_count de $((${#build_types[@]} * ${#flavors[@]}))"
            
            if build_variant "$flavor" "$build_type"; then
                success_count=$((success_count + 1))
            else
                failed_builds+=("$flavor$build_type")
                # Tentar limpar memória antes de continuar
                ./gradlew --stop 2>/dev/null || true
                pkill -f "GradleDaemon" 2>/dev/null || true
                sleep 3
            fi
            
            # Pequena pausa entre builds para estabilizar
            sleep 2
        done
    done
    
    echo
    print_status "📈 RELATÓRIO FINAL:"
    print_success "✅ Sucessos: $success_count de $total_count"
    
    if [ ${#failed_builds[@]} -gt 0 ]; then
        print_error "❌ Falhas: ${failed_builds[*]}"
    fi
}

# Função principal
main() {
    cd "$(dirname "$0")"
    
    print_status "🎵 SonsPhere - Build Todas as Versões Release"
    print_status "=============================================="
    
    # Verificar se estamos no diretório correto
    if [ ! -f "gradlew" ]; then
        print_error "❌ gradlew não encontrado. Execute na raiz do projeto."
        exit 1
    fi
    
    # Verificar memória e configurar ambiente
    if check_memory; then
        setup_environment 0
    else
        setup_environment 1
    fi
    
    # Menu interativo se não houver argumentos
    if [ $# -eq 0 ]; then
        while true; do
            show_menu
            read -p "Escolha uma opção: " choice
            
            case $choice in
                1)
                    cleanup_system
                    build_all_variants "Release" "Userdebug"
                    show_build_results
                    ;;
                2)
                    cleanup_system
                    build_all_variants "Release"
                    show_build_results
                    ;;
                3)
                    cleanup_system
                    build_all_variants "Userdebug"
                    show_build_results
                    ;;
                4)
                    cleanup_system
                    build_all_variants "Release" "Userdebug"
                    show_build_results
                    ;;
                5)
                    cleanup_system
                    for build_type in "Release" "Userdebug"; do
                        build_variant "Arm64" "$build_type"
                    done
                    show_build_results
                    ;;
                6)
                    cleanup_system
                    for build_type in "Release" "Userdebug"; do
                        build_variant "X86_64" "$build_type"
                    done
                    show_build_results
                    ;;
                7)
                    check_memory
                    df -h .
                    echo
                    print_status "📊 Processos Gradle ativos:"
                    ps aux | grep -i gradle | grep -v grep || echo "Nenhum"
                    ;;
                8)
                    print_status "🧹 Limpeza completa..."
                    ./gradlew --stop
                    ./gradlew clean --no-daemon
                    rm -rf "$HOME/.gradle/caches" 2>/dev/null || true
                    print_success "✅ Sistema limpo!"
                    exit 0
                    ;;
                0)
                    print_status "👋 Saindo..."
                    exit 0
                    ;;
                *)
                    print_error "❌ Opção inválida!"
                    ;;
            esac
        done
    else
        # Modo não-interativo - compilar tudo
        cleanup_system
        build_all_variants "Release" "Userdebug"
        show_build_results
    fi
}

# Trap para limpeza em caso de interrupção
trap 'print_warning "⚠️ Build interrompido pelo usuário"; ./gradlew --stop; exit 1' INT TERM

# Verificar dependências
command -v timeout >/dev/null 2>&1 || { print_error "❌ 'timeout' não encontrado. Instale: sudo apt-get install coreutils"; exit 1; }

# Executar função principal
main "$@" 