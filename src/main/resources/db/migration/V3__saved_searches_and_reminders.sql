CREATE TABLE IF NOT EXISTS saved_searches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name TEXT,
  query TEXT,
  field_of_study TEXT,
  min_gpa NUMERIC(3,2),
  financial_need BOOLEAN,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_saved_searches_user ON saved_searches(user_id);

-- Dedup ledger for the deadline-reminder scheduled job: the unique constraint is
-- the actual guarantee against duplicate reminders, even if the job somehow runs
-- twice or two instances race on the same row.
CREATE TABLE IF NOT EXISTS deadline_reminders_sent (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  scholarship_id UUID NOT NULL REFERENCES scholarships(id) ON DELETE CASCADE,
  threshold_days INT NOT NULL,
  sent_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, scholarship_id, threshold_days)
);
