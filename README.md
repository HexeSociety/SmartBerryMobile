# 🍓 SmartBerry 360°

> Gestão inteligente para a sua estufa — aplicativo Android para monitoramento e controle de cultivo.

![Plataforma](https://img.shields.io/badge/plataforma-Android-3DDC84?logo=android&logoColor=white)
![Linguagem](https://img.shields.io/badge/linguagem-Java%2011-ED8B00?logo=openjdk&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-24-blue)
![targetSdk](https://img.shields.io/badge/targetSdk-36-blue)
![Status](https://img.shields.io/badge/status-protótipo%20front--end-yellow)

## 📖 Sobre o projeto

O **SmartBerry 360°** é um aplicativo mobile voltado a produtores que querem acompanhar e controlar a estufa pelo celular. A proposta é reunir em um só lugar os dados dos sensores (temperatura e umidade), o controle de **irrigação** e **ventilação** (manual ou agendado), o histórico de ciclos de irrigação e os alertas do sistema de monitoramento.



## 🛠️ Tecnologias

- **Java 11** com Android SDK nativo (views em XML)
- **Material Components** (`BottomNavigationView`, `BottomSheetDialog`, `MaterialTimePicker`, `MaterialAlertDialogBuilder`, `MaterialSwitch`, `MaterialCardView`)
- **AndroidX**: AppCompat, ConstraintLayout, CardView, RecyclerView, EdgeToEdge
- **Google Fonts** (K2D e Inter) como fontes baixáveis
- **Gradle** (Groovy) com catálogo de versões (`gradle/libs.versions.toml`)

| Item | Versão |
| --- | --- |
| Android Gradle Plugin | 9.1.1 |
| Gradle (wrapper) | 9.3.1 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 (Android 7.0) |
| Material Components | 1.14.0 |
| AppCompat | 1.8.0 |
| ConstraintLayout | 2.2.2 |

> O plugin do Jetpack Compose e o Compose BoM já estão configurados no build, mas as telas atuais usam views XML.

## 📁 Estrutura do projeto

```
SmartBerryMobile/
├── app/
│   └── src/main/
│       ├── java/com/example/smartberry/
│       │   ├── BaseActivity.java            # Configura a barra de navegação inferior
│       │   ├── MainActivity.java            # Activity de entrada (launcher)
│       │   ├── CarregamentoActivity.java
│       │   ├── LoginActivity.java
│       │   ├── CadastroActivity.java
│       │   ├── CadastroEstufaActivity.java
│       │   ├── DashboardActivity.java
│       │   ├── ControleActivity.java
│       │   ├── HistoricoActivity.java
│       │   ├── AlertasActivity.java
│       │   ├── PerfilActivity.java
│       │   ├── adapters/                    # CicloAdapter, HorarioAdapter
│       │   └── models/                      # Ciclo, Horario
│       ├── res/
│       │   ├── layout/                      # Telas e itens de lista
│       │   ├── drawable/                    # Ícones, logos, bolhas decorativas
│       │   ├── font/                        # K2D e Inter (Google Fonts)
│       │   ├── menu/bottom_nav_menu.xml     # Itens da navbar
│       │   └── values/                      # Cores, estilos e temas
│       └── AndroidManifest.xml
├── gradle/libs.versions.toml                # Catálogo de dependências
├── build.gradle
└── settings.gradle
```

## 🚀 Como executar

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) em versão recente, compatível com o AGP 9.1.x
- JDK 17 ou superior (o Android Studio já inclui um)
- Emulador ou dispositivo com **Android 7.0 (API 24)** ou superior
- Para as fontes carregarem corretamente, use um emulador com **Google Play** (ou um aparelho com Google Play Services) e conexão com a internet

### Passo a passo

1. Clone o repositório:

   ```bash
   git clone https://github.com/HexeSociety/SmartBerryMobile.git
   cd SmartBerryMobile
   ```

2. Abra a pasta no Android Studio e aguarde a sincronização do Gradle.
3. Selecione um emulador ou dispositivo e clique em **Run ▶**.

Também é possível compilar pela linha de comando:

```bash
./gradlew assembleDebug      # Linux/macOS
gradlew.bat assembleDebug    # Windows
```

### Visualizando as telas

A `MainActivity`, que é a porta de entrada configurada no manifesto, ainda é uma tela vazia. Para ver cada tela, abra **Run ▶ Edit Configurations ▶ Launch Options ▶ Launch: Specified Activity** e escolha a que deseja (por exemplo `LoginActivity` ou `DashboardActivity`). Também é possível abrir uma Activity pelo adb:

```bash
adb shell am start -n com.example.smartberry/.LoginActivity
```

> As telas ainda não estão encadeadas entre si. Hoje a navegação funciona entre as abas da barra inferior (Início, Controle, Alertas e Perfil) e do Início para o Histórico.


## 👥 Equipe

Projeto desenvolvido pela organização **[HexeSociety](https://github.com/HexeSociety)**:

- **Marina Luiza de Araujo Paiva**
- **Julia Rocha Nogueira**
- **Raul Corrêa**
- **Murillo Miguel Ozorio de Souza**
- **Cauã de Oliveira Santos**
- **Matheus Vieira Peres**

## Executar o MVP na rede local

1. Inicie a SmartBerryAPI com MongoDB e defina `SMARTBERRY_API_KEY` no servidor.
2. Configure o firmware conforme o README do SmartBerryIoT, usando o mesmo endereço e a mesma chave.
3. Com Java 21 e o Android SDK configurados, gere o APK com os valores da rede da apresentação.

```bash
export SMARTBERRY_API_URL=http://IP_DO_COMPUTADOR:8080
export SMARTBERRY_API_KEY=CHAVE_LOCAL_DA_API
bash gradlew :app:assembleDebug :app:testDebugUnitTest
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`. Em um emulador, use `http://10.0.2.2:8080`. Em um celular, use o IP do computador na rede compartilhada com o ESP32. A chave é incorporada ao APK do MVP, destinado à rede local da apresentação.

Depois do login, o Dashboard encontra automaticamente o único ESP32 ativo enviando leituras à API. O parâmetro `SMARTBERRY_ESP32_ID` é opcional e permite escolher uma placa específica se houver várias online. Os controles manuais mostram o retorno do ESP32. A bomba recebe um comando de até 30 segundos e pode ser parada antes. Configure os horários de irrigação e ventilação na tela Controle. No histórico, as medições vêm do DHT22. Os cartões mostram partidas manuais confirmadas e a duração programada. Um traço indica que o volume de água está indisponível.

## Autenticação

Consulte o [guia de configuração do Firebase](docs/FIREBASE-AUTENTICACAO.md) para cadastrar o Android, habilitar e-mail e Google e testar o login.

## Irrigação e ventilação no Android

Em **Controle**, escolha **Adicionar horário** na seção do equipamento. Selecione **Uma vez** ou **Todos os dias**, informe a data inicial, o horário de São Paulo e uma duração de 1 a 5 minutos. Cada equipamento aceita vários horários. Toque em uma linha para editar uma programação pendente ou excluir um horário.

Os dois interruptores **Modo Automático** gerenciam as ocorrências diárias de cada equipamento. Uma programação pontual funciona mesmo com o interruptor desligado. A API guarda a configuração e processa os horários com o aplicativo fechado. O ESP32 executa os comandos e desliga o relé quando a duração termina.
