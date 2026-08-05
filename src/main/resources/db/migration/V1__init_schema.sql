-- Baseline schema, ported from database/schema.sql.
--
-- Two deliberate deviations from the original schema:
-- 1. handle_new_user() and has_role() were dropped: registration side-effects
--    (profile + default role creation) now live in AuthService.register(),
--    and role checks are Spring Security @PreAuthorize expressions.
-- 2. Postgres native ENUM types were replaced with TEXT + CHECK constraints.
--    Native enums require Hibernate's enum value spelling to match the DB
--    label exactly (case-sensitive), which is a recurring source of casting
--    errors; TEXT + CHECK gives the same integrity guarantee and maps to
--    plain Java enums via a JPA AttributeConverter with no casting pitfalls.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_users_updated ON users;
CREATE TRIGGER trg_users_updated
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS profiles (
  id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  full_name TEXT NOT NULL DEFAULT '',
  email TEXT,
  avatar_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_profiles_updated ON profiles;
CREATE TRIGGER trg_profiles_updated
BEFORE UPDATE ON profiles
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS user_roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role TEXT NOT NULL CHECK (role IN ('student', 'provider', 'admin')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, role)
);

CREATE TABLE IF NOT EXISTS student_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  gpa NUMERIC(3,2),
  field_of_study TEXT,
  academic_level TEXT CHECK (academic_level IN ('undergraduate', 'postgraduate', 'phd')),
  nationality TEXT,
  financial_need BOOLEAN NOT NULL DEFAULT false,
  institution TEXT,
  bio TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_student_profiles_updated ON student_profiles;
CREATE TRIGGER trg_student_profiles_updated
BEFORE UPDATE ON student_profiles
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS provider_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  organization_name TEXT,
  organization_description TEXT,
  website TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_provider_profiles_updated ON provider_profiles;
CREATE TRIGGER trg_provider_profiles_updated
BEFORE UPDATE ON provider_profiles
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS scholarships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  provider_id UUID REFERENCES users(id) ON DELETE SET NULL,
  provider_name TEXT NOT NULL DEFAULT 'ScholarMatch Partner',
  title TEXT NOT NULL,
  description TEXT,
  funding_amount NUMERIC(12,2),
  currency VARCHAR(3) NOT NULL DEFAULT 'GHS',
  deadline DATE NOT NULL,
  field_of_study TEXT,
  academic_level TEXT CHECK (academic_level IN ('undergraduate', 'postgraduate', 'phd')),
  nationality TEXT,
  min_gpa NUMERIC(3,2),
  financial_need BOOLEAN NOT NULL DEFAULT false,
  country TEXT,
  requirements TEXT[] NOT NULL DEFAULT '{}',
  is_featured BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_scholarships_updated ON scholarships;
CREATE TRIGGER trg_scholarships_updated
BEFORE UPDATE ON scholarships
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS applications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  scholarship_id UUID NOT NULL REFERENCES scholarships(id) ON DELETE CASCADE,
  status TEXT NOT NULL DEFAULT 'drafting' CHECK (status IN ('drafting', 'submitted', 'pending', 'awarded', 'rejected')),
  essay TEXT,
  provider_notes TEXT,
  submitted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (student_id, scholarship_id)
);

DROP TRIGGER IF EXISTS trg_applications_updated ON applications;
CREATE TRIGGER trg_applications_updated
BEFORE UPDATE ON applications
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS notifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
  type TEXT NOT NULL,
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  read_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  file_name TEXT NOT NULL,
  file_type TEXT,
  doc_type TEXT,
  storage_path TEXT NOT NULL,
  file_size BIGINT,
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bookmarks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  scholarship_id UUID NOT NULL REFERENCES scholarships(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, scholarship_id)
);

-- JWTs are stateless; this table permits immediate logout/revocation until expiry.
CREATE TABLE IF NOT EXISTS revoked_tokens (
  jti UUID PRIMARY KEY,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_roles_lookup ON user_roles(user_id, role);
CREATE INDEX IF NOT EXISTS idx_student_profiles_user ON student_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_scholarships_active ON scholarships(is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_scholarships_featured ON scholarships(is_featured) WHERE is_featured = true;
CREATE INDEX IF NOT EXISTS idx_applications_student ON applications(student_id);
CREATE INDEX IF NOT EXISTS idx_applications_scholarship ON applications(scholarship_id);
CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_user ON documents(user_id);
CREATE INDEX IF NOT EXISTS idx_revoked_tokens_expiry ON revoked_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_provider_profiles_user ON provider_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_created ON notifications(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(user_id) WHERE read_at IS NULL;

INSERT INTO scholarships (provider_name, title, description, funding_amount, currency, deadline, field_of_study, academic_level, nationality, min_gpa, financial_need, country, requirements, is_featured)
VALUES
('Government of Ghana', 'GETFund National Merit Scholarship', 'Full tuition scholarship for high-achieving Ghanaian undergraduates across all fields of study at public universities.', 25000, 'GHS', (now() + interval '45 days')::date, 'Any', 'undergraduate', 'Ghanaian', 3.50, false, 'Ghana', ARRAY['Academic transcript','Admission letter','National ID'], true),
('Mastercard Foundation', 'Mastercard Foundation Scholars Program', 'Comprehensive scholarship covering tuition, accommodation, and stipend for academically talented yet economically disadvantaged students.', 80000, 'GHS', (now() + interval '30 days')::date, 'Any', 'undergraduate', 'African', 3.00, true, 'Ghana', ARRAY['Transcript','Proof of financial need','Two recommendation letters','Personal essay'], true),
('KNUST', 'KNUST Engineering Excellence Award', 'Merit-based award for top-performing engineering students at Kwame Nkrumah University of Science and Technology.', 15000, 'GHS', (now() + interval '60 days')::date, 'Engineering', 'undergraduate', 'Ghanaian', 3.60, false, 'Ghana', ARRAY['Transcript','Department recommendation'], true),
('Commonwealth Scholarship Commission', 'Commonwealth Master''s Scholarship', 'Fully-funded postgraduate scholarship for students from Commonwealth countries to study in the UK.', 120000, 'GHS', (now() + interval '90 days')::date, 'Any', 'postgraduate', 'Commonwealth', 3.30, true, 'United Kingdom', ARRAY['Bachelor degree certificate','Research proposal','Three references','English proficiency'], false),
('Vodafone Ghana Foundation', 'Vodafone STEM Scholarship for Women', 'Empowering young Ghanaian women pursuing science, technology, engineering and mathematics degrees.', 20000, 'GHS', (now() + interval '20 days')::date, 'Science', 'undergraduate', 'Ghanaian', 3.20, true, 'Ghana', ARRAY['Transcript','Statement of purpose','Proof of enrollment'], false),
('Tullow Oil', 'Tullow Group Scholarship Scheme', 'Postgraduate scholarships in geosciences, engineering and law for Ghanaian professionals.', 95000, 'GHS', (now() + interval '15 days')::date, 'Engineering', 'postgraduate', 'Ghanaian', 3.40, false, 'United Kingdom', ARRAY['Degree certificate','CV','Employer recommendation'], false),
('MTN Ghana Foundation', 'MTN Bright Scholarship', 'Supporting brilliant but needy students in their second year and above across all public universities.', 12000, 'GHS', (now() + interval '50 days')::date, 'Any', 'undergraduate', 'Ghanaian', 3.00, true, 'Ghana', ARRAY['Transcript','Proof of financial need','Admission letter'], false),
('DAAD', 'DAAD EPOS Development Studies Scholarship', 'German Academic Exchange Service scholarship for postgraduate development-related studies in Germany.', 150000, 'GHS', (now() + interval '75 days')::date, 'Social Sciences', 'postgraduate', 'African', 3.50, true, 'Germany', ARRAY['Bachelor degree','Two years work experience','Motivation letter','References'], true),
('Ghana National Petroleum Corporation', 'GNPC Foundation PhD Fellowship', 'Doctoral research funding in energy, petrochemicals and sustainability for outstanding Ghanaian researchers.', 200000, 'GHS', (now() + interval '40 days')::date, 'Engineering', 'phd', 'Ghanaian', 3.70, false, 'Ghana', ARRAY['Master degree','Research proposal','Academic publications'], false),
('Stanbic Bank Ghana', 'Stanbic Future Leaders Scholarship', 'Scholarship for business and finance undergraduates demonstrating leadership potential.', 18000, 'GHS', (now() + interval '25 days')::date, 'Business', 'undergraduate', 'Ghanaian', 3.30, false, 'Ghana', ARRAY['Transcript','Leadership essay','Recommendation letter'], false)
ON CONFLICT DO NOTHING;
