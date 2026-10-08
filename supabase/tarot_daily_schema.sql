create table if not exists public.daily_tarot (
  user_id text not null references public.app_users(id) on delete cascade,
  draw_date date not null,
  theme text not null check (theme in ('GENERAL','LOVE','WORK','MONEY','SELF')),
  card_id integer not null check (card_id between 0 and 21),
  detail_unlocked boolean not null default false,
  created_at timestamptz not null default now(),
  primary key (user_id, draw_date)
);
alter table public.daily_tarot enable row level security;
revoke all on public.daily_tarot from anon, authenticated;
grant select on public.daily_tarot to anon, authenticated;
grant all on public.daily_tarot to service_role;
create policy daily_tarot_read_own on public.daily_tarot for select to anon, authenticated
  using (user_id = (select app_private.current_unum_user_id()));

create or replace function app_private.draw_daily_tarot(p_theme text)
returns public.daily_tarot language plpgsql security definer set search_path = '' as $$
declare
  v_user text := app_private.current_unum_user_id();
  v_date date := (current_timestamp at time zone 'Asia/Seoul')::date;
  v_draw public.daily_tarot;
begin
  if v_user is null then raise exception 'login_required' using errcode = '28000'; end if;
  if p_theme is null or p_theme not in ('GENERAL','LOVE','WORK','MONEY','SELF') then raise exception 'invalid_theme'; end if;
  insert into public.daily_tarot(user_id, draw_date, theme, card_id)
    values(v_user, v_date, p_theme, floor(random() * 22)::integer)
    on conflict(user_id, draw_date) do nothing;
  select * into strict v_draw from public.daily_tarot where user_id=v_user and draw_date=v_date;
  return v_draw;
end $$;
revoke all on function app_private.draw_daily_tarot(text) from public;
grant execute on function app_private.draw_daily_tarot(text) to anon, authenticated;
create or replace function public.draw_daily_tarot(p_theme text)
returns public.daily_tarot language sql security invoker set search_path = '' as $$
  select app_private.draw_daily_tarot(p_theme)
$$;
revoke all on function public.draw_daily_tarot(text) from public;
grant execute on function public.draw_daily_tarot(text) to anon, authenticated;

create or replace function public.daily_tarot_status()
returns jsonb language sql stable security invoker set search_path = '' as $$
  select jsonb_build_object('today', (current_timestamp at time zone 'Asia/Seoul')::date,
    'draw', (select to_jsonb(t) from public.daily_tarot t
      where t.user_id=app_private.current_unum_user_id()
      and t.draw_date=(current_timestamp at time zone 'Asia/Seoul')::date))
$$;
revoke all on function public.daily_tarot_status() from public;
grant execute on function public.daily_tarot_status() to anon, authenticated;

-- A non-monetary content unlock. Clients cannot insert, delete, change dates or reroll cards.
create or replace function app_private.unlock_daily_tarot()
returns public.daily_tarot language plpgsql security definer set search_path = '' as $$
declare v_user text := app_private.current_unum_user_id(); v_draw public.daily_tarot;
begin
  if v_user is null then raise exception 'login_required' using errcode = '28000'; end if;
  update public.daily_tarot set detail_unlocked=true where user_id=v_user
    and draw_date=(current_timestamp at time zone 'Asia/Seoul')::date returning * into v_draw;
  if v_draw.user_id is null then raise exception 'draw_required'; end if;
  return v_draw;
end $$;
revoke all on function app_private.unlock_daily_tarot() from public;
grant execute on function app_private.unlock_daily_tarot() to anon, authenticated;
create or replace function public.unlock_daily_tarot()
returns public.daily_tarot language sql security invoker set search_path = '' as $$
  select app_private.unlock_daily_tarot()
$$;
revoke all on function public.unlock_daily_tarot() from public;
grant execute on function public.unlock_daily_tarot() to anon, authenticated;

create table if not exists public.discovery_wallets (
  user_id text primary key references public.app_users(id) on delete cascade,
  balance integer not null default 0 check(balance >= 0),
  attendance_date date,
  streak integer not null default 0 check(streak >= 0)
);
alter table public.discovery_wallets enable row level security;
revoke all on public.discovery_wallets from anon, authenticated;
grant select on public.discovery_wallets to anon, authenticated;
grant all on public.discovery_wallets to service_role;
create policy discovery_wallets_read_own on public.discovery_wallets for select to anon, authenticated
  using (user_id=(select app_private.current_unum_user_id()));

create or replace function app_private.claim_daily_attendance()
returns public.discovery_wallets language plpgsql security definer set search_path = '' as $$
declare
  v_user text := app_private.current_unum_user_id();
  v_date date := (current_timestamp at time zone 'Asia/Seoul')::date;
  v_wallet public.discovery_wallets;
begin
  if v_user is null then raise exception 'login_required' using errcode='28000'; end if;
  insert into public.discovery_wallets(user_id) values(v_user) on conflict(user_id) do nothing;
  select * into strict v_wallet from public.discovery_wallets where user_id=v_user for update;
  if v_wallet.attendance_date is null or v_wallet.attendance_date < v_date then
    update public.discovery_wallets set balance=balance+5,
      streak=case when attendance_date=v_date-1 then streak+1 else 1 end,
      attendance_date=v_date where user_id=v_user returning * into v_wallet;
  end if;
  return v_wallet;
end $$;
revoke all on function app_private.claim_daily_attendance() from public;
grant execute on function app_private.claim_daily_attendance() to anon, authenticated;
create or replace function public.claim_daily_attendance()
returns public.discovery_wallets language sql security invoker set search_path = '' as $$
  select app_private.claim_daily_attendance()
$$;
revoke all on function public.claim_daily_attendance() from public;
grant execute on function public.claim_daily_attendance() to anon, authenticated;
create or replace function public.daily_tarot_status()
returns jsonb language sql stable security invoker set search_path = '' as $$
  select jsonb_build_object('today', (current_timestamp at time zone 'Asia/Seoul')::date,
    'draw', (select to_jsonb(t) from public.daily_tarot t where t.user_id=app_private.current_unum_user_id()
      and t.draw_date=(current_timestamp at time zone 'Asia/Seoul')::date),
    'wallet', (select to_jsonb(w) from public.discovery_wallets w where w.user_id=app_private.current_unum_user_id()))
$$;
