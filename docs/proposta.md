## Visão do Produto

**Para** pessoas que treinam e/ou fazem dieta

**Que** têm dificuldade para acompanhar seu progresso de forma unificada

**O Movva** é um aplicativo registrador de treinos e dietas

**Que** simplifica a jornada do estilo de vida saudável, incentivando a constância através de gamificação (como a "fogueirinha")

**Diferente de** concorrentes focados apenas no exercício físico, como o Hevy

**Nosso produto** integra o acompanhamento de treinos e o acompanhamento nutricional em um único lugar, reunindo as duas vertentes essenciais para quem busca resultados

## Definição do MVP

| No MVP | Fora do MVP |
|---|---|
| Login simples de usuário (identificação básica) | Login social (Google/Apple), recuperação de senha |
| Cadastro de treino | Biblioteca de exercícios com vídeos/gifs |
| Cadastro de dieta | Contagem automática de calorias/macros via API externa |
| Check-in diário (treino feito + dieta seguida) | Validação automática da foto (ex: IA verificando se é uma foto real de treino) |
| Streak (fogueirinha) baseada no check-in | Edição/filtros na foto, comentários e curtidas nas fotos do desafio |
| Desafio em grupo com amigos, com foto como comprovação | Funcionamento offline com fila de sincronização |
| | Ranking social / comparação entre usuários |
| | Painel web, relatórios e gráficos avançados |

**Hipótese de valor:** acreditamos que pessoas que treinam e fazem dieta vão manter o check-in diário e participar de desafios no Movva porque a streak visual (fogueirinha) e a comprovação social por foto criam um incentivo simples e imediato pra não quebrar a sequência.

**Observação técnica:** o app assume conexão com internet para funcionar (cadastro, check-in, envio de foto). Funcionamento offline fica fora do escopo do MVP.

## Backlog Inicial

| ID | User Story | Sprint | Prioridade |
|---|---|---|---|
| MOV01 | Como usuário, quero me cadastrar e fazer login para acessar o app de forma identificada | Sprint 0 | Alta |
| MOV02 | Como usuário, quero ter um perfil com um ID visível e copiável para poder ser adicionado por outras pessoas | Sprint 0 | Alta |
| MOV03 | Como usuário, quero registrar meus treinos e minha dieta para acompanhar minha rotina | Sprint 1 | Alta |
| MOV04 | Como usuário, quero fazer um check-in diário (treino feito + dieta seguida) para confirmar minha constância | Sprint 1 | Alta |
| MOV05 | Como usuário, quero ver uma streak (fogueirinha) com o contador de dias seguidos para me sentir motivado a não quebrar a sequência | Sprint 1 | Alta |
| MOV06 | Como usuário, quero criar um grupo de desafio para treinar/fazer dieta em conjunto com amigos | Sprint 2 | Média |
| MOV07 | Como usuário, quero entrar em um grupo de desafio usando um código para participar sem precisar buscar manualmente | Sprint 2 | Média |
| MOV08 | Como usuário, quero adicionar um membro diretamente pelo ID do usuário para facilitar a formação do grupo | Sprint 2 | Baixa |
| MOV09 | Como usuário, quero enviar uma foto como comprovação no check-in do desafio em grupo para validar minha participação | Sprint 3 | Média |
| MOV10 | Como usuário, quero visualizar um histórico/calendário de frequência para revisar minha constância ao longo do tempo | Sprint 3 | Baixa |

### Itens de infraestrutura

Além das user stories, o backlog inclui itens de infraestrutura que dão suporte ao desenvolvimento, mas não representam funcionalidades entregues diretamente ao usuário.

Na Sprint 0, estão previstas as seguintes tarefas:
- Geração do esqueleto do projeto e criação do repositório público, com README (equipe, matrículas, coorte)
- Organização do ambiente de desenvolvimento, definindo variáveis de ambiente e convenções de código a serem seguidas pela equipe
- Configuração do pipeline de CI no GitHub Actions, rodando ktlint e detekt a cada push
- Integração inicial com as APIs do backend

## Plataforma-alvo


## Estratégia de Backend

O Movva adota uma abordagem de **Backend as a Service (BaaS)**, utilizando o **Supabase** como provedor central de backend. Essa escolha reduz o esforço de infraestrutura da equipe (que é pequena) e permite focar o tempo de desenvolvimento nas regras de negócio e na experiência do app, em vez de construir e manter um servidor próprio do zero.

### Camadas do app (cliente)
- **UI**: telas, formulários, navegação e exibição de gráficos/fotos, construídos de forma multiplataforma.
- **Domínio**: casos de uso, modelos e regras de negócio (ex: cálculo da streak, validação de check-in).
- **Dados**: camada responsável por conversar com o Supabase, via SDK oficial e/ou chamadas diretas à API do Supabase, isolando o restante do app dos detalhes de comunicação externa.

### Backend (Supabase)
- **Auth**: responsável por login/cadastro e gerenciamento de sessão do usuário (cobre a MOV01).
- **Banco de dados (Postgres)**: armazena as entidades principais — usuários, perfis, treinos, dietas, check-ins, streaks, grupos de desafio e membros.
- **Storage / Realtime**: armazenamento das fotos de comprovação enviadas nos desafios em grupo (MOV09) e, se necessário, atualizações em tempo real de status do grupo/desafio.
<img width="1360" height="1320" alt="stack-completa" src="https://github.com/user-attachments/assets/2050f506-2434-4898-bdc5-4b6d122f1cae" />


### Alternativas consideradas

- **Backend próprio (Node.js/Express + PostgreSQL):** descartado por exigir provisionar, hospedar e manter infraestrutura própria (servidor, autenticação, backups), o que não é viável para uma equipe de 3 pessoas em 4 sprints.
- **Firebase (Firestore):** descartado porque seu modelo de dados NoSQL dificulta consultas relacionais necessárias para grupos, membros e histórico de check-ins (MOV06–MOV10), que se beneficiam de um modelo relacional como o Postgres.
- **Parse Platform:** descartado por ter uma comunidade e ecossistema menores, com menos documentação e integrações prontas para autenticação e storage do que o Supabase.

O Supabase foi escolhido por entregar Auth, Postgres e Storage/Realtime prontos e gerenciados, com modelo relacional adequado às entidades do Movva, sem exigir infraestrutura própria.

### Justificativa técnica
- Como o app **assume conexão constante com a internet** (sem suporte a modo offline no MVP), um BaaS gerenciado como o Supabase é suficiente e evita overengineering.
- O uso de Postgres gerenciado facilita consultas relacionais necessárias para grupos, membros e histórico de check-ins (MOV06–MOV10).
- A separação em camadas (UI / Domínio / Dados) no cliente mantém a lógica de negócio independente do provedor de backend, permitindo trocar o Supabase por outra solução no futuro com impacto controlado.

### Fluxo principal do app

O diagrama abaixo ilustra a navegação principal: ao abrir o app, verifica-se se há uma sessão ativa (MOV01); caso não haja, o usuário passa por login/cadastro antes de chegar à Home, de onde acessa o registro de treino e dieta (MOV03), o check-in diário que mantém a streak (MOV04, MOV05) e os grupos de desafio (MOV06–MOV09).

<img width="1146" height="792" alt="fluxo-principal-app" src="https://github.com/user-attachments/assets/1d6ddbc2-6237-4e56-bac1-988ca769aed5" />

### Fluxo do desafio em grupo

O diagrama abaixo detalha o fluxo de desafio em grupo: um usuário pode criar um grupo (nome, tipo, meta, datas) ou entrar em um grupo existente por código ou ID (MOV06, MOV07, MOV08); uma vez membro, o check-in com foto é obrigatório no desafio (MOV09) e alimenta o feed do grupo em tempo real.

<img width="1146" height="700" alt="fluxo-desafio-grupo" src="https://github.com/user-attachments/assets/1239570b-4d6f-457f-bb2f-e8aef77c025c" />

## Equipe

| Nome | Matrícula | Papel |
|---|---|---|
| Déborah Kelly Macedo de Sousa | 20240012685 | Desenvolvedor |
| Bruna Lucena da Costa Souto | 20240034898 | Desenvolvedor |
| Kezia Ketillen Santos Lima | 20240034913 | Desenvolvedor |

**Coorte de apresentação:** Online

**Integração com outra disciplina:** Não haverá
