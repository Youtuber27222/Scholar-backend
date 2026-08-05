CREATE TABLE IF NOT EXISTS provider_invites (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT NOT NULL,
  organization_name TEXT,
  invited_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  accepted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_provider_invites_token_hash ON provider_invites(token_hash);

-- Prevents an admin from stacking multiple live invites to the same address.
CREATE UNIQUE INDEX IF NOT EXISTS uq_provider_invites_pending_email
  ON provider_invites(email) WHERE accepted_at IS NULL;
