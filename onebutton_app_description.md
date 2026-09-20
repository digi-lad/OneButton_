# OneButton — App Description

> **Purpose**: A single, copy-pasteable description for AI-driven UI design and code generation.
> **Context**: Kotlin Multiplatform (Android + Web) hackathon project. 6-hour build.

---

## Overview

**OneButton** is an elderly care companion app that connects **one elderly person** with **multiple family caretakers** through a shared reminder system, real-time activity monitoring, and an emergency SOS button. The name reflects the app's philosophy: everything the elderly needs should be one button press away.

The app is built with **Kotlin Multiplatform (KMP)**:
- **Android app** — used by the elderly person (phone or tablet)
- **Web app** — used by caretakers (browser dashboard)

Both platforms share the same Supabase backend (Postgres + Realtime) for real-time data sync.

---

## Actors

| Actor | Platform | Description |
|-------|----------|-------------|
| **Elderly** | Android (phone/tablet) | The care recipient. Interacts with reminders, has an SOS button, and their activity is passively monitored. |
| **Caretaker** | Android (phone/tablet) + Web (browser) | Family members who manage reminders, monitor activity, and receive alerts. Multiple caretakers per household. Can use either the Android app or the web dashboard — same features on both. |

---

## Household Model

- **One household = one elderly person + N caretakers.**
- The elderly has a unique **Household ID** (generated at setup).
- Caretakers join by entering the elderly's Household ID.
- All members share a single reminder list in real-time via Supabase Realtime.

---

## Authentication & Onboarding

- **Supabase Auth** — simple email/password login, built-in.
- **Elderly flow**: Creates an account → receives a Household ID → enters the elderly interface.
- **Caretaker flow**: Creates an account → enters the elderly's Household ID to join → enters the caretaker interface.
- At login, users choose their role: **"I am the Elder"** or **"I am a Caretaker"**.

---

## Reminder System

### Reminder Data Model

Each reminder has:

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Unique identifier |
| `title` | String | Required. e.g., "Take blood pressure medicine" |
| `description` | String? | Optional. e.g., "The blue pill, with a glass of water" |
| `startTime` | DateTime | When the reminder first fires |
| `recurrence` | Enum | `ONCE`, `EVERY_DAY`, `EVERY_WEEK`, or `EVERY_X_HOURS(n)` |
| `snoozeDuration` | Duration | Per-reminder. How long to wait before re-notifying after a snooze (e.g., 15 min) |
| `inactivityLimit` | Duration | Per-reminder. How long to wait with no response before alerting caretakers (e.g., 30 min) |
| `status` | Enum | `PENDING`, `SNOOZED`, `DONE`, `MISSED` |
| `createdBy` | String | User ID of who created it |
| `lastModifiedAt` | DateTime | Timestamp of last edit |

### Reminder Notification Flow

```
START TIME reached
    │
    ▼
[Notification fires on elderly's Android phone]
[Full-screen overlay: Title + Description + two big buttons]
    │
    ├── [DONE pressed] → Mark as DONE, stop. Activity timestamp updated.
    │
    ├── [SNOOZE pressed] → Activity timestamp updated.
    │       │                Inactivity timer RESETS.
    │       ▼
    │   Wait `snoozeDuration`
    │       │
    │       ▼
    │   [Notification fires again] → loop back to Snooze/Done
    │
    └── [NO RESPONSE for `inactivityLimit`]
            │
            ▼
        Mark as MISSED.
        Write alert row to Supabase `alerts` table.
        Supabase Database Webhook triggers Edge Function → FCM push to ALL caretakers.
```

**Key behaviors:**
- **Unlimited snoozes** — the elderly can snooze as many times as needed.
- **Snoozing resets the inactivity timer** — pressing Snooze proves the elderly is responsive.
- **Inactivity limit is per-reminder** — different reminders can have different urgency levels.
- Recurring reminders auto-reset to `PENDING` for the next occurrence after `DONE` or `MISSED`.

### Who Can Edit Reminders

**Both** the elderly and caretakers can add, edit, and delete reminders. Changes sync in real-time via Supabase Realtime.

---

## Activity Monitoring

### Last Activity Timestamp

- A `last_active_at` timestamp is stored in the Supabase `households` table for the elderly.
- Updated on **any app interaction**: opening the app, tapping any button, snoozing a reminder, completing a reminder, using SOS, editing a reminder.
- Displayed on both the elderly's home screen and the caretaker's dashboard for peace of mind.

### 24-Hour Inactivity Detection

- A **Supabase `pg_cron` job** runs **every hour** inside the database.
- It checks the elderly's `last_active_at` timestamp.
- If `last_active_at` is **more than 24 hours ago**:
  - The cron job inserts an alert row, which triggers a **Database Webhook → Supabase Edge Function → FCM push notification** to all caretakers.
  - Displays the local emergency service number (e.g., 911) with a **tap-to-call button** in the alert (does NOT auto-dial for legal/safety reasons).

---

## SOS Button

- **Always visible** on the elderly's home screen — large, prominent, unmissable.
- **Flow:**
  1. Elderly presses SOS button.
  2. A **5-second countdown** begins with a visible timer and a **Cancel** button.
  3. If **not cancelled** within 5 seconds:
     - Writes an **SOS alert row** to Supabase `alerts` table (audit trail).
     - Database Webhook triggers Supabase Edge Function → **FCM push notification to all caretakers** with SOS urgency.
     - Displays the **local emergency number** with a **tap-to-call button** (does not auto-dial).
  4. If **cancelled** within 5 seconds: nothing happens, return to home screen.

---

## Notifications

### Elderly (Android)
- **Local scheduled notifications** — no server needed. Fires even when app is closed.
- Uses Android's `AlarmManager` or `WorkManager` to schedule reminder notifications at the exact `startTime`.
- When a notification fires: opens a **full-screen overlay** with Snooze/Done buttons.

### Caretakers (Android + Web)
- **FCM push notifications** — instant delivery, triggered server-side.
- Triggered by **Supabase Database Webhooks + Edge Functions** that react to:
  - New rows in the `alerts` table (missed reminders, SOS events).
  - Scheduled `pg_cron` inactivity checks inserting alert rows.
- On Android: native push notifications via FCM (works even when app is closed).
- On Web: uses the Browser Notification API (requires notification permission grant).

---

## Screens & Navigation

### Login / Role Selection Screen (shared)
- Enter credentials.
- Choose role: **"I am the Elder"** / **"I am a Caretaker"**.
- If caretaker: enter the elderly's Household ID.

---

### Elderly Interface (Android)

**Home Screen:**
- **SOS Button** — top of screen, large, red, always visible. Unmissable.
- **Last Activity** — "Last active: 5 minutes ago" — displayed below the SOS button.
- **Today's Reminders** — scrollable list of cards, each showing:
  - Title
  - Next scheduled time
  - Status badge (Pending / Snoozed / Done / Missed)
  - Small edit icon (tap to edit)
- **Add Reminder** button (floating action button or bottom bar).

**Reminder Alert Overlay (fires at reminder time):**
- **Full-screen overlay** that appears over everything.
- Shows: Reminder title + description.
- Two large buttons:
  - 🟢 **DONE** (green, large)
  - 🔵 **SNOOZE** (blue, large) — shows snooze duration, e.g., "Snooze 15 min"

**Add/Edit Reminder Screen:**
- Title (required)
- Description (optional)
- Start time (date + time picker)
- Recurrence: dropdown with `Once`, `Every day`, `Every week`, `Every X hours`
- Snooze duration: picker (e.g., 5 / 10 / 15 / 30 min)
- Inactivity limit: picker (e.g., 15 / 30 / 60 min)
- Save / Cancel buttons

---

### Caretaker Interface (Android)

Same features as the web dashboard, adapted for mobile:

**Home Screen (bottom navigation):**
- **📋 Reminders tab** — scrollable list of all reminders with Add/Edit/Delete.
- **🔔 Alerts tab** — chronological feed of missed reminders, SOS, and inactivity alerts.
- **⚙️ Settings tab** — household ID, elderly name, emergency number.

**Top Bar (always visible):**
- Elderly's name + last active time (e.g., "Last active: 5 min ago")
- 🟢 / 🔴 Online/offline indicator.

---

### Caretaker Interface (Web Dashboard)

**Layout:** Sidebar navigation + main content area.

**Sidebar Tabs:**
1. **📋 Reminders** — shared reminder list
2. **🔔 Alerts** — alert feed
3. **⚙️ Settings** — household settings

**Reminders Tab (main content):**
- Full list of all reminders (not just today's).
- Each reminder shows: title, next scheduled time, recurrence, status.
- Actions: Add / Edit / Delete.
- Add Reminder form: same fields as elderly's add/edit screen.

**Alerts Tab:**
- Chronological feed of events:
  - ⚠️ Missed reminders (title + time + how long inactive)
  - 🚨 SOS events (timestamp)
  - 📵 24-hour inactivity alerts (last active time)
- Each alert shows a timestamp and resolution status.

**Settings Tab:**
- Elderly's Household ID (read-only, for sharing with new caretakers)
- Elderly's name
- Emergency contact number

**Header Bar (always visible):**
- Elderly's name
- Last active: "5 minutes ago" — always visible for peace of mind.
- 🟢 / 🔴 Online/offline indicator.

---

## Visual Design

| Aspect | Direction |
|--------|-----------|
| **Style** | Warm and caring — healthcare app, not clinical |
| **Colors** | Soft blues, greens, warm whites. SOS button: red. |
| **Typography** | Large text throughout, especially on elderly's interface |
| **Shapes** | Rounded corners, friendly icons |
| **Contrast** | High contrast for readability |
| **Elderly UI density** | Minimal — TV-remote simplicity. Big touch targets. |
| **Caretaker UI density** | Standard dashboard density — more information, normal text size. |
| **Language** | English only |

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| **Shared logic** | Kotlin Multiplatform (KMP) — shared data models, Supabase access, business logic |
| **Android UI** | Jetpack Compose |
| **Web UI** | Compose for Web (or Kotlin/JS with React) |
| **Backend / Database** | Supabase (Postgres + Realtime subscriptions + Row Level Security) |
| **Auth** | Supabase Auth (email/password, built-in) |
| **Push Notifications** | FCM (Android) + Browser Notifications API (Web), triggered by Supabase Edge Functions |
| **Server-side Logic** | Supabase Edge Functions (Deno/TypeScript) — triggered by Database Webhooks on `alerts` table inserts |
| **Scheduled Jobs** | Supabase `pg_cron` extension (24h inactivity check, runs every hour in-database) |
| **Local Notifications** | Android: AlarmManager / WorkManager |

---

## Supabase Database Schema (suggested)

```sql
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
```

---

## Scope Boundaries (What's NOT included)

- ❌ Multi-language support
- ❌ Device-level activity detection (accelerometer, screen unlock)
- ❌ Chat/messaging between elderly and caretakers
- ❌ Reminder categories or priorities
- ❌ Auto-dialing emergency services
- ❌ Caretaker managing multiple elderly people
- ❌ Advanced authentication (OAuth, biometrics)
- ❌ Offline-first sync (app requires network connectivity to Supabase)
