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
package org.openrewrite.mainframe.bms.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.bms.Assertions.bms;

class FieldTest implements RewriteTest {

    @Test
    void whatAFieldSaysAboutItself() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
              USERID  DFHMDF ATTRB=(UNPROT,FSET,NORM),COLOR=GREEN,HILIGHT=UNDERLINE, -
                             LENGTH=8,POS=(10,20),INITIAL='________'
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.getName()).isEqualTo("USERID");
                assertThat(field.getPosition()).isEqualTo(new Position(10, 20));
                assertThat(field.getLength()).isEqualTo(8);
                assertThat(field.getInitial()).isEqualTo("________");
                assertThat(field.getColor()).isEqualTo("GREEN");
                assertThat(field.getHighlight()).isEqualTo("UNDERLINE");
                assertThat(field.getAttributes())
                  .containsExactlyInAnyOrder(Attribute.UNPROT, Attribute.FSET, Attribute.NORM);
                assertThat(field.isInput()).isTrue();
                assertThat(field.isProtected()).isFalse();
            })
          )
        );
    }

    /**
     * A field that says nothing about protection cannot be typed into, so reporting it as an input
     * would be wrong. {@code UNPROT} has to be written.
     */
    @Test
    void onlyUnprotectedFieldsAreInputs() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
              TITLE01 DFHMDF ATTRB=(ASKIP,FSET,NORM),LENGTH=40,POS=(1,21)
              CURDATE DFHMDF LENGTH=8,POS=(1,71)
              USERID  DFHMDF ATTRB=(UNPROT,FSET,NORM),LENGTH=8,POS=(10,20)
              ERRMSG  DFHMDF ATTRB=(PROT,BRT),LENGTH=78,POS=(23,1)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Field> fields = new Field.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(fields).filteredOn(Field::isInput).extracting(Field::getName)
                  .containsExactly("USERID");
                assertThat(fields).filteredOn(Field::isProtected).extracting(Field::getName)
                  .containsExactly("TITLE01", "CURDATE", "ERRMSG");
            })
          )
        );
    }

    /**
     * The join to COBOL. A program never names the BMS field — it names the data items BMS generated
     * from it in the symbolic map, and those names are the field's with a letter appended.
     */
    @Test
    void generatesTheSymbolicMapNamesAProgramActuallyUses() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
              TRNNAME DFHMDF ATTRB=(ASKIP,FSET,NORM),LENGTH=4,POS=(1,8)
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.getInputName()).isEqualTo("TRNNAMEI");
                assertThat(field.getOutputName()).isEqualTo("TRNNAMEO");
                assertThat(field.getLengthName()).isEqualTo("TRNNAMEL");

                assertThat(field.generates("TRNNAMEO")).isTrue();
                assertThat(field.generates("trnnamei")).isTrue();
                assertThat(field.generates("TRNNAMEA")).isTrue();
                assertThat(field.generates("TRNNAME")).isFalse();
                assertThat(field.generates("CURDATEO")).isFalse();
            })
          )
        );
    }

    /**
     * Which name a program used, not merely that it used one of them. Only the value subfields put
     * anything on the screen or take anything off it — a lineage row about the colour byte would be
     * describing how the field is drawn as though it were data.
     */
    @Test
    void tellsTheValueSubfieldsFromTheOnesThatOnlySayHowAFieldIsDrawn() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
              TRNNAME DFHMDF ATTRB=(ASKIP,FSET,NORM),LENGTH=4,POS=(1,8)
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.subfieldOf("TRNNAMEO")).isEqualTo(Field.Subfield.OUTPUT);
                assertThat(field.subfieldOf("trnnamei")).isEqualTo(Field.Subfield.INPUT);
                assertThat(field.subfieldOf("TRNNAMEL")).isEqualTo(Field.Subfield.LENGTH);
                assertThat(field.subfieldOf("TRNNAMEC")).isEqualTo(Field.Subfield.COLOR);
                assertThat(field.subfieldOf("TRNNAMEZ")).isNull();
                assertThat(field.subfieldOf("TRNNAME")).isNull();

                assertThat(Field.Subfield.OUTPUT.isValue()).isTrue();
                assertThat(Field.Subfield.INPUT.isValue()).isTrue();
                assertThat(Field.Subfield.LENGTH.isValue()).isFalse();
                assertThat(Field.Subfield.ATTRIBUTE.isValue()).isFalse();
            })
          )
        );
    }

    /**
     * A field whose name is a prefix of another's. Matching on the prefix alone would report
     * {@code CUSTNOL} as the length of {@code CUSTNO} and as the outline attribute of {@code CUSTN}.
     */
    @Test
    void aNameIsOnlyASubfieldOfTheFieldItIsExactlyOneLetterLongerThan() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
              CUSTN   DFHMDF ATTRB=(UNPROT),LENGTH=5,POS=(3,1)
              CUSTNO  DFHMDF ATTRB=(UNPROT),LENGTH=9,POS=(4,1)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Field> fields = new Field.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(fields).filteredOn(f -> f.generates("CUSTNOL")).extracting(Field::getName)
                  .containsExactly("CUSTNO");
                assertThat(fields).filteredOn(f -> f.generates("CUSTNOI")).extracting(Field::getName)
                  .containsExactly("CUSTNO");
            })
          )
        );
    }

    @Test
    void aFieldWritingALiteralHasNoNameAndGeneratesNothing() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
                      DFHMDF ATTRB=(ASKIP,NORM),LENGTH=6,POS=(1,1),INITIAL='Tran :'
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.getName()).isNull();
                assertThat(field.getInitial()).isEqualTo("Tran :");
                assertThat(field.getGeneratedNames()).isEmpty();
                assertThat(field.getInputName()).isNull();
            })
          )
        );
    }

    @Test
    void picturesAndOccurs() {
        rewriteRun(
          bms(
            """
              COTRT0A DFHMDI SIZE=(24,80)
              TRTYPE  DFHMDF POS=(5,10),LENGTH=9,ATTRB=(UNPROT,NUM),                 -
                             PICIN='9(9)',PICOUT='ZZZ,ZZZ,ZZ9',OCCURS=10
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.getPictureIn()).isEqualTo("9(9)");
                assertThat(field.getPictureOut()).isEqualTo("ZZZ,ZZZ,ZZ9");
                assertThat(field.getOccurs()).isEqualTo(10);
                assertThat(field.isNumeric()).isTrue();
            })
          )
        );
    }

    /**
     * The other form of {@code POS}: one number, counted from zero, running along the map a row at a
     * time. Nothing on the field says where that lands — only the map's width does.
     */
    @Test
    void aScalarPosIsAnOffsetIntoTheMap() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
                      DFHMDF ATTRB=(ASKIP,NORM),LENGTH=6,POS=0,INITIAL='Tran :'
              USERID  DFHMDF ATTRB=(UNPROT,FSET,NORM),LENGTH=8,POS=739
              ERRMSG  DFHMDF ATTRB=(PROT,BRT),LENGTH=78,POS=1760
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Field> fields = new Field.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(fields).extracting(Field::getPosition)
                  .containsExactly(new Position(1, 1), new Position(10, 20), new Position(23, 1));
            })
          )
        );
    }

    /**
     * The same number on a narrower map is somewhere else entirely, which is why this cannot be read
     * off the field alone.
     */
    @Test
    void theSameOffsetOnANarrowerMapIsADifferentPlace() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(12,40)
              USERID  DFHMDF ATTRB=(UNPROT),LENGTH=8,POS=45
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.getPosition()).isEqualTo(new Position(2, 6));
            })
          )
        );
    }

    /**
     * A map that declares no {@code SIZE} is a page of whatever the mapset's device is, so that is
     * the width the offset counts against.
     */
    @Test
    void aMapWithoutASizeIsAPageOfTheMapsetsDevice() {
        rewriteRun(
          bms(
            """
              COSGN00 DFHMSD LANG=COBOL,MODE=INOUT,TERM=3270-1
              COSGN0A DFHMDI CTRL=FREEKB
              USERID  DFHMDF ATTRB=(UNPROT),LENGTH=8,POS=45
                      DFHMSD TYPE=FINAL
                      END
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.getPosition()).isEqualTo(new Position(2, 6));
            })
          )
        );
    }

    /**
     * A password field is written by hiding what is typed, so this is how a report finds one.
     */
    @Test
    void darkFieldsAreHidden() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
              PASSWD  DFHMDF ATTRB=(UNPROT,DRK,FSET),LENGTH=8,POS=(11,20)
              """,
            spec -> spec.afterRecipe(cu -> {
                Field field = new Field.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(field.isDark()).isTrue();
                assertThat(field.isInput()).isTrue();
            })
          )
        );
    }
}
