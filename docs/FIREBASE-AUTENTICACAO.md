# Configuração do Firebase Authentication no SmartBerry

O SmartBerry usa o Firebase Authentication para criar contas e entrar por e-mail e senha ou pela conta Google. A autenticação precisa de acesso à internet. A API de monitoramento e o ESP32 continuam na rede local e usam a mesma chave do SmartBerry que já está configurada.

## 1. Escolher o identificador do aplicativo

Use `com.example.smartberry.auth` para testar no celular da apresentação. Essa variante fica instalada ao lado do SmartBerry que já está funcionando. Use `com.example.smartberry` para a compilação padrão, por exemplo em um emulador separado.

Na raiz do repositório Android, com JDK 21 e Android SDK configurados, execute:

```bash
bash gradlew :app:signingReport
```

Na saída, procure a variante `debug` e guarde os valores SHA-1 e SHA-256. Use as impressões da máquina e da chave que assinam o APK que será instalado. Se fizer uma versão de distribuição assinada com outra chave, inclua também as impressões dessa assinatura no aplicativo correspondente do Firebase.

O APK de debug gerado neste computador usa as impressões abaixo. Se recompilar em outra máquina, confirme pelo comando acima.

```text
SHA-1   8A:67:6C:57:40:65:DB:12:67:A2:9C:01:10:99:56:A7:C8:68:0B:54
SHA-256 39:F8:8F:7D:33:6C:AB:CC:FA:28:5A:2A:CA:A4:10:91:BC:31:D4:FC:B8:7E:B9:46:B9:C3:E6:43:5A:3E:69:D2
```

## 2. Criar o projeto e cadastrar o Android

1. Abra o [Console do Firebase](https://console.firebase.google.com/) e entre na sua conta Google.
2. Selecione **Criar projeto**, dê um nome como **SmartBerry 360** e conclua. Analytics é opcional para este fluxo.
3. Na visão geral do projeto, clique no ícone do **Android**. Digite exatamente o identificador escolhido no passo anterior e cadastre o aplicativo.
4. Em **Configurações do projeto → Geral → Seus apps**, abra o aplicativo Android e adicione o SHA-1 e o SHA-256 do relatório. Repita o cadastro para o segundo identificador se for usar as duas variantes. [Guia oficial de configuração do Android](https://firebase.google.com/docs/android/setup)

## 3. Habilitar os métodos de acesso

1. No menu do projeto, abra **Authentication** e clique em **Começar**.
2. Na aba **Método de login**, habilite **E-mail/senha** e salve. O método de link de e-mail pode ficar desativado.
3. Na mesma aba, adicione **Google**, habilite-o e escolha um e-mail de suporte para o projeto. Salve. Se o console solicitar a configuração da tela de consentimento OAuth, conclua essa etapa no projeto vinculado.
4. Volte a **Configurações do projeto → Geral → Seus apps**, abra o Android com o identificador certo e baixe novamente o `google-services.json`. Coloque o arquivo em `SmartBerryMobile/app/google-services.json`. O arquivo baixado depois de habilitar o Google traz a configuração OAuth que o botão usa. As dependências e o plugin já estão no projeto. [Login Google no Firebase](https://firebase.google.com/docs/auth/android/google-signin) · [Login por e-mail e senha](https://firebase.google.com/docs/auth/android/password-auth)

## 4. Compilar o APK com a API local

A partir de `SmartBerryMobile`, carregue a configuração local usada na integração com o ESP32. Gere a variante paralela para testar no mesmo celular.

```bash
source ../SmartBerryAPI/.smartberry-local.env
bash gradlew :app:assembleDebug -PSMARTBERRY_AUTH_TEST_APP=true
```

Instale o arquivo `app/build/outputs/apk/debug/app-debug.apk`. Para gerar a versão padrão, use o JSON que contém `com.example.smartberry` e execute o comando sem `-PSMARTBERRY_AUTH_TEST_APP=true`. Se o Gradle indicar que não encontrou um cliente, confira se o `package_name` dentro do JSON coincide com o identificador do APK. A chave em `.smartberry-local.env` é a chave da API local e continua separada da configuração Firebase.

## 5. Verificar no celular

1. Abra a variante nova. Toque em **Cadastrar**, preencha nome, e-mail e senha, avance para a estufa e finalize. Confira a conta em **Authentication → Users**.
2. Saia pelo Perfil. Entre com o e-mail e a senha e selecione **Lembrar**. Feche o app, abra-o novamente pelo ícone e confirme que volta ao Dashboard.
3. Saia, entre de novo e escolha **Só agora**. Force a parada do app em **Configurações → Aplicativos** e reabra pelo ícone para confirmar que o login é solicitado. A senha não é salva nas preferências do SmartBerry.
4. Teste **Esqueceu a senha?** com o e-mail da conta. Depois use **Entrar com o Google** com uma conta Google disponível no celular. Confira se o Dashboard abre e se o usuário aparece no Firebase.
5. Confira que o Dashboard recebe dados do ESP32 ao voltar à rede da apresentação. Os dados complementares de cadastro da estufa ficam salvos por conta neste aparelho. O nome de exibição da conta é salvo no Firebase.

Se o Google falhar, confira o identificador do Android, as impressões SHA, o método Google ativo e se o JSON foi baixado novamente. Use um aparelho com a conta Google configurada e os serviços Google Play disponíveis. [Verificação do login Google](https://firebase.google.com/docs/auth/android/google-signin)

## Voltar à versão anterior

A instalação `com.example.smartberry.lan` que já exibe as leituras fica separada da variante de teste `com.example.smartberry.auth`. Para voltar à demonstração, abra a instalação anterior.

A cópia local em `.smartberry-checkpoints/antes-autenticacao/` contém os dois APKs anteriores e um arquivo compactado por repositório. Se precisar recuperar código, extraia o arquivo referente ao repositório em uma pasta separada e compare com o projeto atual antes de substituir arquivos. Mantenha as configurações locais da API e do firmware usadas na rede.
