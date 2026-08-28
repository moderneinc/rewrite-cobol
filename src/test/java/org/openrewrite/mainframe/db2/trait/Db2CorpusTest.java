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
package org.openrewrite.mainframe.db2.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.db2.Db2Parser;
import org.openrewrite.mainframe.db2.tree.Db2;
import org.openrewrite.mainframe.jcl.JclParser;
import org.openrewrite.mainframe.jcl.tree.Jcl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the DB2 DDL of real applications, from both places it is written: files of DDL, and the
 * {@code SYSIN} streams of the jobs that create the schema.
 * <p>
 * The fixture is required: every member of its {@code ddl/} and {@code cardlib/}, and the index
 * {@code CLMJ004} creates in-stream, must parse and print back, and the schema they define is
 * checked object by object against the counts INTERLINKS.md writes down. The rest of the corpus is
 * measured and reported — it is other people's DDL, and a dialect nobody has taught this parser is
 * a fact about the corpus rather than a regression.
 * <p>
 * Gated on {@code DB2_CORPUS} pointing at a checkout, because the corpus is not redistributed with
 * this repository.
 */
@EnabledIfEnvironmentVariable(named = "DB2_CORPUS", matches = ".+")
class Db2CorpusTest {

    private static final Pattern CREATE_TABLE = Pattern.compile("(?i)\\bCREATE\\s+TABLE\\b");
    private static final Pattern CREATE_INDEX = Pattern.compile("(?i)\\bCREATE\\s+(UNIQUE\\s+(WHERE\\s+NOT\\s+NULL\\s+)?)?INDEX\\b");
    private static final Pattern PRIMARY_KEY = Pattern.compile("(?i)\\bPRIMARY\\s+KEY\\b");
    private static final Pattern COMMENT = Pattern.compile("--[^\\r\\n]*|/\\*.*?\\*/", Pattern.DOTALL);

    @Test
    void readsRealSchemas() throws IOException {
        Path corpus = Paths.get(System.getenv("DB2_CORPUS"));

        Counts counts = new Counts();
        List<String> failures = new ArrayList<>();
        List<String> fixtureFailures = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        boolean fixtureFound = false;
        int files = 0;
        int streams = 0;

        System.out.println("DDL read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> members = members(repository, ".ddl", ".sql");
            List<Path> jobs = Corpus.jobs(repository);
            List<String> reports = Corpus.isFixture(repository) ? fixtureFailures : failures;
            int found = 0;
            int read = 0;

            for (Path member : members) {
                String name = corpus.relativize(member).toString();
                String source = read(member);
                String reason = outOfScope(member, source);
                if (reason != null) {
                    skipped.add(name + ": " + reason);
                    continue;
                }
                found++;
                files++;
                read += measure(name, source, counts, reports) ? 1 : 0;
            }

            for (Path job : jobs) {
                Jcl.CompilationUnit cu = parseJob(job);
                if (cu == null) {
                    continue;
                }
                for (InstreamDdl instream : new InstreamDdl.Matcher().lower(cu).collect(toList())) {
                    String name = corpus.relativize(job) + " " + instream.getName();
                    String source = instream.getText();
                    String reason = outOfScope(job, source);
                    if (reason != null) {
                        skipped.add(name + ": " + reason);
                        continue;
                    }
                    found++;
                    streams++;
                    read += measure(name, source, counts, reports) ? 1 : 0;
                }
            }

            if (found == 0) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, found);
        }
        assertThat(files).as("no DDL found under %s", corpus).isPositive();

        System.out.printf("DB2 corpus: %d DDL files, %d in-stream DDL streams, " +
                        "%d tables, %d columns, %d indexes, %d primary keys, %d foreign keys%n",
                files, streams, counts.tableNames.size(), counts.columns, counts.indexNames.size(),
                counts.keys, counts.foreignKeys.size());
        counts.foreignKeys.forEach(edge -> System.out.println("  " + edge));
        skipped.forEach(member -> System.out.println("  not DB2 for z/OS DDL: " + member));
        if (!failures.isEmpty()) {
            System.out.println("not read:");
            failures.forEach(failure -> System.out.println("  " + failure));
        }

        assertThat(fixtureFailures).isEmpty();
        // A fixture the walk could not see, a symbolic link say, would otherwise pass as an
        // application with no DDL in it.
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
        assertThat(skipped).as("members held out of the corpus").hasSize(50);

        // Counting what the source says against what the traits found is the only thing that tells
        // a table the grammar failed to read from one that was never there.
        assertThat(counts.tables).as("CREATE TABLE read against written").isEqualTo(counts.tablesWritten);
        assertThat(counts.indexes).as("CREATE INDEX read against written").isEqualTo(counts.indexesWritten);
        assertThat(counts.keys).as("PRIMARY KEY read against written").isEqualTo(counts.keysWritten);

        // Most of the corpus creates its tables from a job rather than from a file of DDL. Reading
        // only the files would find a fraction of the schema and report it as the whole.
        assertThat(streams).as("DDL read out of JCL streams").isPositive();

        assertThat(counts.tableNames).isNotEmpty();
        assertThat(counts.indexNames).isNotEmpty();
        assertThat(counts.foreignKeys).as("tables pointing at other tables").isNotEmpty();
    }

    /**
     * The fixture's own oracle: INTERLINKS.md section 15 writes down the whole CLAIMS schema object
     * by object — 2 tables, 18 columns, 2 primary keys, 1 foreign key and 5 indexes, the fifth of
     * them created in-stream by {@code CLMJ004} rather than by a member of the library. Section 16's
     * four catalog query decks are read here too: they select from {@code SYSIBM} and define
     * nothing, so what follows is the whole of what CLAIMS creates. {@code ddl/CLMVIEW} is the fifth
     * member, and its view must not arrive here as a third table.
     */
    @Test
    void readsTheFixtureSchemaObjectByObject() throws IOException {
        Path claims = Paths.get(System.getenv("DB2_CORPUS"), "mainframe-fixtures", "claims");
        assertThat(Files.isDirectory(claims)).as("%s", claims).isTrue();

        List<Path> members = members(claims.resolve("ddl"), ".ddl");
        List<Path> decks = members(claims.resolve("cardlib"), ".sql");
        assertThat(members).hasSize(5);
        assertThat(decks).hasSize(4);
        members.addAll(decks);

        List<String> failures = new ArrayList<>();
        List<Db2.Ddl> schemas = new ArrayList<>();
        for (Path member : members) {
            Db2.Ddl schema = parse(claims.relativize(member).toString(), read(member), failures);
            if (schema != null) {
                schemas.add(schema);
            }
        }

        Path job = claims.resolve("jcl").resolve("CLMJ004.jcl");
        List<InstreamDdl> instream = new InstreamDdl.Matcher()
                .lower(requireNonNull(parseJob(job))).collect(toList());
        assertThat(instream).as("%s", job).hasSize(1);
        Db2.Ddl added = parse("jcl/CLMJ004 " + instream.get(0).getName(),
                instream.get(0).getText(), failures);

        assertThat(failures).isEmpty();
        schemas.add(requireNonNull(added));

        List<String> tables = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        List<String> indexes = new ArrayList<>();
        List<String> foreignKeys = new ArrayList<>();
        for (Db2.Ddl schema : schemas) {
            for (Table table : new Table.Matcher().lower(schema).collect(toList())) {
                tables.add(table.getQualifiedName() + " IN " + table.getTablespace());
                keys.add(table.getQualifiedName() + " " + table.getPrimaryKey());
                for (Column column : table.getColumns()) {
                    columns.add(describe(table, column));
                }
            }
            for (Index index : new Index.Matcher().lower(schema).collect(toList())) {
                indexes.add(index + " " + index.getKeyColumns());
            }
            for (ForeignKey foreignKey : new ForeignKey.Matcher().lower(schema).collect(toList())) {
                foreignKeys.add(foreignKey + " ON DELETE " + foreignKey.getDeleteRule());
            }
        }

        assertThat(tables).containsExactly(
                "CLM.CLAIM_HIST IN CLMDB01.CLMTSHST",
                "CLM.POLICY IN CLMDB01.CLMTSPOL");
        assertThat(keys).containsExactly(
                "CLM.CLAIM_HIST [CLAIM_NO]",
                "CLM.POLICY [POLICY_NO]");
        assertThat(foreignKeys).containsExactly(
                "CLM.CLAIM_HIST [POLICY_NO] -> CLM.POLICY ON DELETE RESTRICT");
        assertThat(indexes).containsExactly(
                "UNIQUE INDEX CLM.XCLMHST0 ON CLM.CLAIM_HIST [CLAIM_NO]",
                "INDEX CLM.XCLMHST1 ON CLM.CLAIM_HIST [POLICY_NO]",
                "INDEX CLM.XCLMHST2 ON CLM.CLAIM_HIST [POSTED_DATE]",
                "UNIQUE INDEX CLM.XPOLICY0 ON CLM.POLICY [POLICY_NO]",
                "INDEX CLM.XPOLICY1 ON CLM.POLICY [STATUS_CODE, POLICY_NO]");
        assertThat(columns).containsExactly(
                "CLM.CLAIM_HIST 1 CLAIM_NO CHAR(10) NOT NULL",
                "CLM.CLAIM_HIST 2 POLICY_NO CHAR(12) NOT NULL",
                "CLM.CLAIM_HIST 3 LOSS_DATE DATE NOT NULL",
                "CLM.CLAIM_HIST 4 TYPE_CODE CHAR(4) NOT NULL",
                "CLM.CLAIM_HIST 5 STATUS_CODE CHAR(1) NOT NULL",
                "CLM.CLAIM_HIST 6 AMT_CLAIMED DECIMAL(13, 2) NOT NULL",
                "CLM.CLAIM_HIST 7 AMT_PAID DECIMAL(13, 2) NOT NULL",
                "CLM.CLAIM_HIST 8 POSTED_DATE DATE NOT NULL",
                "CLM.CLAIM_HIST 9 RUN_ID CHAR(8) NOT NULL",
                "CLM.POLICY 1 POLICY_NO CHAR(12) NOT NULL",
                "CLM.POLICY 2 POLICY_HOLDER CHAR(30) NOT NULL",
                "CLM.POLICY 3 EFFECTIVE_DATE DATE NOT NULL",
                "CLM.POLICY 4 EXPIRY_DATE DATE NOT NULL",
                "CLM.POLICY 5 PREMIUM_AMT DECIMAL(9, 2) NOT NULL",
                "CLM.POLICY 6 COVERAGE_LIMIT DECIMAL(11, 2) NOT NULL",
                "CLM.POLICY 7 STATUS_CODE CHAR(1) NOT NULL",
                "CLM.POLICY 8 AGENT_ID CHAR(8) NOT NULL",
                // The one nullable column of the schema, which is why CLMC040 and CLMD020 fetch it
                // with an indicator variable and no other column has one.
                "CLM.POLICY 9 LAST_CLAIM_DATE DATE nullable");
    }

    /**
     * Reads one source and counts what it says against what the traits found, returning whether
     * everything about it was read. Anything that was not is added to {@code failures}, which is the
     * fixture's list or the reported one depending on which application the source came from.
     */
    private static boolean measure(String name, String source, Counts counts, List<String> failures) {
        int before = failures.size();
        Db2.Ddl schema = parse(name, source, failures);
        if (schema == null) {
            return false;
        }

        // Counted off a source that read, so a member only reported is left out of the comparison
        // rather than failing it a second time.
        String statements = withoutComments(source);
        counts.tablesWritten += count(CREATE_TABLE, statements);
        counts.indexesWritten += count(CREATE_INDEX, statements);
        counts.keysWritten += count(PRIMARY_KEY, statements);

        for (Table table : new Table.Matcher().lower(schema).collect(toList())) {
            counts.tables++;
            counts.tableNames.add(table.getQualifiedName());
            // A table need not declare one: Bank-of-Z declares no primary key anywhere and
            // enforces uniqueness with a unique index instead.
            if (!table.getPrimaryKey().isEmpty()) {
                counts.keys++;
            }
            for (Column column : table.getColumns()) {
                counts.columns++;
                // A column with no name or no type is one this model failed to read, not one the
                // DDL leaves anonymous or untyped.
                if (column.getName().isEmpty() || column.getTypeName().isEmpty()) {
                    failures.add(name + ": " + table.getQualifiedName() +
                                 " has a column with no name or type");
                }
            }
        }
        for (Index index : new Index.Matcher().lower(schema).collect(toList())) {
            counts.indexes++;
            counts.indexNames.add(index.getQualifiedName());
            if (index.getKeyColumns().isEmpty()) {
                failures.add(name + ": " + index.getQualifiedName() + " has no key");
            }
        }
        for (ForeignKey foreignKey : new ForeignKey.Matcher().lower(schema).collect(toList())) {
            counts.foreignKeys.add(foreignKey.toString());
            if (foreignKey.getTable() == null || foreignKey.getReferencedTable() == null) {
                failures.add(name + ": " + foreignKey + " has an end missing");
            }
        }
        return failures.size() == before;
    }

    /**
     * Why a member of the corpus is not DB2 for z/OS DDL, or null when it is. Each reason is named
     * so that a member this parser ought to read cannot quietly join them, and the count is asserted
     * so the set cannot grow without someone saying why.
     */
    private static @Nullable String outOfScope(Path member, String source) {
        String path = member.toString().replace('\\', '/');
        if (path.contains("/database/postgres/")) {
            // GenevaERS keeps a Postgres port of its schema beside the DB2 one, in its own dialect.
            return "Postgres";
        }
        if (source.contains("--#SET TERMINATOR")) {
            // DB2's command line processor lets a file end its statements with something other than
            // a semicolon. z/OSMF's build.sql uses @, which this parser does not read.
            return "sets its own statement terminator";
        }
        String first = source.split("\n", 2)[0].trim();
        if (first.length() > 1 && first.chars().allMatch(c -> c == '*')) {
            // Eleven GenevaERS members lost the -- from the banner comment on their first line. DB2
            // would reject them too, and the four members that kept theirs parse.
            return "its banner comment lost its --";
        }
        if ("GVBQVWS.DDL".equals(member.getFileName().toString())) {
            // Names four of its views GROUP, USER, EXIT and VIEW, which DB2 for z/OS reserves.
            return "names views with words z/OS reserves";
        }
        return null;
    }

    /**
     * Parses one source, failing it for a syntax error as well as for printing back differently.
     * ANTLR recovers from an error by inventing tokens, so a parse that reported one has read
     * something the source does not say even when the two happen to agree.
     */
    private static Db2.@Nullable Ddl parse(String name, String source, List<String> failures) {
        List<String> errors = new ArrayList<>();
        List<SourceFile> parsed = Db2Parser.builder().build()
                .parse(new InMemoryExecutionContext(t -> errors.add(t.getMessage())), source)
                .collect(toList());
        if (parsed.size() != 1 || !(parsed.get(0) instanceof Db2.Ddl)) {
            failures.add(name + ": did not parse");
            return null;
        }
        Db2.Ddl schema = (Db2.Ddl) parsed.get(0);
        if (!errors.isEmpty()) {
            failures.add(name + ": " + errors.get(0));
            return null;
        }
        if (!source.equals(schema.printAll())) {
            failures.add(name + ": did not print back");
            return null;
        }
        return schema;
    }

    private static Jcl.@Nullable CompilationUnit parseJob(Path job) throws IOException {
        byte[] source = Files.readAllBytes(job);
        List<SourceFile> parsed = JclParser.builder().build()
                .parseInputs(singletonList(new Parser.Input(job, () -> new ByteArrayInputStream(source))),
                        null, new InMemoryExecutionContext())
                .collect(toList());
        return parsed.size() == 1 && parsed.get(0) instanceof Jcl.CompilationUnit ?
                (Jcl.CompilationUnit) parsed.get(0) : null;
    }

    /**
     * A column as INTERLINKS.md section 15 tabulates it: where it sits, what it holds, and whether
     * it may be absent.
     */
    private static String describe(Table table, Column column) {
        StringBuilder type = new StringBuilder(column.getTypeName());
        if (column.getLength() != null) {
            type.append('(').append(column.getLength());
            if (column.getScale() != null) {
                type.append(", ").append(column.getScale());
            }
            type.append(')');
        }
        return table.getQualifiedName() + " " + column.getOrdinal() + " " + column.getName() +
               " " + type + (column.isNullable() ? " nullable" : " NOT NULL");
    }

    /**
     * Counting what a file says means counting what DB2 would run. GenevaERS titles a member with a
     * banner reading DDL CREATE INDEX, which is not an index.
     */
    private static String withoutComments(String source) {
        return COMMENT.matcher(source).replaceAll(" ");
    }

    private static int count(Pattern pattern, String source) {
        int count = 0;
        for (Matcher matcher = pattern.matcher(source); matcher.find(); ) {
            count++;
        }
        return count;
    }

    private static String read(Path member) throws IOException {
        return new String(Files.readAllBytes(member));
    }

    private static List<Path> members(Path root, String... extensions) throws IOException {
        List<Path> members = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> Corpus.isSource(root.relativize(path)))
                    .filter(path -> {
                        String name = path.toString().toLowerCase();
                        for (String extension : extensions) {
                            if (name.endsWith(extension)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .sorted()
                    .forEach(members::add);
        }
        return members;
    }

    /**
     * What one pass over the corpus counted: off the source on one side, off the traits on the
     * other.
     */
    private static final class Counts {
        int tablesWritten;
        int indexesWritten;
        int keysWritten;
        int tables;
        int columns;
        int indexes;
        int keys;
        final Set<String> tableNames = new LinkedHashSet<>();
        final Set<String> indexNames = new LinkedHashSet<>();
        final List<String> foreignKeys = new ArrayList<>();
    }
}
