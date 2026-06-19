create or replace function public.current_unum_user_id()
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

revoke all on function public.current_unum_user_id() from public;
grant execute on function public.current_unum_user_id() to anon, authenticated, service_role;
