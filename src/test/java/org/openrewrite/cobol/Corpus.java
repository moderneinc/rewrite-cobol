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

import java.nio.file.Path;

/**
 * Shared by the corpus tests, which each walk a directory of real applications.
 */
public final class Corpus {

    private Corpus() {
    }

    /**
     * Whether a file is corpus source rather than something a tool left behind.
     * <p>
     * The Moderne CLI writes each run's output under {@code <repo>/.moderne/run/<id>/after-fenced/},
     * keeping the original directory layout — so once anybody has run a recipe over the corpus,
     * walking it finds every program several times over, and the extra copies are recipe output with
     * search markers printed into them. Measured on 2026-08-17 that was 734 of 847 {@code .cbl}
     * files, and nothing failed: the corpus tests went on passing against four times the input they
     * were written for, quietly reporting four times as much of everything.
     * <p>
     * The CLI itself is not exposed to this — {@code RepositoryDirectory.directoryExcludedByDefault}
     * skips any directory whose name begins with a dot. Only these hand-rolled walks are.
     */
    public static boolean isSource(Path path) {
        for (Path element : path) {
            if (".moderne".equals(element.toString())) {
                return false;
            }
        }
        return true;
    }
}
