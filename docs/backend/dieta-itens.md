# Backend dos Itens da Refeição

A tela "Registrar refeição" (`components/dieta`) agora salva os alimentos de
cada refeição, com quantidade, calorias e macros. Eles vão numa coluna nova
`itens` (jsonb) da tabela `dietas`, que já existe.

Rode no **SQL Editor do Supabase**:

```sql
alter table public.dietas
    add column if not exists itens jsonb not null default '[]'::jsonb;
```

Formato de cada item:

```json
{
  "alimento_id": "arroz-branco",
  "nome": "Arroz branco cozido",
  "icone": "🍚",
  "quantidade_g": 150,
  "porcao_nome": "xícara",
  "quantidade_porcao": 1,
  "kcal": 192,
  "proteina_g": 3.75,
  "carbo_g": 42.15,
  "gordura_g": 0.3
}
```

## ⚠️ Enquanto a coluna não existir

O app passou a enviar `itens` em todo envio para `dietas` e a usar `upsert`
(para permitir editar uma refeição). Sem a coluna, o Supabase recusa o envio:
as refeições **continuam salvas no aparelho** (`sincronizado = 0`) e são
reenviadas automaticamente quando a coluna for criada. Nada se perde, mas
nada sobe até lá.

O `upsert` usa a chave primária `id` da tabela `dietas`. Se a política de RLS
da tabela só permitir `insert`, adicione também a de `update`, seguindo a
mesma estratégia já usada nas outras tabelas (ver [check-in.md](check-in.md)).

## Alimentos

Os alimentos vêm de um catálogo fixo no app
(`components/dieta/domain/CatalogoAlimentos.kt`), com valores aproximados da
Tabela TACO. Não há tabela de alimentos no Supabase.

## Refeições feitas (marcação por dia)

A refeição é reutilizável: o botão de check ao lado de cada refeição na aba
Dieta marca que ela foi feita **hoje**. No dia seguinte o check aparece vazio
de novo, com "Última vez: dd/mm/aaaa". Marcar uma refeição como feita é o que
conta como check-in de dieta do dia; só registrar a refeição não conta mais.

```sql
create table if not exists public.dieta_refeicoes_feitas (
    dieta_id uuid not null references public.dietas(id) on delete cascade,
    usuario_id uuid not null,
    data date not null,
    -- desmarcar grava false em vez de apagar a linha
    feita boolean not null default true,
    atualizado_em timestamptz not null default now(),
    primary key (dieta_id, data)
);
```

O app faz `upsert(...) { onConflict = "dieta_id,data" }`. Se `dietas.id` não
for `uuid`, ajuste o tipo de `dieta_id`. Assim como nas execuções de treino,
as marcações só são **enviadas**: o "feita hoje" é calculado com o que está no
aparelho.
