package com.pabloncf.threatlens.triage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataRedactorTest {

    private final SensitiveDataRedactor redactor = new SensitiveDataRedactor();

    @Test
    void redactsAPasswordField() {
        String redacted = redactor.redact("username=admin&password=hunter2");

        assertThat(redacted).isEqualTo("username=admin&password=[REDACTED]");
    }

    @Test
    void redactsATokenField() {
        String redacted = redactor.redact("api_key=sk-abc123xyz");

        assertThat(redacted).isEqualTo("api_key=[REDACTED]");
    }

    @Test
    void redactsABearerAuthorizationHeader() {
        String redacted = redactor.redact("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.abc.def");

        assertThat(redacted).isEqualTo("authorization: Bearer [REDACTED]");
    }

    @Test
    void redactsAnEmailAddress() {
        String redacted = redactor.redact("contact user@example.com for details");

        assertThat(redacted).isEqualTo("contact [REDACTED_EMAIL] for details");
    }

    @Test
    void leavesOrdinaryTextUnchanged() {
        String text = "tautology, UNION SELECT";

        assertThat(redactor.redact(text)).isEqualTo(text);
    }

    @Test
    void handlesNullAndEmptyInput() {
        assertThat(redactor.redact(null)).isNull();
        assertThat(redactor.redact("")).isEmpty();
    }
}
