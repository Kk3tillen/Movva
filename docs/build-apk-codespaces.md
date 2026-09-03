# Guia completo: executar o Movva no GitHub Codespaces e testar o APK online

## Objetivo

Este guia descreve o processo completo para abrir o projeto [Movva](https://github.com/Kk3tillen/Movva) em um GitHub Codespace, configurar o Android SDK, gerar o APK Android em modo Debug, baixar o arquivo e executá-lo em um emulador Android online.

O projeto é Kotlin Multiplatform com Compose e possui módulos Android e iOS. Neste procedimento será utilizada a parte Android. O iOS exige macOS e Xcode, portanto não é executado diretamente dentro de um Codespace Linux.

## 1. Criar o GitHub Codespace

Acesse o repositório:

```
https://github.com/Kk3tillen/Movva
```

No GitHub, selecione **Code → Codespaces → Create codespace on main**. Aguarde o carregamento do ambiente de desenvolvimento.

Quando o terminal estiver aberto, confirme que está na raiz do projeto:

```bash
pwd
ls
```

O diretório correto deve conter arquivos como:

```
androidApp
shared
gradle
gradlew
build.gradle.kts
settings.gradle.kts
```

Se necessário, entre no projeto:

```bash
cd /workspaces/Movva
```

## 2. Verificar o Java

O Android Gradle Plugin utilizado pelo projeto precisa de uma versão compatível do Java. Verifique a versão instalada:

```bash
java -version
```

Se o Java 17 não estiver instalado, instale-o:

```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk
```

Configure o Java 17 no terminal atual:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## 3. Verificar se o Android SDK existe

Antes de instalar qualquer coisa, verifique se o SDK e o `sdkmanager` já estão disponíveis:

```bash
echo "$ANDROID_HOME"
ls -la /usr/local/lib/android/sdk 2>/dev/null || true
ls -la "$HOME/Android/Sdk" 2>/dev/null || true
command -v sdkmanager || true
```

No caso utilizado, o `sdkmanager` não existia. Por isso foi necessário instalar as Android Command-line Tools manualmente.

## 4. Instalar as Android Command-line Tools

O SDK será instalado no diretório do usuário, evitando dependência de permissões administrativas:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

mkdir -p "$ANDROID_HOME/cmdline-tools"
cd /tmp

wget -O commandlinetools.zip \
  https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip

rm -rf /tmp/cmdline-tools
unzip -q commandlinetools.zip -d /tmp

rm -rf "$ANDROID_HOME/cmdline-tools/latest"
mv /tmp/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"

export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

sdkmanager --version
```

Se o endereço do arquivo retornar erro 404, consulte a página oficial das ferramentas Android e copie o link atual para **Command line tools only → Linux**:

```
https://developer.android.com/tools
```

## 5. Instalar os componentes Android usados pelo projeto

O projeto Movva está configurado para usar `compileSdk 36`. Instale a plataforma, o Platform Tools e o Build Tools:

```bash
yes | sdkmanager --licenses

sdkmanager \
  "platform-tools" \
  "platforms;android-36" \
  "build-tools;36.0.0"
```

A instalação pode levar alguns minutos.

Confirme que a plataforma foi instalada:

```bash
ls "$ANDROID_HOME/platforms"
ls "$ANDROID_HOME/build-tools"
```

## 6. Salvar a configuração do SDK

Na raiz do projeto, crie o arquivo `local.properties` apontando para o SDK:

```bash
cd /workspaces/Movva
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
```

Verifique o conteúdo:

```bash
cat local.properties
```

O resultado esperado será parecido com:

```
sdk.dir=/home/ubuntu/Android/Sdk
```

O arquivo `local.properties` é específico do ambiente local. Ele não deve ser commitado no GitHub.

## 7. Salvar as variáveis para futuros terminais

Para não precisar exportar as variáveis toda vez que abrir um terminal:

```bash
cat >> ~/.bashrc <<'EOF'
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
EOF
```

Carregue a configuração no terminal atual:

```bash
source ~/.bashrc
```

Confirme:

```bash
echo "$ANDROID_HOME"
command -v sdkmanager
sdkmanager --version
```

## 8. Dar permissão ao Gradle Wrapper

Na raiz do projeto:

```bash
cd /workspaces/Movva
chmod +x ./gradlew
./gradlew --version
```

O `gradlew` é o Gradle Wrapper do projeto. Ele baixa e utiliza a versão Gradle esperada pelo repositório.

## 9. Gerar o APK Android

Primeiro, limpe builds anteriores:

```bash
./gradlew clean
```

Depois, gere o APK Debug:

```bash
./gradlew :androidApp:assembleDebug
```

Se tudo estiver correto, aparecerá:

```
BUILD SUCCESSFUL
```

O arquivo gerado será:

```
androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Para confirmar que o arquivo existe:

```bash
ls -lh androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

## 10. Baixar o APK pelo explorador do Codespace

No painel de arquivos do Codespace, navegue até:

```
androidApp → build → outputs → apk → debug
```

Clique com o botão direito em `androidApp-debug.apk` e selecione **Download**.

Depois do download, o APK estará disponível no computador local.

## 11. Executar no emulador online MyAndroid

Acesse:

```
https://www.myandroid.org/filemanager_myandroid.php
```

O serviço descreve um fluxo de upload e execução de APK em um emulador Android online.

Utilize o arquivo baixado:

```
androidApp-debug.apk
```

No gerenciador do site, selecione a opção de upload, escolha o APK e aguarde o carregamento. Depois, use a opção de instalar ou executar o aplicativo no emulador.

Se estiver usando a URL temporária do Codespace, o emulador ou o navegador do serviço poderá acessar:

```
https://seu-codespace-8000.app.github.dev/androidApp-debug.apk
```

O comportamento pode depender do navegador, do JavaScript e da disponibilidade temporária do serviço.


## 12. Executar os testes Android

O README do projeto também informa a tarefa de testes Android:

```bash
./gradlew :shared:testAndroidHostTest
```

Para o iOS, a tarefa documentada é:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

Essa tarefa precisa de um ambiente macOS com suporte ao simulador iOS e pode permanecer desabilitada no Codespace.

## Resumo do fluxo completo

```
1. Criar Codespace a partir do repositório
2. Verificar Java
3. Instalar Android Command-line Tools
4. Instalar platform-tools, Android 36 e build-tools
5. Criar local.properties
6. Configurar ANDROID_HOME
7. Executar ./gradlew clean
8. Executar ./gradlew :androidApp:assembleDebug
9. Localizar androidApp-debug.apk
10. Baixar o APK
11. Enviar o APK para o MyAndroid
12. Instalar e executar no emulador online
```

## Comandos principais em um único bloco

Depois que o ambiente já estiver configurado, o processo para novas versões do APK é simplesmente:

```bash
cd /workspaces/Movva
source ~/.bashrc
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
chmod +x ./gradlew
./gradlew clean
./gradlew :androidApp:assembleDebug
ls -lh androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

## Referências

- [Repositório Movva no GitHub](https://github.com/Kk3tillen/Movva)

- [README do Movva](https://raw.githubusercontent.com/Kk3tillen/Movva/main/README.md)

- [Configuração de versões Gradle do Movva](https://raw.githubusercontent.com/Kk3tillen/Movva/main/gradle/libs.versions.toml)

- [Configuração do módulo Android do Movva](https://raw.githubusercontent.com/Kk3tillen/Movva/main/androidApp/build.gradle.kts)

- [Criar um Codespace a partir de um repositório — GitHub Docs](https://docs.github.com/codespaces/developing-in-codespaces/creating-a-codespace)

- [sdkmanager — Android Developers](https://developer.android.com/tools/sdkmanager)

- [Android Command-line Tools — Android Developers](https://developer.android.com/tools)

- [MyAndroid — gerenciador online de APKs](https://www.myandroid.org/android-manager-to-run-apk-online/)

- [MyAndroid — gerenciador de arquivos e execução de APK](https://www.myandroid.org/filemanager_myandroid.php)

---