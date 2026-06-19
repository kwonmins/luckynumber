create extension if not exists pgcrypto with schema extensions;

create table if not exists public.app_sessions (
  token_hash text primary key,
  user_id text not null references public.app_users(id) on delete cascade,
  provider text not null,
  provider_user_id text not null,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null,
  last_used_at timestamptz
);

create index if not exists app_sessions_user_id_idx
  on public.app_sessions (user_id);

create index if not exists app_sessions_expires_at_idx
  on public.app_sessions (expires_at);

alter table public.app_sessions enable row level security;

create or replace function public.current_unum_user_id()
returns text
language sql
stable
set search_path = ''
as $$
  with headers as (
    select coalesce(nullif(current_setting('request.headers', true), ''), '{}')::json as value
  )
  select sessions.user_id
  from public.app_sessions as sessions, headers
  where sessions.token_hash = encode(
      extensions.digest(coalesce(headers.value ->> 'x-unum-session-token', ''), 'sha256'),
      'hex'
    )
    and sessions.expires_at > now()
  limit 1
$$;
