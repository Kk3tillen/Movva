# Backend dos Exercícios e Execuções do Treino

Este documento traz o SQL que precisa ser executado no **SQL Editor do Supabase**
(Dashboard do projeto → SQL Editor → New query) para sincronizar os exercícios
cadastrados na tela de Novo Treino (`components/treino`).

Nada aqui é aplicado automaticamente. Enquanto a tabela não existir, o app
continua funcionando: os exercícios ficam salvos localmente (`exercicioEntity`)
com `sincronizado = 0` e são reenviados na próxima vez que a lista de treinos
for carregada. A falha no envio dos exercícios não bloqueia a sincronização
dos treinos.

## ⚠️ RLS

Mesma situação descrita em [check-in.md](check-in.md): o app não autentica no
Supabase Auth, então `auth.uid()` tende a ser `NULL`. Replique aqui a mesma
estratégia de RLS usada em `treinos`.

## SQL a executar

```sql
create table if not exists public.treino_exercicios (
    id uuid primary key,
    treino_id uuid not null references public.treinos(id) on delete cascade,
    usuario_id uuid not null,
    ordem integer not null,
    nome text not null,
    series integer not null check (series > 0),
    reps integer not null check (reps > 0),
    carga_kg numeric(6, 1) null check (carga_kg >= 0),
    criado_em timestamptz not null default now()
);

create index if not exists treino_exercicios_treino_id_idx
    on public.treino_exercicios (treino_id, ordem);
```

Notas:

- O `id` é gerado no app (UUID), por isso não tem `default`. O envio é feito
  com `upsert`, então reenviar o mesmo exercício não duplica a linha.
- Se `treinos.id` não for do tipo `uuid` no seu schema, ajuste o tipo de
  `treino_id` para o mesmo tipo.
- O app só envia exercícios de treinos que já foram sincronizados, para
  respeitar a foreign key.

## Execuções (cada vez que o treino é feito)

Um treino é uma ficha reutilizável: a pessoa pode fazê-lo várias vezes.
Cada vez que finaliza o treino na tela de Detalhe, o app grava uma
**execução** com a data do dia e as séries registradas. Finalizar de novo no
mesmo dia substitui a execução daquele dia; em outro dia, cria uma nova.

```sql
create table if not exists public.treino_execucoes (
    treino_id uuid not null references public.treinos(id) on delete cascade,
    usuario_id uuid not null,
    data date not null,
    -- [{ "exercicio_id", "numero", "reps", "carga_kg", "concluida" }, ...]
    series jsonb not null default '[]'::jsonb,
    atualizado_em timestamptz not null default now(),
    primary key (treino_id, data)
);
```

O app faz `upsert(...) { onConflict = "treino_id,data" }`, por isso a chave
primária precisa ser exatamente essas duas colunas.

Por enquanto as execuções só são **enviadas** ao Supabase: o "Feito hoje" é
calculado com o que está no aparelho. Um treino feito em outro aparelho não
aparece como feito até existir a leitura das execuções remotas.
