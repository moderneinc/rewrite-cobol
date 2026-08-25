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
package org.openrewrite.controlm.internal;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.tree.Section;
import org.openrewrite.controlm.tree.Space;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.openrewrite.Tree.randomId;

/**
 * The XML half of Control-M: {@code exportdeftable} writes a {@code DEFTABLE} of folders and jobs,
 * {@code exportdefcal} a {@code DEFCAL} of calendars. Both are read here, because they differ only
 * in what their elements are called.
 * <p>
 * Read by hand rather than through a document parser: a single production export runs to millions of
 * lines, and every space has to survive so the file prints back as it came.
 */
@RequiredArgsConstructor
public class ExportReader {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    /**
     * Element and attribute names repeat once per job across the whole export, so holding one copy of
     * each is the difference between a few thousand strings and tens of millions.
     */
    private final Map<String, String> names = new HashMap<>();

    private int cursor;

    /**
     * Whether this file is an export rather than a job-definition panel. An export opens with markup
     * — an XML declaration, a doctype, then {@code DEFTABLE} or {@code DEFCAL} — where a panel opens
     * with the browse banner the z/OS screen draws, so the first character settles it.
     */
    public static boolean isExport(String source) {
        int at = indexOfNextNonWhitespace(0, source);
        return at < source.length() && source.charAt(at) == '<';
    }

    public ControlM.CompilationUnit read() {
        Space prefix = whitespace();
        List<Section> sections = new ArrayList<>(2);
        Space between = Space.EMPTY;
        while (cursor < source.length()) {
            sections.add(readSection(between));
            between = whitespace();
        }
        return new ControlM.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                prefix,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                sections,
                between
        );
    }

    private Section readSection(Space prefix) {
        expect('<');
        if (source.startsWith("<?", cursor) || source.startsWith("<!", cursor)) {
            return directive(prefix);
        }
        return element(prefix);
    }

    private ControlM.Directive directive(Space prefix) {
        int start = cursor;
        int end = source.startsWith("<!--", cursor) ?
                source.indexOf("-->", cursor) + 3 :
                source.startsWith("<?", cursor) ? source.indexOf("?>", cursor) + 2 : source.indexOf('>', cursor) + 1;
        if (end <= start) {
            throw new IllegalArgumentException("Unterminated markup at " + where(start));
        }
        cursor = end;
        return new ControlM.Directive(randomId(), prefix, Markers.EMPTY, source.substring(start, end));
    }

    private ControlM.Element element(Space prefix) {
        cursor++;
        String name = name();
        List<ControlM.Attribute> attributes = new ArrayList<>(4);
        while (true) {
            Space beforeTagEnd = whitespace();
            if (source.startsWith("/>", cursor)) {
                cursor += 2;
                return new ControlM.Element(randomId(), prefix, Markers.EMPTY, name,
                        attributes.isEmpty() ? emptyList() : attributes, beforeTagEnd, null, Space.EMPTY);
            }
            if (source.startsWith(">", cursor)) {
                cursor++;
                List<ControlM> elements = new ArrayList<>();
                Space beforeEndTag = body(name, elements);
                return new ControlM.Element(randomId(), prefix, Markers.EMPTY, name,
                        attributes.isEmpty() ? emptyList() : attributes, beforeTagEnd, elements, beforeEndTag);
            }
            attributes.add(attribute(beforeTagEnd));
        }
    }

    /**
     * What is written between an element's tags, up to the closing one, whose own prefix is returned.
     */
    private Space body(String name, List<ControlM> elements) {
        while (true) {
            Space beforeEndTag = whitespace();
            if (!source.startsWith("</", cursor)) {
                elements.add(readSection(beforeEndTag));
                continue;
            }
            cursor += 2;
            String closing = name();
            if (!closing.equals(name)) {
                throw new IllegalArgumentException("<" + name + "> closed by </" + closing + "> at " + where(cursor));
            }
            expect('>');
            cursor++;
            return beforeEndTag;
        }
    }

    private ControlM.Attribute attribute(Space prefix) {
        String name = name();
        Space beforeEquals = whitespace();
        expect('=');
        cursor++;
        Space beforeValue = whitespace();
        char quote = cursor < source.length() ? source.charAt(cursor) : ' ';
        if (quote != '"' && quote != '\'') {
            throw new IllegalArgumentException("Unquoted value of " + name + " at " + where(cursor));
        }
        int end = source.indexOf(quote, cursor + 1);
        if (end < 0) {
            throw new IllegalArgumentException("Unterminated value of " + name + " at " + where(cursor));
        }
        ControlM.Word value = new ControlM.Word(randomId(), beforeValue, Markers.EMPTY, source.substring(cursor, end + 1));
        cursor = end + 1;
        return new ControlM.Attribute(randomId(), prefix, Markers.EMPTY, name, beforeEquals, value);
    }

    private String name() {
        int start = cursor;
        while (cursor < source.length()) {
            char c = source.charAt(cursor);
            if (Character.isWhitespace(c) || c == '=' || c == '/' || c == '>') {
                break;
            }
            cursor++;
        }
        if (cursor == start) {
            throw new IllegalArgumentException("Expected a name at " + where(start));
        }
        String name = source.substring(start, cursor);
        String held = names.putIfAbsent(name, name);
        return held == null ? name : held;
    }

    private void expect(char c) {
        if (cursor >= source.length() || source.charAt(cursor) != c) {
            throw new IllegalArgumentException("Expected '" + c + "' at " + where(cursor));
        }
    }

    private String where(int at) {
        int line = 1;
        for (int i = 0; i < at && i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                line++;
            }
        }
        return "line " + line + " of " + path;
    }

    private Space whitespace() {
        int end = indexOfNextNonWhitespace(cursor, source);
        Space space = Space.build(source.substring(cursor, end));
        cursor = end;
        return space;
    }

    private static int indexOfNextNonWhitespace(int cursor, String source) {
        int at = cursor;
        while (at < source.length() && Character.isWhitespace(source.charAt(at))) {
            at++;
        }
        return at;
    }
}
