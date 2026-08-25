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
package org.openrewrite.db2;

import org.jspecify.annotations.Nullable;
import org.openrewrite.TreeVisitor;
import org.openrewrite.db2.tree.*;
import org.openrewrite.internal.ListUtils;

public class Db2Visitor<P> extends TreeVisitor<Db2, P> {

    public Db2 visitDdl(Db2.Ddl ddl, P p) {
        Db2.Ddl d = ddl;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DDL_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.getPadding().withStatements(ListUtils.map(d.getPadding().getStatements(),
                s -> visitRightPadded(s, p)));
        return d.withEof(visitSpace(d.getEof(), Space.Location.DDL_EOF, p));
    }

    public Db2 visitCreateTable(Db2.CreateTable createTable, P p) {
        Db2.CreateTable x = createTable;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_TABLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withContents(visitAndCast(x.getContents(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateIndex(Db2.CreateIndex createIndex, P p) {
        Db2.CreateIndex x = createIndex;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_INDEX_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOn(visitAndCast(x.getOn(), p));
        x = x.withTable(visitAndCast(x.getTable(), p));
        x = x.withKeys(visitContainer(x.getKeys(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateTablespace(Db2.CreateTablespace createTablespace, P p) {
        Db2.CreateTablespace x = createTablespace;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_TABLESPACE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        if (x.getIn() != null) {
            x = x.withIn(visitAndCast(x.getIn(), p));
        }
        if (x.getDatabase() != null) {
            x = x.withDatabase(visitAndCast(x.getDatabase(), p));
        }
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateDatabase(Db2.CreateDatabase createDatabase, P p) {
        Db2.CreateDatabase x = createDatabase;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_DATABASE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateStogroup(Db2.CreateStogroup createStogroup, P p) {
        Db2.CreateStogroup x = createStogroup;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_STOGROUP_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateView(Db2.CreateView createView, P p) {
        Db2.CreateView x = createView;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_VIEW_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        if (x.getColumns() != null) {
            x = x.withColumns(visitContainer(x.getColumns(), p));
        }
        x = x.withQuery(visitLeftPadded(x.getQuery(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateAlias(Db2.CreateAlias createAlias, P p) {
        Db2.CreateAlias x = createAlias;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_ALIAS_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withTarget(visitLeftPadded(x.getTarget(), p));
        return x;
    }

    public Db2 visitCreateSynonym(Db2.CreateSynonym createSynonym, P p) {
        Db2.CreateSynonym x = createSynonym;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_SYNONYM_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withTarget(visitLeftPadded(x.getTarget(), p));
        return x;
    }

    public Db2 visitCreateSequence(Db2.CreateSequence createSequence, P p) {
        Db2.CreateSequence x = createSequence;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_SEQUENCE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        if (x.getType() != null) {
            x = x.withType(visitLeftPadded(x.getType(), p));
        }
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateRole(Db2.CreateRole createRole, P p) {
        Db2.CreateRole x = createRole;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_ROLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        return x;
    }

    public Db2 visitCreateAuxiliaryTable(Db2.CreateAuxiliaryTable createAuxiliaryTable, P p) {
        Db2.CreateAuxiliaryTable x = createAuxiliaryTable;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_AUXILIARY_TABLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateType(Db2.CreateType createType, P p) {
        Db2.CreateType x = createType;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_TYPE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withType(visitLeftPadded(x.getType(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateVariable(Db2.CreateVariable createVariable, P p) {
        Db2.CreateVariable x = createVariable;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_VARIABLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withType(visitAndCast(x.getType(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateMask(Db2.CreateMask createMask, P p) {
        Db2.CreateMask x = createMask;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_MASK_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withTable(visitAndCast(x.getTable(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreatePermission(Db2.CreatePermission createPermission, P p) {
        Db2.CreatePermission x = createPermission;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_PERMISSION_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withTable(visitAndCast(x.getTable(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateTrustedContext(Db2.CreateTrustedContext createTrustedContext, P p) {
        Db2.CreateTrustedContext x = createTrustedContext;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_TRUSTED_CONTEXT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitCreateTrigger(Db2.CreateTrigger createTrigger, P p) {
        Db2.CreateTrigger x = createTrigger;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_TRIGGER_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withEvents(ListUtils.map(x.getEvents(), e -> visitAndCast(e, p)));
        x = x.withTable(visitAndCast(x.getTable(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        if (x.getBody() != null) {
            x = x.withBody(visitAndCast(x.getBody(), p));
        }
        return x;
    }

    public Db2 visitCreateProcedure(Db2.CreateProcedure createProcedure, P p) {
        Db2.CreateProcedure x = createProcedure;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_PROCEDURE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withParameters(visitContainer(x.getParameters(), p));
        x = x.withClauses(ListUtils.map(x.getClauses(), e -> visitAndCast(e, p)));
        if (x.getBody() != null) {
            x = x.withBody(visitAndCast(x.getBody(), p));
        }
        return x;
    }

    public Db2 visitCreateFunction(Db2.CreateFunction createFunction, P p) {
        Db2.CreateFunction x = createFunction;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CREATE_FUNCTION_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withParameters(visitContainer(x.getParameters(), p));
        x = x.withClauses(ListUtils.map(x.getClauses(), e -> visitAndCast(e, p)));
        if (x.getBody() != null) {
            x = x.withBody(visitAndCast(x.getBody(), p));
        }
        return x;
    }

    public Db2 visitDeclareGlobalTemporaryTable(Db2.DeclareGlobalTemporaryTable declareGlobalTemporaryTable, P p) {
        Db2.DeclareGlobalTemporaryTable x = declareGlobalTemporaryTable;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.DECLARE_GLOBAL_TEMPORARY_TABLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withContents(visitAndCast(x.getContents(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterTable(Db2.AlterTable alterTable, P p) {
        Db2.AlterTable x = alterTable;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_TABLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withActions(ListUtils.map(x.getActions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterTablespace(Db2.AlterTablespace alterTablespace, P p) {
        Db2.AlterTablespace x = alterTablespace;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_TABLESPACE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        if (x.getIn() != null) {
            x = x.withIn(visitAndCast(x.getIn(), p));
        }
        if (x.getDatabase() != null) {
            x = x.withDatabase(visitAndCast(x.getDatabase(), p));
        }
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterIndex(Db2.AlterIndex alterIndex, P p) {
        Db2.AlterIndex x = alterIndex;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_INDEX_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterDatabase(Db2.AlterDatabase alterDatabase, P p) {
        Db2.AlterDatabase x = alterDatabase;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_DATABASE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterStogroup(Db2.AlterStogroup alterStogroup, P p) {
        Db2.AlterStogroup x = alterStogroup;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_STOGROUP_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterSequence(Db2.AlterSequence alterSequence, P p) {
        Db2.AlterSequence x = alterSequence;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_SEQUENCE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterView(Db2.AlterView alterView, P p) {
        Db2.AlterView x = alterView;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_VIEW_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterProcedure(Db2.AlterProcedure alterProcedure, P p) {
        Db2.AlterProcedure x = alterProcedure;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_PROCEDURE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withParameters(visitContainer(x.getParameters(), p));
        x = x.withClauses(ListUtils.map(x.getClauses(), e -> visitAndCast(e, p)));
        if (x.getBody() != null) {
            x = x.withBody(visitAndCast(x.getBody(), p));
        }
        return x;
    }

    public Db2 visitAlterFunction(Db2.AlterFunction alterFunction, P p) {
        Db2.AlterFunction x = alterFunction;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_FUNCTION_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withParameters(visitContainer(x.getParameters(), p));
        x = x.withClauses(ListUtils.map(x.getClauses(), e -> visitAndCast(e, p)));
        if (x.getBody() != null) {
            x = x.withBody(visitAndCast(x.getBody(), p));
        }
        return x;
    }

    public Db2 visitAlterTrigger(Db2.AlterTrigger alterTrigger, P p) {
        Db2.AlterTrigger x = alterTrigger;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_TRIGGER_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterMask(Db2.AlterMask alterMask, P p) {
        Db2.AlterMask x = alterMask;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_MASK_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterPermission(Db2.AlterPermission alterPermission, P p) {
        Db2.AlterPermission x = alterPermission;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_PERMISSION_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitAlterTrustedContext(Db2.AlterTrustedContext alterTrustedContext, P p) {
        Db2.AlterTrustedContext x = alterTrustedContext;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ALTER_TRUSTED_CONTEXT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitDrop(Db2.Drop drop, P p) {
        Db2.Drop x = drop;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.DROP_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitRename(Db2.Rename rename, P p) {
        Db2.Rename x = rename;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.RENAME_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withNewName(visitLeftPadded(x.getNewName(), p));
        return x;
    }

    public Db2 visitComment(Db2.Comment comment, P p) {
        Db2.Comment x = comment;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withTarget(visitAndCast(x.getTarget(), p));
        x = x.withText(visitLeftPadded(x.getText(), p));
        return x;
    }

    public Db2 visitLabel(Db2.Label label, P p) {
        Db2.Label x = label;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.LABEL_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withTarget(visitAndCast(x.getTarget(), p));
        x = x.withText(visitLeftPadded(x.getText(), p));
        return x;
    }

    public Db2 visitGrant(Db2.Grant grant, P p) {
        Db2.Grant x = grant;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.GRANT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withPrivileges(visitContainer(x.getPrivileges(), p));
        x = x.withOn(ListUtils.map(x.getOn(), e -> visitAndCast(e, p)));
        x = x.withObjects(visitContainer(x.getObjects(), p));
        if (x.getTo() != null) {
            x = x.withTo(visitAndCast(x.getTo(), p));
        }
        x = x.withGrantees(visitContainer(x.getGrantees(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitRevoke(Db2.Revoke revoke, P p) {
        Db2.Revoke x = revoke;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.REVOKE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withPrivileges(visitContainer(x.getPrivileges(), p));
        x = x.withOn(ListUtils.map(x.getOn(), e -> visitAndCast(e, p)));
        x = x.withObjects(visitContainer(x.getObjects(), p));
        if (x.getTo() != null) {
            x = x.withTo(visitAndCast(x.getTo(), p));
        }
        x = x.withGrantees(visitContainer(x.getGrantees(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitSet(Db2.Set set, P p) {
        Db2.Set x = set;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.SET_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withValue(visitLeftPadded(x.getValue(), p));
        return x;
    }

    public Db2 visitCommit(Db2.Commit commit, P p) {
        Db2.Commit x = commit;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.COMMIT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitRollback(Db2.Rollback rollback, P p) {
        Db2.Rollback x = rollback;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.ROLLBACK_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        if (x.getSavepoint() != null) {
            x = x.withSavepoint(visitLeftPadded(x.getSavepoint(), p));
        }
        return x;
    }

    public Db2 visitSavepoint(Db2.Savepoint savepoint, P p) {
        Db2.Savepoint x = savepoint;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.SAVEPOINT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitReleaseSavepoint(Db2.ReleaseSavepoint releaseSavepoint, P p) {
        Db2.ReleaseSavepoint x = releaseSavepoint;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.RELEASE_SAVEPOINT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        return x;
    }

    public Db2 visitLockTable(Db2.LockTable lockTable, P p) {
        Db2.LockTable x = lockTable;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.LOCK_TABLE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withName(visitAndCast(x.getName(), p));
        return x;
    }

    public Db2 visitInsert(Db2.Insert insert, P p) {
        Db2.Insert x = insert;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.INSERT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withTable(visitAndCast(x.getTable(), p));
        if (x.getColumns() != null) {
            x = x.withColumns(visitContainer(x.getColumns(), p));
        }
        x = x.withValues(visitContainer(x.getValues(), p));
        return x;
    }

    public Db2 visitEmpty(Db2.Empty empty, P p) {
        Db2.Empty x = empty;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.EMPTY_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        return x;
    }

    public Db2 visitTableElements(Db2.TableElements tableElements, P p) {
        Db2.TableElements x = tableElements;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.TABLE_ELEMENTS_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withElements(visitContainer(x.getElements(), p));
        return x;
    }

    public Db2 visitTableLike(Db2.TableLike tableLike, P p) {
        Db2.TableLike x = tableLike;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.TABLE_LIKE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withTable(visitAndCast(x.getTable(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitTableAsQuery(Db2.TableAsQuery tableAsQuery, P p) {
        Db2.TableAsQuery x = tableAsQuery;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.TABLE_AS_QUERY_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withQuery(visitContainer(x.getQuery(), p));
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitColumnDefinition(Db2.ColumnDefinition columnDefinition, P p) {
        Db2.ColumnDefinition x = columnDefinition;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.COLUMN_DEFINITION_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.withType(visitAndCast(x.getType(), p));
        x = x.withAttributes(ListUtils.map(x.getAttributes(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitConstraint(Db2.Constraint constraint, P p) {
        Db2.Constraint x = constraint;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.CONSTRAINT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        if (x.getConstraintKeyword() != null) {
            x = x.withConstraintKeyword(visitAndCast(x.getConstraintKeyword(), p));
        }
        if (x.getConstraintName() != null) {
            x = x.withConstraintName(visitAndCast(x.getConstraintName(), p));
        }
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        if (x.getKeyName() != null) {
            x = x.withKeyName(visitAndCast(x.getKeyName(), p));
        }
        if (x.getColumns() != null) {
            x = x.withColumns(visitContainer(x.getColumns(), p));
        }
        if (x.getReferences() != null) {
            x = x.withReferences(visitAndCast(x.getReferences(), p));
        }
        if (x.getReferencedTable() != null) {
            x = x.withReferencedTable(visitAndCast(x.getReferencedTable(), p));
        }
        if (x.getReferencedColumns() != null) {
            x = x.withReferencedColumns(visitContainer(x.getReferencedColumns(), p));
        }
        x = x.withOptions(ListUtils.map(x.getOptions(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitDataType(Db2.DataType dataType, P p) {
        Db2.DataType x = dataType;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.DATA_TYPE_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withName(visitAndCast(x.getName(), p));
        if (x.getArguments() != null) {
            x = x.withArguments(visitContainer(x.getArguments(), p));
        }
        x = x.withAttributes(ListUtils.map(x.getAttributes(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitIndexKey(Db2.IndexKey indexKey, P p) {
        Db2.IndexKey x = indexKey;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.INDEX_KEY_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withName(visitAndCast(x.getName(), p));
        if (x.getDirection() != null) {
            x = x.withDirection(visitAndCast(x.getDirection(), p));
        }
        return x;
    }

    public Db2 visitOption(Db2.Option option, P p) {
        Db2.Option x = option;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.OPTION_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withValues(ListUtils.map(x.getValues(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitParameter(Db2.Parameter parameter, P p) {
        Db2.Parameter x = parameter;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.PARAMETER_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        if (x.getMode() != null) {
            x = x.withMode(visitAndCast(x.getMode(), p));
        }
        if (x.getName() != null) {
            x = x.withName(visitAndCast(x.getName(), p));
        }
        x = x.withType(visitAndCast(x.getType(), p));
        return x;
    }

    public Db2 visitQuery(Db2.Query query, P p) {
        Db2.Query x = query;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.QUERY_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withParts(ListUtils.map(x.getParts(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitBlock(Db2.Block block, P p) {
        Db2.Block x = block;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.BLOCK_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withKeywords(ListUtils.map(x.getKeywords(), e -> visitAndCast(e, p)));
        x = x.withBody(ListUtils.map(x.getBody(), e -> visitAndCast(e, p)));
        if (x.getEnd() != null) {
            x = x.withEnd(visitAndCast(x.getEnd(), p));
        }
        if (x.getLabel() != null) {
            x = x.withLabel(visitAndCast(x.getLabel(), p));
        }
        return x;
    }

    public Db2 visitName(Db2.Name name, P p) {
        Db2.Name x = name;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.NAME_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withParts(ListUtils.map(x.getParts(), e -> visitAndCast(e, p)));
        return x;
    }

    public Db2 visitKeyword(Db2.Keyword keyword, P p) {
        Db2.Keyword x = keyword;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.KEYWORD_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        return x;
    }

    public Db2 visitWord(Db2.Word word, P p) {
        Db2.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public <T extends Db2> Db2Container<T> visitContainer(Db2Container<T> container, P p) {
        Db2Container<T> c = container.withBefore(
                visitSpace(container.getBefore(), Space.Location.CONTAINER_BEFORE, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.getPadding().withElements(
                ListUtils.map(c.getPadding().getElements(), e -> visitRightPadded(e, p)));
    }

    public <T> Db2RightPadded<T> visitRightPadded(Db2RightPadded<T> padded, P p) {
        Db2RightPadded<T> r = padded;
        if (r.getElement() instanceof Db2) {
            //noinspection unchecked
            r = r.withElement((T) visitAndCast((Db2) r.getElement(), p));
        }
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withAfter(visitSpace(r.getAfter(), Space.Location.RIGHT_PADDED_AFTER, p));
    }

    public <T> Db2LeftPadded<T> visitLeftPadded(Db2LeftPadded<T> padded, P p) {
        Db2LeftPadded<T> l = padded.withBefore(
                visitSpace(padded.getBefore(), Space.Location.LEFT_PADDED_BEFORE, p));
        if (l.getElement() instanceof Db2) {
            //noinspection unchecked
            l = l.withElement((T) visitAndCast((Db2) l.getElement(), p));
        }
        return l.withMarkers(visitMarkers(l.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
