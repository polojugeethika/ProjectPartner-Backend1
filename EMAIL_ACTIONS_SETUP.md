# ProjectPartner backend — Gmail request actions

This version adds email notifications for new collaboration requests and a secure, one-time email action link. The email link opens a confirmation page; the request is only changed after the recipient explicitly presses **Accept request** or **Decline request**.

## 1. Gmail setup

1. Turn on 2-Step Verification for the Gmail account that will send notifications.
2. Create an App Password from the Google Account security settings. Use the App Password, **not** your normal Gmail password.
3. Never commit the App Password or put it directly into `application.properties`.

Open PowerShell in this `backend` folder and set the variables for the current terminal session:

```powershell
$env:MAIL_USERNAME = "your-sender@gmail.com"
$env:MAIL_PASSWORD = "your-16-character-app-password"
$env:MAIL_FROM = "your-sender@gmail.com"
$env:APP_PUBLIC_BASE_URL = "https://projectpartner-backend1-2.onrender.com"
$env:EMAIL_ACTION_TOKEN_HOURS = "24"
```

Then run:

```powershell
.\mvnw.cmd spring-boot:run
```

Keep the backend running. A collaboration request created through `POST /api/collaboration-requests` will be saved and an email will be attempted to the receiver's student email address.

If email delivery is not configured or SMTP fails, the collaboration request is still saved; the backend log records that the notification failed, without logging the token or email address.

## 2. Local testing limitations

`https://projectpartner-backend1-2.onrender.com` works only on the computer where the backend is running. For a Gmail link to work on a phone or another computer, deploy the backend to a publicly reachable HTTPS address and set:

```powershell
$env:APP_PUBLIC_BASE_URL = "https://your-public-backend.example"
```

Do not expose a development server publicly. Use HTTPS and appropriate production hosting before sharing links.

## 3. Security notes

- Raw action tokens are sent only in the email link; only SHA-256 token hashes are stored in the database.
- Tokens expire after the configured number of hours (24 by default) and can be used only once.
- Opening the email link with GET does not change request status. The recipient must explicitly submit the confirmation form.
- A request that is already accepted/declined cannot be changed using the email link.
- This workflow uses a bearer link as proof of authority. Anyone who obtains and uses the private link before expiry could act on that request, so do not forward the email.
- This adds email-link authorization, not full student login/authentication. For a production app, also add user authentication and authorization to the existing API endpoints.

## 4. Existing project configuration

The existing MySQL configuration remains in `src/main/resources/application.properties`. Configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` as environment variables if your local database credentials differ from the defaults.
