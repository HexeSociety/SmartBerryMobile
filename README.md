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
