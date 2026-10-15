# Backend do Check-in Diário (US06)

Este documento traz o SQL que precisa ser executado no **SQL Editor do Supabase**
(Dashboard do projeto → SQL Editor → New query) para suportar a feature de
check-in diário implementada em `shared/src/commonMain/kotlin/br/com/movva/feature/checkin`.

Nada aqui é aplicado automaticamente — não existem migrations versionadas no
repositório (as tabelas `treinos` e `dietas` também foram criadas manualmente
no dashboard). Copie o script abaixo e rode manualmente.

## ⚠️ Antes de rodar: confirme a política de RLS

O app **não autentica no Supabase Auth**. O login (`AuthRepository.login()`)
chama a RPC própria `login_user` e guarda a sessão só localmente no
SQLDelight — o `SupabaseClient` nunca recebe um `sign in`, então `auth.uid()`
tende a ser `NULL` em toda chamada feita com a `anon key`.

Se for esse o caso, a policy `usuario_id = auth.uid()` abaixo **vai bloquear
todo insert/select** em `check_ins`, do mesmo jeito que (presumivelmente) já
bloquearia em `treinos`/`dietas` — a menos que essas tabelas tenham RLS
desligada ou uma policy mais permissiva.

**Antes de aplicar a policy como está**, verifique no dashboard como
`treinos`/`dietas` resolveram isso (RLS desligada? policy `using (true)`?
alguma função `auth.uid()` customizada?) e replique a mesma estratégia aqui.
Se preferir, comece só com a tabela + constraint (sem RLS) para destravar o
desenvolvimento, e trate a policy correta numa task separada de Auth.

## SQL a executar

```sql
create table if not exists public.check_ins (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null,
    data date not null,
    grupo_id uuid null,
    treino_feito boolean not null default false,
    dieta_seguida boolean not null default false,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    -- NULLS NOT DISTINCT exige Postgres 15+ (padrão em projetos novos do Supabase).
    -- Sem isso, o unique não barra duplicatas quando grupo_id é NULL, porque o
    -- Postgres trata cada NULL como distinto dos demais.
    constraint check_ins_usuario_dia_grupo_key
        unique nulls not distinct (usuario_id, data, grupo_id)
);

alter table public.check_ins enable row level security;

create policy "check_ins_select_own" on public.check_ins
    for select using (usuario_id = auth.uid());

create policy "check_ins_upsert_own" on public.check_ins
    for insert with check (usuario_id = auth.uid());

create policy "check_ins_update_own" on public.check_ins
    for update using (usuario_id = auth.uid());
```

Notas sobre o script:

- `grupo_id` fica sem `references` porque a tabela de grupos (MOV06–MOV09)
  ainda não existe no schema. Quando ela for criada, adicione
  `references public.grupos(id) on delete cascade`.
- `usuario_id` também está sem `references` por não sabermos o nome exato da
  tabela/coluna que a RPC `login_user` usa como id de usuário neste projeto.
  Ajuste para `references public.usuarios(id)` (ou o nome correto) se fizer
  sentido no seu schema.
- O app nunca envia `id` no upsert (deixa o banco gerar), e faz
  `upsert(...) { onConflict = "usuario_id,data,grupo_id" }` — por isso o
  `unique` acima precisa cobrir exatamente essas três colunas.

## Como verificar depois de rodar

Rode duas vezes o upsert abaixo (simulando o app marcando o check-in e depois
editando no mesmo dia) e confira que fica **1 única linha**:

```sql
insert into public.check_ins (usuario_id, data, treino_feito, dieta_seguida)
values ('00000000-0000-0000-0000-000000000000', current_date, true, false)
on conflict (usuario_id, data, grupo_id)
do update set treino_feito = excluded.treino_feito,
              dieta_seguida = excluded.dieta_seguida,
              atualizado_em = now();

insert into public.check_ins (usuario_id, data, treino_feito, dieta_seguida)
values ('00000000-0000-0000-0000-000000000000', current_date, true, true)
on conflict (usuario_id, data, grupo_id)
do update set treino_feito = excluded.treino_feito,
              dieta_seguida = excluded.dieta_seguida,
              atualizado_em = now();

select count(*) from public.check_ins
where usuario_id = '00000000-0000-0000-0000-000000000000'
  and data = current_date;
-- esperado: 1
```

Apague a linha de teste depois:

```sql
delete from public.check_ins
where usuario_id = '00000000-0000-0000-0000-000000000000';
```

## Observação sobre o app mobile

Não há migrations configuradas no SQLDelight local (o banco `movva.db` é
criado do zero na primeira execução do app). Como a feature adiciona uma
tabela nova (`checkInEntity`), quem já tiver o app instalado deve
desinstalar/limpar os dados antes de testar — senão o SQLite antigo não terá
essa tabela.
