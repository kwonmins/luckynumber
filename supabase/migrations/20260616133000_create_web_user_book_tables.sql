create extension if not exists pgcrypto with schema extensions;

create table if not exists public.web_users (
  id text primary key,
  provider text not null,
  provider_user_id text not null,
  display_name text not null,
  email text,
  avatar_url text,
  last_login_at timestamptz,
  created_at timestamptz not null default now()
);

create unique index if not exists web_users_provider_user_id_idx
  on public.web_users (provider, provider_user_id);

create table if not exists public.web_fortune_books (
  book_id text primary key,
  user_id text not null references public.web_users(id) on delete cascade,
  book_type text not null,
  cover_title text not null,
  book_json jsonb not null,
  updated_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

create index if not exists web_fortune_books_user_id_idx
  on public.web_fortune_books (user_id);

create table if not exists public.web_sessions (
  token_hash text primary key,
  user_id text not null references public.web_users(id) on delete cascade,
  provider text not null,
  provider_user_id text not null,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null,
  last_used_at timestamptz
);

create index if not exists web_sessions_user_id_idx
  on public.web_sessions (user_id);

create index if not exists web_sessions_expires_at_idx
  on public.web_sessions (expires_at);

alter table public.web_users enable row level security;
alter table public.web_fortune_books enable row level security;
alter table public.web_sessions enable row level security;

create or replace function public.current_unum_web_user_id()
returns text
language sql
stable
set search_path = ''
as $$
  with headers as (
    select coalesce(nullif(current_setting('request.headers', true), ''), '{}')::json as value
  )
  select sessions.user_id
  from public.web_sessions as sessions, headers
  where sessions.token_hash = encode(
      extensions.digest(coalesce(headers.value ->> 'x-unum-web-session-token', ''), 'sha256'),
      'hex'
    )
    and sessions.expires_at > now()
  limit 1
$$;

create policy "web_users_select_own_session" on public.web_users
  for select using (id = (select public.current_unum_web_user_id()));

create policy "web_fortune_books_select_own_session" on public.web_fortune_books
  for select using (user_id = (select public.current_unum_web_user_id()));

create policy "web_fortune_books_insert_own_session" on public.web_fortune_books
  for insert with check (user_id = (select public.current_unum_web_user_id()));

create policy "web_fortune_books_update_own_session" on public.web_fortune_books
  for update using (user_id = (select public.current_unum_web_user_id()))
  with check (user_id = (select public.current_unum_web_user_id()));

create policy "web_fortune_books_delete_own_session" on public.web_fortune_books
  for delete using (user_id = (select public.current_unum_web_user_id()));

create policy "web_sessions_deny_direct_access" on public.web_sessions
  for all
  using (false)
  with check (false);
