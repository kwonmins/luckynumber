create table if not exists public.app_users (
  id text primary key,
  provider text not null,
  provider_user_id text not null,
  display_name text not null,
  email text,
  avatar_url text,
  last_login_at timestamptz,
  created_at timestamptz not null default now()
);

create unique index if not exists app_users_provider_user_id_idx
  on public.app_users (provider, provider_user_id);

create table if not exists public.fortune_books (
  book_id text primary key,
  user_id text not null references public.app_users(id) on delete cascade,
  book_type text not null,
  cover_title text not null,
  book_json jsonb not null,
  updated_at timestamptz,
  created_at timestamptz not null default now()
);

create index if not exists fortune_books_user_id_idx
  on public.fortune_books (user_id);

create or replace function public.current_unum_user_id()
returns text
language sql
stable
set search_path = ''
as $$
  select nullif(current_setting('request.headers', true)::json ->> 'x-unum-user-id', '')
$$;

alter table public.app_users enable row level security;
alter table public.fortune_books enable row level security;

create policy "app_users_select_own_header" on public.app_users
  for select using (id = (select public.current_unum_user_id()));

create policy "app_users_insert_own_header" on public.app_users
  for insert with check (id = (select public.current_unum_user_id()));

create policy "app_users_update_own_header" on public.app_users
  for update using (id = (select public.current_unum_user_id()))
  with check (id = (select public.current_unum_user_id()));

create policy "fortune_books_select_own_header" on public.fortune_books
  for select using (user_id = (select public.current_unum_user_id()));

create policy "fortune_books_insert_own_header" on public.fortune_books
  for insert with check (user_id = (select public.current_unum_user_id()));

create policy "fortune_books_update_own_header" on public.fortune_books
  for update using (user_id = (select public.current_unum_user_id()))
  with check (user_id = (select public.current_unum_user_id()));
