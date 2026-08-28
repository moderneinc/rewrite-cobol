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
package org.openrewrite.mainframe.db2.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.synchronizedMap;

/**
 * Everything between two DB2 tokens: white space, and the line and block comments among it.
 * Comments are not nodes of their own, so a prefix is whatever the source holds up to the token it
 * belongs to.
 */
@EqualsAndHashCode
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class Space {
    public static final Space EMPTY = new Space("");

    @Nullable
    private final String whitespace;

    /*
     * Most occurrences of spaces will have no comments or markers and will be repeated frequently throughout a source file.
     * e.g.: a single space between keywords, or the common indentation of every line in a block.
     * So use flyweights to avoid storing many instances of functionally identical spaces
     */
    private static final Map<String, Space> flyweights = synchronizedMap(new WeakHashMap<>());

    private Space(@Nullable String whitespace) {
        this.whitespace = whitespace == null || whitespace.isEmpty() ? null : whitespace;
    }

    @JsonCreator
    public static Space build(@Nullable String whitespace) {
        if (whitespace == null || whitespace.isEmpty()) {
            return Space.EMPTY;
        }
        if (whitespace.length() <= 100) {
            return flyweights.computeIfAbsent(whitespace, k -> new Space(whitespace));
        }
        return new Space(whitespace);
    }

    public String getIndent() {
        return getWhitespaceIndent(whitespace);
    }

    private String getWhitespaceIndent(@Nullable String whitespace) {
        if (whitespace == null) {
            return "";
        }
        int lastNewline = whitespace.lastIndexOf('\n');
        if (lastNewline >= 0) {
            return whitespace.substring(lastNewline + 1);
        }
        if (lastNewline == whitespace.length() - 1) {
            return "";
        }
        return whitespace;
    }

    public String getWhitespace() {
        return whitespace == null ? "" : whitespace;
    }

    public Space withWhitespace(String whitespace) {
        if (whitespace.isEmpty()) {
            return Space.EMPTY;
        }

        if (this.whitespace == null || whitespace.equals(this.whitespace)) {
            return this;
        }
        return build(whitespace);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public String toString() {
        return "Space(whitespace='" + getWhitespace().replace("\n", "\\n").replace("\r", "\\r") + "')";
    }

    public enum Location {
        ALTER_DATABASE_PREFIX,
        ALTER_FUNCTION_PREFIX,
        ALTER_INDEX_PREFIX,
        ALTER_MASK_PREFIX,
        ALTER_PERMISSION_PREFIX,
        ALTER_PROCEDURE_PREFIX,
        ALTER_SEQUENCE_PREFIX,
        ALTER_STOGROUP_PREFIX,
        ALTER_TABLESPACE_PREFIX,
        ALTER_TABLE_PREFIX,
        ALTER_TRIGGER_PREFIX,
        ALTER_TRUSTED_CONTEXT_PREFIX,
        ALTER_VIEW_PREFIX,
        BLOCK_PREFIX,
        COLUMN_DEFINITION_PREFIX,
        COMMENT_PREFIX,
        COMMIT_PREFIX,
        CONSTRAINT_PREFIX,
        CONTAINER_BEFORE,
        CREATE_ALIAS_PREFIX,
        CREATE_AUXILIARY_TABLE_PREFIX,
        CREATE_DATABASE_PREFIX,
        CREATE_FUNCTION_PREFIX,
        CREATE_INDEX_PREFIX,
        CREATE_MASK_PREFIX,
        CREATE_PERMISSION_PREFIX,
        CREATE_PROCEDURE_PREFIX,
        CREATE_ROLE_PREFIX,
        CREATE_SEQUENCE_PREFIX,
        CREATE_STOGROUP_PREFIX,
        CREATE_SYNONYM_PREFIX,
        CREATE_TABLESPACE_PREFIX,
        CREATE_TABLE_PREFIX,
        CREATE_TRIGGER_PREFIX,
        CREATE_TRUSTED_CONTEXT_PREFIX,
        CREATE_TYPE_PREFIX,
        CREATE_VARIABLE_PREFIX,
        CREATE_VIEW_PREFIX,
        DATA_TYPE_PREFIX,
        DDL_EOF,
        DDL_PREFIX,
        DECLARE_GLOBAL_TEMPORARY_TABLE_PREFIX,
        DROP_PREFIX,
        EMPTY_PREFIX,
        GRANT_PREFIX,
        INDEX_KEY_PREFIX,
        INSERT_PREFIX,
        KEYWORD_PREFIX,
        LABEL_PREFIX,
        LEFT_PADDED_BEFORE,
        LOCK_TABLE_PREFIX,
        NAME_PREFIX,
        OPTION_PREFIX,
        PARAMETER_PREFIX,
        QUERY_PREFIX,
        RELEASE_SAVEPOINT_PREFIX,
        RENAME_PREFIX,
        REVOKE_PREFIX,
        RIGHT_PADDED_AFTER,
        ROLLBACK_PREFIX,
        SAVEPOINT_PREFIX,
        SET_PREFIX,
        TABLE_AS_QUERY_PREFIX,
        TABLE_ELEMENTS_PREFIX,
        TABLE_LIKE_PREFIX,
        WORD_PREFIX
    }
}
