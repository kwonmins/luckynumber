grant delete on table public.fortune_books to anon, authenticated, service_role;

do $$
begin
  if not exists (
    select 1
    from pg_policies
    where schemaname = 'public'
      and tablename = 'fortune_books'
      and policyname = 'fortune_books_delete_own_header'
  ) then
    create policy "fortune_books_delete_own_header" on public.fortune_books
      for delete using (user_id = (select public.current_unum_user_id()));
  end if;
end $$;
