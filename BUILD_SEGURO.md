# 🚀 Build Release Seguro - SonsPhere

Este documento explica como compilar o SonsPhere de forma segura em sistemas com 8GB de RAM, evitando travamentos e problemas de memória.

## 📋 Pré-requisitos

- **Memória RAM**: 8GB (recomendado pelo menos 4GB livres)
- **Java/JDK**: OpenJDK 21 (já configurado)
- **Sistema**: Linux (Ubuntu/Debian)
- **Espaço livre**: Pelo menos 5GB de espaço em disco

## 🔧 Configurações Otimizadas

### Configurações de Memória

As seguintes otimizações foram aplicadas no `gradle.properties`:

```properties
# Gradle JVM limitado a ~3.5GB (deixando espaço para o sistema)
org.gradle.jvmargs=-Xmx3584M -Xms2048M -XX:+UseG1GC

# Kotlin daemon limitado a 2GB
kotlin.daemon.jvmargs=-Xmx2048M -Xms1024M -XX:+UseG1GC

# Workers limitados para evitar sobrecarga
org.gradle.workers.max=2
```

### Configurações do Android Build

- **R8 Full Mode**: Otimização máxima de código
- **DEX Options**: Heap limitado a 2GB
- **Multi-DEX**: Desabilitado para economizar memória
- **Pre-DEX**: Desabilitado para compilação mais eficiente

## 🚀 Como Usar

### Método 1: Script Interativo (Recomendado)

Execute o script de build seguro:

```bash
./build_release_safe.sh
```

O script oferece um menu interativo com as seguintes opções:

1. **Build Universal** - Todas as arquiteturas (ARM64, x86_64, ARMv7)
2. **Build ARM64** - Apenas ARM64 (menor tamanho, mais rápido)
3. **Build x86_64** - Para emuladores/PCs x86_64
4. **Verificar Sistema** - Mostra status de memória e disco
5. **Limpar Cache** - Remove caches para liberar espaço

### Método 2: Linha de Comando

Para builds não-interativos:

```bash
# Build universal
./build_release_safe.sh universal

# Build ARM64 (recomendado para dispositivos modernos)
./build_release_safe.sh arm64

# Build x86_64
./build_release_safe.sh x86_64
```

### Método 3: Gradle Manual (Avançado)

Se preferir usar Gradle diretamente:

```bash
# Limpar primeiro
./gradlew --stop
./gradlew clean

# Build com configurações seguras
./gradlew --no-daemon --max-workers=2 --parallel assembleUniversalRelease
```

## 🔍 Monitoramento

O script automático inclui:

- **Monitor de Memória**: Verifica a cada 30 segundos
- **Limpeza Automática**: Remove daemons se memória baixa
- **Timeout de Segurança**: Build cancelado se exceder 30 minutos
- **Verificação Pré-Build**: Confirma memória disponível

## 📦 Saída

Os APKs gerados ficam em:
```
app/build/outputs/apk/[variant]/release/
```

Exemplo de nomes:
- `SonsPhere-25.06.1-universalRelease.apk`
- `SonsPhere-25.06.1-arm64Release.apk`

## ⚠️ Solução de Problemas

### Build Trava ou Falha

1. **Verificar Memória**:
   ```bash
   free -h
   ```

2. **Limpar Cache**:
   ```bash
   ./gradlew --stop
   rm -rf ~/.gradle/caches
   ```

3. **Fechar Aplicações**: Feche navegadores e programas pesados

### OutOfMemoryError

Se ainda ocorrer erro de memória:

1. **Reduzir Workers**:
   ```bash
   ./gradlew --max-workers=1 assembleArm64Release
   ```

2. **Build Sequencial**:
   ```bash
   ./gradlew --no-parallel assembleArm64Release
   ```

3. **Usar Swap**: Configure swap se necessário:
   ```bash
   sudo swapon --show
   ```

### Compilação Muito Lenta

1. **Build ARM64 Apenas**: Mais rápido que universal
2. **SSD**: Use SSD se possível
3. **Limpar Projeto**: `./gradlew clean` antes do build

## 🎯 Recomendações

### Para Dispositivos Finais
- **Use ARM64**: Menor tamanho, melhor performance
- **Build Release**: Sempre use variant release para distribuição

### Para Testes
- **Use Universal**: Compatível com mais dispositivos
- **Build Debug**: Para desenvolvimento e testes

### Para Performance
- **Feche Aplicações**: Durante o build
- **Use SSD**: Se disponível
- **Monitore Temperatura**: CPUs podem reduzir clock se superaquecerem

## 📊 Tempos Estimados

Em sistema com 8GB RAM, SSD e CPU moderna:

- **ARM64 Release**: 8-15 minutos
- **Universal Release**: 15-25 minutos
- **Build Limpo**: +5-10 minutos adicionais

## 🔒 Segurança

O build inclui:
- **Assinatura Release**: APK assinado automaticamente
- **Obfuscação**: Código ofuscado com R8
- **Otimizações**: Remoção de código não usado
- **Verificação Hash**: SHA256 dos APKs gerados

## 📝 Logs

Logs detalhados ficam em:
- Terminal durante o build
- `gradle.log` se configurado
- Cache do Gradle em `~/.gradle/`

## 🆘 Suporte

Se tiver problemas:

1. Verifique se tem pelo menos 4GB de RAM livre
2. Execute `./build_release_safe.sh` e escolha opção 4 (verificar sistema)
3. Tente build ARM64 primeiro (menor consumo de memória)
4. Limpe cache se necessário (opção 5 do menu)

---

**Nota**: Este sistema foi otimizado especificamente para sistemas com 8GB de RAM. Para sistemas com menos RAM, considere reduzir ainda mais os limites de memória no `gradle.properties`. 