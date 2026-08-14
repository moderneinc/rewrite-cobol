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
    }

    public enum ResourceType {
        COBOL,
        COPYBOOK,
        LINKEDIT,
        BINDPLAN,
        BINDPACKAGE,
        @Deprecated // This may be removed after the next ingest.
        SQL_CURSOR,
        SQL_TABLE,
        CONTROL_M_SCHEDULE,
        JCL,
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
        IMS_MESSAGE_QUEUE
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
        RECEIVE
    }
}
