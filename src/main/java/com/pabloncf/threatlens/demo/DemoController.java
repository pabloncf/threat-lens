package com.pabloncf.threatlens.demo;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Intentionally-vulnerable demo endpoints - never part of the real product surface. Exists
 * only under the {@code demo} profile, to give the bundled synthetic attack simulator
 * (docker-compose) something real to attack and {@code DetectionFilter} something real to
 * catch. See claude.md §4: "self-contained, never pointed at third-party targets."
 */
@RestController
@RequestMapping("/demo")
@Profile("demo")
public class DemoController {

    private final JdbcTemplate jdbcTemplate;

    public DemoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Intentionally SQL-injectable: the query is built by string concatenation instead of a
     * parameterized statement. A payload like {@code ' OR '1'='1' --} in either field bypasses
     * authentication for real - that's the point. See {@code SqlInjectionDetector} for the
     * mitigation a real login endpoint would use instead (parameterized queries, never this).
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username, @RequestParam String password) {
        String sql = "SELECT username FROM demo_users WHERE username = '" + username + "' AND password = '"
                + password + "'";
        List<Map<String, Object>> matches = jdbcTemplate.queryForList(sql);
        if (matches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("authenticated", false));
        }
        return ResponseEntity.ok(Map.of("authenticated", true, "username", matches.getFirst().get("username")));
    }

    @PostMapping("/comments")
    public ResponseEntity<Void> addComment(@RequestParam String body) {
        jdbcTemplate.update("INSERT INTO demo_comments (body) VALUES (?)", body);
        return ResponseEntity.ok().build();
    }

    /**
     * Intentionally stored-XSS-vulnerable: comment bodies are rendered into HTML without
     * escaping. A stored {@code <script>...</script>} payload executes for real when this page
     * is opened in a browser - that's the point.
     */
    @GetMapping(value = "/comments", produces = MediaType.TEXT_HTML_VALUE)
    public String listComments() {
        List<String> bodies =
                jdbcTemplate.queryForList("SELECT body FROM demo_comments ORDER BY created_at", String.class);
        StringBuilder html = new StringBuilder("<html><body>");
        for (String body : bodies) {
            html.append("<div class=\"comment\">").append(body).append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }
}
