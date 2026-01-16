/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.cobol.tree;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.cobol.internal.CobolPreprocessorSourcePrinter;
import org.openrewrite.cobol.internal.CobolSourcePrinter;
import org.openrewrite.marker.Markers;

/**
 * There may be one or more comments and/or empty lines between any token via whitespace.
 * The Lines Marker preserves the column areas for each of the lines that come before a COBOL word.
 * <p>
 * Line comments are indicated with a `*` (depends on dialect) in the indicator areas.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@c")
public interface CobolLine {

    Markers getMarkers();
    <C extends CobolLine> C withMarkers(Markers markers);

    @Nullable
    SequenceArea getSequenceArea();
    <C extends CobolLine> C withSequenceArea(SequenceArea sequenceArea);

    IndicatorArea getIndicatorArea();
    <C extends CobolLine> C withIndicatorArea(IndicatorArea indicatorArea);

    @Nullable
    CommentArea getCommentArea();
    <C extends CobolLine> C withCommentArea(CommentArea commentArea);

    boolean isCopiedSource();
    <C extends CobolLine> C withCopiedSource(boolean isCopiedSource);

    <P> void printCobolLine(CobolPreprocessorSourcePrinter<P> sourcePrinter, Cursor cursor, PrintOutputCapture<P> p);
    <P> void printCobolLine(CobolSourcePrinter<P> sourcePrinter, Cursor cursor, PrintOutputCapture<P> p);
}
