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
package org.openrewrite.cobol.table;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class CobolRelationships extends DataTable<CobolRelationships.Row> {

    public CobolRelationships(Recipe recipe) {
        super(recipe, "COBOL relationships",
                "Relationships between different COBOL resources.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Dependent",
                description = "The resource that defines the relationship.")
        String dependent;

        @Column(displayName = "Dependent type",
                description = "The resource type of the resource that defines the relationship.")
        ResourceType dependentType;

        @Column(displayName = "Action",
                description = "The action that links the dependent and dependency.")
        ResourceAction action;

        @Column(displayName = "Dependency",
                description = "The resource that is exercised or linked to.")
        String dependency;

        @Column(displayName = "Dependency type",
                description = "The resource type of the resource that is exercised or linked to.")
        ResourceType dependencyType;

        @Column(displayName = "Dependency missing",
                description = "Indicates whether the dependency is a known resource.")
        boolean dependencyMissing;

        @Column(displayName = "Action metadata",
                description = "Additional data about the action.")
        String actionMetadata;

        @Column(displayName = "Dependent path",
                description = "The source file the dependent's statement was written in, or null when the " +
                              "dependent is a name that has no source here.")
        @Nullable
        String dependentPath;

        @Column(displayName = "Dependent line",
                description = "The one-based line the dependent's statement was written on, or null when it " +
                              "has no source here.")
        @Nullable
        Integer dependentLine;

        @Column(displayName = "Dependency path",
                description = "The source file the dependency was written in, or null when the dependency is " +
                              "a name that has no source here, such as a vendor copybook.")
        @Nullable
        String dependencyPath;

        @Column(displayName = "Dependency line",
                description = "The one-based line the dependency's statement was written on, or null when the " +
                              "dependency is a whole file rather than a statement within one.")
        @Nullable
        Integer dependencyLine;
    }

    public enum ResourceType {
        COBOL,
        COPYBOOK,
        LINKEDIT,
        /**
         * A DB2 plan, named by the bind card that declares it.
         */
        BINDPLAN,
        /**
         * A DB2 package, named by the bind card that declares it.
         */
        BINDPACKAGE,
        SQL_TABLE,
        CONTROL_M_SCHEDULE,
        JCL,
        /**
         * A data set, named as the installation knows it. This is the only name a batch job and the
         * program it runs both see: the JCL names the data set and not what is done with it, the
         * program names the DD and not what it is bound to.
         */
        DATA_SET,
        CICS_FILE,
        CICS_TS_QUEUE,
        CICS_TD_QUEUE,
        CICS_TRANSACTION,
        CICS_MAP,
        CICS_MAPSET,
        /**
         * An IMS database segment. Which database it belongs to comes from the PSB, not the COBOL.
         */
        IMS_SEGMENT,
        /**
         * A PCB, identified by the name the program gives it. Its position in the PSB is carried in
         * the action metadata, because that position is what names the database.
         */
        IMS_PCB,
        /**
         * The IMS message queue, reached through the I/O PCB.
         */
        IMS_MESSAGE_QUEUE,
        /**
         * A link-edited load module, which is what a JCL step actually runs.
         */
        LOAD_MODULE,
        /**
         * A control section: what one compile or assembly produced and the binder placed in a load
         * module. A program's own CSECT carries the program's name, and the rest are the stubs a
         * language interface supplies and the runtime the autocall pulled in.
         */
        CSECT,
        /**
         * The database request module a DB2 precompile leaves behind, which a package is bound from.
         */
        DBRM,
        /**
         * A JCL procedure, cataloged or in-stream.
         */
        PROC,
        /**
         * A member a job pulls in with a JCL {@code INCLUDE} statement.
         */
        INCLUDE_MEMBER,
        /**
         * A control card member a step reads: DFSORT, IDCAMS, DSN, or plain SYSIN.
         */
        CONTROL_CARD,
        /**
         * A Control-M job group, which the schedules it contains name.
         */
        CONTROL_M_GROUP,
        /**
         * A Control-M calendar, which says on what days a schedule runs.
         */
        CONTROL_M_CALENDAR,
        /**
         * An IMS database, named by its DBD.
         */
        IMS_DATABASE,
        /**
         * A program specification block, which says what PCBs a program is given.
         */
        IMS_PSB,
        /**
         * An IMS transaction code, which names the program that processes it.
         */
        IMS_TRANSACTION,
        /**
         * A message format service map: one {@code MSG}, being a MOD a program sends or a MID a
         * terminal's reply arrives on. This is the name a program passes, so it is the IMS answer to
         * a BMS map.
         */
        MFS_MAP,
        /**
         * A message format service device format: the {@code FMT} a message names on its
         * {@code SOR=}, holding the fields at their places on the screen. It is the IMS answer to a
         * BMS map set, and no program names one.
         */
        MFS_FORMAT,
        /**
         * An assembler program or macro.
         */
        ASSEMBLER,
        /**
         * A SAS program, named by its member. A SAS program has no name of its own the way a COBOL
         * program has a {@code PROGRAM-ID}, so a program a job writes in-stream has none at all and
         * is named by the job.
         */
        SAS,
        /**
         * A Java class that reaches a COBOL program or a DB2 table: through a CICS gateway, a queue,
         * a z/OS Connect API, or JDBC.
         */
        JAVA
    }

    public enum ResourceAction {
        /**
         * A COBOL program accesses a DB2 table.
         */
        ACCESS,
        /**
         * Copybook call
         */
        COPY,
        /**
         * Program call
         */
        CALL,
        /**
         * Link-Edit card includes COBOL program.
         * <p>
         * A COBOL program includes a copybook through EXEC SQL INCLUDE.
         */
        INCLUDE,
        /**
         * A Bind Card (plan) specifies the name of a Link Edit Card in its PLAN field
         */
        PLAN,
        /**
         * A Bind Card (package) specifies the name of a COBOL program in its MEMBER field
         */
        MEMBER,
        /**
         * A Control-M schedule triggers a JCL job or a Control-M schedules another Control-M job.
         */
        TRIGGERS,
        /**
         * A JCL step runs a program. Which load module that program is comes from the STEPLIB, so
         * this is a name and not yet a resolution.
         */
        EXEC,
        /**
         * {@code EXEC CICS LINK} calls another program and gets control back.
         */
        LINK,
        /**
         * {@code EXEC CICS XCTL} transfers to another program and does not get control back.
         */
        XCTL,
        /**
         * {@code EXEC CICS START} schedules a transaction, or {@code RETURN TRANSID} names the one
         * that continues the conversation.
         */
        START,
        /**
         * A BMS map is sent to or received from a terminal.
         */
        SEND,
        RECEIVE,
        /**
         * A scheduler runs a job, in contrast to {@link #TRIGGERS}, which is one job making another
         * eligible. An IMS transaction schedules the PSB that answers it the same way.
         */
        SCHEDULES,
        /**
         * A member creates a resource: an IDCAMS DEFINE of a VSAM file, a DBD of a database, a DDL of a table.
         */
        DEFINES,
        /**
         * A member is held in a second library, so that the two environments can be told apart.
         */
        COPIED_TO,
        /**
         * A bind card binds a DBRM into a package, or packages into a plan.
         */
        BINDS,
        /**
         * A compile turns a program into an object deck or a load module.
         */
        COMPILES_INTO,
        /**
         * A DB2 precompile turns a program into a DBRM.
         */
        PRECOMPILES_INTO,
        /**
         * A program is the entry point control arrives at when a load module is given control.
         */
        ENTRY,
        /**
         * A caller outside COBOL reaches a program by name, such as a Java call site or a dynamic call.
         */
        INVOKES,
        /**
         * A resource holds another: a load module its programs, a PSB its PCBs, a DBD its segments.
         */
        CONTAINS,
        /**
         * A name is mentioned somewhere that is not a call, an access, or a definition.
         */
        REFERENCES
    }
}
