create table if not exists public.star_wallets (
  user_id text primary key references public.app_users(id) on delete cascade,
  balance integer not null default 0 check (balance >= 0),
  lifetime_earned integer not null default 0 check (lifetime_earned >= 0),
  signup_bonus_claimed boolean not null default false,
  last_attendance_date date,
  attendance_streak integer not null default 0 check (attendance_streak >= 0),
  last_ad_reward_date date,
  daily_ad_reward_count integer not null default 0 check (daily_ad_reward_count >= 0 and daily_ad_reward_count <= 20),
  last_ad_reward_at bigint,
  last_premium_use_date date,
  daily_premium_use_count integer not null default 0 check (daily_premium_use_count >= 0 and daily_premium_use_count <= 1),
  referral_code text unique,
  referred_by text,
  referral_bonus_claimed boolean not null default false,
  beta_feedback_reward_claimed boolean not null default false,
  share_reward_count integer not null default 0 check (share_reward_count >= 0),
  updated_at timestamptz,
  created_at timestamptz not null default now()
);

create index if not exists star_wallets_referral_code_idx
  on public.star_wallets (referral_code);

alter table public.star_wallets enable row level security;

grant select, insert, update on table public.star_wallets to anon, authenticated, service_role;

create policy "star_wallets_select_own_session" on public.star_wallets
  for select using (user_id = (select public.current_unum_user_id()));

create policy "star_wallets_insert_own_session" on public.star_wallets
  for insert with check (user_id = (select public.current_unum_user_id()));

create policy "star_wallets_update_own_session" on public.star_wallets
  for update using (user_id = (select public.current_unum_user_id()))
  with check (user_id = (select public.current_unum_user_id()));
