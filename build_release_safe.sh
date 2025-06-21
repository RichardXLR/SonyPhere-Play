#!/bin/bash

# Script de Build Release Seguro para SonsPhere
# Otimizado para sistemas com 8GB de RAM

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Verificar memória disponível
check_memory() {
    local mem_total=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    local mem_available=$(grep MemAvailable /proc/meminfo | awk '{print $2}')
    local mem_total_gb=$((mem_total / 1024 / 1024))
    local mem_available_gb=$((mem_available / 1024 / 1024))
    
    print_status "Memória total: ${mem_total_gb}GB"
    print_status "Memória disponível: ${mem_available_gb}GB"
    
    if [ $mem_available_gb -lt 4 ]; then
        print_warning "Pouca memória disponível (${mem_available_gb}GB). Recomenda-se pelo menos 4GB livres."
        read -p "Continuar mesmo assim? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Build cancelado pelo usuário."
            exit 1
        fi
    fi
}

# Limpeza antes do build
cleanup_before_build() {
    print_status "Limpando builds anteriores..."
    
    # Parar daemon do Gradle para liberar memória
    ./gradlew --stop
    
    # Limpar builds anteriores
    ./gradlew clean
    
    # Limpar cache do Gradle se necessário
    if [ -d "$HOME/.gradle/caches" ]; then
        local cache_size=$(du -sh "$HOME/.gradle/caches" | cut -f1)
        print_status "Cache do Gradle: $cache_size"
        
        # Se o cache for muito grande (>2GB), limpar
        local cache_size_gb=$(du -s "$HOME/.gradle/caches" | awk '{print $1/1024/1024}')
        if (( $(echo "$cache_size_gb > 2" | bc -l) )); then
            print_warning "Cache do Gradle muito grande, limpando..."
            rm -rf "$HOME/.gradle/caches"
        fi
    fi
}

# Monitorar memória durante o build
monitor_memory() {
    while true; do
        local mem_available=$(grep MemAvailable /proc/meminfo | awk '{print $2}')
        local mem_available_gb=$((mem_available / 1024 / 1024))
        
        if [ $mem_available_gb -lt 2 ]; then
            print_warning "Memória baixa: ${mem_available_gb}GB disponível"
            # Parar daemons para liberar memória
            pkill -f "GradleDaemon" || true
            pkill -f "KotlinCompileDaemon" || true
            sleep 5
        fi
        
        sleep 30
    done &
    echo $! > /tmp/memory_monitor.pid
}

# Parar monitor de memória
stop_memory_monitor() {
    if [ -f /tmp/memory_monitor.pid ]; then
        kill $(cat /tmp/memory_monitor.pid) 2>/dev/null || true
        rm /tmp/memory_monitor.pid
    fi
}

# Função principal de build
build_release() {
    local variant=${1:-"universal"}
    
    print_status "Iniciando build release para variant: $variant"
    
    # Configurar variáveis de ambiente para otimização
    export GRADLE_OPTS="-Xmx3584m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    export KOTLIN_DAEMON_OPTS="-Xmx2048m -XX:+UseG1GC"
    
    # Build command com configurações seguras
    local build_cmd="./gradlew"
    build_cmd+=" --no-daemon"  # Evitar daemon para controlar melhor a memória
    build_cmd+=" --max-workers=2"  # Limitar workers
    build_cmd+=" --parallel"  # Usar paralelização controlada
    build_cmd+=" --configure-on-demand"  # Configurar apenas o necessário
    build_cmd+=" assemble${variant^}Release"  # Build do variant específico
    
    print_status "Executando: $build_cmd"
    
    # Executar build com timeout de segurança (30 minutos)
    if timeout 1800 $build_cmd; then
        print_success "Build concluído com sucesso!"
        return 0
    else
        print_error "Build falhou ou excedeu tempo limite"
        return 1
    fi
}

# Função para mostrar informações do APK gerado
show_apk_info() {
    local apk_dir="app/build/outputs/apk"
    
    if [ -d "$apk_dir" ]; then
        print_status "APKs gerados:"
        find "$apk_dir" -name "*.apk" -type f -exec ls -lh {} \; | while read line; do
            echo "  $line"
        done
        
        # Mostrar hashes para verificação
        print_status "Hashes SHA256:"
        find "$apk_dir" -name "*.apk" -type f -exec sha256sum {} \;
    else
        print_warning "Diretório de APKs não encontrado"
    fi
}

# Menu interativo
show_menu() {
    echo
    print_status "=== BUILD RELEASE SEGURO - SonsPhere ==="
    echo "1) Build Universal (todas as arquiteturas)"
    echo "2) Build ARM64 (apenas arm64-v8a)"
    echo "3) Build x86_64 (apenas x86_64)"
    echo "4) Verificar status do sistema"
    echo "5) Limpar cache e sair"
    echo "0) Sair"
    echo
}

# Função principal
main() {
    cd "$(dirname "$0")"
    
    print_status "Iniciando Build Release Seguro"
    print_status "Verificando sistema..."
    
    # Verificar se estamos no diretório correto
    if [ ! -f "gradlew" ]; then
        print_error "gradlew não encontrado. Execute este script na raiz do projeto."
        exit 1
    fi
    
    # Verificar memória
    check_memory
    
    # Menu interativo se não houver argumentos
    if [ $# -eq 0 ]; then
        while true; do
            show_menu
            read -p "Escolha uma opção: " choice
            
            case $choice in
                1)
                    print_status "Preparando build Universal..."
                    cleanup_before_build
                    monitor_memory
                    if build_release "universal"; then
                        show_apk_info
                    fi
                    stop_memory_monitor
                    ;;
                2)
                    print_status "Preparando build ARM64..."
                    cleanup_before_build
                    monitor_memory
                    if build_release "arm64"; then
                        show_apk_info
                    fi
                    stop_memory_monitor
                    ;;
                3)
                    print_status "Preparando build x86_64..."
                    cleanup_before_build
                    monitor_memory
                    if build_release "x86_64"; then
                        show_apk_info
                    fi
                    stop_memory_monitor
                    ;;
                4)
                    check_memory
                    df -h .
                    ;;
                5)
                    print_status "Limpando cache..."
                    ./gradlew --stop
                    ./gradlew clean
                    rm -rf "$HOME/.gradle/caches" 2>/dev/null || true
                    print_success "Cache limpo!"
                    exit 0
                    ;;
                0)
                    print_status "Saindo..."
                    exit 0
                    ;;
                *)
                    print_error "Opção inválida!"
                    ;;
            esac
        done
    else
        # Modo não-interativo
        cleanup_before_build
        monitor_memory
        if build_release "$1"; then
            show_apk_info
        fi
        stop_memory_monitor
    fi
}

# Trap para limpeza em caso de interrupção
trap 'stop_memory_monitor; print_warning "Build interrompido"; exit 1' INT TERM

# Executar função principal
main "$@" 