/*
 * Copyright 2026 the original author or authors.
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
package org.openrewrite.mainframe.linkedit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The names a CICS, DB2 or DL/I module holds that are nobody's program.
 * <p>
 * Every such module is bound with a stub the vendor supplies — {@code DFHECI} for CICS,
 * {@code DSNELI} for DB2, {@code DFSLI000} for DL/I — and control arrives in a DL/I program at the
 * {@code DLITCBL} label the interface asks for rather than at the program's own name. There is no
 * member of any of these names to look one up in, in this repository or any other, so a deck naming
 * one is naming machinery and the name has to be known by itself.
 */
public final class LanguageInterface {

    private static final Set<String> STUBS = new HashSet<>(Arrays.asList(
            "DFHECI", "DFHEAI", "DFHEAI0", "DFHELII", "DFHEXCI", "DFHNCTR",
            "DSNELI", "DSNALI", "DSNCLI", "DSNRLI",
            "DFSLI000"));

    /**
     * The labels control arrives at: the entry names of the stubs above, and the {@code DLITCBL} a
     * COBOL DL/I program declares.
     */
    private static final Set<String> LABELS = new HashSet<>(Arrays.asList(
            "DFHEI1", "DSNHLI", "DLITCBL",
            "ASMTDLI", "CBLTDLI", "PLITDLI", "AIBTDLI", "CEETDLI"));

    private LanguageInterface() {
    }

    /**
     * Whether the name is a stub, which is an object a module is bound with and not a subroutine
     * anybody wrote.
     */
    public static boolean isStub(String name) {
        return STUBS.contains(name.toUpperCase(Locale.ROOT));
    }

    /**
     * Whether the name belongs to the language interface rather than to a program: a stub, or a label
     * control arrives at.
     */
    public static boolean isName(String name) {
        return isStub(name) || LABELS.contains(name.toUpperCase(Locale.ROOT));
    }
}
