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
package org.openrewrite.db2.bind.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.db2.bind.Assertions.bind;

class BindCommandTest implements RewriteTest {

    @Test
    void packageBind() {
        rewriteRun(
          bind(
            """
              DSN SYSTEM(DB2P)
              BIND PACKAGE(CLMPKG) OWNER(CLMPROD) QUALIFIER(CLM) -
                   MEMBER(CLMD010) LIBRARY('CLM.PROD.DBRMLIB') -
                   ACTION(REPLACE) VALIDATE(BIND) ISOLATION(CS) -
                   RELEASE(COMMIT) CURRENTDATA(NO) EXPLAIN(NO) -
                   ENABLE(BATCH)
              END
              """,
            spec -> spec.afterRecipe(cu -> {
                List<BindCommand> commands = new BindCommand.Matcher().lower(cu).collect(Collectors.toList());
                // DSN and END name no object, so neither is a bind.
                assertThat(commands).hasSize(1);

                BindCommand bind = commands.get(0);
                assertThat(bind.getKind()).isEqualTo(BindCommand.Kind.BIND);
                assertThat(bind.bindsPackage()).isTrue();
                assertThat(bind.getCollection()).isEqualTo("CLMPKG");
                assertThat(bind.getMembers()).containsExactly("CLMD010");
                // DB2 names the package after the DBRM it is bound from, so the deck never writes it.
                assertThat(bind.getPackages()).containsExactly("CLMD010");
                assertThat(bind.getOwner()).isEqualTo("CLMPROD");
                assertThat(bind.getQualifier()).isEqualTo("CLM");
                assertThat(bind.getLibrary()).isEqualTo("CLM.PROD.DBRMLIB");
                assertThat(bind.getIsolation()).isEqualTo("CS");
                assertThat(bind.getRelease()).isEqualTo("COMMIT");
                assertThat(bind.getValidate()).isEqualTo("BIND");
                assertThat(bind.getAction()).isEqualTo("REPLACE");
                assertThat(bind.getLine()).isEqualTo(2);
            })
          )
        );
    }

    @Test
    void planBind() {
        rewriteRun(
          bind(
            """
              DSN SYSTEM(DB2P)
              BIND PLAN(CLMPLAN) OWNER(CLMPROD) QUALIFIER(CLM) -
                   PKLIST(CLMPKG.*) -
                   ACTION(REPLACE) RETAIN VALIDATE(BIND) ISOLATION(CS)
              BIND PLAN(CLMCICS) OWNER(CLMPROD) QUALIFIER(CLM) -
                   PKLIST(CLMPKG.CLMC040) -
                   ACTION(REPLACE) RETAIN
              END
              """,
            spec -> spec.afterRecipe(cu -> {
                List<BindCommand> commands = new BindCommand.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(commands).hasSize(2);

                assertThat(commands.get(0).bindsPlan()).isTrue();
                assertThat(commands.get(0).getPlans()).containsExactly("CLMPLAN");
                assertThat(commands.get(0).getPackageList()).containsExactly("CLMPKG.*");
                assertThat(commands.get(0).getCollection()).isNull();
                // RETAIN takes no value, so only its presence says the plan keeps its authorizations.
                assertThat(commands.get(0).hasOperand("RETAIN")).isTrue();
                assertThat(commands.get(1).getPackageList()).containsExactly("CLMPKG.CLMC040");
                assertThat(commands.get(1).getLine()).isEqualTo(5);
            })
          )
        );
    }

    @Test
    void rebind() {
        rewriteRun(
          bind(
            """
              DSN SYSTEM(DB2P)
              REBIND PACKAGE(CLMPKG.*) -
                   VALIDATE(BIND) EXPLAIN(NO)
              REBIND PLAN(CLMPLAN,CLMCICS) -
                   VALIDATE(BIND)
              END
              """,
            spec -> spec.afterRecipe(cu -> {
                List<BindCommand> commands = new BindCommand.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(commands).hasSize(2);

                BindCommand packages = commands.get(0);
                assertThat(packages.getKind()).isEqualTo(BindCommand.Kind.REBIND);
                // A REBIND names collection.package outright, where a BIND leaves the package to DB2.
                assertThat(packages.getCollection()).isEqualTo("CLMPKG");
                assertThat(packages.getPackages()).containsExactly("*");
                assertThat(packages.getMembers()).isEmpty();

                assertThat(commands.get(1).getPlans()).containsExactly("CLMPLAN", "CLMCICS");
            })
          )
        );
    }

    @Test
    void abbreviatedKeywords() {
        rewriteRun(
          bind(
            """
              BIND PLAN(STOCKPL) -
                   PKLIST(NULLID.*, *.STOCKTRD.*) -
                   ISO(CS) ACT(REP) REL(DEALLOCATE)
              """,
            spec -> spec.afterRecipe(cu -> {
                BindCommand bind = new BindCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(bind.getPackageList()).containsExactly("NULLID.*", "*.STOCKTRD.*");
                assertThat(bind.getIsolation()).isEqualTo("CS");
                assertThat(bind.getAction()).isEqualTo("REP");
                assertThat(bind.getRelease()).isEqualTo("DEALLOCATE");
            })
          )
        );
    }

    @Test
    void packageListWrittenOnePackageToALine() {
        rewriteRun(
          bind(
            """
              BIND PLAN(&DBPLAN)      -
               PKLIST(&DBPACK..KMGPTRAN -
                      &DBPACK..KMGP0004 -
                      &DBPACK..KMGP0005) -
               ACTION(REP)
              """,
            spec -> spec.afterRecipe(cu -> {
                BindCommand bind = new BindCommand.Matcher().lower(cu).findFirst().orElseThrow();
                // The dashes hold the list together across the lines; they are not entries in it.
                assertThat(bind.getPackageList())
                  .containsExactly("&DBPACK..KMGPTRAN", "&DBPACK..KMGP0004", "&DBPACK..KMGP0005");
                assertThat(bind.getAction()).isEqualTo("REP");
            })
          )
        );
    }

    @Test
    void packageQualifiedByItsLocation() {
        rewriteRun(
          bind(
            """
              BIND PACKAGE(SITE1.CLMPKG) MEMBER(CLMD010)
              REBIND PACKAGE(SITE1.CLMPKG.CLMD010)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<BindCommand> commands = new BindCommand.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(commands.get(0).getCollection()).isEqualTo("CLMPKG");
                assertThat(commands.get(1).getCollection()).isEqualTo("CLMPKG");
                assertThat(commands.get(1).getPackages()).containsExactly("CLMD010");
            })
          )
        );
    }
}
