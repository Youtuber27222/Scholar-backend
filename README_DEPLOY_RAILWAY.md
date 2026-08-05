Railway Deploy

This file documents quick steps to deploy the ScholarMatch backend to Railway and the environment variables the app expects.

Required environment variables

- DATABASE_URL: JDBC URL, e.g. `jdbc:postgresql://<host>:<port>/<db>?sslmode=require`
- DATABASE_USERNAME
- DATABASE_PASSWORD
- JWT_SECRET (>= 32 chars)
- PORT (default 8080)
- CORS_ORIGIN
- UPLOADS_ROOT
- MAIL_MODE (log|sendgrid|smtp)
- MAIL_HOST
- MAIL_PORT
- MAIL_SMTP_AUTH (true|false)
- MAIL_SMTP_STARTTLS (true|false)
- MAIL_USERNAME
- MAIL_PASSWORD
- MAIL_FROM
- SENDGRID_API_KEY (if using SendGrid)

Railway setup steps (summary)

1. Create a new Railway project and connect your GitHub repository (Scholar-backend).
2. Add a PostgreSQL plugin in Railway and copy the generated connection credentials.
3. In Railway project settings -> Environment, add the variables above using values from the PostgreSQL plugin and your secret keys.
   - Convert the Postgres connection into a JDBC `DATABASE_URL` used by the app. Example:
     `jdbc:postgresql://<host>:<port>/<db>?sslmode=require`
4. Set `PORT` to `8080` (Railway uses `$PORT` but Spring Boot will respect it when set).
5. Enable persistent `uploads/` by adding a Persistent Storage plugin or using an external S3-compatible bucket; update `UPLOADS_ROOT` accordingly.
6. Configure Railway to deploy from the `main` branch. Set the build command to `./mvnw -DskipTests package` and the start command to `java -jar target/scholarmatch-api-0.1.0.jar` (adjust artifact name if needed).
7. Deploy and monitor logs. Use `MAIL_MODE=log` initially to avoid sending real emails until you verify functionality.

Notes

- Rotate any exposed API keys immediately (SendGrid key leaked). After rotating, update `SENDGRID_API_KEY` in Railway environment and redeploy.
- If you prefer not to host uploads on Railway disk, use S3/MinIO and update code/config to support it.

