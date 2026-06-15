/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.cobol;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.CobolParser.Builder.TIMEOUT_PROPERTY;

class CobolParserTest {

    @AfterEach
    void clearProperty() {
        System.clearProperty(TIMEOUT_PROPERTY);
    }

    @Test
    void usesDefaultTimeoutWhenUnset() {
        System.clearProperty(TIMEOUT_PROPERTY);
        assertThat(CobolParser.Builder.configureTimeout()).isEqualTo(CobolParser.Builder.DEFAULT_TIMEOUT);
    }

    @Test
    void timeoutFromPlainSeconds() {
        System.setProperty(TIMEOUT_PROPERTY, "45");
        assertThat(CobolParser.Builder.configureTimeout()).isEqualTo(Duration.ofSeconds(45));
    }

    @Test
    void timeoutFromIso8601Duration() {
        System.setProperty(TIMEOUT_PROPERTY, "PT2M");
        assertThat(CobolParser.Builder.configureTimeout()).isEqualTo(Duration.ofMinutes(2));
    }

    @Test
    void invalidTimeoutFallsBackToDefault() {
        System.setProperty(TIMEOUT_PROPERTY, "not-a-duration");
        assertThat(CobolParser.Builder.configureTimeout()).isEqualTo(CobolParser.Builder.DEFAULT_TIMEOUT);
    }

    @Test
    void timeoutAboveMaxIsRejectedAndFallsBackToDefault() {
        System.setProperty(TIMEOUT_PROPERTY, "PT48H");
        assertThat(CobolParser.Builder.configureTimeout()).isEqualTo(CobolParser.Builder.DEFAULT_TIMEOUT);
    }

    @Test
    void timeoutAtMaxIsAllowed() {
        System.setProperty(TIMEOUT_PROPERTY, "PT24H");
        assertThat(CobolParser.Builder.configureTimeout()).isEqualTo(CobolParser.Builder.MAX_TIMEOUT);
    }
}
