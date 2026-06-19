create schema if not exists app_private;

create or replace function app_private.current_unum_user_id()
returns text
language sql
stable
security definer
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

grant usage on schema app_private to anon, authenticated, service_role;
grant execute on function app_private.current_unum_user_id() to anon, authenticated, service_role;

alter policy app_users_select_own_header on public.app_users
  using (id = (select app_private.current_unum_user_id()));
alter policy app_users_insert_own_header on public.app_users
  with check (id = (select app_private.current_unum_user_id()));
alter policy app_users_update_own_header on public.app_users
  using (id = (select app_private.current_unum_user_id()))
  with check (id = (select app_private.current_unum_user_id()));

alter policy fortune_books_select_own_header on public.fortune_books
  using (user_id = (select app_private.current_unum_user_id()));
alter policy fortune_books_insert_own_header on public.fortune_books
  with check (user_id = (select app_private.current_unum_user_id()));
alter policy fortune_books_update_own_header on public.fortune_books
  using (user_id = (select app_private.current_unum_user_id()))
  with check (user_id = (select app_private.current_unum_user_id()));
alter policy fortune_books_delete_own_header on public.fortune_books
  using (user_id = (select app_private.current_unum_user_id()));

alter policy star_wallets_select_own_session on public.star_wallets
  using (user_id = (select app_private.current_unum_user_id()));
alter policy star_wallets_insert_own_session on public.star_wallets
  with check (user_id = (select app_private.current_unum_user_id()));
alter policy star_wallets_update_own_session on public.star_wallets
  using (user_id = (select app_private.current_unum_user_id()))
  with check (user_id = (select app_private.current_unum_user_id()));

drop function if exists public.current_unum_user_id();
