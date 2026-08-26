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
package org.openrewrite.ims.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.ims.ImsParser;
import org.openrewrite.ims.tree.Ims;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * What a DBD means, read off members written the way the corpus writes them.
 * <p>
 * These parse directly rather than through {@code Assertions.ims}: the operand field and column 72
 * are where the meaning is, and the test framework trims the common indentation off a source block.
 */
class DatabaseTest {

    private static final String CLAIM_DATABASE =
      "*  THE CLAIM DATABASE, INDEXED BY ADJUSTER\n" +
      "         DBD   NAME=CLMDBD01,ACCESS=(HDAM,VSAM)," + blanks(23) + "X\n" +
      "               RMNAME=(DFSHDC40,5,500,824)\n" +
      "         DATASET DD1=CLMDB01,DEVICE=3390,SIZE=(4096),SCAN=3\n" +
      "*\n" +
      "         SEGM  NAME=CLMROOT,PARENT=0,BYTES=65\n" +
      "         FIELD NAME=(CLMKEY,SEQ,U),BYTES=10,START=1,TYPE=C\n" +
      "         FIELD NAME=CLMADJR,BYTES=8,START=50,TYPE=C\n" +
      "         LCHILD NAME=(CLMXSEG,CLMDBX01),POINTER=INDX\n" +
      "         XDFLD NAME=CLMXADJ,SRCH=CLMADJR\n" +
      "*\n" +
      "         SEGM  NAME=CLMPLNK," + blanks(43) + "X\n" +
      "               PARENT=((CLMROOT),(POLROOT,PHYSICAL,CLMDBD02))," + blanks(9) + "X\n" +
      "               BYTES=12,POINTER=(LPARNT),RULES=(LLL,LAST)\n" +
      "         FIELD NAME=(CLMLPCK,SEQ,U),BYTES=12,START=1,TYPE=C\n" +
      "*\n" +
      "         DBDGEN\n" +
      "         FINISH\n" +
      "         END\n";

    @Test
    void theDatabaseAndItsDataSets() {
        Database database = database(CLAIM_DATABASE);

        assertThat(database.getName()).isEqualTo("CLMDBD01");
        assertThat(database.getAccessMethod()).isEqualTo("HDAM");
        assertThat(database.getAccess()).containsExactly("HDAM", "VSAM");
        assertThat(database.getRandomizer()).isEqualTo("DFSHDC40");
        assertThat(database.isSequential()).isFalse();
        assertThat(database.getLine()).isEqualTo(2);

        // The DD name is what a job step has to supply, and it is a DD name and not a data set.
        assertThat(database.getDdNames()).containsExactly("CLMDB01");
        assertThat(database.getDataSetGroups()).singleElement()
          .satisfies(group -> assertThat(group.getDevice()).isEqualTo("3390"));
    }

    @Test
    void theSegmentsAndTheirHierarchy() {
        Database database = database(CLAIM_DATABASE);

        assertThat(database.getSegments()).extracting(Segment::getName, Segment::getParentName,
            Segment::getBytes, Segment::getLine)
          .containsExactly(
            tuple("CLMROOT", null, 65, 6),
            tuple("CLMPLNK", "CLMROOT", 12, 12));
        assertThat(database.getRootSegment()).isNotNull()
          .extracting(Segment::getName).isEqualTo("CLMROOT");

        Segment root = database.getSegment("CLMROOT");
        assertThat(root).isNotNull();
        assertThat(root.isRoot()).isTrue();
        assertThat(root.getSequenceField()).isNotNull()
          .extracting(Field::getName).isEqualTo("CLMKEY");

        Segment link = database.getSegment("CLMPLNK");
        assertThat(link).isNotNull();
        assertThat(link.getPointers()).containsExactly("LPARNT");
        assertThat(link.getRules()).containsExactly("LLL", "LAST");
    }

    @Test
    void theFieldsOfASegment() {
        Segment root = database(CLAIM_DATABASE).getSegment("CLMROOT");
        assertThat(root).isNotNull();

        assertThat(root.getFields()).extracting(Field::getName, Field::isSequence, Field::isUnique,
            Field::getBytes, Field::getStart, Field::getType, Field::getLine)
          .containsExactly(
            tuple("CLMKEY", true, true, 10, 1, "C", 7),
            tuple("CLMADJR", false, false, 8, 50, "C", 8));

        // Containment is read from position, so a field knows the segment it was written under.
        assertThat(root.getField("CLMADJR")).isNotNull()
          .extracting(field -> field.getSegment().getName()).isEqualTo("CLMROOT");
    }

    /**
     * A sequence field written {@code M} allows duplicates, which is what a secondary index needs.
     */
    @Test
    void aSequenceFieldThatAllowsDuplicates() {
        Database index = database(
          "         DBD   NAME=CLMDBX01,ACCESS=INDEX\n" +
          "         DATASET DD1=CLMDBX01,DEVICE=3390\n" +
          "         SEGM  NAME=CLMXSEG,PARENT=0,BYTES=18\n" +
          "         FIELD NAME=(CLMXKEY,SEQ,M),BYTES=8,START=1,TYPE=C\n" +
          "         LCHILD NAME=(CLMROOT,CLMDBD01),INDEX=CLMXADJ,POINTER=SYMB\n" +
          "         DBDGEN\n");

        Segment segment = index.getSegment("CLMXSEG");
        assertThat(segment).isNotNull();
        Field key = segment.getSequenceField();
        assertThat(key).isNotNull();
        assertThat(key.getName()).isEqualTo("CLMXKEY");
        assertThat(key.isUnique()).isFalse();

        assertThat(segment.getLogicalChildren()).singleElement().satisfies(child -> {
            assertThat(child.getSegmentName()).isEqualTo("CLMROOT");
            assertThat(child.getDatabaseName()).isEqualTo("CLMDBD01");
            assertThat(child.getIndexFieldName()).isEqualTo("CLMXADJ");
            assertThat(child.getPointer()).isEqualTo("SYMB");
        });
    }

    /**
     * Every name a DBD writes down that belongs to another database, which is the whole of what one
     * says about the rest of an estate.
     */
    @Test
    void theDatabasesADatabaseNames() {
        assertThat(database(CLAIM_DATABASE).getReferences())
          .extracting(Database.Reference::getKind, Database.Reference::getDatabase,
            Database.Reference::getMember, Database.Reference::getLine)
          .containsExactly(
            tuple(Database.Reference.Kind.LOGICAL_CHILD, "CLMDBX01", "CLMXSEG", 9),
            tuple(Database.Reference.Kind.INDEX_SOURCE, "CLMDBD01", "CLMADJR", 10),
            tuple(Database.Reference.Kind.LOGICAL_PARENT, "CLMDBD02", "POLROOT", 12));
    }

    @Test
    void theSegmentAndDatabaseAnIndexFieldIsBuiltFrom() {
        Segment root = database(CLAIM_DATABASE).getSegment("CLMROOT");
        assertThat(root).isNotNull();

        assertThat(root.getIndexFields()).singleElement().satisfies(index -> {
            assertThat(index.getName()).isEqualTo("CLMXADJ");
            assertThat(index.getSearchFields()).containsExactly("CLMADJR");
            assertThat(index.getSubsequenceFields()).isEmpty();
            assertThat(index.getSegment().getName()).isEqualTo("CLMROOT");
            assertThat(index.getDatabase().getName()).isEqualTo("CLMDBD01");
        });
    }

    /**
     * A GSAM database is a sequential file DL/I repositions on a checkpoint. It has no {@code SEGM}
     * at all, so the record length on the data set is the whole of what it holds.
     */
    @Test
    void aSequentialDatabaseHasNoSegments() {
        Database gsam = database(
          "         DBD   NAME=CLMDBG01,ACCESS=(GSAM,BSAM)\n" +
          "         DATASET DD1=CLMGSIN,RECFM=FB,RECORD=(200,27800)\n" +
          "         DBDGEN\n" +
          "         FINISH\n" +
          "         END\n");

        assertThat(gsam.isSequential()).isTrue();
        assertThat(gsam.getSegments()).isEmpty();
        assertThat(gsam.getRootSegment()).isNull();
        assertThat(gsam.getReferences()).isEmpty();
        assertThat(gsam.getDataSetGroups()).singleElement()
          .satisfies(group -> assertThat(group.getRecordLengths()).containsExactly(200, 27800));
    }

    /**
     * HISAM writes two DD names, and a job that allocates one and not the other cannot open the
     * database.
     */
    @Test
    void anOverflowDataSetIsASecondDdName() {
        Database hisam = database(
          "         DBD   NAME=CLMDBD03,ACCESS=(HISAM,VSAM)\n" +
          "         DATASET DD1=CLMTYP1,DD2=CLMTYP2,DEVICE=3390," + blanks(18) + "X\n" +
          "               RECORD=(80,80)\n" +
          "         SEGM  NAME=TYPROOT,PARENT=0,BYTES=80\n" +
          "         DBDGEN\n");

        assertThat(hisam.getDdNames()).containsExactly("CLMTYP1", "CLMTYP2");
        assertThat(hisam.getDataSetGroups()).singleElement().satisfies(group -> {
            assertThat(group.getPrimaryDdName()).isEqualTo("CLMTYP1");
            assertThat(group.getOverflowDdName()).isEqualTo("CLMTYP2");
            assertThat(group.getRecordLengths()).containsExactly(80, 80);
        });
    }

    /**
     * CardDemo labels its data set group in column 1 and writes {@code PARENT=((PAUTSUM0,))}, which
     * is the parenthesised form of one physical parent.
     */
    @Test
    void aLabelledDataSetGroupAndAParenthesisedParent() {
        Database database = database(
          "         TITLE   'ASSEMBLE OF DBDNAME=DBPAUTP0 '\n" +
          "       DBD     NAME=DBPAUTP0,ACCESS=(HIDAM,VSAM),PASSWD=NO," + blanks(12) + "C\n" +
          "               VERSION=\n" +
          "DSG001 DATASET DD1=DDPAUTP0,SIZE=(4096),SCAN=3\n" +
          "       SEGM    NAME=PAUTSUM0,PARENT=0,BYTES=100\n" +
          "       SEGM    NAME=PAUTDTL1,PARENT=((PAUTSUM0,)),BYTES=200\n" +
          "       DBDGEN\n");

        assertThat(database.getName()).isEqualTo("DBPAUTP0");
        assertThat(database.getDataSetGroups()).singleElement()
          .satisfies(group -> assertThat(group.getName()).isEqualTo("DSG001"));
        assertThat(database.getSegments()).extracting(Segment::getName, Segment::getParentName)
          .containsExactly(tuple("PAUTSUM0", null), tuple("PAUTDTL1", "PAUTSUM0"));
        assertThat(database.getSegment("PAUTDTL1").getLogicalParent()).isNull();
    }

    private static Database database(String source) {
        Parser.Input input = new Parser.Input(Paths.get("CLMDBD01.dbd"),
          () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        List<SourceFile> parsed = ImsParser.builder().build()
          .parseInputs(singletonList(input), null, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).singleElement().isInstanceOf(Ims.CompilationUnit.class);
        return new Database.Matcher().lower(parsed.get(0)).findFirst().orElseThrow(AssertionError::new);
    }

    private static String blanks(int n) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < n; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }
}
