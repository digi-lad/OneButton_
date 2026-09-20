-- Users (extends Supabase Auth)
create table profiles (
  id          uuid primary key references auth.users(id),
  role        text not null check (role in ('elderly', 'caretaker')),
  display_name text not null,
  household_id uuid references households(id),
  fcm_token   text,           -- for push notifications
  created_at  timestamptz default now()
);

-- Households
create table households (
  id              uuid primary key default gen_random_uuid(),
  elderly_user_id uuid references auth.users(id),
  elderly_name    text not null,
  last_active_at  timestamptz default now(),
  emergency_number text,
  created_at      timestamptz default now()
);

-- Reminders
create table reminders (
  id                      uuid primary key default gen_random_uuid(),
  household_id            uuid not null references households(id) on delete cascade,
  title                   text not null,
  description             text,
  start_time              timestamptz not null,
  recurrence              text not null default 'ONCE',  -- 'ONCE' | 'EVERY_DAY' | 'EVERY_WEEK' | 'EVERY_X_HOURS_4'
  snooze_duration_minutes int not null default 15,
  inactivity_limit_minutes int not null default 30,
  status                  text not null default 'PENDING',  -- 'PENDING' | 'SNOOZED' | 'DONE' | 'MISSED'
  created_by              uuid references auth.users(id),
  last_modified_at        timestamptz default now()
);

-- Alerts (triggers Database Webhook → Edge Function → FCM)
create table alerts (
  id           uuid primary key default gen_random_uuid(),
  household_id uuid not null references households(id) on delete cascade,
  type         text not null,  -- 'MISSED_REMINDER' | 'SOS' | 'INACTIVITY_24H'
  reminder_id  uuid references reminders(id),  -- null for SOS and inactivity
  message      text,
  resolved     boolean default false,
  created_at   timestamptz default now()
);

-- Enable Realtime on tables that need live sync
alter publication supabase_realtime add table reminders, alerts, households;

-- pg_cron: check for 24h inactivity every hour
select cron.schedule(
  'inactivity-check',
  '0 * * * *',  -- every hour
  $$
    insert into alerts (household_id, type, message)
    select id, 'INACTIVITY_24H', 'No activity detected for over 24 hours'
    from households
    where last_active_at < now() - interval '24 hours'
      and id not in (
        select household_id from alerts
        where type = 'INACTIVITY_24H' and resolved = false
      );
  $$
);
