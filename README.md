# ScholarMatch API (Spring Boot)

Java 21 + Spring Boot + PostgreSQL. Listens on `http://localhost:8080/api` and implements
the same contract `../database/openapi.yaml` describes (springdoc also
generates a live spec at `/api/v3/api-docs` and a UI at `/api/swagger-ui.html`).

## Setup

1. Create a PostgreSQL database (or reuse an existing one — Flyway will
   create all tables on first run).
2. Copy `.env.example` to `.env` and fill in real values. Local runs import
   `backend/.env` automatically; environment variables from your shell or host
   still override the file.
3. Run it:
   ```bash
   ./mvnw spring-boot:run       # bash
   mvnw.cmd spring-boot:run     # Windows
   ```

Check it with:

```bash
curl http://localhost:8080/api/actuator/health
```

Flyway applies `src/main/resources/db/migration/V1__init_schema.sql` on
startup (schema + seed scholarships), then Hibernate validates entities
against it (`ddl-auto: validate` — Flyway owns all schema changes, Hibernate
never auto-generates DDL).

## Granting a role

New registrations only get the `student` role. To make a user a `provider`
or `admin` during development, run directly against the database:

```sql
INSERT INTO user_roles (user_id, role)
SELECT id, 'provider' FROM users WHERE email = 'someone@example.com'
ON CONFLICT DO NOTHING;
```

## Email delivery

For real SendGrid delivery, use the HTTP API mode:

```
MAIL_MODE=sendgrid
SENDGRID_API_KEY=<sendgrid-api-key>
MAIL_FROM=verified-sender@example.com
```

`MAIL_FROM` must be a verified SendGrid sender or domain. Restart the backend
after changing `.env`.

`MAIL_MODE=log` (the default) just logs verification codes/reset links to
the console — nothing is actually sent. Fine for a quick local check, but
for anything you actually need delivered to a real inbox, switch to real
SMTP:

```
MAIL_MODE=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_USERNAME=you@gmail.com
MAIL_PASSWORD=<16-character app password, not your regular password>
MAIL_FROM=you@gmail.com
```

Provider notes:
- **Gmail**: requires 2-Step Verification enabled on the account, then an
  [App Password](https://myaccount.google.com/apppasswords) generated for
  "Mail" — a regular Gmail password will be rejected by SMTP auth.
- **Outlook/Microsoft 365**: `smtp.office365.com`, port `587`, STARTTLS on,
  same app-password requirement if 2FA is enabled.
- **Transactional providers** (SendGrid, Mailgun, Postmark, AWS SES, etc.):
  use the SMTP relay host/port/credentials from that provider's dashboard —
  generally more reliable than a personal inbox once you're past local
  testing, since personal-account SMTP gets rate-limited fast.
- **Local catcher** (no real delivery, just a UI to view what would have
  been sent): [Mailpit](https://github.com/axllent/mailpit) —
  `docker run -d -p 1025:1025 -p 8025:8025 axllent/mailpit`, then
  `MAIL_MODE=smtp` with the default `MAIL_HOST=localhost`/`MAIL_PORT=1025`
  and `MAIL_SMTP_AUTH=false` (Mailpit doesn't require auth). View mail at
  `http://localhost:8025`.

After changing `.env`, re-export the variables and restart the backend —
Spring Boot doesn't hot-reload environment variables.

## Uploads

Avatars and documents are stored on local disk under `uploads/` (gitignored)
and served at `/api/uploads/**`. Fine for single-instance dev/demo use; swap
for S3/MinIO + presigned URLs before running multiple instances or in
production.

## Tests

```bash
./mvnw test
```

Integration tests use Testcontainers to spin up a real PostgreSQL instance —
Docker must be running.
