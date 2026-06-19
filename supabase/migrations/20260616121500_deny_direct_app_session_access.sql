create policy "app_sessions_deny_direct_access" on public.app_sessions
  for all
  using (false)
  with check (false);
