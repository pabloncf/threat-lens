package com.pabloncf.threatlens.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeverityTest {

    @ParameterizedTest
    @CsvSource({
        "0, LOW",
        "39, LOW",
        "40, MEDIUM",
        "69, MEDIUM",
        "70, HIGH",
        "89, HIGH",
        "90, CRITICAL",
        "100, CRITICAL"
    })
    void mapsScoreToSeverityAtThresholdBoundaries(int score, Severity expected) {
        assertThat(Severity.fromScore(score)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"-1", "101"})
    void rejectsScoresOutsideZeroToOneHundred(int score) {
        assertThatThrownBy(() -> Severity.fromScore(score))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
