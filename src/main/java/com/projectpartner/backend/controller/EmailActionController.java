package com.projectpartner.backend.controller;

import com.projectpartner.backend.service.EmailActionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmailActionController {
    private final EmailActionService actions;

    public EmailActionController(EmailActionService actions) {
        this.actions = actions;
    }

    // GET never changes the request. This protects against mail scanners that automatically
    // visit links. The user must explicitly submit one of the POST buttons below.
    @GetMapping(value = "/email-actions/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> showConfirmation(@PathVariable String token) {
        if (token == null || !token.matches("[a-fA-F0-9]{64}")) {
            return ResponseEntity.badRequest().body(page("Invalid link",
                    "This email action link is invalid."));
        }
        if (!actions.isActionAvailable(token)) {
            return ResponseEntity.status(410).body(page("Link expired",
                    "This link has expired, was already used, or the request was already answered."));
        }
        String safeToken = token;
        String html = """
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>ProjectPartner request</title>
                <style>
                body{font-family:Arial,sans-serif;background:#f4f6fb;margin:0;padding:24px;color:#172033}
                main{max-width:520px;margin:8vh auto;background:white;padding:28px;border-radius:16px;
                box-shadow:0 8px 30px #18294a14}h1{font-size:24px}p{line-height:1.5;color:#4b5563}
                form{display:inline-block;margin:8px 8px 0 0}button{border:0;border-radius:8px;padding:12px 18px;
                font-weight:700;cursor:pointer}.accept{background:#166534;color:white}
                .decline{background:#fee2e2;color:#991b1b}.note{font-size:13px}
                </style></head><body><main><h1>ProjectPartner teammate request</h1>
                <p>Please confirm your choice. This action will update the request status once.</p>
                <form method="post" action="/api/email-actions/%s/respond">
                <input type="hidden" name="decision" value="ACCEPTED">
                <button class="accept" type="submit">Accept request</button></form>
                <form method="post" action="/api/email-actions/%s/respond">
                <input type="hidden" name="decision" value="DECLINED">
                <button class="decline" type="submit">Decline request</button></form>
                <p class="note">This link is private, expires, and can be used once. Do not forward this email.</p>
                </main></body></html>
                """.formatted(safeToken, safeToken);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @PostMapping(value = "/api/email-actions/{token}/respond",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> respond(@PathVariable String token,
                                          @RequestParam String decision) {
        try {
            String result = actions.respond(token, decision);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML)
                    .body(page("Request " + result.toLowerCase(),
                            "Your response has been saved. You can close this page."));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).contentType(MediaType.TEXT_HTML)
                    .body(page("Could not update request", ex.getReason() == null
                            ? "This action could not be completed." : ex.getReason()));
        }
    }

    private String page(String title, String message) {
        String safeTitle = escape(title);
        String safeMessage = escape(message);
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + safeTitle + "</title><style>"
                + "body{font-family:Arial,sans-serif;background:#f4f6fb;padding:24px;color:#172033}"
                + "main{max-width:520px;margin:8vh auto;background:white;padding:28px;border-radius:16px}"
                + "p{line-height:1.5;color:#4b5563}</style></head><body><main><h1>"
                + safeTitle + "</h1><p>" + safeMessage + "</p></main></body></html>";
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
