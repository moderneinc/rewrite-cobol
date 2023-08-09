/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
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
    }

    public enum ResourceType {
        COBOL,
        COPYBOOK,
        LINKEDIT,
        BINDPLAN,
        BINDPACKAGE,
    }

    public enum ResourceAction {
        /**
         * Copybook call
         */
        COPY,
        /**
         * Program call
         */
        CALL,
        /**
         * Link-Edit card includes COBOL program
         */
        INCLUDE,
        /**
         * A Bind Card (plan) specifies the name of a Link Edit Card in its PLAN field
         */
        PLAN,
        /**
         * A Bind Card (package) specifies the name of a COBOL program in its MEMBER field
         */
        MEMBER
    }
}
