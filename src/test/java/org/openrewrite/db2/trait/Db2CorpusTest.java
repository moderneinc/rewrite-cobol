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
package org.openrewrite.db2.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.Corpus;
import org.openrewrite.db2.Db2Parser;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.db2.tree.Statement;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.tree.Jcl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the DB2 DDL of real applications, from both places it is written: files of DDL, and the
 * {@code SYSIN} streams of the jobs that create the schema.
 * <p>
 * Gated on {@code DB2_CORPUS} pointing at a checkout, because the corpus is not redistributed with
 * this repository.
 */
@EnabledIfEnvironmentVariable(named = "DB2_CORPUS", matches = ".+")
class Db2CorpusTest {

    private static final Pattern CREATE_TABLE = Pattern.compile("(?i)\\bCREATE\\s+TABLE\\b");
    private static final Pattern CREATE_INDEX = Pattern.compile("(?i)\\bCREATE\\s+(UNIQUE\\s+(WHERE\\s+NOT\\s+NULL\\s+)?)?INDEX\\b");
    private static final Pattern PRIMARY_KEY = Pattern.compile("(?i)\\bPRIMARY\\s+KEY\\b");
    private static final Pattern ISLAND = Pattern.compile(
            "(?i)\\b(CREATE\\s+(UNIQUE\\s+)?(TABLE|INDEX)|ALTER\\s+TABLE)\\b");

    @Test
    void readsRealSchemas() throws IOException {
        Path corpus = Paths.get(System.getenv("DB2_CORPUS"));
        List<Path> ddl = members(corpus, ".ddl", ".sql");
        // Bank-of-Z ships its jobs as Jinja templates, and creates every one of its tables from
        // a templated member, so the .j2 half is not an extra — it is a whole application.
        List<Path> jcl = members(corpus, ".jcl", ".j2");
        assertThat(ddl).as("no DDL found under %s", corpus).isNotEmpty();
        assertThat(jcl).as("no JCL found under %s", corpus).isNotEmpty();

        List<String> failures = new ArrayList<>();
        List<Db2.Ddl> schemas = new ArrayList<>();
        int written = 0;
        int indexesWritten = 0;
        int keysWritten = 0;

        for (Path member : ddl) {
            String source = read(member);
            written += count(CREATE_TABLE, source);
            indexesWritten += count(CREATE_INDEX, source);
            keysWritten += count(PRIMARY_KEY, source);
            schemas.add(read(member.getFileName().toString(), source, failures));
        }

        int streams = 0;
        for (Path member : jcl) {
            if (!JclParser.builder().build().accept(member)) {
                continue;
            }
            List<SourceFile> parsed = JclParser.builder().build()
                    .parse(new InMemoryExecutionContext(), read(member))
                    .collect(toList());
            if (parsed.isEmpty() || !(parsed.get(0) instanceof Jcl.CompilationUnit)) {
                continue;
            }
            for (InstreamDdl instream : new InstreamDdl.Matcher()
                    .lower((Jcl.CompilationUnit) parsed.get(0)).collect(toList())) {
                streams++;
                String source = instream.getText();
                written += count(CREATE_TABLE, source);
                indexesWritten += count(CREATE_INDEX, source);
                keysWritten += count(PRIMARY_KEY, source);
                schemas.add(read(member.getFileName() + " " + instream.getName(), source, failures));
            }
        }
        schemas.removeIf(Objects::isNull);

        Set<String> tables = new LinkedHashSet<>();
        Set<String> indexes = new LinkedHashSet<>();
        List<String> edges = new ArrayList<>();
        int tablesRead = 0;
        int indexesRead = 0;
        int columns = 0;
        int keysRead = 0;
        int water = 0;

        for (Db2.Ddl schema : schemas) {
            for (Table table : new Table.Matcher().lower(schema).collect(toList())) {
                tablesRead++;
                tables.add(table.getQualifiedName());
                // A table need not declare one: Bank-of-Z declares no primary key anywhere and
                // enforces uniqueness with a unique index instead.
                if (!table.getPrimaryKey().isEmpty()) {
                    keysRead++;
                }
                for (Column column : table.getColumns()) {
                    columns++;
                    // A column with no name or no type is one this model failed to read, not one
                    // the DDL leaves anonymous or untyped.
                    if (column.getName().isEmpty() || column.getTypeName().isEmpty()) {
                        failures.add(table.getQualifiedName() + ": a column with no name or type");
                    }
                }
            }
            for (Index index : new Index.Matcher().lower(schema).collect(toList())) {
                indexesRead++;
                indexes.add(index.getQualifiedName());
                if (index.getKeyColumns().isEmpty()) {
                    failures.add(index.getQualifiedName() + ": an index with no key");
                }
            }
            for (ForeignKey foreignKey : new ForeignKey.Matcher().lower(schema).collect(toList())) {
                edges.add(foreignKey.toString());
                if (foreignKey.getTable() == null || foreignKey.getReferencedTable() == null) {
                    failures.add(foreignKey + ": a foreign key with an end missing");
                }
            }
            // No CREATE TABLE or CREATE INDEX may have ended up as water.
            for (Statement statement : schema.getStatements()) {
                if (!(statement instanceof Db2.Unknown)) {
                    continue;
                }
                water++;
                String text = ((Db2.Unknown) statement).getWords().stream()
                        .map(Db2.Word::getText)
                        .collect(joining(" "));
                if (ISLAND.matcher(text).find()) {
                    failures.add("read as water: " + text.substring(0, Math.min(80, text.length())));
                }
            }
        }

        System.out.printf("DB2 corpus: %d DDL files, %d in-stream DDL streams, " +
                        "%d tables, %d columns, %d indexes, %d primary keys, %d foreign keys, %d water%n",
                ddl.size(), streams, tables.size(), columns, indexes.size(), keysRead,
                edges.size(), water);
        edges.forEach(edge -> System.out.println("  " + edge));

        assertThat(failures).isEmpty();

        // Counting what the source says against what the traits found is the only thing that tells
        // a table read as water from one that was never there.
        assertThat(tablesRead).as("CREATE TABLE read against written").isEqualTo(written);
        assertThat(indexesRead).as("CREATE INDEX read against written").isEqualTo(indexesWritten);
        assertThat(keysRead).as("PRIMARY KEY read against written").isEqualTo(keysWritten);

        // Most of the corpus creates its tables from a job rather than from a file of DDL. Reading
        // only the files would find a fraction of the schema and report it as the whole.
        assertThat(streams).as("DDL read out of JCL streams").isPositive();

        assertThat(tables).isNotEmpty();
        assertThat(indexes).isNotEmpty();
        assertThat(edges).as("tables pointing at other tables").isNotEmpty();
    }

    /**
     * Parses one source, failing it for a syntax error as well as for printing back differently.
     * ANTLR recovers from an error by inventing tokens, so a parse that reported one has read
     * something the source does not say even when the two happen to agree.
     */
    private static Db2.@Nullable Ddl read(String name, String source, List<String> failures) {
        List<String> errors = new ArrayList<>();
        List<SourceFile> parsed = Db2Parser.builder().build()
                .parse(new InMemoryExecutionContext(t -> errors.add(t.getMessage())), source)
                .collect(toList());
        if (parsed.size() != 1 || !(parsed.get(0) instanceof Db2.Ddl)) {
            failures.add(name + ": did not parse");
            return null;
        }
        Db2.Ddl cu = (Db2.Ddl) parsed.get(0);
        if (!errors.isEmpty()) {
            failures.add(name + ": " + errors.get(0));
        }
        if (!source.equals(cu.printAll())) {
            failures.add(name + ": did not print back");
        }
        return cu;
    }

    private static int count(Pattern pattern, String source) {
        int count = 0;
        for (java.util.regex.Matcher matcher = pattern.matcher(source); matcher.find(); ) {
            count++;
        }
        return count;
    }

    private static String read(Path member) throws IOException {
        return new String(Files.readAllBytes(member));
    }

    private static List<Path> members(Path corpus, String... extensions) throws IOException {
        List<Path> members = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(corpus)) {
            walk.filter(Files::isRegularFile)
                    .filter(Corpus::isSource)
                    .filter(path -> {
                        String name = path.toString().toLowerCase();
                        for (String extension : extensions) {
                            if (name.endsWith(extension)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(members::add);
        }
        return members;
    }
}
