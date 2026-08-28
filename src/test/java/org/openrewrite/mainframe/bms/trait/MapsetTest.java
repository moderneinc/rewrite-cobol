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

class MapsetTest implements RewriteTest {

    @Test
    void aMapsetHoldsItsMapsAndTheirFields() {
        rewriteRun(
          bms(
            """
              COSGN00 DFHMSD LANG=COBOL,MODE=INOUT,STORAGE=AUTO,TIOAPFX=YES,         -
                             EXTATT=YES,CTRL=(ALARM,FREEKB)
              COSGN0A DFHMDI SIZE=(24,80)
                      DFHMDF ATTRB=(ASKIP,NORM),LENGTH=6,POS=(1,1),INITIAL='Tran :'
              TRNNAME DFHMDF ATTRB=(ASKIP,FSET,NORM),LENGTH=4,POS=(1,8)
              USERID  DFHMDF ATTRB=(UNPROT,FSET,NORM),LENGTH=8,POS=(10,20)
              PASSWD  DFHMDF ATTRB=(UNPROT,DRK,FSET),LENGTH=8,POS=(11,20)
                      DFHMSD TYPE=FINAL
                      END
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Mapset> mapsets = new Mapset.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(mapsets).hasSize(1);

                Mapset mapset = mapsets.get(0);
                assertThat(mapset.getName()).isEqualTo("COSGN00");
                assertThat(mapset.getLanguage()).isEqualTo("COBOL");
                assertThat(mapset.getMode()).isEqualTo("INOUT");
                assertThat(mapset.getStorage()).isEqualTo("AUTO");
                assertThat(mapset.getControls()).containsExactly("ALARM", "FREEKB");
                assertThat(mapset.hasExtendedAttributes()).isTrue();
                assertThat(mapset.hasTerminalPrefix()).isTrue();

                assertThat(mapset.getMaps()).hasSize(1);
                MapDefinition map = mapset.getMap("COSGN0A");
                assertThat(map).isNotNull();
                assertThat(map.getLines()).isEqualTo(24);
                assertThat(map.getColumns()).isEqualTo(80);

                // Four DFHMDF, of which the literal 'Tran :' has no name and so is not one a
                // program can reach.
                assertThat(map.getFields()).hasSize(4);
                assertThat(map.getNamedFields()).extracting(Field::getName)
                  .containsExactly("TRNNAME", "USERID", "PASSWD");
            })
          )
        );
    }

    /**
     * The other half of the join to COBOL. A program that decides at run time which map it sends
     * names no map the analysis can read, but the record it sends from is generated from one — so
     * the map is recovered from the record rather than from the command.
     */
    @Test
    void aMapIsRecoverableFromTheSymbolicMapRecordAProgramSendsFrom() {
        rewriteRun(
          bms(
            """
              COACTVW DFHMSD LANG=COBOL,MODE=INOUT
              CACTVWA DFHMDI SIZE=(24,80)
              ACCTSID DFHMDF ATTRB=(UNPROT),LENGTH=11,POS=(5,20)
                      DFHMSD TYPE=FINAL
                      END
              """,
            spec -> spec.afterRecipe(cu -> {
                MapDefinition map = new MapDefinition.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(map.getOutputRecordName()).isEqualTo("CACTVWAO");
                assertThat(map.getInputRecordName()).isEqualTo("CACTVWAI");
                assertThat(map.generates("CACTVWAO")).isTrue();
                assertThat(map.generates("cactvwai")).isTrue();
                assertThat(map.generates("CACTVWA")).isFalse();
                assertThat(map.generates("CACTUPAO")).isFalse();
            })
          )
        );
    }

    /**
     * {@code DFHMSD TYPE=FINAL} closes a mapset rather than opening one, so it is not a mapset of
     * its own and the maps before it belong to the mapset that was opened.
     */
    @Test
    void typeFinalIsNotAMapset() {
        rewriteRun(
          bms(
            """
              SSMAP   DFHMSD TYPE=MAP,MODE=INOUT,LANG=COBOL
              SSMAPC1 DFHMDI SIZE=(24,80)
                      DFHMDF POS=(1,1),LENGTH=4,ATTRB=(ASKIP,BRT),INITIAL='SSC1'
                      DFHMSD TYPE=FINAL
                      END
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Mapset> mapsets = new Mapset.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(mapsets).extracting(Mapset::getName).containsExactly("SSMAP");
                assertThat(mapsets.get(0).getMaps()).hasSize(1);
            })
          )
        );
    }

    @Test
    void severalMapsInOneMapset() {
        rewriteRun(
          bms(
            """
              SSMAP   DFHMSD TYPE=MAP,MODE=INOUT,LANG=COBOL
              SSMAPC1 DFHMDI SIZE=(24,80)
              ENT1CNO DFHMDF POS=(4,50),LENGTH=10,ATTRB=(NORM,UNPROT,IC,FSET)
              SSMAPP1 DFHMDI SIZE=(24,80)
              ENP1CNO DFHMDF POS=(4,50),LENGTH=10,ATTRB=(NORM,UNPROT,FSET)
              ENP1PNO DFHMDF POS=(5,50),LENGTH=10,ATTRB=(NORM,UNPROT,FSET)
                      DFHMSD TYPE=FINAL
                      END
              """,
            spec -> spec.afterRecipe(cu -> {
                Mapset mapset = new Mapset.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(mapset.getMaps()).extracting(MapDefinition::getName)
                  .containsExactly("SSMAPC1", "SSMAPP1");
                assertThat(mapset.getMap("SSMAPC1").getFields()).hasSize(1);
                assertThat(mapset.getMap("SSMAPP1").getFields()).hasSize(2);

                // And back the other way, which is what a report reading a field needs.
                Field field = mapset.getMap("SSMAPP1").getField("ENP1PNO");
                assertThat(field.getMap().getName()).isEqualTo("SSMAPP1");
                assertThat(field.getMap().getMapset().getName()).isEqualTo("SSMAP");
            })
          )
        );
    }
}
