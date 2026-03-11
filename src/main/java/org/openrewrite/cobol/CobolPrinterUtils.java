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
package org.openrewrite.cobol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.internal.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CobolPrinterUtils {

    public static int getInsertIndex(String output) {
        int insertIndex = output.lastIndexOf("\n");
        return insertIndex == -1 ? 0 : insertIndex + 1;
    }

    public static int getCurrentIndex(String output) {
        int index = output.lastIndexOf("\n");
        return index == -1 ? index : output.substring(index + 1).length();
    }

    public static String generateWhitespace(int count) {
        if (count < 0) {
            throw new IllegalStateException("Negative index detected.");
        }
        return fillArea(' ', count);
    }

    public static String fillArea(Character character, int count) {
        if (count < 0) {
            throw new IllegalStateException("Negative index detected.");
        }
        return StringUtils.repeat(String.valueOf(character), count);
    }

    public static int getContentAreaLength(CobolDialect cobolDialect) {
        if (cobolDialect.getColumns().getOtherArea() - cobolDialect.getColumns().getContentArea() < 0) {
            throw new IllegalStateException("Negative index detected.");
        }
        return cobolDialect.getColumns().getOtherArea() - cobolDialect.getColumns().getContentArea();
    }
}
