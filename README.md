# Movva

> Treino e dieta, todo dia.

O **Movva** é um aplicativo registrador de **treinos e dietas** que incentiva a constância por meio de gamificação (a "fogueirinha", um contador de dias seguidos). Projeto da disciplina **DIM0524 – Desenvolvimento de Dispositivos Móveis**, feito com Kotlin Multiplatform para Android e iOS.

## Sobre o projeto

**Problema.** Quem treina e faz dieta costuma acompanhar o progresso em apps separados e acaba desistindo por falta de constância.

**Público-alvo.** Pessoas que treinam e/ou fazem dieta e querem acompanhar a rotina de forma unificada.

**Proposta de valor.** Reunir o acompanhamento de treino e de alimentação em um único lugar. Diferente de apps focados só em exercício físico, como o Hevy, o Movva une as duas frentes. A hipótese é que a streak visual cria um incentivo simples e imediato para não quebrar a sequência.

A proposta completa está em [`docs/sprint-0/proposta.md`](docs/sprint-0/proposta.md).

## Funcionalidades

| Funcionalidade | Status |
|---|---|
| Login por e-mail e senha | ✅ |
| Perfil com nome e ID (`user_tag`) copiável | ✅ |
| Cadastro e listagem de treinos | ✅ |
| Cadastro e listagem de refeições (dieta) | ✅ |
| Navegação entre telas, tema claro/escuro e responsividade | 🚧 Em andamento |
| Streak (contador de dias seguidos) | 🔜 Sprint 2 |
| Histórico e calendário de frequência | 🔜 Sprint 2 |
| Tela Home (resumo do dia) | 🔜 Sprint 2 |
| Testes de interface no CI | 🔜 Sprint 2 |

**Fora do escopo do MVP:** desafio em grupo com amigos (planejado como funcionalidade futura), login social, recuperação de senha, contagem automática de calorias e macros, funcionamento offline completo, ranking social e painel web.

## Tecnologias

- **Kotlin Multiplatform** e **Compose Multiplatform**, com Material 3, para compartilhar a UI entre Android e iOS
- **Supabase** (Postgres e Auth) como backend, acessado com `supabase-kt`
- **SQLDelight** para o cache local
- **Koin** para injeção de dependências
- **Ktor** e **kotlinx.serialization** para rede e serialização
- **GitHub Actions** para integração contínua

## Arquitetura

O código compartilhado fica no módulo `shared` e segue três camadas: **UI** (telas Compose e ViewModels), **Domínio** (modelos e regras, como a validação dos formulários) e **Dados** (repositórios com fontes de dados local e remota).

```
Movva/
├── androidApp/            # ponto de entrada do app Android
├── iosApp/                # ponto de entrada do app iOS (Xcode)
├── shared/
│   └── src/
│       ├── commonMain/    # UI, ViewModels, repositórios e SQLDelight
│       ├── androidMain/   # implementações específicas do Android
│       └── iosMain/       # implementações específicas do iOS
├── docs/                  # proposta e documentação das sprints
└── .github/workflows/     # pipelines de CI
```

Dentro de `br.com.movva`, cada funcionalidade tem seu pacote em `feature/<nome>` (`perfil`, `treino`, `dieta`), dividido em `data`, `domain`, `di` e `ui`. Os pacotes `auth` e `core` guardam o que é compartilhado (login, tema, componentes e rede).

**Salvar local primeiro.** Ao registrar um treino ou uma refeição, o app grava no cache local e só depois envia ao Supabase. Se o envio falhar, o registro continua salvo no aparelho, marcado como pendente, e é reenviado quando o app abre de novo. As listas mostram o cache na hora e se atualizam depois de buscar os dados remotos.

## Como executar

### Pré-requisitos

- Android Studio e JDK 17
- Xcode (apenas para rodar o iOS localmente, em um Mac)
- Um projeto Supabase com as tabelas do app

### Configuração do Supabase

Crie (ou edite) o arquivo `local.properties` na raiz do projeto e adicione a URL e a chave pública do seu projeto Supabase:

```properties
supabase.url=https://SEU-PROJETO.supabase.co
supabase.anonKey=SUA_CHAVE_PUBLICA
```

Sem isso, o build do módulo `shared` falha. O `local.properties` não é versionado. No CI, os mesmos valores vêm das variáveis de ambiente `SUPABASE_URL` e `SUPABASE_ANON_KEY`.

### Android

Use a configuração de execução do Android Studio ou rode:

```bash
./gradlew :androidApp:assembleDebug
```

No Windows, use `.\gradlew.bat :androidApp:assembleDebug`.

> Se o app fechar com um erro do tipo `no such table`, o banco local do aparelho está desatualizado. Desinstale o app e instale de novo.

### iOS

- **Com Mac:** abra a pasta [`iosApp`](iosApp) no Xcode e execute por lá.
- **Sem Mac:** rode o workflow manual **iOS device IPA (personal sideload)** na aba *Actions*, baixe o artefato `.ipa` e instale no iPhone com uma ferramenta de sideload, como o Sideloadly.


## Equipe

| Nome | Papel |
|---|---|
| Déborah Kelly Macedo de Sousa | Desenvolvedora |
| Bruna Lucena da Costa Souto | Desenvolvedora |
| Kezia Ketillen Santos Lima | Desenvolvedora |