/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.jspecify.annotations.Nullable;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.tree.*;
import org.openrewrite.internal.ListUtils;

public class CobolVisitor<P> extends TreeVisitor<Cobol, P> {

    @Nullable
    protected CobolPreprocessorVisitor<P> cobolPreprocessorVisitor;

    protected CobolPreprocessorVisitor<P> getCobolPreprocessorVisitor() {
        if (cobolPreprocessorVisitor == null) {
            cobolPreprocessorVisitor = new CobolPreprocessorVisitor<>(this);
        }
        return cobolPreprocessorVisitor;
    }

    /* Cobol visits */
    public Cobol visitAbbreviation(Cobol.Abbreviation abbreviation, P p) {
        Cobol.Abbreviation a = abbreviation;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ABBREVIATION_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withNot((Cobol.Word) visit(a.getNot(), p));
        a = a.withLeftParen((Cobol.Word) visit(a.getLeftParen(), p));
        a = a.withRelationalOperator((Cobol.RelationalOperator) visit(a.getRelationalOperator(), p));
        a = a.withArithmeticExpression(visit(a.getArithmeticExpression(), p));
        a = a.withAbbreviation(visit(a.getAbbreviation(), p));
        return a.withRightParen((Cobol.Word) visit(a.getRightParen(), p));
    }

    public Cobol visitAccept(Cobol.Accept acceptStatement, P p) {
        Cobol.Accept a = acceptStatement;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ACCEPT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withAccept((Cobol.Word) visit(a.getAccept(), p));
        a = a.withIdentifier((Identifier) visit(a.getIdentifier(), p));
        a = a.withOperation(visit(a.getOperation(), p));
        a = a.withOnExceptionClause((Cobol.StatementPhrase) visit(a.getOnExceptionClause(), p));
        a = a.withNotOnExceptionClause((Cobol.StatementPhrase) visit(a.getNotOnExceptionClause(), p));
        return a.withEndAccept((Cobol.Word) visit(a.getEndAccept(), p));
    }

    public Cobol visitAcceptFromDateStatement(Cobol.AcceptFromDateStatement acceptFromDateStatement, P p) {
        Cobol.AcceptFromDateStatement a = acceptFromDateStatement;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ACCEPT_FROM_DATE_STATEMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        return a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitAcceptFromEscapeKeyStatement(Cobol.AcceptFromEscapeKeyStatement acceptFromEscapeKeyStatement, P p) {
        Cobol.AcceptFromEscapeKeyStatement a = acceptFromEscapeKeyStatement;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ACCEPT_FROM_ESCAPE_KEY_STATEMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        return a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitAcceptFromMnemonicStatement(Cobol.AcceptFromMnemonicStatement acceptFromMnemonicStatement, P p) {
        Cobol.AcceptFromMnemonicStatement a = acceptFromMnemonicStatement;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ACCEPT_FROM_MNEMONIC_STATEMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withFrom((Cobol.Word) visit(a.getFrom(), p));
        return a.withMnemonicName((Identifier) visit(a.getMnemonicName(), p));
    }

    public Cobol visitAcceptMessageCountStatement(Cobol.AcceptMessageCountStatement acceptMessageCountStatement, P p) {
        Cobol.AcceptMessageCountStatement a = acceptMessageCountStatement;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ACCEPT_MESSAGE_COUNT_STATEMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        return a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitAccessModeClause(Cobol.AccessModeClause accessModeClause, P p) {
        Cobol.AccessModeClause a = accessModeClause;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ACCESS_MODE_CLAUSE_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
        return a.withType((Cobol.Word) visit(a.getType(), p));
    }

    public Cobol visitAdd(Cobol.Add add, P p) {
        Cobol.Add a = add;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ADD_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withAdd((Cobol.Word) visit(a.getAdd(), p));
        a = a.withOperation(visit(a.getOperation(), p));
        a = a.withOnSizeError((Cobol.StatementPhrase) visit(a.getOnSizeError(), p));
        a = a.withNotOnSizeError((Cobol.StatementPhrase) visit(a.getNotOnSizeError(), p));
        return a.withEndAdd((Cobol.Word) visit(a.getEndAdd(), p));
    }

    public Cobol visitAddCorresponding(Cobol.AddCorresponding addCorresponding, P p) {
        Cobol.AddCorresponding a = addCorresponding;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ADD_CORRESPONDING_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withCorresponding((Cobol.Word) visit(a.getCorresponding(), p));
        a = a.withIdentifier((Identifier) visit(a.getIdentifier(), p));
        a = a.withTo((Cobol.Word) visit(a.getTo(), p));
        return a.withRoundable((Cobol.Roundable) visit(a.getRoundable(), p));
    }

    public Cobol visitAddTo(Cobol.AddTo addTo, P p) {
        Cobol.AddTo a = addTo;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ADD_TO_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withFrom(ListUtils.map(a.getFrom(), t -> (Name) visit(t, p)));
        a = a.withTo((Cobol.Word) visit(a.getTo(), p));
        return a.withRoundables(ListUtils.map(a.getRoundables(), t -> visit(t, p)));
    }

    public Cobol visitAddToGiving(Cobol.AddToGiving addToGiving, P p) {
        Cobol.AddToGiving a = addToGiving;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ADD_TO_GIVING_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withFrom(ListUtils.map(a.getFrom(), t -> (Name) visit(t, p)));
        a = a.withTo((Cobol.Word) visit(a.getTo(), p));
        a = a.withNames(ListUtils.map(a.getNames(), t -> (Name) visit(t, p)));
        a = a.withGiving((Cobol.Word) visit(a.getGiving(), p));
        return a.withRoundables(ListUtils.map(a.getRoundables(), t -> (Cobol.Roundable) visit(t, p)));
    }

    public Cobol visitAlphabetAlso(Cobol.AlphabetAlso alphabetAlso, P p) {
        Cobol.AlphabetAlso a = alphabetAlso;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALPHABET_ALSO_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withWord((Cobol.Word) visit(a.getWord(), p));
        return a.withLiterals(ListUtils.map(a.getLiterals(), t -> (Literal) visit(t, p)));
    }

    public Cobol visitAlphabetClause(Cobol.AlphabetClause alphabetClause, P p) {
        Cobol.AlphabetClause a = alphabetClause;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALPHABET_CLAUSE_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withAlphabet((Cobol.Word) visit(a.getAlphabet(), p));
        a = a.withName((Name) visit(a.getName(), p));
        return a.withWords(ListUtils.map(a.getWords(), it -> visit(it, p)));
    }

    public Cobol visitAlphabetLiteral(Cobol.AlphabetLiteral alphabetLiteral, P p) {
        Cobol.AlphabetLiteral a = alphabetLiteral;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALPHABET_LITERAL_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withLiteral((Literal) visit(a.getLiteral(), p));
        a = a.withAlphabetThrough((Cobol.AlphabetThrough) visit(a.getAlphabetThrough(), p));
        return a.withAlphabetAlso(ListUtils.map(a.getAlphabetAlso(), t -> (Cobol.AlphabetAlso) visit(t, p)));
    }

    public Cobol visitAlphabetThrough(Cobol.AlphabetThrough alphabetThrough, P p) {
        Cobol.AlphabetThrough a = alphabetThrough;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALPHABET_THROUGH_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withWord((Cobol.Word) visit(a.getWord(), p));
        return a.withLiteral((Literal) visit(a.getLiteral(), p));
    }

    public Cobol visitAlterProceedTo(Cobol.AlterProceedTo alterProceedTo, P p) {
        Cobol.AlterProceedTo a = alterProceedTo;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALTER_PROCEED_TO_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withFrom((Cobol.ProcedureName) visit(a.getFrom(), p));
        a = a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
        return a.withTo((Cobol.ProcedureName) visit(a.getTo(), p));
    }

    public Cobol visitAlterStatement(Cobol.AlterStatement alterStatement, P p) {
        Cobol.AlterStatement a = alterStatement;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALTER_STATEMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withWord((Cobol.Word) visit(a.getWord(), p));
        return a.withAlterProceedTo(ListUtils.map(a.getAlterProceedTo(), t -> (Cobol.AlterProceedTo) visit(t, p)));
    }

    public Cobol visitAlteredGoTo(Cobol.AlteredGoTo alteredGoTo, P p) {
        Cobol.AlteredGoTo a = alteredGoTo;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALTERED_GO_TO_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
        return a.withDot((Cobol.Word) visit(a.getDot(), p));
    }

    public Cobol visitAlternateRecordKeyClause(Cobol.AlternateRecordKeyClause alternateRecordKeyClause, P p) {
        Cobol.AlternateRecordKeyClause a = alternateRecordKeyClause;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALTERNATE_RECORD_KEY_CLAUSE_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withAlternateWords(ListUtils.map(a.getAlternateWords(), it -> (Cobol.Word) visit(it, p)));
        a = a.withQualifiedDataName((Cobol.QualifiedDataName) visit(a.getQualifiedDataName(), p));
        a = a.withPasswordClause((Cobol.PasswordClause) visit(a.getPasswordClause(), p));
        return a.withDuplicates(ListUtils.map(a.getDuplicates(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitAndOrCondition(Cobol.AndOrCondition andOrCondition, P p) {
        Cobol.AndOrCondition a = andOrCondition;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.AND_OR_CONDITION_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withLogicalOperator((Cobol.Word) visit(a.getLogicalOperator(), p));
        a = a.withCombinableCondition((Cobol.CombinableCondition) visit(a.getCombinableCondition(), p));
        return a.withAbbreviations(ListUtils.map(a.getAbbreviations(), t -> visit(t, p)));
    }

    public Cobol visitArgument(Cobol.Argument argument, P p) {
        Cobol.Argument a = argument;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ARGUMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withFirst(visit(a.getFirst(), p));
        return a.withIntegerLiteral((Cobol.Word) visit(a.getIntegerLiteral(), p));
    }

    public Cobol visitArithmeticExpression(Cobol.ArithmeticExpression arithmeticExpression, P p) {
        Cobol.ArithmeticExpression a = arithmeticExpression;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ARITHMETIC_EXPRESSION_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withMultDivs((Cobol.MultDivs) visit(a.getMultDivs(), p));
        return a.withPlusMinuses(ListUtils.map(a.getPlusMinuses(), t -> (Cobol.PlusMinus) visit(t, p)));
    }

    public Cobol visitAssignClause(Cobol.AssignClause assignClause, P p) {
        Cobol.AssignClause a = assignClause;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ASSIGN_CLAUSE_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withWords(ListUtils.map(a.getWords(), it -> (Cobol.Word) visit(it, p)));
        return a.withName((Name) visit(a.getName(), p));
    }

    public Cobol visitBlockContainsClause(Cobol.BlockContainsClause blockContainsClause, P p) {
        Cobol.BlockContainsClause b = blockContainsClause;
        b = b.withPrefix(visitSpace(b.getPrefix(), Space.Location.BLOCK_CONTAINS_PREFIX, p));
        b = b.withMarkers(visitMarkers(b.getMarkers(), p));
        b = b.withFirstWords(ListUtils.map(b.getFirstWords(), it -> (Cobol.Word) visit(it, p)));
        b = b.withIntegerLiteral((Cobol.Word) visit(b.getIntegerLiteral(), p));
        b = b.withBlockContainsTo((Cobol.BlockContainsTo) visit(b.getBlockContainsTo(), p));
        return b.withLastWord((Cobol.Word) visit(b.getLastWord(), p));
    }

    public Cobol visitBlockContainsTo(Cobol.BlockContainsTo blockContainsTo, P p) {
        Cobol.BlockContainsTo b = blockContainsTo;
        b = b.withPrefix(visitSpace(b.getPrefix(), Space.Location.BLOCK_CONTAINS_TO_PREFIX, p));
        b = b.withMarkers(visitMarkers(b.getMarkers(), p));
        b = b.withTo((Cobol.Word) visit(b.getTo(), p));
        return b.withIntegerLiteral((Cobol.Word) visit(b.getIntegerLiteral(), p));
    }

    public Cobol visitCall(Cobol.Call call, P p) {
        Cobol.Call c = call;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CALL_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCall((Cobol.Word) visit(c.getCall(), p));
        c = c.withIdentifier((Name) visit(c.getIdentifier(), p));
        c = c.withCallUsingPhrase((Cobol.CallPhrase) visit(c.getCallUsingPhrase(), p));
        c = c.withCallGivingPhrase((Cobol.CallGivingPhrase) visit(c.getCallGivingPhrase(), p));
        c = c.withOnOverflowPhrase((Cobol.StatementPhrase) visit(c.getOnOverflowPhrase(), p));
        c = c.withOnExceptionClause((Cobol.StatementPhrase) visit(c.getOnExceptionClause(), p));
        c = c.withNotOnExceptionClause((Cobol.StatementPhrase) visit(c.getNotOnExceptionClause(), p));
        return c.withEndCall((Cobol.Word) visit(c.getEndCall(), p));
    }

    public Cobol visitCallBy(Cobol.CallBy callBy, P p) {
        Cobol.CallBy c = callBy;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CALL_BY_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withIdentifier((Name) visit(c.getIdentifier(), p));
    }

    public Cobol visitCallGivingPhrase(Cobol.CallGivingPhrase callGivingPhrase, P p) {
        Cobol.CallGivingPhrase c = callGivingPhrase;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CALL_GIVING_PHRASE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord((Cobol.Word) visit(c.getWord(), p));
        return c.withIdentifier((Name) visit(c.getIdentifier(), p));
    }

    public Cobol visitCallPhrase(Cobol.CallPhrase callPhrase, P p) {
        Cobol.CallPhrase c = callPhrase;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CALL_PHRASE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withParameters(ListUtils.map(c.getParameters(), t -> visit(t, p)));
    }

    public Cobol visitCancel(Cobol.Cancel cancel, P p) {
        Cobol.Cancel c = cancel;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CANCEL_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCancel((Cobol.Word) visit(c.getCancel(), p));
        return c.withCancelCalls(ListUtils.map(c.getCancelCalls(), t -> (Cobol.CancelCall) visit(t, p)));
    }

    public Cobol visitCancelCall(Cobol.CancelCall cancelCall, P p) {
        Cobol.CancelCall c = cancelCall;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CANCEL_CALL_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withLibraryName((Name) visit(c.getLibraryName(), p));
        c = c.withBy(ListUtils.map(c.getBy(), it -> (Cobol.Word) visit(it, p)));
        c = c.withIdentifier((Identifier) visit(c.getIdentifier(), p));
        return c.withLiteral((Literal) visit(c.getLiteral(), p));
    }

    public Cobol visitChannelClause(Cobol.ChannelClause channelClause, P p) {
        Cobol.ChannelClause c = channelClause;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CHANNEL_CLAUSE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord((Cobol.Word) visit(c.getWord(), p));
        c = c.withLiteral((Literal) visit(c.getLiteral(), p));
        c = c.withIs((Cobol.Word) visit(c.getIs(), p));
        return c.withMnemonicName((Identifier) visit(c.getMnemonicName(), p));
    }

    public Cobol visitClassClause(Cobol.ClassClause classClause, P p) {
        Cobol.ClassClause c = classClause;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLASS_CLAUSE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withClazz((Cobol.Word) visit(c.getClazz(), p));
        c = c.withClassName((Cobol.Word) visit(c.getClassName(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withThroughs(ListUtils.map(c.getThroughs(), t -> (Cobol.ClassClauseThrough) visit(t, p)));
    }

    public Cobol visitClassClauseThrough(Cobol.ClassClauseThrough classClauseThrough, P p) {
        Cobol.ClassClauseThrough c = classClauseThrough;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLASS_CLAUSE_THROUGH_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withFrom((Name) visit(c.getFrom(), p));
        c = c.withThrough((Cobol.Word) visit(c.getThrough(), p));
        return c.withTo((Name) visit(c.getTo(), p));
    }

    public Cobol visitClassCondition(Cobol.ClassCondition classCondition, P p) {
        Cobol.ClassCondition c = classCondition;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLASS_CONDITION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withName((Name) visit(c.getName(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withType(visit(c.getType(), p));
    }

    public Cobol visitClose(Cobol.Close close, P p) {
        Cobol.Close c = close;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withClose((Cobol.Word) visit(c.getClose(), p));
        return c.withCloseFiles(ListUtils.map(c.getCloseFiles(), t -> visit(t, p)));
    }

    public Cobol visitCloseFile(Cobol.CloseFile closeFile, P p) {
        Cobol.CloseFile c = closeFile;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_FILE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withFileName((Name) visit(c.getFileName(), p));
        return c.withCloseStatement(visit(c.getCloseStatement(), p));
    }

    public Cobol visitClosePortFileIOStatement(Cobol.ClosePortFileIOStatement closePortFileIOStatement, P p) {
        Cobol.ClosePortFileIOStatement c = closePortFileIOStatement;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_PORT_FILE_IO_STATEMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withClosePortFileIOUsing(ListUtils.map(c.getClosePortFileIOUsing(), t -> visit(t, p)));
    }

    public Cobol visitClosePortFileIOUsingAssociatedData(Cobol.ClosePortFileIOUsingAssociatedData closePortFileIOUsingAssociatedData, P p) {
        Cobol.ClosePortFileIOUsingAssociatedData c = closePortFileIOUsingAssociatedData;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_PORT_FILE_IO_USING_ASSOCIATED_DATA_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withAssociatedData((Cobol.Word) visit(c.getAssociatedData(), p));
        return c.withIdentifier((Identifier) visit(c.getIdentifier(), p));
    }

    public Cobol visitClosePortFileIOUsingAssociatedDataLength(Cobol.ClosePortFileIOUsingAssociatedDataLength closePortFileIOUsingAssociatedDataLength, P p) {
        Cobol.ClosePortFileIOUsingAssociatedDataLength c = closePortFileIOUsingAssociatedDataLength;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_PORT_FILE_IO_USING_ASSOCIATED_DATA_LENGTH_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withIdentifier((Identifier) visit(c.getIdentifier(), p));
    }

    public Cobol visitClosePortFileIOUsingCloseDisposition(Cobol.ClosePortFileIOUsingCloseDisposition closePortFileIOUsingCloseDisposition, P p) {
        Cobol.ClosePortFileIOUsingCloseDisposition c = closePortFileIOUsingCloseDisposition;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_PORT_FILE_IO_USING_CLOSE_DISPOSITION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitCloseReelUnitStatement(Cobol.CloseReelUnitStatement closeReelUnitStatement, P p) {
        Cobol.CloseReelUnitStatement c = closeReelUnitStatement;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_REEL_UNIT_STATEMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitCloseRelativeStatement(Cobol.CloseRelativeStatement closeRelativeStatement, P p) {
        Cobol.CloseRelativeStatement c = closeRelativeStatement;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_RELATIVE_STATEMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitCodeSetClause(Cobol.CodeSetClause codeSetClause, P p) {
        Cobol.CodeSetClause c = codeSetClause;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CLOSE_SET_CLAUSE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withAlphabetName((Cobol.Word) visit(c.getAlphabetName(), p));
    }

    public Cobol visitCollatingSequenceAlphabet(Cobol.CollatingSequenceAlphabet collatingSequenceAlphabet, P p) {
        Cobol.CollatingSequenceAlphabet c = collatingSequenceAlphabet;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COLLATING_SEQUENCE_ALPHABET_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withAlphabetName((Identifier) visit(c.getAlphabetName(), p));
    }

    public Cobol visitCollatingSequenceClause(Cobol.CollatingSequenceClause collatingSequenceClause, P p) {
        Cobol.CollatingSequenceClause c = collatingSequenceClause;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COLLATING_SEQUENCE_CLAUSE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withIs((Cobol.Word) visit(c.getIs(), p));
        c = c.withAlphabetName(ListUtils.map(c.getAlphabetName(), it -> (Cobol.Word) visit(it, p)));
        c = c.withAlphanumeric((Cobol.CollatingSequenceAlphabet) visit(c.getAlphanumeric(), p));
        return c.withNational((Cobol.CollatingSequenceAlphabet) visit(c.getNational(), p));
    }

    public Cobol visitCombinableCondition(Cobol.CombinableCondition combinableCondition, P p) {
        Cobol.CombinableCondition c = combinableCondition;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMBINABLE_CONDITION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withNot((Cobol.Word) visit(c.getNot(), p));
        return c.withSimpleCondition(visit(c.getSimpleCondition(), p));
    }

    public Cobol visitCommentEntry(Cobol.CommentEntry commentEntry, P p) {
        Cobol.CommentEntry c = commentEntry;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_ENTRY_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withComments(ListUtils.map(c.getComments(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitCommitmentControlClause(Cobol.CommitmentControlClause commitmentControlClause, P p) {
        Cobol.CommitmentControlClause c = commitmentControlClause;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMITMENT_CONTROL_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        return c.withFileName((Cobol.Word) visit(c.getFileName(), p));
    }

    public Cobol visitCommunicationDescriptionEntryFormat1(Cobol.CommunicationDescriptionEntryFormat1 communicationDescriptionEntryFormat1, P p) {
        Cobol.CommunicationDescriptionEntryFormat1 c = communicationDescriptionEntryFormat1;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMUNICATION_DESCRIPTION_ENTRY_FORMAT_1_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCd((Cobol.Word) visit(c.getCd(), p));
        c = c.withName((Cobol.Word) visit(c.getName(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withInputs(ListUtils.map(c.getInputs(), it -> visit(it, p)));
        return c.withDot((Cobol.Word) visit(c.getDot(), p));
    }

    public Cobol visitCommunicationDescriptionEntryFormat2(Cobol.CommunicationDescriptionEntryFormat2 communicationDescriptionEntryFormat2, P p) {
        Cobol.CommunicationDescriptionEntryFormat2 c = communicationDescriptionEntryFormat2;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMUNICATION_DESCRIPTION_ENTRY_FORMAT_2_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCd((Cobol.Word) visit(c.getCd(), p));
        c = c.withName((Cobol.Word) visit(c.getName(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withOutputs(ListUtils.map(c.getOutputs(), it -> visit(it, p)));
        return c.withDot((Cobol.Word) visit(c.getDot(), p));
    }

    public Cobol visitCommunicationDescriptionEntryFormat3(Cobol.CommunicationDescriptionEntryFormat3 communicationDescriptionEntryFormat3, P p) {
        Cobol.CommunicationDescriptionEntryFormat3 c = communicationDescriptionEntryFormat3;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMUNICATION_DESCRIPTION_ENTRY_FORMAT_3_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCd((Cobol.Word) visit(c.getCd(), p));
        c = c.withName((Cobol.Word) visit(c.getName(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withInitialIOs(ListUtils.map(c.getInitialIOs(), it -> visit(it, p)));
        return c.withDot((Cobol.Word) visit(c.getDot(), p));
    }

    public Cobol visitCommunicationSection(Cobol.CommunicationSection communicationSection, P p) {
        Cobol.CommunicationSection c = communicationSection;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMUNICATION_SECTION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withDot((Cobol.Word) visit(c.getDot(), p));
        return c.withEntries(ListUtils.map(c.getEntries(), it -> visit(it, p)));
    }

    public Cobol visitCompilationUnit(Cobol.CompilationUnit compilationUnit, P p) {
        Cobol.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withProgramUnits(ListUtils.map(c.getProgramUnits(), it -> (Cobol.ProgramUnit) visit(it, p)));
        return c.withEof((Cobol.Word) visit(c.getEof(), p));
    }

    public Cobol visitCompute(Cobol.Compute compute, P p) {
        Cobol.Compute c = compute;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPUTE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCompute((Cobol.Word) visit(c.getCompute(), p));
        c = c.withRoundables(ListUtils.map(c.getRoundables(), t -> (Cobol.Roundable) visit(t, p)));
        c = c.withEqualWord((Cobol.Word) visit(c.getEqualWord(), p));
        c = c.withArithmeticExpression((Cobol.ArithmeticExpression) visit(c.getArithmeticExpression(), p));
        c = c.withOnSizeErrorPhrase((Cobol.StatementPhrase) visit(c.getOnSizeErrorPhrase(), p));
        c = c.withNotOnSizeErrorPhrase((Cobol.StatementPhrase) visit(c.getNotOnSizeErrorPhrase(), p));
        return c.withEndCompute((Cobol.Word) visit(c.getEndCompute(), p));
    }

    public Cobol visitCondition(Cobol.Condition condition, P p) {
        Cobol.Condition c = condition;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONDITION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCombinableCondition((Cobol.CombinableCondition) visit(c.getCombinableCondition(), p));
        return c.withAndOrConditions(ListUtils.map(c.getAndOrConditions(), t -> (Cobol.AndOrCondition) visit(t, p)));
    }

    public Cobol visitConditionNameReference(Cobol.ConditionNameReference conditionNameReference, P p) {
        Cobol.ConditionNameReference c = conditionNameReference;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONDITION_NAME_REFERENCE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withName((Name) visit(c.getName(), p));
        c = c.withInDatas(ListUtils.map(c.getInDatas(), t -> (Cobol.InData) visit(t, p)));
        c = c.withInFile((Cobol.InFile) visit(c.getInFile(), p));
        c = c.withReferences(ListUtils.map(c.getReferences(), t -> (Cobol.Parenthesized) visit(t, p)));
        return c.withInMnemonics(ListUtils.map(c.getInMnemonics(), t -> (Cobol.InMnemonic) visit(t, p)));
    }

    public Cobol visitConditionNameSubscriptReference(Cobol.ConditionNameSubscriptReference conditionNameSubscriptReference, P p) {
        Cobol.ConditionNameSubscriptReference c = conditionNameSubscriptReference;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONDITION_NAME_SUBSCRIPT_REFERENCE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withLeftParen((Cobol.Word) visit(c.getLeftParen(), p));
        c = c.withSubscripts(ListUtils.map(c.getSubscripts(), it -> visit(it, p)));
        return c.withRightParen((Cobol.Word) visit(c.getRightParen(), p));
    }

    public Cobol visitConfigurationSection(Cobol.ConfigurationSection configurationSection, P p) {
        Cobol.ConfigurationSection c = configurationSection;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONFIGURATION_SECTION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withDot((Cobol.Word) visit(c.getDot(), p));
        return c.withParagraphs(ListUtils.map(c.getParagraphs(), it -> visit(it, p)));
    }

    public Cobol visitContinue(Cobol.Continue continuez, P p) {
        Cobol.Continue c = continuez;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONTINUE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWord((Cobol.Word) visit(c.getWord(), p));
    }

    public Cobol visitCurrencyClause(Cobol.CurrencyClause currencyClause, P p) {
        Cobol.CurrencyClause c = currencyClause;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CURRENCY_CLAUSE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWords(ListUtils.map(c.getWords(), it -> (Cobol.Word) visit(it, p)));
        c = c.withLiteral((Literal) visit(c.getLiteral(), p));
        c = c.withPictureSymbols(ListUtils.map(c.getPictureSymbols(), it -> (Cobol.Word) visit(it, p)));
        return c.withPictureSymbolLiteral((Literal) visit(c.getPictureSymbolLiteral(), p));
    }

    public Cobol visitDataAlignedClause(Cobol.DataAlignedClause dataAlignedClause, P p) {
        Cobol.DataAlignedClause d = dataAlignedClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_ALIGNED_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withAligned((Cobol.Word) visit(d.getAligned(), p));
    }

    public Cobol visitDataBaseSection(Cobol.DataBaseSection dataBaseSection, P p) {
        Cobol.DataBaseSection d = dataBaseSection;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_BASE_SECTION_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        d = d.withDot((Cobol.Word) visit(d.getDot(), p));
        return d.withEntries(ListUtils.map(d.getEntries(), it -> (Cobol.DataBaseSectionEntry) visit(it, p)));
    }

    public Cobol visitDataBaseSectionEntry(Cobol.DataBaseSectionEntry dataBaseSectionEntry, P p) {
        Cobol.DataBaseSectionEntry d = dataBaseSectionEntry;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_BASE_SECTION_ENTRY_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withDb((Cobol.Word) visit(d.getDb(), p));
        d = d.withFrom((Literal) visit(d.getFrom(), p));
        d = d.withInvoke((Cobol.Word) visit(d.getInvoke(), p));
        return d.withTo((Literal) visit(d.getTo(), p));
    }

    public Cobol visitDataBlankWhenZeroClause(Cobol.DataBlankWhenZeroClause dataBlankWhenZeroClause, P p) {
        Cobol.DataBlankWhenZeroClause d = dataBlankWhenZeroClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_BLANK_WHEN_ZERO_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataCommonOwnLocalClause(Cobol.DataCommonOwnLocalClause dataCommonOwnLocalClause, P p) {
        Cobol.DataCommonOwnLocalClause d = dataCommonOwnLocalClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_COMMON_OWN_LOCAL_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWord((Cobol.Word) visit(d.getWord(), p));
    }

    public Cobol visitDataDescriptionEntry(Cobol.DataDescriptionEntry dataDescriptionEntry, P p) {
        Cobol.DataDescriptionEntry d = dataDescriptionEntry;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_DESCRIPTION_ENTRY_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        d = d.withName((Cobol.Word) visit(d.getName(), p));
        d = d.withClauses(ListUtils.map(d.getClauses(), it -> visit(it, p)));
        return d.withDot((Cobol.Word) visit(d.getDot(), p));
    }

    public Cobol visitDataDivision(Cobol.DataDivision dataDivision, P p) {
        Cobol.DataDivision d = dataDivision;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_DIVISION_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        d = d.withDot((Cobol.Word) visit(d.getDot(), p));
        return d.withSections(ListUtils.map(d.getSections(), it -> (DataDivisionSection) visit(it, p)));
    }

    public Cobol visitDataExternalClause(Cobol.DataExternalClause dataExternalClause, P p) {
        Cobol.DataExternalClause d = dataExternalClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_EXTERNAL_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withRedefines(ListUtils.map(d.getRedefines(), it -> (Cobol.Word) visit(it, p)));
        return d.withLiteral((Cobol.Word) visit(d.getLiteral(), p));
    }

    public Cobol visitDataGlobalClause(Cobol.DataGlobalClause dataGlobalClause, P p) {
        Cobol.DataGlobalClause d = dataGlobalClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_GLOBAL_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataIntegerStringClause(Cobol.DataIntegerStringClause dataIntegerStringClause, P p) {
        Cobol.DataIntegerStringClause d = dataIntegerStringClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_INTEGER_STRING_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWord((Cobol.Word) visit(d.getWord(), p));
    }

    public Cobol visitDataJustifiedClause(Cobol.DataJustifiedClause dataJustifiedClause, P p) {
        Cobol.DataJustifiedClause d = dataJustifiedClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_JUSTIFIED_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataOccursClause(Cobol.DataOccursClause dataOccursClause, P p) {
        Cobol.DataOccursClause d = dataOccursClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_OCCURS_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withOccurs((Cobol.Word) visit(d.getOccurs(), p));
        d = d.withName((Name) visit(d.getName(), p));
        d = d.withDataOccursTo((Cobol.DataOccursTo) visit(d.getDataOccursTo(), p));
        d = d.withTimes((Cobol.Word) visit(d.getTimes(), p));
        d = d.withDataOccursDepending((Cobol.DataOccursDepending) visit(d.getDataOccursDepending(), p));
        return d.withSortIndexed(ListUtils.map(d.getSortIndexed(), t -> visit(t, p)));
    }

    public Cobol visitDataOccursDepending(Cobol.DataOccursDepending dataOccursDepending, P p) {
        Cobol.DataOccursDepending d = dataOccursDepending;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_OCCURS_DEPENDING_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withQualifiedDataName((Cobol.QualifiedDataName) visit(d.getQualifiedDataName(), p));
    }

    public Cobol visitDataOccursIndexed(Cobol.DataOccursIndexed dataOccursIndexed, P p) {
        Cobol.DataOccursIndexed d = dataOccursIndexed;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_OCCURS_INDEXED_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withIndexNames(ListUtils.map(d.getIndexNames(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitDataOccursSort(Cobol.DataOccursSort dataOccursSort, P p) {
        Cobol.DataOccursSort d = dataOccursSort;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_OCCURS_SORT_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withQualifiedDataNames(ListUtils.map(d.getQualifiedDataNames(), t -> (Cobol.QualifiedDataName) visit(t, p)));
    }

    public Cobol visitDataOccursTo(Cobol.DataOccursTo dataOccursTo, P p) {
        Cobol.DataOccursTo d = dataOccursTo;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_OCCURS_TO_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withTo((Cobol.Word) visit(d.getTo(), p));
        return d.withIntegerLiteral((Cobol.Word) visit(d.getIntegerLiteral(), p));
    }

    public Cobol visitDataPictureClause(Cobol.DataPictureClause dataPictureClause, P p) {
        Cobol.DataPictureClause d = dataPictureClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_PICTURE_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withPictures(ListUtils.map(d.getPictures(), it -> visit(it, p)));
    }

    public Cobol visitDataReceivedByClause(Cobol.DataReceivedByClause dataReceivedByClause, P p) {
        Cobol.DataReceivedByClause d = dataReceivedByClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_RECEIVED_BY_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataRecordAreaClause(Cobol.DataRecordAreaClause dataRecordAreaClause, P p) {
        Cobol.DataRecordAreaClause d = dataRecordAreaClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_RECORD_AREA_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataRecordsClause(Cobol.DataRecordsClause dataRecordsClause, P p) {
        Cobol.DataRecordsClause d = dataRecordsClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_RECORDS_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withDataName(ListUtils.map(d.getDataName(), t -> (Name) visit(t, p)));
    }

    public Cobol visitDataRedefinesClause(Cobol.DataRedefinesClause dataRedefinesClause, P p) {
        Cobol.DataRedefinesClause d = dataRedefinesClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_REDEFINES_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withRedefines((Cobol.Word) visit(d.getRedefines(), p));
        return d.withDataName((Cobol.Word) visit(d.getDataName(), p));
    }

    public Cobol visitDataRenamesClause(Cobol.DataRenamesClause dataRenamesClause, P p) {
        Cobol.DataRenamesClause d = dataRenamesClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_RENAMES_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withRenames((Cobol.Word) visit(d.getRenames(), p));
        d = d.withFromName((Cobol.QualifiedDataName) visit(d.getFromName(), p));
        d = d.withThrough((Cobol.Word) visit(d.getThrough(), p));
        return d.withToName((Cobol.QualifiedDataName) visit(d.getToName(), p));
    }

    public Cobol visitDataSignClause(Cobol.DataSignClause dataSignClause, P p) {
        Cobol.DataSignClause d = dataSignClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_SIGN_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataSynchronizedClause(Cobol.DataSynchronizedClause dataSynchronizedClause, P p) {
        Cobol.DataSynchronizedClause d = dataSynchronizedClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_SYNCHRONIZED_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataThreadLocalClause(Cobol.DataThreadLocalClause dataThreadLocalClause, P p) {
        Cobol.DataThreadLocalClause d = dataThreadLocalClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_THREAD_LOCAL_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataTypeClause(Cobol.DataTypeClause dataTypeClause, P p) {
        Cobol.DataTypeClause d = dataTypeClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_TYPE_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withParenthesized((Cobol.Parenthesized) visit(d.getParenthesized(), p));
    }

    public Cobol visitDataTypeDefClause(Cobol.DataTypeDefClause dataTypeDefClause, P p) {
        Cobol.DataTypeDefClause d = dataTypeDefClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_TYPE_DEF_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataUsageClause(Cobol.DataUsageClause dataUsageClause, P p) {
        Cobol.DataUsageClause d = dataUsageClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_USAGE_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDataUsingClause(Cobol.DataUsingClause dataUsingClause, P p) {
        Cobol.DataUsingClause d = dataUsingClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_USING_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withName((Name) visit(d.getName(), p));
    }

    public Cobol visitDataValueClause(Cobol.DataValueClause dataValueClause, P p) {
        Cobol.DataValueClause d = dataValueClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_VALUE_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withCobols(ListUtils.map(d.getCobols(), t -> visit(t, p)));
    }

    public Cobol visitDataValueInterval(Cobol.DataValueInterval dataValueInterval, P p) {
        Cobol.DataValueInterval d = dataValueInterval;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_VALUE_INTERVAL_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withFrom((Name) visit(d.getFrom(), p));
        return d.withTo((Cobol.DataValueIntervalTo) visit(d.getTo(), p));
    }

    public Cobol visitDataValueIntervalTo(Cobol.DataValueIntervalTo dataValueIntervalTo, P p) {
        Cobol.DataValueIntervalTo d = dataValueIntervalTo;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_VALUE_INTERVAL_TO_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withThrough((Cobol.Word) visit(d.getThrough(), p));
        return d.withLiteral((Literal) visit(d.getLiteral(), p));
    }

    public Cobol visitDataWithLowerBoundsClause(Cobol.DataWithLowerBoundsClause dataWithLowerBoundsClause, P p) {
        Cobol.DataWithLowerBoundsClause d = dataWithLowerBoundsClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_WITH_LOWER_BOUNDS_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDecimalPointClause(Cobol.DecimalPointClause decimalPointClause, P p) {
        Cobol.DecimalPointClause d = decimalPointClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DECIMAL_POINT_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDefaultComputationalSignClause(Cobol.DefaultComputationalSignClause defaultComputationalSignClause, P p) {
        Cobol.DefaultComputationalSignClause d = defaultComputationalSignClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DEFAULT_COMPUTATIONAL_SIGN_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDefaultDisplaySignClause(Cobol.DefaultDisplaySignClause defaultDisplaySignClause, P p) {
        Cobol.DefaultDisplaySignClause d = defaultDisplaySignClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DEFAULT_DISPLAY_SIGN_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitDelete(Cobol.Delete delete, P p) {
        Cobol.Delete d = delete;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DELETE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withDelete((Cobol.Word) visit(d.getDelete(), p));
        d = d.withFileName((Name) visit(d.getFileName(), p));
        d = d.withRecord((Cobol.Word) visit(d.getRecord(), p));
        d = d.withInvalidKey((Cobol.StatementPhrase) visit(d.getInvalidKey(), p));
        d = d.withNotInvalidKey((Cobol.StatementPhrase) visit(d.getNotInvalidKey(), p));
        return d.withEndDelete((Cobol.Word) visit(d.getEndDelete(), p));
    }

    public Cobol visitDestinationCountClause(Cobol.DestinationCountClause destinationCountClause, P p) {
        Cobol.DestinationCountClause d = destinationCountClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DESTINATION_COUNT_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWords(ListUtils.map(d.getWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withDataDescName((Cobol.Word) visit(d.getDataDescName(), p));
    }

    public Cobol visitDestinationTableClause(Cobol.DestinationTableClause destinationTableClause, P p) {
        Cobol.DestinationTableClause d = destinationTableClause;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DESTINATION_TABLE_CLAUSE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withFirstWords(ListUtils.map(d.getFirstWords(), it -> (Cobol.Word) visit(it, p)));
        d = d.withIntegerLiteral((Cobol.Word) visit(d.getIntegerLiteral(), p));
        d = d.withSecondWords(ListUtils.map(d.getSecondWords(), it -> (Cobol.Word) visit(it, p)));
        return d.withIndexNames(ListUtils.map(d.getIndexNames(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitDisable(Cobol.Disable disable, P p) {
        Cobol.Disable d = disable;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DISABLE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withDisable((Cobol.Word) visit(d.getDisable(), p));
        d = d.withTypes(ListUtils.map(d.getTypes(), it -> (Cobol.Word) visit(it, p)));
        d = d.withCdName((Name) visit(d.getCdName(), p));
        d = d.withWith((Cobol.Word) visit(d.getWith(), p));
        d = d.withKey((Cobol.Word) visit(d.getKey(), p));
        return d.withKeyName((Name) visit(d.getKeyName(), p));
    }

    public Cobol visitDisplay(Cobol.Display display, P p) {
        Cobol.Display d = display;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DISPLAY_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withDisplay((Cobol.Word) visit(d.getDisplay(), p));
        d = d.withOperands(ListUtils.map(d.getOperands(), t -> visit(t, p)));
        d = d.withDisplayAt((Cobol.DisplayAt) visit(d.getDisplayAt(), p));
        d = d.withDisplayUpon((Cobol.DisplayUpon) visit(d.getDisplayUpon(), p));
        d = d.withDisplayWith(ListUtils.map(d.getDisplayWith(), it -> (Cobol.Word) visit(it, p)));
        d = d.withOnExceptionClause((Cobol.StatementPhrase) visit(d.getOnExceptionClause(), p));
        d = d.withNotOnExceptionClause((Cobol.StatementPhrase) visit(d.getNotOnExceptionClause(), p));
        return d.withEndDisplay((Cobol.Word) visit(d.getEndDisplay(), p));
    }

    public Cobol visitDisplayAt(Cobol.DisplayAt displayAt, P p) {
        Cobol.DisplayAt d = displayAt;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DISPLAY_AT_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withAt((Cobol.Word) visit(d.getAt(), p));
        return d.withName((Name) visit(d.getName(), p));
    }

    public Cobol visitDisplayUpon(Cobol.DisplayUpon displayUpon, P p) {
        Cobol.DisplayUpon d = displayUpon;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DISPLAY_UPON_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withUpon((Cobol.Word) visit(d.getUpon(), p));
        return d.withName((Cobol.Word) visit(d.getName(), p));
    }

    public Cobol visitDivide(Cobol.Divide divide, P p) {
        Cobol.Divide d = divide;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DIVIDE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withDivide((Cobol.Word) visit(d.getDivide(), p));
        d = d.withName((Name) visit(d.getName(), p));
        d = d.withAction(visit(d.getAction(), p));
        d = d.withDivideRemainder((Cobol.DivideRemainder) visit(d.getDivideRemainder(), p));
        d = d.withOnSizeErrorPhrase((Cobol.StatementPhrase) visit(d.getOnSizeErrorPhrase(), p));
        d = d.withNotOnSizeErrorPhrase((Cobol.StatementPhrase) visit(d.getNotOnSizeErrorPhrase(), p));
        return d.withEndDivide((Cobol.Word) visit(d.getEndDivide(), p));
    }

    public Cobol visitDivideGiving(Cobol.DivideGiving divideGiving, P p) {
        Cobol.DivideGiving d = divideGiving;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DIVIDE_GIVING_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWord((Cobol.Word) visit(d.getWord(), p));
        d = d.withName((Name) visit(d.getName(), p));
        return d.withDivideGivingPhrase((Cobol.DivideGivingPhrase) visit(d.getDivideGivingPhrase(), p));
    }

    public Cobol visitDivideGivingPhrase(Cobol.DivideGivingPhrase divideGivingPhrase, P p) {
        Cobol.DivideGivingPhrase d = divideGivingPhrase;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DIVIDE_GIVING_PHRASE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withGiving((Cobol.Word) visit(d.getGiving(), p));
        return d.withRoundables(ListUtils.map(d.getRoundables(), t -> (Cobol.Roundable) visit(t, p)));
    }

    public Cobol visitDivideInto(Cobol.DivideInto divideInto, P p) {
        Cobol.DivideInto d = divideInto;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DIVIDE_INTO_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withInto((Cobol.Word) visit(d.getInto(), p));
        return d.withRoundables(ListUtils.map(d.getRoundables(), t -> (Cobol.Roundable) visit(t, p)));
    }

    public Cobol visitDivideRemainder(Cobol.DivideRemainder divideRemainder, P p) {
        Cobol.DivideRemainder d = divideRemainder;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DIVIDE_REMAINDER_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withRemainder((Cobol.Word) visit(d.getRemainder(), p));
        return d.withName((Name) visit(d.getName(), p));
    }

    public Cobol visitEnable(Cobol.Enable enable, P p) {
        Cobol.Enable e = enable;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ENABLE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withEnable((Cobol.Word) visit(e.getEnable(), p));
        e = e.withTypes(ListUtils.map(e.getTypes(), it -> (Cobol.Word) visit(it, p)));
        e = e.withCdName((Name) visit(e.getCdName(), p));
        e = e.withWith((Cobol.Word) visit(e.getWith(), p));
        e = e.withKey((Cobol.Word) visit(e.getKey(), p));
        return e.withKeyName((Name) visit(e.getKeyName(), p));
    }

    public Cobol visitEndKeyClause(Cobol.EndKeyClause endKeyClause, P p) {
        Cobol.EndKeyClause e = endKeyClause;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.END_KEY_CLAUSE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
        return e.withName((Cobol.Word) visit(e.getName(), p));
    }

    public Cobol visitEndProgram(Cobol.EndProgram endProgram, P p) {
        Cobol.EndProgram e = endProgram;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.END_PROGRAM_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
        e = e.withProgramName((Name) visit(e.getProgramName(), p));
        return e.withDot((Cobol.Word) visit(e.getDot(), p));
    }

    public Cobol.Entry visitEntry(Cobol.Entry entry, P p) {
        Cobol.Entry e = entry;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ENTRY_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withEntry((Cobol.Word) visit(e.getEntry(), p));
        e = e.withLiteral((Literal) visit(e.getLiteral(), p));
        e = e.withUsing((Cobol.Word) visit(e.getUsing(), p));
        return e.withIdentifiers(ListUtils.map(e.getIdentifiers(), it -> (Identifier) visit(it, p)));
    }

    public Cobol visitEnvironmentDivision(Cobol.EnvironmentDivision environmentDivision, P p) {
        Cobol.EnvironmentDivision e = environmentDivision;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ENVIRONMENT_DIVISION_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
        e = e.withDot((Cobol.Word) visit(e.getDot(), p));
        return e.withBody(ListUtils.map(e.getBody(), it -> visit(it, p)));
    }

    public Cobol visitEvaluate(Cobol.Evaluate evaluate, P p) {
        Cobol.Evaluate e = evaluate;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withEvaluate((Cobol.Word) visit(e.getEvaluate(), p));
        e = e.withSelect(visit(e.getSelect(), p));
        e = e.withAlsoSelect(ListUtils.map(e.getAlsoSelect(), t -> (Cobol.EvaluateAlso) visit(t, p)));
        e = e.withWhenPhrase(ListUtils.map(e.getWhenPhrase(), t -> (Cobol.EvaluateWhenPhrase) visit(t, p)));
        e = e.withWhenOther((Cobol.StatementPhrase) visit(e.getWhenOther(), p));
        return e.withEndPhrase((Cobol.Word) visit(e.getEndPhrase(), p));
    }

    public Cobol visitEvaluateAlso(Cobol.EvaluateAlso evaluateAlso, P p) {
        Cobol.EvaluateAlso e = evaluateAlso;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_ALSO_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withAlso((Cobol.Word) visit(e.getAlso(), p));
        return e.withSelect(visit(e.getSelect(), p));
    }

    public Cobol visitEnvironmentSwitchNameClause(Cobol.EnvironmentSwitchNameClause environmentSwitchNameClause, P p) {
        Cobol.EnvironmentSwitchNameClause e = environmentSwitchNameClause;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ENVIRONMENT_SWITCH_NAME_CLAUSE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withEnvironmentName((Name) visit(e.getEnvironmentName(), p));
        e = e.withIs((Cobol.Word) visit(e.getIs(), p));
        e = e.withMnemonicName((Name) visit(e.getMnemonicName(), p));
        return e.withEnvironmentSwitchNameSpecialNamesStatusPhrase((Cobol.EnvironmentSwitchNameSpecialNamesStatusPhrase) visit(e.getEnvironmentSwitchNameSpecialNamesStatusPhrase(), p));
    }

    public Cobol visitEnvironmentSwitchNameSpecialNamesStatusPhrase(Cobol.EnvironmentSwitchNameSpecialNamesStatusPhrase environmentSwitchNameSpecialNamesStatusPhrase, P p) {
        Cobol.EnvironmentSwitchNameSpecialNamesStatusPhrase e = environmentSwitchNameSpecialNamesStatusPhrase;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ENVIRONMENT_SWITCH_NAME_SPECIAL_NAMES_STATUS_PHRASE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        return e.withCobols(ListUtils.map(e.getCobols(), it -> visit(it, p)));
    }

    public Cobol visitErrorKeyClause(Cobol.ErrorKeyClause errorKeyClause, P p) {
        Cobol.ErrorKeyClause e = errorKeyClause;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ERROR_KEY_CLAUSE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
        return e.withName((Name) visit(e.getName(), p));
    }

    public Cobol visitEvaluateAlsoCondition(Cobol.EvaluateAlsoCondition evaluateAlsoCondition, P p) {
        Cobol.EvaluateAlsoCondition e = evaluateAlsoCondition;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_ALSO_CONDITION_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withAlso((Cobol.Word) visit(e.getAlso(), p));
        return e.withCondition((Cobol.EvaluateCondition) visit(e.getCondition(), p));
    }

    public Cobol visitEvaluateCondition(Cobol.EvaluateCondition evaluateCondition, P p) {
        Cobol.EvaluateCondition e = evaluateCondition;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_CONDITION_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withCondition(visit(e.getCondition(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
        e = e.withCondition(visit(e.getCondition(), p));
        return e.withEvaluateThrough((Cobol.EvaluateThrough) visit(e.getEvaluateThrough(), p));
    }

    public Cobol visitEvaluateThrough(Cobol.EvaluateThrough evaluateThrough, P p) {
        Cobol.EvaluateThrough e = evaluateThrough;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_THROUGH_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withThrough((Cobol.Word) visit(e.getThrough(), p));
        return e.withValue(visit(e.getValue(), p));
    }

    public Cobol visitEvaluateWhen(Cobol.EvaluateWhen evaluateWhen, P p) {
        Cobol.EvaluateWhen e = evaluateWhen;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_WHEN_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWhen((Cobol.Word) visit(e.getWhen(), p));
        e = e.withCondition((Cobol.EvaluateCondition) visit(e.getCondition(), p));
        return e.withAlsoCondition(ListUtils.map(e.getAlsoCondition(), t -> (Cobol.EvaluateAlsoCondition) visit(t, p)));
    }

    public Cobol visitEvaluateWhenPhrase(Cobol.EvaluateWhenPhrase evaluateWhenPhrase, P p) {
        Cobol.EvaluateWhenPhrase e = evaluateWhenPhrase;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EVALUATE_WHEN_PHRASE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWhens(ListUtils.map(e.getWhens(), t -> (Cobol.EvaluateWhen) visit(t, p)));
        return e.withStatements(ListUtils.map(e.getStatements(), t -> (Statement) visit(t, p)));
    }

    public Cobol visitExecCicsStatement(Cobol.ExecCicsStatement execCicsStatement, P p) {
        Cobol.ExecCicsStatement e = execCicsStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXEC_CICS_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        return e.withExecCicsLines(ListUtils.map(e.getExecCicsLines(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitExecSqlImsStatement(Cobol.ExecSqlImsStatement execSqlImsStatement, P p) {
        Cobol.ExecSqlImsStatement e = execSqlImsStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXEC_SQL_IMS_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        return e.withExecSqlLmsLines(ListUtils.map(e.getExecSqlLmsLines(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitExecSqlStatement(Cobol.ExecSqlStatement execSqlStatement, P p) {
        Cobol.ExecSqlStatement e = execSqlStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXEC_SQL_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        return e.withExecSqlLines(ListUtils.map(e.getExecSqlLines(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitExhibit(Cobol.Exhibit exhibit, P p) {
        Cobol.Exhibit e = exhibit;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXHIBIT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
        return e.withOperands(ListUtils.map(e.getOperands(), t -> (Identifier) visit(t, p)));
    }

    public Cobol visitExit(Cobol.Exit exit, P p) {
        Cobol.Exit e = exit;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXIT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        return e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitExternalClause(Cobol.ExternalClause externalClause, P p) {
        Cobol.ExternalClause e = externalClause;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXTERNAL_CLAUSE_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        return e.withWords(ListUtils.map(e.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitFigurativeConstant(Cobol.FigurativeConstant figurativeConstant, P p) {
        Cobol.FigurativeConstant f = figurativeConstant;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FIGURATIVE_CONSTANT_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withWord((Cobol.Word) visit(f.getWord(), p));
        return f.withLiteral((Literal) visit(f.getLiteral(), p));
    }

    public Cobol visitFileControlEntry(Cobol.FileControlEntry fileControlEntry, P p) {
        Cobol.FileControlEntry f = fileControlEntry;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FILE_CONTROL_ENTRY_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withSelectClause(visit(f.getSelectClause(), p));
        return f.withControlClauses(ListUtils.map(f.getControlClauses(), t -> visit(t, p)));
    }

    public Cobol visitFileControlParagraph(Cobol.FileControlParagraph fileControlParagraph, P p) {
        Cobol.FileControlParagraph f = fileControlParagraph;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FILE_CONTROL_PARAGRAPH_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withFileControl((Cobol.Word) visit(f.getFileControl(), p));
        return f.withControlEntries(ListUtils.map(f.getControlEntries(), t -> visit(t, p)));
    }

    public Cobol visitFileDescriptionEntry(Cobol.FileDescriptionEntry fileDescriptionEntry, P p) {
        Cobol.FileDescriptionEntry f = fileDescriptionEntry;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FILE_DESCRIPTION_ENTRY_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withWord((Cobol.Word) visit(f.getWord(), p));
        f = f.withName((Cobol.Word) visit(f.getName(), p));
        f = f.withClauses(ListUtils.map(f.getClauses(), it -> visit(it, p)));
        return f.withDataDescriptions(ListUtils.map(f.getDataDescriptions(), it -> (Cobol.DataDescriptionEntry) visit(it, p)));
    }

    public Cobol visitFileSection(Cobol.FileSection fileSection, P p) {
        Cobol.FileSection f = fileSection;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FILE_SECTION_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withWords(ListUtils.map(f.getWords(), it -> (Cobol.Word) visit(it, p)));
        f = f.withDot((Cobol.Word) visit(f.getDot(), p));
        return f.withFileDescriptionEntry(ListUtils.map(f.getFileDescriptionEntry(), it -> (Cobol.FileDescriptionEntry) visit(it, p)));
    }

    public Cobol visitFileStatusClause(Cobol.FileStatusClause fileStatusClause, P p) {
        Cobol.FileStatusClause f = fileStatusClause;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FILE_STATUS_CLAUSE_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withWords(ListUtils.map(f.getWords(), it -> (Cobol.Word) visit(it, p)));
        return f.withQualifiedDataNames(ListUtils.map(f.getQualifiedDataNames(), t -> (Cobol.QualifiedDataName) visit(t, p)));
    }

    public Cobol visitFunctionCall(Cobol.FunctionCall functionCall, P p) {
        Cobol.FunctionCall f = functionCall;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FUNCTION_CALL_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withFunction((Cobol.Word) visit(f.getFunction(), p));
        f = f.withFunctionName((Cobol.Word) visit(f.getFunctionName(), p));
        f = f.withArguments(ListUtils.map(f.getArguments(), t -> (Cobol.Parenthesized) visit(t, p)));
        return f.withReferenceModifier((Cobol.ReferenceModifier) visit(f.getReferenceModifier(), p));
    }

    public Cobol visitGenerate(Cobol.Generate generate, P p) {
        Cobol.Generate g = generate;
        g = g.withPrefix(visitSpace(g.getPrefix(), Space.Location.GENERATE_PREFIX, p));
        g = g.withMarkers(visitMarkers(g.getMarkers(), p));
        g = g.withGenerate((Cobol.Word) visit(g.getGenerate(), p));
        return g.withReportName((Cobol.QualifiedDataName) visit(g.getReportName(), p));
    }

    public Cobol visitGlobalClause(Cobol.GlobalClause globalClause, P p) {
        Cobol.GlobalClause g = globalClause;
        g = g.withPrefix(visitSpace(g.getPrefix(), Space.Location.GLOBAL_CLAUSE_PREFIX, p));
        g = g.withMarkers(visitMarkers(g.getMarkers(), p));
        return g.withWords(ListUtils.map(g.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitGoBack(Cobol.GoBack goBack, P p) {
        Cobol.GoBack g = goBack;
        g = g.withPrefix(visitSpace(g.getPrefix(), Space.Location.GO_BACK_PREFIX, p));
        g = g.withMarkers(visitMarkers(g.getMarkers(), p));
        return g.withGoBack((Cobol.Word) visit(g.getGoBack(), p));
    }

    public Cobol visitGoTo(Cobol.GoTo _goTo, P p) {
        Cobol.GoTo g = _goTo;
        g = g.withPrefix(visitSpace(g.getPrefix(), Space.Location.GO_TO_PREFIX, p));
        g = g.withMarkers(visitMarkers(g.getMarkers(), p));
        g = g.withWords(ListUtils.map(g.getWords(), it -> (Cobol.Word) visit(it, p)));
        return g.withStatement(visit(g.getStatement(), p));
    }

    public Cobol visitGoToDependingOnStatement(Cobol.GoToDependingOnStatement goToDependingOnStatement, P p) {
        Cobol.GoToDependingOnStatement g = goToDependingOnStatement;
        g = g.withPrefix(visitSpace(g.getPrefix(), Space.Location.GO_TO_DEPENDING_ON_STATEMENT_PREFIX, p));
        g = g.withMarkers(visitMarkers(g.getMarkers(), p));
        g = g.withProcedureNames(ListUtils.map(g.getProcedureNames(), t -> (Cobol.ProcedureName) visit(t, p)));
        g = g.withWords(ListUtils.map(g.getWords(), it -> (Cobol.Word) visit(it, p)));
        return g.withIdentifier((Identifier) visit(g.getIdentifier(), p));
    }

    public Cobol visitIdentificationDivision(Cobol.IdentificationDivision identificationDivision, P p) {
        Cobol.IdentificationDivision i = identificationDivision;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IDENTIFICATION_DIVISION_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWords(ListUtils.map(i.getWords(), it -> (Cobol.Word) visit(it, p)));
        i = i.withProgramIdParagraph((Cobol.ProgramIdParagraph) visit(i.getProgramIdParagraph(), p));
        return i.withParagraphs(ListUtils.map(i.getParagraphs(), it -> visit(it, p)));
    }

    public Cobol visitIdentificationDivisionParagraph(Cobol.IdentificationDivisionParagraph identificationDivisionParagraph, P p) {
        Cobol.IdentificationDivisionParagraph i = identificationDivisionParagraph;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IDENTIFICATION_DIVISION_PARAGRAPH_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        i = i.withDot((Cobol.Word) visit(i.getDot(), p));
        i = i.withCommentEntry((Cobol.CommentEntry) visit(i.getCommentEntry(), p));
        i = i.withWords(ListUtils.map(i.getWords(), it -> (Cobol.Word) visit(it, p)));
        return i.withDot2((Cobol.Word) visit(i.getDot2(), p));
    }

    public Cobol visitIf(Cobol.If _if, P p) {
        Cobol.If i = _if;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IF_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        i = i.withCondition((Cobol.Condition) visit(i.getCondition(), p));
        i = i.withIfThen((Cobol.IfThen) visit(i.getIfThen(), p));
        i = i.withIfElse((Cobol.IfElse) visit(i.getIfElse(), p));
        return i.withEndIf((Cobol.Word) visit(i.getEndIf(), p));
    }

    public Cobol visitIfElse(Cobol.IfElse ifElse, P p) {
        Cobol.IfElse i = ifElse;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IF_ELSE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        i = i.withNextSentences(ListUtils.map(i.getNextSentences(), t -> (Cobol.Word) visit(t, p)));
        return i.withStatements(ListUtils.map(i.getStatements(), t -> (Statement) visit(t, p)));
    }

    public Cobol visitIfThen(Cobol.IfThen ifThen, P p) {
        Cobol.IfThen i = ifThen;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IF_THEN_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        i = i.withNextSentences(ListUtils.map(i.getNextSentences(), t -> (Cobol.Word) visit(t, p)));
        return i.withStatements(ListUtils.map(i.getStatements(), t -> (Statement) visit(t, p)));
    }

    public Cobol visitInData(Cobol.InData inData, P p) {
        Cobol.InData i = inData;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IN_DATA_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withName((Name) visit(i.getName(), p));
    }

    public Cobol visitInFile(Cobol.InFile inFile, P p) {
        Cobol.InFile i = inFile;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IN_FILE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withName((Name) visit(i.getName(), p));
    }

    public Cobol visitInLibrary(Cobol.InLibrary inLibrary, P p) {
        Cobol.InLibrary i = inLibrary;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IN_LIBRARY_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withName((Name) visit(i.getName(), p));
    }

    public Cobol visitInMnemonic(Cobol.InMnemonic inMnemonic, P p) {
        Cobol.InMnemonic i = inMnemonic;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IN_MNEMONIC_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withName((Name) visit(i.getName(), p));
    }

    public Cobol visitInSection(Cobol.InSection inSection, P p) {
        Cobol.InSection i = inSection;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IN_SECTION_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withName((Name) visit(i.getName(), p));
    }

    public Cobol visitInTable(Cobol.InTable inTable, P p) {
        Cobol.InTable i = inTable;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IN_TABLE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withTableCall((Cobol.TableCall) visit(i.getTableCall(), p));
    }

    public Cobol visitInitialize(Cobol.Initialize initialize, P p) {
        Cobol.Initialize i = initialize;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INITIALIZE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withInitialize((Cobol.Word) visit(i.getInitialize(), p));
        i = i.withIdentifiers(ListUtils.map(i.getIdentifiers(), t -> visit(t, p)));
        return i.withInitializeReplacingPhrase((Cobol.InitializeReplacingPhrase) visit(i.getInitializeReplacingPhrase(), p));
    }

    public Cobol visitInitializeReplacingBy(Cobol.InitializeReplacingBy initializeReplacingBy, P p) {
        Cobol.InitializeReplacingBy i = initializeReplacingBy;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INITIALIZE_REPLACING_BY_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWords(ListUtils.map(i.getWords(), it -> (Cobol.Word) visit(it, p)));
        return i.withIdentifier((Name) visit(i.getIdentifier(), p));
    }

    public Cobol visitInitializeReplacingPhrase(Cobol.InitializeReplacingPhrase initializeReplacingPhrase, P p) {
        Cobol.InitializeReplacingPhrase i = initializeReplacingPhrase;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INITIALIZE_REPLACING_PHRASE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withReplacing((Cobol.Word) visit(i.getReplacing(), p));
        return i.withInitializeReplacingBy(ListUtils.map(i.getInitializeReplacingBy(), t -> (Cobol.InitializeReplacingBy) visit(t, p)));
    }

    public Cobol visitInitiate(Cobol.Initiate initiate, P p) {
        Cobol.Initiate i = initiate;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INITIATE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withInitiate((Cobol.Word) visit(i.getInitiate(), p));
        return i.withReportNames(ListUtils.map(i.getReportNames(), t -> (Cobol.QualifiedDataName) visit(t, p)));
    }

    public Cobol visitInputOutputSection(Cobol.InputOutputSection inputOutputSection, P p) {
        Cobol.InputOutputSection i = inputOutputSection;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INPUT_OUTPUT_SECTION_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWords(ListUtils.map(i.getWords(), it -> (Cobol.Word) visit(it, p)));
        return i.withParagraphs(ListUtils.map(i.getParagraphs(), t -> visit(t, p)));
    }

    public Cobol visitInspect(Cobol.Inspect inspect, P p) {
        Cobol.Inspect i = inspect;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withInspect((Cobol.Word) visit(i.getInspect(), p));
        i = i.withIdentifier((Identifier) visit(i.getIdentifier(), p));
        return i.withPhrase(visit(i.getPhrase(), p));
    }

    public Cobol visitInspectAllLeading(Cobol.InspectAllLeading inspectAllLeading, P p) {
        Cobol.InspectAllLeading i = inspectAllLeading;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_ALL_LEADING_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withName((Name) visit(i.getName(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> (Cobol.InspectBeforeAfter) visit(t, p)));
    }

    public Cobol visitInspectAllLeadings(Cobol.InspectAllLeadings inspectAllLeadings, P p) {
        Cobol.InspectAllLeadings i = inspectAllLeadings;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_ALL_LEADINGS_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withLeadings(ListUtils.map(i.getLeadings(), t -> (Cobol.InspectAllLeading) visit(t, p)));
    }

    public Cobol visitInspectBeforeAfter(Cobol.InspectBeforeAfter inspectBeforeAfter, P p) {
        Cobol.InspectBeforeAfter i = inspectBeforeAfter;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_BEFORE_AFTER_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWords(ListUtils.map(i.getWords(), it -> (Cobol.Word) visit(it, p)));
        return i.withIdentifier((Name) visit(i.getIdentifier(), p));
    }

    public Cobol visitInspectBy(Cobol.InspectBy inspectBy, P p) {
        Cobol.InspectBy i = inspectBy;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_BY_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withBy((Cobol.Word) visit(i.getBy(), p));
        return i.withIdentifier((Name) visit(i.getIdentifier(), p));
    }

    public Cobol visitInspectCharacters(Cobol.InspectCharacters inspectCharacters, P p) {
        Cobol.InspectCharacters i = inspectCharacters;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_CHARACTERS_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withCharacter((Cobol.Word) visit(i.getCharacter(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> (Cobol.InspectBeforeAfter) visit(t, p)));
    }

    public Cobol visitInspectConvertingPhrase(Cobol.InspectConvertingPhrase inspectConvertingPhrase, P p) {
        Cobol.InspectConvertingPhrase i = inspectConvertingPhrase;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_CONVERTING_PHRASE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withConverting((Cobol.Word) visit(i.getConverting(), p));
        i = i.withIdentifier((Name) visit(i.getIdentifier(), p));
        i = i.withInspectTo((Cobol.InspectTo) visit(i.getInspectTo(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> (Cobol.InspectBeforeAfter) visit(t, p)));
    }

    public Cobol visitInspectFor(Cobol.InspectFor inspectFor, P p) {
        Cobol.InspectFor i = inspectFor;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_FOR_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withIdentifier((Identifier) visit(i.getIdentifier(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withInspects(ListUtils.map(i.getInspects(), t -> visit(t, p)));
    }

    public Cobol visitInspectReplacingAllLeading(Cobol.InspectReplacingAllLeading inspectReplacingAllLeading, P p) {
        Cobol.InspectReplacingAllLeading i = inspectReplacingAllLeading;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_REPLACING_ALL_LEADING_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withIdentifier((Name) visit(i.getIdentifier(), p));
        i = i.withInspectBy((Cobol.InspectBy) visit(i.getInspectBy(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> (Cobol.InspectBeforeAfter) visit(t, p)));
    }

    public Cobol visitInspectReplacingAllLeadings(Cobol.InspectReplacingAllLeadings inspectReplacingAllLeadings, P p) {
        Cobol.InspectReplacingAllLeadings i = inspectReplacingAllLeadings;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_REPLACING_ALL_LEADINGS_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> (Cobol.InspectReplacingAllLeading) visit(t, p)));
    }

    public Cobol visitInspectReplacingCharacters(Cobol.InspectReplacingCharacters inspectReplacingCharacters, P p) {
        Cobol.InspectReplacingCharacters i = inspectReplacingCharacters;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_REPLACING_CHARACTERS_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        i = i.withInspectBy((Cobol.InspectBy) visit(i.getInspectBy(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> (Cobol.InspectBeforeAfter) visit(t, p)));
    }

    public Cobol visitInspectReplacingPhrase(Cobol.InspectReplacingPhrase inspectReplacingPhrase, P p) {
        Cobol.InspectReplacingPhrase i = inspectReplacingPhrase;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_REPLACING_PHRASE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withWord((Cobol.Word) visit(i.getWord(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), t -> visit(t, p)));
    }

    public Cobol visitInspectTallyingPhrase(Cobol.InspectTallyingPhrase inspectTallyingPhrase, P p) {
        Cobol.InspectTallyingPhrase i = inspectTallyingPhrase;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_TALLYING_PHRASE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withTallying((Cobol.Word) visit(i.getTallying(), p));
        return i.withInspectFors(ListUtils.map(i.getInspectFors(), t -> (Cobol.InspectFor) visit(t, p)));
    }

    public Cobol visitInspectTallyingReplacingPhrase(Cobol.InspectTallyingReplacingPhrase inspectTallyingReplacingPhrase, P p) {
        Cobol.InspectTallyingReplacingPhrase i = inspectTallyingReplacingPhrase;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_TALLYING_REPLACING_PHRASE_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withTallying((Cobol.Word) visit(i.getTallying(), p));
        i = i.withInspectFors(ListUtils.map(i.getInspectFors(), t -> (Cobol.InspectFor) visit(t, p)));
        return i.withReplacingPhrases(ListUtils.map(i.getReplacingPhrases(), t -> (Cobol.InspectReplacingPhrase) visit(t, p)));
    }

    public Cobol visitInspectTo(Cobol.InspectTo inspectTo, P p) {
        Cobol.InspectTo i = inspectTo;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSPECT_TO_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withTo((Cobol.Word) visit(i.getTo(), p));
        return i.withIdentifier((Name) visit(i.getIdentifier(), p));
    }

    public Cobol visitIoControlParagraph(Cobol.IoControlParagraph ioControlParagraph, P p) {
        Cobol.IoControlParagraph i = ioControlParagraph;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IO_CONTROL_PARAGRAPH_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withIOControl((Cobol.Word) visit(i.getIOControl(), p));
        i = i.withDot((Cobol.Word) visit(i.getDot(), p));
        i = i.withFileName((Cobol.Word) visit(i.getFileName(), p));
        i = i.withFileNameDot((Cobol.Word) visit(i.getFileNameDot(), p));
        i = i.withClauses(ListUtils.map(i.getClauses(), it -> visit(it, p)));
        return i.withDot2((Cobol.Word) visit(i.getDot2(), p));
    }

    public Cobol visitLabelRecordsClause(Cobol.LabelRecordsClause labelRecordsClause, P p) {
        Cobol.LabelRecordsClause l = labelRecordsClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LABEL_RECORDS_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withDataNames(ListUtils.map(l.getDataNames(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitLibraryAttributeClauseFormat1(Cobol.LibraryAttributeClauseFormat1 libraryAttributeClauseFormat1, P p) {
        Cobol.LibraryAttributeClauseFormat1 l = libraryAttributeClauseFormat1;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ATTRIBUTE_CLAUSE_1_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        return l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitLibraryAttributeClauseFormat2(Cobol.LibraryAttributeClauseFormat2 libraryAttributeClauseFormat2, P p) {
        Cobol.LibraryAttributeClauseFormat2 l = libraryAttributeClauseFormat2;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ATTRIBUTE_CLAUSE_2_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withAttribute((Cobol.Word) visit(l.getAttribute(), p));
        l = l.withLibraryAttributeFunction((Cobol.LibraryAttributeFunction) visit(l.getLibraryAttributeFunction(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        l = l.withLibraryAttributeParameter((Cobol.LibraryAttributeParameter) visit(l.getLibraryAttributeParameter(), p));
        return l.withLibraryAttributeTitle((Cobol.LibraryAttributeTitle) visit(l.getLibraryAttributeTitle(), p));
    }

    public Cobol visitLibraryAttributeFunction(Cobol.LibraryAttributeFunction libraryAttributeFunction, P p) {
        Cobol.LibraryAttributeFunction l = libraryAttributeFunction;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ATTRIBUTE_FUNCTION_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withLiteral((Name) visit(l.getLiteral(), p));
    }

    public Cobol visitLibraryAttributeParameter(Cobol.LibraryAttributeParameter libraryAttributeParameter, P p) {
        Cobol.LibraryAttributeParameter l = libraryAttributeParameter;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ATTRIBUTE_PARAMETER_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withLiteral((Name) visit(l.getLiteral(), p));
    }

    public Cobol visitLibraryAttributeTitle(Cobol.LibraryAttributeTitle libraryAttributeTitle, P p) {
        Cobol.LibraryAttributeTitle l = libraryAttributeTitle;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ATTRIBUTE_TITLE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withLiteral((Name) visit(l.getLiteral(), p));
    }

    public Cobol visitLibraryDescriptionEntryFormat1(Cobol.LibraryDescriptionEntryFormat1 libraryDescriptionEntryFormat1, P p) {
        Cobol.LibraryDescriptionEntryFormat1 l = libraryDescriptionEntryFormat1;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_DESCRIPTION_ENTRY_FORMAT_1_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withLd((Cobol.Word) visit(l.getLd(), p));
        l = l.withLibraryName((Cobol.Word) visit(l.getLibraryName(), p));
        l = l.withExport((Cobol.Word) visit(l.getExport(), p));
        l = l.withLibraryAttributeClauseFormat1((Cobol.LibraryAttributeClauseFormat1) visit(l.getLibraryAttributeClauseFormat1(), p));
        return l.withLibraryEntryProcedureClauseFormat1((Cobol.LibraryEntryProcedureClauseFormat1) visit(l.getLibraryEntryProcedureClauseFormat1(), p));
    }

    public Cobol visitLibraryDescriptionEntryFormat2(Cobol.LibraryDescriptionEntryFormat2 libraryDescriptionEntryFormat2, P p) {
        Cobol.LibraryDescriptionEntryFormat2 l = libraryDescriptionEntryFormat2;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_DESCRIPTION_ENTRY_FORMAT_2_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withLb((Cobol.Word) visit(l.getLb(), p));
        l = l.withLibraryName((Cobol.Word) visit(l.getLibraryName(), p));
        l = l.withExport((Cobol.Word) visit(l.getExport(), p));
        l = l.withLibraryIsGlobalClause((Cobol.LibraryIsGlobalClause) visit(l.getLibraryIsGlobalClause(), p));
        l = l.withLibraryIsCommonClause((Cobol.LibraryIsCommonClause) visit(l.getLibraryIsCommonClause(), p));
        return l.withClauseFormats(ListUtils.map(l.getClauseFormats(), it -> visit(it, p)));
    }

    public Cobol visitLibraryEntryProcedureClauseFormat1(Cobol.LibraryEntryProcedureClauseFormat1 libraryEntryProcedureClauseFormat1, P p) {
        Cobol.LibraryEntryProcedureClauseFormat1 l = libraryEntryProcedureClauseFormat1;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ENTRY_PROCEDURE_CLAUSE_FORMAT_1_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withEntryProcedure((Cobol.Word) visit(l.getEntryProcedure(), p));
        l = l.withProgramName((Cobol.Word) visit(l.getProgramName(), p));
        return l.withLibraryEntryProcedureForClause((Cobol.LibraryEntryProcedureForClause) visit(l.getLibraryEntryProcedureForClause(), p));
    }

    public Cobol visitLibraryEntryProcedureClauseFormat2(Cobol.LibraryEntryProcedureClauseFormat2 libraryEntryProcedureClauseFormat2, P p) {
        Cobol.LibraryEntryProcedureClauseFormat2 l = libraryEntryProcedureClauseFormat2;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ENTRY_PROCEDURE_CLAUSE_FORMAT_2_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withEntryProcedure((Cobol.Word) visit(l.getEntryProcedure(), p));
        l = l.withProgramName((Cobol.Word) visit(l.getProgramName(), p));
        l = l.withLibraryEntryProcedureForClause((Cobol.LibraryEntryProcedureForClause) visit(l.getLibraryEntryProcedureForClause(), p));
        l = l.withLibraryEntryProcedureWithClause((Cobol.LibraryEntryProcedureWithClause) visit(l.getLibraryEntryProcedureWithClause(), p));
        l = l.withLibraryEntryProcedureUsingClause((Cobol.LibraryEntryProcedureUsingClause) visit(l.getLibraryEntryProcedureUsingClause(), p));
        return l.withLibraryEntryProcedureGivingClause((Cobol.LibraryEntryProcedureGivingClause) visit(l.getLibraryEntryProcedureGivingClause(), p));
    }

    public Cobol visitLibraryEntryProcedureForClause(Cobol.LibraryEntryProcedureForClause libraryEntryProcedureForClause, P p) {
        Cobol.LibraryEntryProcedureForClause l = libraryEntryProcedureForClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ENTRY_PROCEDURE_FOR_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWord((Cobol.Word) visit(l.getWord(), p));
        return l.withLiteral((Name) visit(l.getLiteral(), p));
    }

    public Cobol visitLibraryEntryProcedureGivingClause(Cobol.LibraryEntryProcedureGivingClause libraryEntryProcedureGivingClause, P p) {
        Cobol.LibraryEntryProcedureGivingClause l = libraryEntryProcedureGivingClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ENTRY_PROCEDURE_GIVING_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withGiving((Cobol.Word) visit(l.getGiving(), p));
        return l.withDataName((Cobol.Word) visit(l.getDataName(), p));
    }

    public Cobol visitLibraryEntryProcedureUsingClause(Cobol.LibraryEntryProcedureUsingClause libraryEntryProcedureUsingClause, P p) {
        Cobol.LibraryEntryProcedureUsingClause l = libraryEntryProcedureUsingClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ENTRY_PROCEDURE_USING_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withUsing((Cobol.Word) visit(l.getUsing(), p));
        return l.withNames(ListUtils.map(l.getNames(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitLibraryEntryProcedureWithClause(Cobol.LibraryEntryProcedureWithClause libraryEntryProcedureWithClause, P p) {
        Cobol.LibraryEntryProcedureWithClause l = libraryEntryProcedureWithClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_ENTRY_PROCEDURE_WITH_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWith((Cobol.Word) visit(l.getWith(), p));
        return l.withNames(ListUtils.map(l.getNames(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitLibraryIsCommonClause(Cobol.LibraryIsCommonClause libraryIsCommonClause, P p) {
        Cobol.LibraryIsCommonClause l = libraryIsCommonClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_IS_COMMON_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        return l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitLibraryIsGlobalClause(Cobol.LibraryIsGlobalClause libraryIsGlobalClause, P p) {
        Cobol.LibraryIsGlobalClause l = libraryIsGlobalClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LIBRARY_IS_GLOBAL_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        return l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitLinageClause(Cobol.LinageClause linageClause, P p) {
        Cobol.LinageClause l = linageClause;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LINAGE_CLAUSE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        l = l.withName((Name) visit(l.getName(), p));
        l = l.withLine((Cobol.Word) visit(l.getLine(), p));
        return l.withLinageAt(ListUtils.map(l.getLinageAt(), t -> visit(t, p)));
    }

    public Cobol visitLinageFootingAt(Cobol.LinageFootingAt linageFootingAt, P p) {
        Cobol.LinageFootingAt l = linageFootingAt;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LINAGE_FOOTING_AT_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withName((Name) visit(l.getName(), p));
    }

    public Cobol visitLinageLinesAtBottom(Cobol.LinageLinesAtBottom linageLinesAtBottom, P p) {
        Cobol.LinageLinesAtBottom l = linageLinesAtBottom;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LINAGE_LINES_AT_BOTTOM_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withName((Name) visit(l.getName(), p));
    }

    public Cobol visitLinageLinesAtTop(Cobol.LinageLinesAtTop linageLinesAtTop, P p) {
        Cobol.LinageLinesAtTop l = linageLinesAtTop;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LINAGE_LINES_AT_TOP_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), it -> (Cobol.Word) visit(it, p)));
        return l.withName((Name) visit(l.getName(), p));
    }

    public Cobol visitLinkageSection(Cobol.LinkageSection linkageSection, P p) {
        Cobol.LinkageSection l = linkageSection;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LINKAGE_SECTION_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), t -> (Cobol.Word) visit(t, p)));
        l = l.withDot((Cobol.Word) visit(l.getDot(), p));
        return l.withDataDescriptions(ListUtils.map(l.getDataDescriptions(), t -> (Cobol.DataDescriptionEntry) visit(t, p)));
    }

    public Cobol visitLocalStorageSection(Cobol.LocalStorageSection localStorageSection, P p) {
        Cobol.LocalStorageSection l = localStorageSection;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LOCAL_STORAGE_SECTION_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withWords(ListUtils.map(l.getWords(), t -> (Cobol.Word) visit(t, p)));
        l = l.withDot((Cobol.Word) visit(l.getDot(), p));
        l = l.withLocalData((Cobol.Word) visit(l.getLocalData(), p));
        l = l.withLocalName((Name) visit(l.getLocalName(), p));
        l = l.withDot2((Cobol.Word) visit(l.getDot2(), p));
        return l.withDataDescriptions(ListUtils.map(l.getDataDescriptions(), t -> (Cobol.DataDescriptionEntry) visit(t, p)));
    }

    public Cobol visitMerge(Cobol.Merge merge, P p) {
        Cobol.Merge m = merge;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        m = m.withFileName((Name) visit(m.getFileName(), p));
        m = m.withMergeOnKeyClause(ListUtils.map(m.getMergeOnKeyClause(), t -> (Cobol.MergeOnKeyClause) visit(t, p)));
        m = m.withMergeCollatingSequencePhrase((Cobol.MergeCollatingSequencePhrase) visit(m.getMergeCollatingSequencePhrase(), p));
        m = m.withMergeUsing(ListUtils.map(m.getMergeUsing(), t -> (Cobol.MergeUsing) visit(t, p)));
        m = m.withMergeOutputProcedurePhrase((Cobol.MergeOutputProcedurePhrase) visit(m.getMergeOutputProcedurePhrase(), p));
        return m.withMergeGivingPhrase(ListUtils.map(m.getMergeGivingPhrase(), t -> (Cobol.MergeGivingPhrase) visit(t, p)));
    }

    public Cobol visitMergeCollatingSequencePhrase(Cobol.MergeCollatingSequencePhrase mergeCollatingSequencePhrase, P p) {
        Cobol.MergeCollatingSequencePhrase m = mergeCollatingSequencePhrase;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_COLLATING_SEQUENCE_PHRASE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        m = m.withName(ListUtils.map(m.getName(), it -> (Name) visit(it, p)));
        m = m.withMergeCollatingAlphanumeric((Cobol.Mergeable) visit(m.getMergeCollatingAlphanumeric(), p));
        return m.withMergeCollatingNational((Cobol.Mergeable) visit(m.getMergeCollatingNational(), p));
    }

    public Cobol visitMergeGiving(Cobol.MergeGiving mergeGiving, P p) {
        Cobol.MergeGiving m = mergeGiving;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_GIVING_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withName((Name) visit(m.getName(), p));
        return m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitMergeGivingPhrase(Cobol.MergeGivingPhrase mergeGivingPhrase, P p) {
        Cobol.MergeGivingPhrase m = mergeGivingPhrase;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_GIVING_PHRASE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        return m.withMergeGiving(ListUtils.map(m.getMergeGiving(), t -> (Cobol.MergeGiving) visit(t, p)));
    }

    public Cobol visitMergeOnKeyClause(Cobol.MergeOnKeyClause mergeOnKeyClause, P p) {
        Cobol.MergeOnKeyClause m = mergeOnKeyClause;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_ON_KEY_CLAUSE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withQualifiedDataName(ListUtils.map(m.getQualifiedDataName(), t -> (Cobol.QualifiedDataName) visit(t, p)));
    }

    public Cobol visitMergeOutputProcedurePhrase(Cobol.MergeOutputProcedurePhrase mergeOutputProcedurePhrase, P p) {
        Cobol.MergeOutputProcedurePhrase m = mergeOutputProcedurePhrase;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_OUTPUT_PROCEDURE_PHRASE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        m = m.withProcedureName((Cobol.ProcedureName) visit(m.getProcedureName(), p));
        return m.withMergeOutputThrough((Cobol.MergeOutputThrough) visit(m.getMergeOutputThrough(), p));
    }

    public Cobol visitMergeOutputThrough(Cobol.MergeOutputThrough mergeOutputThrough, P p) {
        Cobol.MergeOutputThrough m = mergeOutputThrough;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_OUTPUT_THROUGH_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        return m.withProcedureName((Cobol.ProcedureName) visit(m.getProcedureName(), p));
    }

    public Cobol visitMergeUsing(Cobol.MergeUsing mergeUsing, P p) {
        Cobol.MergeUsing m = mergeUsing;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGE_USING_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        return m.withFileNames(ListUtils.map(m.getFileNames(), t -> (Name) visit(t, p)));
    }

    public Cobol visitMergeable(Cobol.Mergeable mergeable, P p) {
        Cobol.Mergeable m = mergeable;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MERGEABLE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withName((Name) visit(m.getName(), p));
    }

    public Cobol visitMessageCountClause(Cobol.MessageCountClause messageCountClause, P p) {
        Cobol.MessageCountClause m = messageCountClause;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MESSAGE_COUNT_CLAUSE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withDataDescName((Cobol.Word) visit(m.getDataDescName(), p));
    }

    public Cobol visitMessageDateClause(Cobol.MessageDateClause messageDateClause, P p) {
        Cobol.MessageDateClause m = messageDateClause;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MESSAGE_DATA_CLAUSE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withDataDescName((Cobol.Word) visit(m.getDataDescName(), p));
    }

    public Cobol visitMessageTimeClause(Cobol.MessageTimeClause messageTimeClause, P p) {
        Cobol.MessageTimeClause m = messageTimeClause;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MESSAGE_TIME_CLAUSE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withDataDescName((Cobol.Word) visit(m.getDataDescName(), p));
    }

    public Cobol visitMoveCorrespondingToStatement(Cobol.MoveCorrespondingToStatement moveCorrespondingToStatement, P p) {
        Cobol.MoveCorrespondingToStatement m = moveCorrespondingToStatement;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MOVE_CORRESPONDING_TO_STATEMENT_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        m = m.withMoveCorrespondingToSendingArea((Identifier) visit(m.getMoveCorrespondingToSendingArea(), p));
        m = m.withTo((Cobol.Word) visit(m.getTo(), p));
        return m.withIdentifiers(ListUtils.map(m.getIdentifiers(), t -> (Identifier) visit(t, p)));
    }

    public Cobol visitMoveStatement(Cobol.MoveStatement moveStatement, P p) {
        Cobol.MoveStatement m = moveStatement;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MOVE_STATEMENT_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withMoveToStatement(visit(m.getMoveToStatement(), p));
    }

    public Cobol visitMoveToStatement(Cobol.MoveToStatement moveToStatement, P p) {
        Cobol.MoveToStatement m = moveToStatement;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MOVE_TO_STATEMENT_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withFrom((Name) visit(m.getFrom(), p));
        m = m.withTo((Cobol.Word) visit(m.getTo(), p));
        return m.withNames(ListUtils.map(m.getNames(), it -> visit(it, p)));
    }

    public Cobol visitMultDiv(Cobol.MultDiv multDiv, P p) {
        Cobol.MultDiv m = multDiv;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULT_DIV_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        return m.withPowers((Cobol.Powers) visit(m.getPowers(), p));
    }

    public Cobol visitMultDivs(Cobol.MultDivs multDivs, P p) {
        Cobol.MultDivs m = multDivs;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULT_DIVS_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withPowers((Cobol.Powers) visit(m.getPowers(), p));
        return m.withMultDivs(ListUtils.map(m.getMultDivs(), t -> (Cobol.MultDiv) visit(t, p)));
    }

    public Cobol visitMultipleFileClause(Cobol.MultipleFileClause multipleFileClause, P p) {
        Cobol.MultipleFileClause m = multipleFileClause;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULTIPLE_FILE_CLAUSE_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWords(ListUtils.map(m.getWords(), it -> (Cobol.Word) visit(it, p)));
        return m.withFilePositions(ListUtils.map(m.getFilePositions(), t -> visit(t, p)));
    }

    public Cobol visitMultipleFilePosition(Cobol.MultipleFilePosition multipleFilePosition, P p) {
        Cobol.MultipleFilePosition m = multipleFilePosition;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULTIPLE_FILE_POSITION_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withFileName((Cobol.Word) visit(m.getFileName(), p));
        m = m.withPosition((Cobol.Word) visit(m.getPosition(), p));
        return m.withIntegerLiteral((Cobol.Word) visit(m.getIntegerLiteral(), p));
    }

    public Cobol visitMultiply(Cobol.Multiply multiply, P p) {
        Cobol.Multiply m = multiply;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULTIPLY_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withWord((Cobol.Word) visit(m.getWord(), p));
        m = m.withMultiplicand((Name) visit(m.getMultiplicand(), p));
        m = m.withBy((Cobol.Word) visit(m.getBy(), p));
        m = m.withMultiply(visit(m.getMultiply(), p));
        m = m.withOnSizeErrorPhrase((Cobol.StatementPhrase) visit(m.getOnSizeErrorPhrase(), p));
        m = m.withNotOnSizeErrorPhrase((Cobol.StatementPhrase) visit(m.getNotOnSizeErrorPhrase(), p));
        return m.withEndMultiply((Cobol.Word) visit(m.getEndMultiply(), p));
    }

    public Cobol visitMultiplyGiving(Cobol.MultiplyGiving multiplyGiving, P p) {
        Cobol.MultiplyGiving m = multiplyGiving;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULTIPLY_GIVING_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        m = m.withOperand((Name) visit(m.getOperand(), p));
        m = m.withGiving((Cobol.Word) visit(m.getGiving(), p));
        return m.withResult(ListUtils.map(m.getResult(), it -> (Cobol.Roundable) visit(it, p)));
    }

    public Cobol visitMultiplyRegular(Cobol.MultiplyRegular multiplyRegular, P p) {
        Cobol.MultiplyRegular m = multiplyRegular;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MULTIPLY_REGULAR_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        return m.withOperand(ListUtils.map(m.getOperand(), t -> (Cobol.Roundable) visit(t, p)));
    }

    public Cobol visitNextSentence(Cobol.NextSentence nextSentence, P p) {
        Cobol.NextSentence n = nextSentence;
        n = n.withPrefix(visitSpace(n.getPrefix(), Space.Location.NEXT_SENTENCE_PREFIX, p));
        n = n.withMarkers(visitMarkers(n.getMarkers(), p));
        return n.withWords(ListUtils.map(n.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitObjectComputer(Cobol.ObjectComputer objectComputer, P p) {
        Cobol.ObjectComputer o = objectComputer;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OBJECT_COMPUTER_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withWords(ListUtils.map(o.getWords(), it -> (Cobol.Word) visit(it, p)));
        return o.withComputer((Cobol.ObjectComputerDefinition) visit(o.getComputer(), p));
    }

    public Cobol visitObjectComputerDefinition(Cobol.ObjectComputerDefinition objectComputerDefinition, P p) {
        Cobol.ObjectComputerDefinition o = objectComputerDefinition;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OBJECT_COMPUTER_DEFINITION_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withComputerName((Cobol.Word) visit(o.getComputerName(), p));
        o = o.withSpecifications(ListUtils.map(o.getSpecifications(), t -> visit(t, p)));
        return o.withDot((Cobol.Word) visit(o.getDot(), p));
    }

    public Cobol visitOdtClause(Cobol.OdtClause odtClause, P p) {
        Cobol.OdtClause o = odtClause;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.ODT_CLAUSE_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withWords(ListUtils.map(o.getWords(), it -> (Cobol.Word) visit(it, p)));
        return o.withMnemonicName((Identifier) visit(o.getMnemonicName(), p));
    }

    public Cobol visitOpen(Cobol.Open open, P p) {
        Cobol.Open o = open;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPEN_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withWord((Cobol.Word) visit(o.getWord(), p));
        return o.withOpen(ListUtils.map(o.getOpen(), it -> visit(it, p)));
    }

    public Cobol visitOpenIOExtendStatement(Cobol.OpenIOExtendStatement openIOExtendStatement, P p) {
        Cobol.OpenIOExtendStatement o = openIOExtendStatement;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPEN_IO_EXTEND_STATEMENT_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withWord((Cobol.Word) visit(o.getWord(), p));
        return o.withFileNames(ListUtils.map(o.getFileNames(), t -> visit(t, p)));
    }

    public Cobol visitOpenInputOutputStatement(Cobol.OpenInputOutputStatement openInputOutputStatement, P p) {
        Cobol.OpenInputOutputStatement o = openInputOutputStatement;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPEN_INPUT_OUTPUT_STATEMENT_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withWord((Cobol.Word) visit(o.getWord(), p));
        return o.withOpenInput(ListUtils.map(o.getOpenInput(), t -> visit(t, p)));
    }

    public Cobol visitOpenable(Cobol.Openable openable, P p) {
        Cobol.Openable o = openable;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPENABLE_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withFileName((Name) visit(o.getFileName(), p));
        return o.withWords(ListUtils.map(o.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitOrganizationClause(Cobol.OrganizationClause organizationClause, P p) {
        Cobol.OrganizationClause o = organizationClause;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.ORGANIZATION_CLAUSE_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        return o.withWords(ListUtils.map(o.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitPaddingCharacterClause(Cobol.PaddingCharacterClause paddingCharacterClause, P p) {
        Cobol.PaddingCharacterClause pp = paddingCharacterClause;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PADDING_CHARACTER_CLAUSE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        return pp.withName((Name) visit(pp.getName(), p));
    }

    public Cobol visitParagraph(Cobol.Paragraph paragraph, P p) {
        Cobol.Paragraph pp = paragraph;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PARAGRAPH_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withParagraphName((Name) visit(pp.getParagraphName(), p));
        pp = pp.withDot((Cobol.Word) visit(pp.getDot(), p));
        pp = pp.withAlteredGoTo((Cobol.AlteredGoTo) visit(pp.getAlteredGoTo(), p));
        return pp.withSentences(ListUtils.map(pp.getSentences(), t -> (Cobol.Sentence) visit(t, p)));
    }

    public Cobol visitParagraphs(Cobol.Paragraphs paragraphs, P p) {
        Cobol.Paragraphs pp = paragraphs;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PARAGRAPHS_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withSentences(ListUtils.map(pp.getSentences(), t -> (Cobol.Sentence) visit(t, p)));
        return pp.withParagraphs(ListUtils.map(pp.getParagraphs(), t -> (Cobol.Paragraph) visit(t, p)));
    }

    public Cobol visitParenthesized(Cobol.Parenthesized parenthesized, P p) {
        Cobol.Parenthesized pp = parenthesized;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PARENTHESIZED_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withLeftParen((Cobol.Word) visit(pp.getLeftParen(), p));
        pp = pp.withContents(ListUtils.map(pp.getContents(), it -> visit(it, p)));
        return pp.withRightParen((Cobol.Word) visit(pp.getRightParen(), p));
    }

    public Cobol visitPasswordClause(Cobol.PasswordClause passwordClause, P p) {
        Cobol.PasswordClause pp = passwordClause;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PASSWORD_CLAUSE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        return pp.withDataName((Cobol.Word) visit(pp.getDataName(), p));
    }

    public Cobol visitPerform(Cobol.Perform perform, P p) {
        Cobol.Perform pp = perform;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withStatement(visit(pp.getStatement(), p));
    }

    public Cobol visitPerformInlineStatement(Cobol.PerformInlineStatement performInlineStatement, P p) {
        Cobol.PerformInlineStatement pp = performInlineStatement;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_IN_LINE_STATEMENT_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withPerformType(visit(pp.getPerformType(), p));
        pp = pp.withStatements(ListUtils.map(pp.getStatements(), t -> (Statement) visit(t, p)));
        return pp.withWord((Cobol.Word) visit(pp.getWord(), p));
    }

    public Cobol visitPerformProcedureStatement(Cobol.PerformProcedureStatement performProcedureStatement, P p) {
        Cobol.PerformProcedureStatement pp = performProcedureStatement;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_PROCEDURE_STATEMENT_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withProcedureName((Cobol.ProcedureName) visit(pp.getProcedureName(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        pp = pp.withThroughProcedure((Cobol.ProcedureName) visit(pp.getThroughProcedure(), p));
        return pp.withPerformType(visit(pp.getPerformType(), p));
    }

    public Cobol visitPerformTestClause(Cobol.PerformTestClause performTestClause, P p) {
        Cobol.PerformTestClause pp = performTestClause;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_TEST_CLAUSE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        return pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitPerformTimes(Cobol.PerformTimes performTimes, P p) {
        Cobol.PerformTimes pp = performTimes;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_TIMES_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withValue((Name) visit(pp.getValue(), p));
        return pp.withWord((Cobol.Word) visit(pp.getWord(), p));
    }

    public Cobol visitPerformUntil(Cobol.PerformUntil performUntil, P p) {
        Cobol.PerformUntil pp = performUntil;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_UNTIL_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withPerformTestClause((Cobol.PerformTestClause) visit(pp.getPerformTestClause(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withCondition((Cobol.Condition) visit(pp.getCondition(), p));
    }

    public Cobol visitPerformVarying(Cobol.PerformVarying performVarying, P p) {
        Cobol.PerformVarying pp = performVarying;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_VARYING_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        return pp.withCobols(ListUtils.map(pp.getCobols(), it -> visit(it, p)));
    }

    public Cobol visitPerformVaryingClause(Cobol.PerformVaryingClause performVaryingClause, P p) {
        Cobol.PerformVaryingClause pp = performVaryingClause;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_VARYING_CLAUSE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        pp = pp.withPerformVaryingPhrase((Cobol.PerformVaryingPhrase) visit(pp.getPerformVaryingPhrase(), p));
        return pp.withPerformAfter(ListUtils.map(pp.getPerformAfter(), t -> (Cobol.Performable) visit(t, p)));
    }

    public Cobol visitPerformVaryingPhrase(Cobol.PerformVaryingPhrase performVaryingPhrase, P p) {
        Cobol.PerformVaryingPhrase pp = performVaryingPhrase;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORM_VARYING_PHRASE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withName((Name) visit(pp.getName(), p));
        pp = pp.withFrom((Cobol.Performable) visit(pp.getFrom(), p));
        pp = pp.withBy((Cobol.Performable) visit(pp.getBy(), p));
        return pp.withUntil((Cobol.PerformUntil) visit(pp.getUntil(), p));
    }

    public Cobol visitPerformable(Cobol.Performable performable, P p) {
        Cobol.Performable pp = performable;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PERFORMABLE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withExpression(visit(pp.getExpression(), p));
    }

    public Cobol visitPicture(Cobol.Picture picture, P p) {
        Cobol.Picture pp = picture;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PICTURE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        return pp.withParenthesized((Cobol.Parenthesized) visit(pp.getParenthesized(), p));
    }

    public Cobol visitPictureString(Cobol.PictureString pictureString, P p) {
        Cobol.PictureString pp = pictureString;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PICTURE_STRING_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        return pp.withPictures(ListUtils.map(pp.getPictures(), t -> (Cobol.Picture) visit(t, p)));
    }

    public Cobol visitPlusMinus(Cobol.PlusMinus plusMinus, P p) {
        Cobol.PlusMinus pp = plusMinus;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PLUS_MINUS_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withMultDivs((Cobol.MultDivs) visit(pp.getMultDivs(), p));
    }

    public Cobol visitPower(Cobol.Power power, P p) {
        Cobol.Power pp = power;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.POWER_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withPower((Cobol.Word) visit(pp.getPower(), p));
        return pp.withExpression(visit(pp.getExpression(), p));
    }

    public Cobol visitPowers(Cobol.Powers powers, P p) {
        Cobol.Powers pp = powers;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.POWERS_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withPlusMinusChar((Cobol.Word) visit(pp.getPlusMinusChar(), p));
        pp = pp.withExpression(visit(pp.getExpression(), p));
        return pp.withPowers(ListUtils.map(pp.getPowers(), t -> (Cobol.Power) visit(t, p)));
    }

    public Cobol visitProcedureDeclarative(Cobol.ProcedureDeclarative procedureDeclarative, P p) {
        Cobol.ProcedureDeclarative pp = procedureDeclarative;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DECLARATIVE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withProcedureSectionHeader((Cobol.ProcedureSectionHeader) visit(pp.getProcedureSectionHeader(), p));
        pp = pp.withDot((Cobol.Word) visit(pp.getDot(), p));
        pp = pp.withUseStatement((Cobol.UseStatement) visit(pp.getUseStatement(), p));
        pp = pp.withDot2((Cobol.Word) visit(pp.getDot2(), p));
        return pp.withParagraphs((Cobol.Paragraphs) visit(pp.getParagraphs(), p));
    }

    public Cobol visitProcedureDeclaratives(Cobol.ProcedureDeclaratives procedureDeclaratives, P p) {
        Cobol.ProcedureDeclaratives pp = procedureDeclaratives;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DECLARATIVES_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withDeclaratives((Cobol.Word) visit(pp.getDeclaratives(), p));
        pp = pp.withDot((Cobol.Word) visit(pp.getDot(), p));
        pp = pp.withProcedureDeclarative(ListUtils.map(pp.getProcedureDeclarative(), it -> (Cobol.ProcedureDeclarative) visit(it, p)));
        pp = pp.withEndDeclaratives(ListUtils.map(pp.getEndDeclaratives(), it -> (Cobol.Word) visit(it, p)));
        return pp.withDot2((Cobol.Word) visit(pp.getDot2(), p));
    }

    public Cobol visitProcedureDivision(Cobol.ProcedureDivision procedureDivision, P p) {
        Cobol.ProcedureDivision pp = procedureDivision;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        pp = pp.withProcedureDivisionUsingClause((Cobol.ProcedureDivisionUsingClause) visit(pp.getProcedureDivisionUsingClause(), p));
        pp = pp.withProcedureDivisionGivingClause((Cobol.ProcedureDivisionGivingClause) visit(pp.getProcedureDivisionGivingClause(), p));
        pp = pp.withDot((Cobol.Word) visit(pp.getDot(), p));
        pp = pp.withProcedureDeclaratives((Cobol.ProcedureDeclaratives) visit(pp.getProcedureDeclaratives(), p));
        return pp.withBody((Cobol.ProcedureDivisionBody) visit(pp.getBody(), p));
    }

    public Cobol visitProcedureDivisionBody(Cobol.ProcedureDivisionBody procedureDivisionBody, P p) {
        Cobol.ProcedureDivisionBody pp = procedureDivisionBody;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_BODY_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withParagraphs((Cobol.Paragraphs) visit(pp.getParagraphs(), p));
        return pp.withProcedureSection(ListUtils.map(pp.getProcedureSection(), it -> (Cobol.ProcedureSection) visit(it, p)));
    }

    public Cobol visitProcedureDivisionByReference(Cobol.ProcedureDivisionByReference procedureDivisionByReference, P p) {
        Cobol.ProcedureDivisionByReference pp = procedureDivisionByReference;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_BY_REFERENCE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withReference((Name) visit(pp.getReference(), p));
    }

    public Cobol visitProcedureDivisionByReferencePhrase(Cobol.ProcedureDivisionByReferencePhrase procedureDivisionByReferencePhrase, P p) {
        Cobol.ProcedureDivisionByReferencePhrase pp = procedureDivisionByReferencePhrase;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_BY_REFERENCE_PHRASE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        return pp.withProcedureDivisionByReference(ListUtils.map(pp.getProcedureDivisionByReference(), t -> (Cobol.ProcedureDivisionByReference) visit(t, p)));
    }

    public Cobol visitProcedureDivisionByValuePhrase(Cobol.ProcedureDivisionByValuePhrase procedureDivisionByValuePhrase, P p) {
        Cobol.ProcedureDivisionByValuePhrase pp = procedureDivisionByValuePhrase;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_BY_VALUE_PHRASE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        return pp.withPhrases(ListUtils.map(pp.getPhrases(), t -> (Name) visit(t, p)));
    }

    public Cobol visitProcedureDivisionGivingClause(Cobol.ProcedureDivisionGivingClause procedureDivisionGivingClause, P p) {
        Cobol.ProcedureDivisionGivingClause pp = procedureDivisionGivingClause;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_GIVING_CLAUSE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withDataName((Name) visit(pp.getDataName(), p));
    }

    public Cobol visitProcedureDivisionUsingClause(Cobol.ProcedureDivisionUsingClause procedureDivisionUsingClause, P p) {
        Cobol.ProcedureDivisionUsingClause pp = procedureDivisionUsingClause;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_DIVISION_USING_CLAUSE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWord((Cobol.Word) visit(pp.getWord(), p));
        return pp.withProcedureDivisionUsingParameter(ListUtils.map(pp.getProcedureDivisionUsingParameter(), t -> visit(t, p)));
    }

    public Cobol visitProcedureName(Cobol.ProcedureName procedureName, P p) {
        Cobol.ProcedureName pp = procedureName;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_NAME_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withParagraphName((Name) visit(pp.getParagraphName(), p));
        pp = pp.withInSection((Cobol.InSection) visit(pp.getInSection(), p));
        return pp.withSectionName((Name) visit(pp.getSectionName(), p));
    }

    public Cobol visitProcedureSection(Cobol.ProcedureSection procedureSection, P p) {
        Cobol.ProcedureSection pp = procedureSection;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_SECTION_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withProcedureSectionHeader((Cobol.ProcedureSectionHeader) visit(pp.getProcedureSectionHeader(), p));
        pp = pp.withDot((Cobol.Word) visit(pp.getDot(), p));
        return pp.withParagraphs((Cobol.Paragraphs) visit(pp.getParagraphs(), p));
    }

    public Cobol visitProcedureSectionHeader(Cobol.ProcedureSectionHeader procedureSectionHeader, P p) {
        Cobol.ProcedureSectionHeader pp = procedureSectionHeader;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROCEDURE_SECTION_HEADER_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withSectionName((Name) visit(pp.getSectionName(), p));
        pp = pp.withSection((Cobol.Word) visit(pp.getSection(), p));
        return pp.withIdentifier((Name) visit(pp.getIdentifier(), p));
    }

    public Cobol visitProgramIdParagraph(Cobol.ProgramIdParagraph programIdParagraph, P p) {
        Cobol.ProgramIdParagraph pp = programIdParagraph;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROGRAM_ID_PARAGRAPH_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withProgramId((Cobol.Word) visit(pp.getProgramId(), p));
        pp = pp.withDot((Cobol.Word) visit(pp.getDot(), p));
        pp = pp.withProgramName((Name) visit(pp.getProgramName(), p));
        pp = pp.withProgramAttributes(ListUtils.map(pp.getProgramAttributes(), it -> (Cobol.Word) visit(it, p)));
        pp = pp.withDot2((Cobol.Word) visit(pp.getDot2(), p));
        return pp.withCommentEntry((Cobol.CommentEntry) visit(pp.getCommentEntry(), p));
    }

    public Cobol visitProgramLibrarySection(Cobol.ProgramLibrarySection programLibrarySection, P p) {
        Cobol.ProgramLibrarySection pp = programLibrarySection;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROGRAM_LIBRARY_SECTION_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> (Cobol.Word) visit(it, p)));
        return pp.withLibraryDescriptionEntries(ListUtils.map(pp.getLibraryDescriptionEntries(), t -> visit(t, p)));
    }

    public Cobol visitProgramUnit(Cobol.ProgramUnit programUnit, P p) {
        Cobol.ProgramUnit pp = programUnit;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PROGRAM_UNIT_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withIdentificationDivision((Cobol.IdentificationDivision) visit(pp.getIdentificationDivision(), p));
        pp = pp.withEnvironmentDivision((Cobol.EnvironmentDivision) visit(pp.getEnvironmentDivision(), p));
        pp = pp.withDataDivision((Cobol.DataDivision) visit(pp.getDataDivision(), p));
        pp = pp.withProcedureDivision((Cobol.ProcedureDivision) visit(pp.getProcedureDivision(), p));
        pp = pp.withProgramUnits(ListUtils.map(pp.getProgramUnits(), t -> (Cobol.ProgramUnit) visit(t, p)));
        return pp.withEndProgram((Cobol.EndProgram) visit(pp.getEndProgram(), p));
    }

    public Cobol visitPurge(Cobol.Purge purge, P p) {
        Cobol.Purge pp = purge;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PURGE_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withPurge((Cobol.Word) visit(pp.getPurge(), p));
        return pp.withNames(ListUtils.map(pp.getNames(), t -> (Name) visit(t, p)));
    }

    public Cobol visitQualifiedDataName(Cobol.QualifiedDataName qualifiedDataName, P p) {
        Cobol.QualifiedDataName q = qualifiedDataName;
        q = q.withPrefix(visitSpace(q.getPrefix(), Space.Location.QUALIFIED_DATA_NAME_PREFIX, p));
        q = q.withMarkers(visitMarkers(q.getMarkers(), p));
        return q.withDataName(visit(q.getDataName(), p));
    }

    public Cobol visitQualifiedDataNameFormat1(Cobol.QualifiedDataNameFormat1 qualifiedDataNameFormat1, P p) {
        Cobol.QualifiedDataNameFormat1 q = qualifiedDataNameFormat1;
        q = q.withPrefix(visitSpace(q.getPrefix(), Space.Location.QUALIFIED_DATA_NAME_FORMAT_1_PREFIX, p));
        q = q.withMarkers(visitMarkers(q.getMarkers(), p));
        q = q.withName((Name) visit(q.getName(), p));
        q = q.withQualifiedInData(ListUtils.map(q.getQualifiedInData(), t -> visit(t, p)));
        return q.withInFile((Cobol.InFile) visit(q.getInFile(), p));
    }

    public Cobol visitQualifiedDataNameFormat2(Cobol.QualifiedDataNameFormat2 qualifiedDataNameFormat2, P p) {
        Cobol.QualifiedDataNameFormat2 q = qualifiedDataNameFormat2;
        q = q.withPrefix(visitSpace(q.getPrefix(), Space.Location.QUALIFIED_DATA_NAME_FORMAT_2_PREFIX, p));
        q = q.withMarkers(visitMarkers(q.getMarkers(), p));
        q = q.withParagraphName((Name) visit(q.getParagraphName(), p));
        return q.withInSection((Cobol.InSection) visit(q.getInSection(), p));
    }

    public Cobol visitQualifiedDataNameFormat3(Cobol.QualifiedDataNameFormat3 qualifiedDataNameFormat3, P p) {
        Cobol.QualifiedDataNameFormat3 q = qualifiedDataNameFormat3;
        q = q.withPrefix(visitSpace(q.getPrefix(), Space.Location.QUALIFIED_DATA_NAME_FORMAT_3_PREFIX, p));
        q = q.withMarkers(visitMarkers(q.getMarkers(), p));
        q = q.withTextName((Name) visit(q.getTextName(), p));
        return q.withInLibrary((Cobol.InLibrary) visit(q.getInLibrary(), p));
    }

    public Cobol visitQualifiedDataNameFormat4(Cobol.QualifiedDataNameFormat4 qualifiedDataNameFormat4, P p) {
        Cobol.QualifiedDataNameFormat4 q = qualifiedDataNameFormat4;
        q = q.withPrefix(visitSpace(q.getPrefix(), Space.Location.QUALIFIED_DATA_NAME_FORMAT_4_PREFIX, p));
        q = q.withMarkers(visitMarkers(q.getMarkers(), p));
        q = q.withLinageCounter((Cobol.Word) visit(q.getLinageCounter(), p));
        return q.withInFile((Cobol.InFile) visit(q.getInFile(), p));
    }

    public Cobol visitQualifiedInData(Cobol.QualifiedInData qualifiedInData, P p) {
        Cobol.QualifiedInData q = qualifiedInData;
        q = q.withPrefix(visitSpace(q.getPrefix(), Space.Location.QUALIFIED_IN_DATA_PREFIX, p));
        q = q.withMarkers(visitMarkers(q.getMarkers(), p));
        return q.withIn(visit(q.getIn(), p));
    }

    public Cobol visitRead(Cobol.Read read, P p) {
        Cobol.Read r = read;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.READ_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((Cobol.Word) visit(r.getWord(), p));
        r = r.withFileName((Name) visit(r.getFileName(), p));
        r = r.withNextRecord(ListUtils.map(r.getNextRecord(), it -> (Cobol.Word) visit(it, p)));
        r = r.withReadInto((Cobol.ReadInto) visit(r.getReadInto(), p));
        r = r.withReadWith((Cobol.ReadWith) visit(r.getReadWith(), p));
        r = r.withReadKey((Cobol.ReadKey) visit(r.getReadKey(), p));
        r = r.withInvalidKeyPhrase((Cobol.StatementPhrase) visit(r.getInvalidKeyPhrase(), p));
        r = r.withNotInvalidKeyPhrase((Cobol.StatementPhrase) visit(r.getNotInvalidKeyPhrase(), p));
        r = r.withAtEndPhrase((Cobol.StatementPhrase) visit(r.getAtEndPhrase(), p));
        r = r.withNotAtEndPhrase((Cobol.StatementPhrase) visit(r.getNotAtEndPhrase(), p));
        return r.withEndRead((Cobol.Word) visit(r.getEndRead(), p));
    }

    public Cobol visitReadInto(Cobol.ReadInto readInto, P p) {
        Cobol.ReadInto r = readInto;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.READ_INTO_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((Cobol.Word) visit(r.getWord(), p));
        return r.withIdentifier((Identifier) visit(r.getIdentifier(), p));
    }

    public Cobol visitReadKey(Cobol.ReadKey readKey, P p) {
        Cobol.ReadKey r = readKey;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.READ_KEY_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
    }

    public Cobol visitReadWith(Cobol.ReadWith readWith, P p) {
        Cobol.ReadWith r = readWith;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.READ_WITH_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReceivable(Cobol.Receivable receivable, P p) {
        Cobol.Receivable r = receivable;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECEIVABLE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withValue((Name) visit(r.getValue(), p));
    }

    public Cobol visitReceiveWith(Cobol.ReceiveWith receiveWith, P p) {
        Cobol.ReceiveWith r = receiveWith;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECEIVE_WITH_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReceive(Cobol.Receive receive, P p) {
        Cobol.Receive r = receive;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECEIVE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withReceive((Cobol.Word) visit(r.getReceive(), p));
        r = r.withFromOrInto(visit(r.getFromOrInto(), p));
        r = r.withOnExceptionClause((Cobol.StatementPhrase) visit(r.getOnExceptionClause(), p));
        r = r.withNotOnExceptionClause((Cobol.StatementPhrase) visit(r.getNotOnExceptionClause(), p));
        return r.withEndReceive((Cobol.Word) visit(r.getEndReceive(), p));
    }

    public Cobol visitReceiveFrom(Cobol.ReceiveFrom receiveFrom, P p) {
        Cobol.ReceiveFrom r = receiveFrom;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECEIVE_FROM_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Cobol.Word) visit(r.getDataName(), p));
    }

    public Cobol visitReceiveFromStatement(Cobol.ReceiveFromStatement receiveFromStatement, P p) {
        Cobol.ReceiveFromStatement r = receiveFromStatement;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECEIVE_FROM_STATEMENT_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withDataName((Cobol.Word) visit(r.getDataName(), p));
        r = r.withFrom((Cobol.Word) visit(r.getFrom(), p));
        r = r.withReceiveFrom((Cobol.ReceiveFrom) visit(r.getReceiveFrom(), p));
        return r.withBeforeWithThreadSizeStatus(ListUtils.map(r.getBeforeWithThreadSizeStatus(), it -> visit(it, p)));
    }

    public Cobol visitReceiveIntoStatement(Cobol.ReceiveIntoStatement receiveIntoStatement, P p) {
        Cobol.ReceiveIntoStatement r = receiveIntoStatement;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECEIVE_INTO_STATEMENT_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withCdName((Cobol.Word) visit(r.getCdName(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        r = r.withIdentifier((Identifier) visit(r.getIdentifier(), p));
        r = r.withReceiveNoData((Cobol.StatementPhrase) visit(r.getReceiveNoData(), p));
        return r.withReceiveWithData((Cobol.StatementPhrase) visit(r.getReceiveWithData(), p));
    }

    public Cobol visitRecordContainsClause(Cobol.RecordContainsClause recordContainsClause, P p) {
        Cobol.RecordContainsClause r = recordContainsClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_CONTAINS_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRecord((Cobol.Word) visit(r.getRecord(), p));
        return r.withClause(visit(r.getClause(), p));
    }

    public Cobol visitRecordContainsClauseFormat1(Cobol.RecordContainsClauseFormat1 recordContainsClauseFormat1, P p) {
        Cobol.RecordContainsClauseFormat1 r = recordContainsClauseFormat1;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_CONTAINS_CLAUSE_FORMAT_1_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withContains((Cobol.Word) visit(r.getContains(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        return r.withCharacters((Cobol.Word) visit(r.getCharacters(), p));
    }

    public Cobol visitRecordContainsClauseFormat2(Cobol.RecordContainsClauseFormat2 recordContainsClauseFormat2, P p) {
        Cobol.RecordContainsClauseFormat2 r = recordContainsClauseFormat2;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_CONTAINS_CLAUSE_FORMAT_2_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        r = r.withFromClause(ListUtils.map(r.getFromClause(), t -> visit(t, p)));
        return r.withQualifiedDataName(ListUtils.map(r.getQualifiedDataName(), t -> visit(t, p)));
    }

    public Cobol visitRecordContainsClauseFormat3(Cobol.RecordContainsClauseFormat3 recordContainsClauseFormat3, P p) {
        Cobol.RecordContainsClauseFormat3 r = recordContainsClauseFormat3;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_CONTAINS_CLAUSE_FORMAT_3_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withContains((Cobol.Word) visit(r.getContains(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        r = r.withRecordContainsTo((Cobol.RecordContainsTo) visit(r.getRecordContainsTo(), p));
        return r.withCharacters((Cobol.Word) visit(r.getCharacters(), p));
    }

    public Cobol visitRecordContainsTo(Cobol.RecordContainsTo recordContainsTo, P p) {
        Cobol.RecordContainsTo r = recordContainsTo;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_CONTAINS_TO_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withTo((Cobol.Word) visit(r.getTo(), p));
        return r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
    }

    public Cobol visitRecordDelimiterClause(Cobol.RecordDelimiterClause recordDelimiterClause, P p) {
        Cobol.RecordDelimiterClause r = recordDelimiterClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_DELIMITER_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withName((Name) visit(r.getName(), p));
    }

    public Cobol visitRecordKeyClause(Cobol.RecordKeyClause recordKeyClause, P p) {
        Cobol.RecordKeyClause r = recordKeyClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORD_KEY_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRecordWords(ListUtils.map(r.getRecordWords(), it -> (Cobol.Word) visit(it, p)));
        r = r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
        r = r.withPasswordClause((Cobol.PasswordClause) visit(r.getPasswordClause(), p));
        return r.withDuplicates(ListUtils.map(r.getDuplicates(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitRecordingModeClause(Cobol.RecordingModeClause recordingModeClause, P p) {
        Cobol.RecordingModeClause r = recordingModeClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RECORDING_MODE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withMode((Cobol.Word) visit(r.getMode(), p));
    }

    public Cobol visitReferenceModifier(Cobol.ReferenceModifier referenceModifier, P p) {
        Cobol.ReferenceModifier r = referenceModifier;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REFERENCE_MODIFIER_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withLeftParen((Cobol.Word) visit(r.getLeftParen(), p));
        r = r.withCharacterPosition((Cobol.ArithmeticExpression) visit(r.getCharacterPosition(), p));
        r = r.withColon((Cobol.Word) visit(r.getColon(), p));
        r = r.withLength((Cobol.ArithmeticExpression) visit(r.getLength(), p));
        return r.withRightParen((Cobol.Word) visit(r.getRightParen(), p));
    }

    public Cobol visitRelationArithmeticComparison(Cobol.RelationArithmeticComparison relationArithmeticComparison, P p) {
        Cobol.RelationArithmeticComparison r = relationArithmeticComparison;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELATION_ARITHMETIC_COMPARISON_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withArithmeticExpressionA((Cobol.ArithmeticExpression) visit(r.getArithmeticExpressionA(), p));
        r = r.withRelationalOperator((Cobol.RelationalOperator) visit(r.getRelationalOperator(), p));
        return r.withArithmeticExpressionB((Cobol.ArithmeticExpression) visit(r.getArithmeticExpressionB(), p));
    }

    public Cobol visitRelationCombinedComparison(Cobol.RelationCombinedComparison relationCombinedComparison, P p) {
        Cobol.RelationCombinedComparison r = relationCombinedComparison;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELATION_COMBINED_COMPARISON_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withArithmeticExpression((Cobol.ArithmeticExpression) visit(r.getArithmeticExpression(), p));
        r = r.withRelationalOperator((Cobol.RelationalOperator) visit(r.getRelationalOperator(), p));
        return r.withCombinedCondition((Cobol.Parenthesized) visit(r.getCombinedCondition(), p));
    }

    public Cobol visitRelationCombinedCondition(Cobol.RelationCombinedCondition relationCombinedCondition, P p) {
        Cobol.RelationCombinedCondition r = relationCombinedCondition;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELATION_COMBINED_CONDITION_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withRelationalArithmeticExpressions(ListUtils.map(r.getRelationalArithmeticExpressions(), it -> visit(it, p)));
    }

    public Cobol visitRelationSignCondition(Cobol.RelationSignCondition relationSignCondition, P p) {
        Cobol.RelationSignCondition r = relationSignCondition;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELATION_SIGN_CONDITION_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withArithmeticExpression((Cobol.ArithmeticExpression) visit(r.getArithmeticExpression(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitRelationalOperator(Cobol.RelationalOperator relationalOperator, P p) {
        Cobol.RelationalOperator r = relationalOperator;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELATIONAL_OPERATOR_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitRelativeKeyClause(Cobol.RelativeKeyClause relativeKeyClause, P p) {
        Cobol.RelativeKeyClause r = relativeKeyClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELATIVE_KEY_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
    }

    public Cobol visitRelease(Cobol.Release release, P p) {
        Cobol.Release r = release;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RELEASE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRelease((Cobol.Word) visit(r.getRelease(), p));
        r = r.withRecordName((Cobol.QualifiedDataName) visit(r.getRecordName(), p));
        r = r.withFrom((Cobol.Word) visit(r.getFrom(), p));
        return r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
    }

    public Cobol visitReportClause(Cobol.ReportClause reportClause, P p) {
        Cobol.ReportClause r = reportClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withReportName(ListUtils.map(r.getReportName(), t -> (Name) visit(t, p)));
    }

    public Cobol visitReportDescription(Cobol.ReportDescription reportDescription, P p) {
        Cobol.ReportDescription r = reportDescription;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withReportDescriptionEntry((Cobol.ReportDescriptionEntry) visit(r.getReportDescriptionEntry(), p));
        return r.withGroupDescriptionEntries(ListUtils.map(r.getGroupDescriptionEntries(), t -> visit(t, p)));
    }

    public Cobol visitReportDescriptionEntry(Cobol.ReportDescriptionEntry reportDescriptionEntry, P p) {
        Cobol.ReportDescriptionEntry r = reportDescriptionEntry;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_ENTRY_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRd((Cobol.Word) visit(r.getRd(), p));
        r = r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
        r = r.withReportDescriptionGlobalClause((Cobol.ReportDescriptionGlobalClause) visit(r.getReportDescriptionGlobalClause(), p));
        r = r.withReportDescriptionPageLimitClause((Cobol.ReportDescriptionPageLimitClause) visit(r.getReportDescriptionPageLimitClause(), p));
        r = r.withReportDescriptionHeadingClause((Cobol.ReportDescriptionHeadingClause) visit(r.getReportDescriptionHeadingClause(), p));
        r = r.withReportDescriptionFirstDetailClause((Cobol.ReportDescriptionFirstDetailClause) visit(r.getReportDescriptionFirstDetailClause(), p));
        r = r.withReportDescriptionLastDetailClause((Cobol.ReportDescriptionLastDetailClause) visit(r.getReportDescriptionLastDetailClause(), p));
        r = r.withReportDescriptionFootingClause((Cobol.ReportDescriptionFootingClause) visit(r.getReportDescriptionFootingClause(), p));
        return r.withDot((Cobol.Word) visit(r.getDot(), p));
    }

    public Cobol visitReportDescriptionFirstDetailClause(Cobol.ReportDescriptionFirstDetailClause reportDescriptionFirstDetailClause, P p) {
        Cobol.ReportDescriptionFirstDetailClause r = reportDescriptionFirstDetailClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_FIRST_DETAIL_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportDescriptionFootingClause(Cobol.ReportDescriptionFootingClause reportDescriptionFootingClause, P p) {
        Cobol.ReportDescriptionFootingClause r = reportDescriptionFootingClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_FOOTING_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((Cobol.Word) visit(r.getWord(), p));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportDescriptionGlobalClause(Cobol.ReportDescriptionGlobalClause reportDescriptionGlobalClause, P p) {
        Cobol.ReportDescriptionGlobalClause r = reportDescriptionGlobalClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_GLOBAL_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportDescriptionHeadingClause(Cobol.ReportDescriptionHeadingClause reportDescriptionHeadingClause, P p) {
        Cobol.ReportDescriptionHeadingClause r = reportDescriptionHeadingClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_HEADING_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((Cobol.Word) visit(r.getWord(), p));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportDescriptionLastDetailClause(Cobol.ReportDescriptionLastDetailClause reportDescriptionLastDetailClause, P p) {
        Cobol.ReportDescriptionLastDetailClause r = reportDescriptionLastDetailClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_LAST_DETAIL_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportDescriptionPageLimitClause(Cobol.ReportDescriptionPageLimitClause reportDescriptionPageLimitClause, P p) {
        Cobol.ReportDescriptionPageLimitClause r = reportDescriptionPageLimitClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_DESCRIPTION_PAGE_LIMIT_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withFirstWords(ListUtils.map(r.getFirstWords(), it -> (Cobol.Word) visit(it, p)));
        r = r.withIntegerLiteral((Name) visit(r.getIntegerLiteral(), p));
        return r.withSecondWords((Cobol.Word) visit(r.getSecondWords(), p));
    }

    public Cobol visitReportGroupBlankWhenZeroClause(Cobol.ReportGroupBlankWhenZeroClause reportGroupBlankWhenZeroClause, P p) {
        Cobol.ReportGroupBlankWhenZeroClause r = reportGroupBlankWhenZeroClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_BLANK_WHEN_ZERO_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupColumnNumberClause(Cobol.ReportGroupColumnNumberClause reportGroupColumnNumberClause, P p) {
        Cobol.ReportGroupColumnNumberClause r = reportGroupColumnNumberClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_COLUMN_NUMBER_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportGroupDescriptionEntryFormat1(Cobol.ReportGroupDescriptionEntryFormat1 reportGroupDescriptionEntryFormat1, P p) {
        Cobol.ReportGroupDescriptionEntryFormat1 r = reportGroupDescriptionEntryFormat1;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_DESCRIPTION_ENTRY_FORMAT_1_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        r = r.withDataName((Cobol.Word) visit(r.getDataName(), p));
        r = r.withGroupLineNumberClause((Cobol.ReportGroupLineNumberClause) visit(r.getGroupLineNumberClause(), p));
        r = r.withGroupNextGroupClause((Cobol.ReportGroupNextGroupClause) visit(r.getGroupNextGroupClause(), p));
        r = r.withGroupTypeClause((Cobol.ReportGroupTypeClause) visit(r.getGroupTypeClause(), p));
        r = r.withGroupUsageClause((Cobol.ReportGroupUsageClause) visit(r.getGroupUsageClause(), p));
        return r.withDot((Cobol.Word) visit(r.getDot(), p));
    }

    public Cobol visitReportGroupDescriptionEntryFormat2(Cobol.ReportGroupDescriptionEntryFormat2 reportGroupDescriptionEntryFormat2, P p) {
        Cobol.ReportGroupDescriptionEntryFormat2 r = reportGroupDescriptionEntryFormat2;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_DESCRIPTION_ENTRY_FORMAT_2_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        r = r.withDataName((Cobol.Word) visit(r.getDataName(), p));
        r = r.withReportGroupLineNumberClause((Cobol.ReportGroupLineNumberClause) visit(r.getReportGroupLineNumberClause(), p));
        r = r.withGroupUsageClause((Cobol.ReportGroupUsageClause) visit(r.getGroupUsageClause(), p));
        return r.withDot((Cobol.Word) visit(r.getDot(), p));
    }

    public Cobol visitReportGroupDescriptionEntryFormat3(Cobol.ReportGroupDescriptionEntryFormat3 reportGroupDescriptionEntryFormat3, P p) {
        Cobol.ReportGroupDescriptionEntryFormat3 r = reportGroupDescriptionEntryFormat3;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_DESCRIPTION_ENTRY_FORMAT_3_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        r = r.withDataName((Cobol.Word) visit(r.getDataName(), p));
        r = r.withClauses(ListUtils.map(r.getClauses(), it -> visit(it, p)));
        return r.withDot((Cobol.Word) visit(r.getDot(), p));
    }

    public Cobol visitReportGroupIndicateClause(Cobol.ReportGroupIndicateClause reportGroupIndicateClause, P p) {
        Cobol.ReportGroupIndicateClause r = reportGroupIndicateClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_INDICATOR_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupJustifiedClause(Cobol.ReportGroupJustifiedClause reportGroupJustifiedClause, P p) {
        Cobol.ReportGroupJustifiedClause r = reportGroupJustifiedClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_JUSTIFIED_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupLineNumberClause(Cobol.ReportGroupLineNumberClause reportGroupLineNumberClause, P p) {
        Cobol.ReportGroupLineNumberClause r = reportGroupLineNumberClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_LINE_NUMBER_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withClause(visit(r.getClause(), p));
    }

    public Cobol visitReportGroupLineNumberNextPage(Cobol.ReportGroupLineNumberNextPage reportGroupLineNumberNextPage, P p) {
        Cobol.ReportGroupLineNumberNextPage r = reportGroupLineNumberNextPage;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_LINE_NUMBER_NEXT_PAGE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupLineNumberPlus(Cobol.ReportGroupLineNumberPlus reportGroupLineNumberPlus, P p) {
        Cobol.ReportGroupLineNumberPlus r = reportGroupLineNumberPlus;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_LINE_NUMBER_PLUS_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withPlus((Cobol.Word) visit(r.getPlus(), p));
        return r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
    }

    public Cobol visitReportGroupNextGroupClause(Cobol.ReportGroupNextGroupClause reportGroupNextGroupClause, P p) {
        Cobol.ReportGroupNextGroupClause r = reportGroupNextGroupClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_NEXT_GROUP_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withClause(visit(r.getClause(), p));
    }

    public Cobol visitReportGroupNextGroupNextPage(Cobol.ReportGroupNextGroupNextPage reportGroupNextGroupNextPage, P p) {
        Cobol.ReportGroupNextGroupNextPage r = reportGroupNextGroupNextPage;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_NEXT_GROUP_NEXT_PAGE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withNextPage(ListUtils.map(r.getNextPage(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupNextGroupPlus(Cobol.ReportGroupNextGroupPlus reportGroupNextGroupPlus, P p) {
        Cobol.ReportGroupNextGroupPlus r = reportGroupNextGroupPlus;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_NEXT_GROUP_PLUS_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withPlus((Cobol.Word) visit(r.getPlus(), p));
        return r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
    }

    public Cobol visitReportGroupPictureClause(Cobol.ReportGroupPictureClause reportGroupPictureClause, P p) {
        Cobol.ReportGroupPictureClause r = reportGroupPictureClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_PICTURE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withPictureString((Cobol.PictureString) visit(r.getPictureString(), p));
    }

    public Cobol visitReportGroupResetClause(Cobol.ReportGroupResetClause reportGroupResetClause, P p) {
        Cobol.ReportGroupResetClause r = reportGroupResetClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_RESET_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportGroupSignClause(Cobol.ReportGroupSignClause reportGroupSignClause, P p) {
        Cobol.ReportGroupSignClause r = reportGroupSignClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_SIGN_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupSourceClause(Cobol.ReportGroupSourceClause reportGroupSourceClause, P p) {
        Cobol.ReportGroupSourceClause r = reportGroupSourceClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_SOURCE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withIdentifier((Name) visit(r.getIdentifier(), p));
    }

    public Cobol visitReportGroupSumClause(Cobol.ReportGroupSumClause reportGroupSumClause, P p) {
        Cobol.ReportGroupSumClause r = reportGroupSumClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_SUM_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withCobols(ListUtils.map(r.getCobols(), it -> visit(it, p)));
    }

    public Cobol visitReportGroupTypeClause(Cobol.ReportGroupTypeClause reportGroupTypeClause, P p) {
        Cobol.ReportGroupTypeClause r = reportGroupTypeClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withType(visit(r.getType(), p));
    }

    public Cobol visitReportGroupTypeControlFooting(Cobol.ReportGroupTypeControlFooting reportGroupTypeControlFooting, P p) {
        Cobol.ReportGroupTypeControlFooting r = reportGroupTypeControlFooting;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_CONTROL_FOOTING_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportGroupTypeControlHeading(Cobol.ReportGroupTypeControlHeading reportGroupTypeControlHeading, P p) {
        Cobol.ReportGroupTypeControlHeading r = reportGroupTypeControlHeading;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_CONTROL_HEADING_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDataName((Name) visit(r.getDataName(), p));
    }

    public Cobol visitReportGroupTypeDetail(Cobol.ReportGroupTypeDetail reportGroupTypeDetail, P p) {
        Cobol.ReportGroupTypeDetail r = reportGroupTypeDetail;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_DETAIL_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupTypePageFooting(Cobol.ReportGroupTypePageFooting reportGroupTypePageFooting, P p) {
        Cobol.ReportGroupTypePageFooting r = reportGroupTypePageFooting;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_PAGE_FOOTING_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupTypeReportFooting(Cobol.ReportGroupTypeReportFooting reportGroupTypeReportFooting, P p) {
        Cobol.ReportGroupTypeReportFooting r = reportGroupTypeReportFooting;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_REPORT_FOOTING_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupTypePageHeading(Cobol.ReportGroupTypePageHeading reportGroupTypePageHeading, P p) {
        Cobol.ReportGroupTypePageHeading r = reportGroupTypePageHeading;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_PAGE_HEADING_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupTypeReportHeading(Cobol.ReportGroupTypeReportHeading reportGroupTypeReportHeading, P p) {
        Cobol.ReportGroupTypeReportHeading r = reportGroupTypeReportHeading;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_TYPE_REPORT_HEADING_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupUsageClause(Cobol.ReportGroupUsageClause reportGroupUsageClause, P p) {
        Cobol.ReportGroupUsageClause r = reportGroupUsageClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_USAGE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReportGroupValueClause(Cobol.ReportGroupValueClause reportGroupValueClause, P p) {
        Cobol.ReportGroupValueClause r = reportGroupValueClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_GROUP_VALUE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withLiteral((Name) visit(r.getLiteral(), p));
    }

    public Cobol visitReportName(Cobol.ReportName reportName, P p) {
        Cobol.ReportName r = reportName;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_NAME_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
    }

    public Cobol visitReportSection(Cobol.ReportSection reportSection, P p) {
        Cobol.ReportSection r = reportSection;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPORT_SECTION_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
        return r.withDescriptions(ListUtils.map(r.getDescriptions(), t -> visit(t, p)));
    }

    public Cobol visitRerunClause(Cobol.RerunClause rerunClause, P p) {
        Cobol.RerunClause r = rerunClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RERUN_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRerun((Cobol.Word) visit(r.getRerun(), p));
        r = r.withOn((Cobol.Word) visit(r.getOn(), p));
        r = r.withName((Cobol.Word) visit(r.getName(), p));
        r = r.withEvery((Cobol.Word) visit(r.getEvery(), p));
        return r.withAction(visit(r.getAction(), p));
    }

    public Cobol visitRerunEveryClock(Cobol.RerunEveryClock rerunEveryClock, P p) {
        Cobol.RerunEveryClock r = rerunEveryClock;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RERUN_EVERY_CLOCK_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        return r.withClockUnits((Cobol.Word) visit(r.getClockUnits(), p));
    }

    public Cobol visitRerunEveryOf(Cobol.RerunEveryOf rerunEveryOf, P p) {
        Cobol.RerunEveryOf r = rerunEveryOf;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RERUN_EVERY_OF_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRecords(ListUtils.map(r.getRecords(), it -> (Cobol.Word) visit(it, p)));
        return r.withFileName((Cobol.Word) visit(r.getFileName(), p));
    }

    public Cobol visitRerunEveryRecords(Cobol.RerunEveryRecords rerunEveryRecords, P p) {
        Cobol.RerunEveryRecords r = rerunEveryRecords;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RERUN_EVERY_RECORDS_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIntegerLiteral((Cobol.Word) visit(r.getIntegerLiteral(), p));
        return r.withRecords((Cobol.Word) visit(r.getRecords(), p));
    }

    public Cobol visitReserveClause(Cobol.ReserveClause reserveClause, P p) {
        Cobol.ReserveClause r = reserveClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RERUN_RESERVE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), t -> visit(t, p)));
    }

    public Cobol visitReserveNetworkClause(Cobol.ReserveNetworkClause reserveNetworkClause, P p) {
        Cobol.ReserveNetworkClause r = reserveNetworkClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RESERVE_NETWORK_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        return r.withWords(ListUtils.map(r.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitReturn(Cobol.Return r, P p) {
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RETURN_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((Cobol.Word) visit(r.getWord(), p));
        r = r.withFileName((Name) visit(r.getFileName(), p));
        r = r.withRecord((Cobol.Word) visit(r.getRecord(), p));
        r = r.withInto((Cobol.ReturnInto) visit(r.getInto(), p));
        r = r.withAtEndPhrase((Cobol.StatementPhrase) visit(r.getAtEndPhrase(), p));
        r = r.withNotAtEndPhrase((Cobol.StatementPhrase) visit(r.getNotAtEndPhrase(), p));
        return r.withEndReturn((Cobol.Word) visit(r.getEndReturn(), p));
    }

    public Cobol visitReturnInto(Cobol.ReturnInto returnInto, P p) {
        Cobol.ReturnInto r = returnInto;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.RETURN_INTO_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withInto((Cobol.Word) visit(r.getInto(), p));
        return r.withQualifiedDataName((Cobol.QualifiedDataName) visit(r.getQualifiedDataName(), p));
    }

    public Cobol visitRewrite(Cobol.Rewrite rewrite, P p) {
        Cobol.Rewrite r = rewrite;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REWRITE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withRewrite((Cobol.Word) visit(r.getRewrite(), p));
        r = r.withRecordName((Cobol.QualifiedDataName) visit(r.getRecordName(), p));
        r = r.withRewriteFrom((Cobol.RewriteFrom) visit(r.getRewriteFrom(), p));
        r = r.withInvalidKeyPhrase((Cobol.StatementPhrase) visit(r.getInvalidKeyPhrase(), p));
        r = r.withNotInvalidKeyPhrase((Cobol.StatementPhrase) visit(r.getNotInvalidKeyPhrase(), p));
        return r.withEndRewrite((Cobol.Word) visit(r.getEndRewrite(), p));
    }

    public Cobol visitRewriteFrom(Cobol.RewriteFrom rewriteFrom, P p) {
        Cobol.RewriteFrom r = rewriteFrom;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REWRITE_FROM_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withFrom((Cobol.Word) visit(r.getFrom(), p));
        return r.withIdentifier((Name) visit(r.getIdentifier(), p));
    }

    public Cobol visitRoundable(Cobol.Roundable roundable, P p) {
        Cobol.Roundable r = roundable;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.ROUNDABLE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withIdentifier((Identifier) visit(r.getIdentifier(), p));
        return r.withRounded((Cobol.Word) visit(r.getRounded(), p));
    }

    public Cobol visitSameClause(Cobol.SameClause sameClause, P p) {
        Cobol.SameClause s = sameClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SAME_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withFileNames(ListUtils.map(s.getFileNames(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitScreenDescriptionAutoClause(Cobol.ScreenDescriptionAutoClause screenDescriptionAutoClause, P p) {
        Cobol.ScreenDescriptionAutoClause s = screenDescriptionAutoClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_AUTO_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withAuto((Cobol.Word) visit(s.getAuto(), p));
    }

    public Cobol visitScreenDescriptionBackgroundColorClause(Cobol.ScreenDescriptionBackgroundColorClause screenDescriptionBackgroundColorClause, P p) {
        Cobol.ScreenDescriptionBackgroundColorClause s = screenDescriptionBackgroundColorClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_BACKGROUND_COLOR_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withBackground((Cobol.Word) visit(s.getBackground(), p));
        s = s.withIs((Cobol.Word) visit(s.getIs(), p));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionBellClause(Cobol.ScreenDescriptionBellClause screenDescriptionBellClause, P p) {
        Cobol.ScreenDescriptionBellClause s = screenDescriptionBellClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_BELL_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withBell((Cobol.Word) visit(s.getBell(), p));
    }

    public Cobol visitScreenDescriptionBlankClause(Cobol.ScreenDescriptionBlankClause screenDescriptionBlankClause, P p) {
        Cobol.ScreenDescriptionBlankClause s = screenDescriptionBlankClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_BLANK_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitScreenDescriptionBlankWhenZeroClause(Cobol.ScreenDescriptionBlankWhenZeroClause screenDescriptionBlankWhenZeroClause, P p) {
        Cobol.ScreenDescriptionBlankWhenZeroClause s = screenDescriptionBlankWhenZeroClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_BLANK_WHEN_ZERO_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitScreenDescriptionBlinkClause(Cobol.ScreenDescriptionBlinkClause screenDescriptionBlinkClause, P p) {
        Cobol.ScreenDescriptionBlinkClause s = screenDescriptionBlinkClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_BLINK_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withBlink((Cobol.Word) visit(s.getBlink(), p));
    }

    public Cobol visitScreenDescriptionColumnClause(Cobol.ScreenDescriptionColumnClause screenDescriptionColumnClause, P p) {
        Cobol.ScreenDescriptionColumnClause s = screenDescriptionColumnClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_COLUMN_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionControlClause(Cobol.ScreenDescriptionControlClause screenDescriptionControlClause, P p) {
        Cobol.ScreenDescriptionControlClause s = screenDescriptionControlClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_CONTROL_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionEntry(Cobol.ScreenDescriptionEntry screenDescriptionEntry, P p) {
        Cobol.ScreenDescriptionEntry s = screenDescriptionEntry;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_ENTRY_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWord((Cobol.Word) visit(s.getWord(), p));
        s = s.withName((Cobol.Word) visit(s.getName(), p));
        s = s.withClauses(ListUtils.map(s.getClauses(), it -> visit(it, p)));
        return s.withDot((Cobol.Word) visit(s.getDot(), p));
    }

    public Cobol visitScreenDescriptionEraseClause(Cobol.ScreenDescriptionEraseClause screenDescriptionEraseClause, P p) {
        Cobol.ScreenDescriptionEraseClause s = screenDescriptionEraseClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_ERASE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitScreenDescriptionForegroundColorClause(Cobol.ScreenDescriptionForegroundColorClause screenDescriptionForegroundColorClause, P p) {
        Cobol.ScreenDescriptionForegroundColorClause s = screenDescriptionForegroundColorClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_FOREGROUND_COLOR_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionFromClause(Cobol.ScreenDescriptionFromClause screenDescriptionFromClause, P p) {
        Cobol.ScreenDescriptionFromClause s = screenDescriptionFromClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_FROM_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withFrom((Cobol.Word) visit(s.getFrom(), p));
        s = s.withValue((Name) visit(s.getValue(), p));
        return s.withScreenDescriptionToClause((Cobol.ScreenDescriptionToClause) visit(s.getScreenDescriptionToClause(), p));
    }

    public Cobol visitScreenDescriptionFullClause(Cobol.ScreenDescriptionFullClause screenDescriptionFullClause, P p) {
        Cobol.ScreenDescriptionFullClause s = screenDescriptionFullClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_FULL_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWord((Cobol.Word) visit(s.getWord(), p));
    }

    public Cobol visitScreenDescriptionGridClause(Cobol.ScreenDescriptionGridClause screenDescriptionGridClause, P p) {
        Cobol.ScreenDescriptionGridClause s = screenDescriptionGridClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_GRID_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWord((Cobol.Word) visit(s.getWord(), p));
    }

    public Cobol visitScreenDescriptionJustifiedClause(Cobol.ScreenDescriptionJustifiedClause screenDescriptionJustifiedClause, P p) {
        Cobol.ScreenDescriptionJustifiedClause s = screenDescriptionJustifiedClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_JUSTIFIED_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitScreenDescriptionLightClause(Cobol.ScreenDescriptionLightClause screenDescriptionLightClause, P p) {
        Cobol.ScreenDescriptionLightClause s = screenDescriptionLightClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_LIGHT_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withLight((Cobol.Word) visit(s.getLight(), p));
    }

    public Cobol visitScreenDescriptionLineClause(Cobol.ScreenDescriptionLineClause screenDescriptionLineClause, P p) {
        Cobol.ScreenDescriptionLineClause s = screenDescriptionLineClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_LINE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionPictureClause(Cobol.ScreenDescriptionPictureClause screenDescriptionPictureClause, P p) {
        Cobol.ScreenDescriptionPictureClause s = screenDescriptionPictureClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_PICTURE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withPictureString((Cobol.PictureString) visit(s.getPictureString(), p));
    }

    public Cobol visitScreenDescriptionPromptClause(Cobol.ScreenDescriptionPromptClause screenDescriptionPromptClause, P p) {
        Cobol.ScreenDescriptionPromptClause s = screenDescriptionPromptClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_PROMPT_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        s = s.withName((Name) visit(s.getName(), p));
        return s.withScreenDescriptionPromptOccursClause((Cobol.ScreenDescriptionPromptOccursClause) visit(s.getScreenDescriptionPromptOccursClause(), p));
    }

    public Cobol visitScreenDescriptionPromptOccursClause(Cobol.ScreenDescriptionPromptOccursClause screenDescriptionPromptOccursClause, P p) {
        Cobol.ScreenDescriptionPromptOccursClause s = screenDescriptionPromptOccursClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_PROMPT_OCCURS_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withOccurs((Cobol.Word) visit(s.getOccurs(), p));
        s = s.withInteger((Cobol.Word) visit(s.getInteger(), p));
        return s.withTimes((Cobol.Word) visit(s.getTimes(), p));
    }

    public Cobol visitScreenDescriptionRequiredClause(Cobol.ScreenDescriptionRequiredClause screenDescriptionRequiredClause, P p) {
        Cobol.ScreenDescriptionRequiredClause s = screenDescriptionRequiredClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_REQUIRED_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withRequired((Cobol.Word) visit(s.getRequired(), p));
    }

    public Cobol visitScreenDescriptionReverseVideoClause(Cobol.ScreenDescriptionReverseVideoClause screenDescriptionReverseVideoClause, P p) {
        Cobol.ScreenDescriptionReverseVideoClause s = screenDescriptionReverseVideoClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_REVERSE_VIDEO_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWord((Cobol.Word) visit(s.getWord(), p));
    }

    public Cobol visitScreenDescriptionSecureClause(Cobol.ScreenDescriptionSecureClause screenDescriptionSecureClause, P p) {
        Cobol.ScreenDescriptionSecureClause s = screenDescriptionSecureClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_SECURE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWord((Cobol.Word) visit(s.getWord(), p));
    }

    public Cobol visitScreenDescriptionSignClause(Cobol.ScreenDescriptionSignClause screenDescriptionSignClause, P p) {
        Cobol.ScreenDescriptionSignClause s = screenDescriptionSignClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_SIGN_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitScreenDescriptionSizeClause(Cobol.ScreenDescriptionSizeClause screenDescriptionSizeClause, P p) {
        Cobol.ScreenDescriptionSizeClause s = screenDescriptionSizeClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_SIZE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionToClause(Cobol.ScreenDescriptionToClause screenDescriptionToClause, P p) {
        Cobol.ScreenDescriptionToClause s = screenDescriptionToClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_TO_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withTo((Cobol.Word) visit(s.getTo(), p));
        return s.withIdentifier((Identifier) visit(s.getIdentifier(), p));
    }

    public Cobol visitScreenDescriptionUnderlineClause(Cobol.ScreenDescriptionUnderlineClause screenDescriptionUnderlineClause, P p) {
        Cobol.ScreenDescriptionUnderlineClause s = screenDescriptionUnderlineClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_UNDERLINE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withUnderline((Cobol.Word) visit(s.getUnderline(), p));
    }

    public Cobol visitScreenDescriptionUsageClause(Cobol.ScreenDescriptionUsageClause screenDescriptionUsageClause, P p) {
        Cobol.ScreenDescriptionUsageClause s = screenDescriptionUsageClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_USAGE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitScreenDescriptionUsingClause(Cobol.ScreenDescriptionUsingClause screenDescriptionUsingClause, P p) {
        Cobol.ScreenDescriptionUsingClause s = screenDescriptionUsingClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_USING_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withUsing((Cobol.Word) visit(s.getUsing(), p));
        return s.withIdentifier((Identifier) visit(s.getIdentifier(), p));
    }

    public Cobol visitScreenDescriptionValueClause(Cobol.ScreenDescriptionValueClause screenDescriptionValueClause, P p) {
        Cobol.ScreenDescriptionValueClause s = screenDescriptionValueClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_VALUE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitScreenDescriptionZeroFillClause(Cobol.ScreenDescriptionZeroFillClause screenDescriptionZeroFillClause, P p) {
        Cobol.ScreenDescriptionZeroFillClause s = screenDescriptionZeroFillClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_DESCRIPTION_ZERO_FILL_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        return s.withWord((Cobol.Word) visit(s.getWord(), p));
    }

    public Cobol visitScreenSection(Cobol.ScreenSection screenSection, P p) {
        Cobol.ScreenSection s = screenSection;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SCREEN_SECTION_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        s = s.withDot((Cobol.Word) visit(s.getDot(), p));
        return s.withDescriptions(ListUtils.map(s.getDescriptions(), it -> (Cobol.ScreenDescriptionEntry) visit(it, p)));
    }

    public Cobol visitSearch(Cobol.Search search, P p) {
        Cobol.Search s = search;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEARCH_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        s = s.withQualifiedDataName((Cobol.QualifiedDataName) visit(s.getQualifiedDataName(), p));
        s = s.withSearchVarying((Cobol.SearchVarying) visit(s.getSearchVarying(), p));
        s = s.withAtEndPhrase((Cobol.StatementPhrase) visit(s.getAtEndPhrase(), p));
        s = s.withSearchWhen(ListUtils.map(s.getSearchWhen(), t -> (Cobol.SearchWhen) visit(t, p)));
        return s.withEndSearch((Cobol.Word) visit(s.getEndSearch(), p));
    }

    public Cobol visitSearchVarying(Cobol.SearchVarying searchVarying, P p) {
        Cobol.SearchVarying s = searchVarying;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEARCH_VARYING_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withVarying((Cobol.Word) visit(s.getVarying(), p));
        return s.withQualifiedDataName((Cobol.QualifiedDataName) visit(s.getQualifiedDataName(), p));
    }

    public Cobol visitSearchWhen(Cobol.SearchWhen searchWhen, P p) {
        Cobol.SearchWhen s = searchWhen;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEARCH_WHEN_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWhen((Cobol.Word) visit(s.getWhen(), p));
        s = s.withCondition((Cobol.Condition) visit(s.getCondition(), p));
        s = s.withNextSentence(ListUtils.map(s.getNextSentence(), it -> (Cobol.Word) visit(it, p)));
        return s.withStatements(ListUtils.map(s.getStatements(), t -> (Statement) visit(t, p)));
    }

    public Cobol visitSelectClause(Cobol.SelectClause selectClause, P p) {
        Cobol.SelectClause s = selectClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEARCH_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withFileName((Cobol.Word) visit(s.getFileName(), p));
    }

    public Cobol visitSend(Cobol.Send send, P p) {
        Cobol.Send s = send;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEND_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSend((Cobol.Word) visit(s.getSend(), p));
        s = s.withStatement(visit(s.getStatement(), p));
        s = s.withOnExceptionClause((Cobol.StatementPhrase) visit(s.getOnExceptionClause(), p));
        return s.withNotOnExceptionClause((Cobol.StatementPhrase) visit(s.getNotOnExceptionClause(), p));
    }

    public Cobol visitSendAdvancingLines(Cobol.SendAdvancingLines sendAdvancingLines, P p) {
        Cobol.SendAdvancingLines s = sendAdvancingLines;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEND_ADVANCING_LINES_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withName((Name) visit(s.getName(), p));
        return s.withLines((Cobol.Word) visit(s.getLines(), p));
    }

    public Cobol visitSendPhrase(Cobol.SendPhrase sendPhrase, P p) {
        Cobol.SendPhrase s = sendPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEND_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withTarget(visit(s.getTarget(), p));
    }

    public Cobol visitSendStatementSync(Cobol.SendStatementSync sendStatementSync, P p) {
        Cobol.SendStatementSync s = sendStatementSync;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SEND_STATEMENT_SYNC_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withName((Name) visit(s.getName(), p));
        s = s.withSendFromPhrase((Cobol.SendPhrase) visit(s.getSendFromPhrase(), p));
        s = s.withSendWithPhrase((Cobol.SendPhrase) visit(s.getSendWithPhrase(), p));
        s = s.withSendReplacingPhrase((Cobol.SendPhrase) visit(s.getSendReplacingPhrase(), p));
        return s.withSendAdvancingPhrase((Cobol.SendPhrase) visit(s.getSendAdvancingPhrase(), p));
    }

    public Cobol visitSentence(Cobol.Sentence sentence, P p) {
        Cobol.Sentence s = sentence;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SENTENCE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withStatements(ListUtils.map(s.getStatements(), t -> (Statement) visit(t, p)));
        return s.withDot((Cobol.Word) visit(s.getDot(), p));
    }

    public Cobol visitSet(Cobol.Set set, P p) {
        Cobol.Set s = set;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SET_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSet((Cobol.Word) visit(s.getSet(), p));
        s = s.withTo(ListUtils.map(s.getTo(), t -> (Cobol.SetTo) visit(t, p)));
        return s.withUpDown((Cobol.SetUpDown) visit(s.getUpDown(), p));
    }

    public Cobol visitSetTo(Cobol.SetTo setTo, P p) {
        Cobol.SetTo s = setTo;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SET_TO_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withIdentifiers(ListUtils.map(s.getIdentifiers(), t -> visit(t, p)));
        s = s.withTo((Cobol.Word) visit(s.getTo(), p));
        return s.withValues(ListUtils.map(s.getValues(), t -> visit(t, p)));
    }

    public Cobol visitSetUpDown(Cobol.SetUpDown setUpDown, P p) {
        Cobol.SetUpDown s = setUpDown;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SET_UP_DOWN_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withTo(ListUtils.map(s.getTo(), t -> visit(t, p)));
        s = s.withOperation(ListUtils.map(s.getOperation(), it -> (Cobol.Word) visit(it, p)));
        return s.withValue((Name) visit(s.getValue(), p));
    }

    public Cobol visitSort(Cobol.Sort sort, P p) {
        Cobol.Sort s = sort;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SORT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSort((Cobol.Word) visit(s.getSort(), p));
        s = s.withFileName((Cobol.Word) visit(s.getFileName(), p));
        s = s.withSortOnKeyClause(ListUtils.map(s.getSortOnKeyClause(), t -> (Cobol.Sortable) visit(t, p)));
        s = s.withSortDuplicatesPhrase((Cobol.Sortable) visit(s.getSortDuplicatesPhrase(), p));
        s = s.withSortCollatingSequencePhrase((Cobol.SortCollatingSequencePhrase) visit(s.getSortCollatingSequencePhrase(), p));
        s = s.withSortInputProcedurePhrase((Cobol.SortProcedurePhrase) visit(s.getSortInputProcedurePhrase(), p));
        s = s.withSortUsing(ListUtils.map(s.getSortUsing(), t -> (Cobol.Sortable) visit(t, p)));
        s = s.withSortOutputProcedurePhrase((Cobol.SortProcedurePhrase) visit(s.getSortOutputProcedurePhrase(), p));
        return s.withSortGiving(ListUtils.map(s.getSortGiving(), t -> (Cobol.Sortable) visit(t, p)));
    }

    public Cobol visitSortCollatingSequencePhrase(Cobol.SortCollatingSequencePhrase sortCollatingSequencePhrase, P p) {
        Cobol.SortCollatingSequencePhrase s = sortCollatingSequencePhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SORT_COLLATING_SEQUENCE_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        s = s.withAlphabetNames(ListUtils.map(s.getAlphabetNames(), t -> (Cobol.Word) visit(t, p)));
        s = s.withSortCollatingAlphanumeric((Cobol.Sortable) visit(s.getSortCollatingAlphanumeric(), p));
        return s.withSortCollatingNational((Cobol.Sortable) visit(s.getSortCollatingNational(), p));
    }

    public Cobol visitSortGiving(Cobol.SortGiving sortGiving, P p) {
        Cobol.SortGiving s = sortGiving;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SORT_GIVING_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withFileName((Cobol.Word) visit(s.getFileName(), p));
        return s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
    }

    public Cobol visitSortProcedurePhrase(Cobol.SortProcedurePhrase sortProcedurePhrase, P p) {
        Cobol.SortProcedurePhrase s = sortProcedurePhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SORT_PROCEDURE_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        s = s.withProcedureName((Name) visit(s.getProcedureName(), p));
        return s.withSortInputThrough((Cobol.Sortable) visit(s.getSortInputThrough(), p));
    }

    public Cobol visitSortable(Cobol.Sortable sortable, P p) {
        Cobol.Sortable s = sortable;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SORTABLE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), t -> (Cobol.Word) visit(t, p)));
        return s.withNames(ListUtils.map(s.getNames(), t -> visit(t, p)));
    }

    public Cobol visitSourceComputer(Cobol.SourceComputer sourceComputer, P p) {
        Cobol.SourceComputer s = sourceComputer;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SOURCE_COMPUTER_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withComputer((Cobol.SourceComputerDefinition) visit(s.getComputer(), p));
    }

    public Cobol visitSourceComputerDefinition(Cobol.SourceComputerDefinition sourceComputerDefinition, P p) {
        Cobol.SourceComputerDefinition s = sourceComputerDefinition;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SOURCE_COMPUTER_DEFINITION_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withComputerName((Cobol.Word) visit(s.getComputerName(), p));
        s = s.withDebuggingMode(ListUtils.map(s.getDebuggingMode(), it -> (Cobol.Word) visit(it, p)));
        return s.withDot((Cobol.Word) visit(s.getDot(), p));
    }

    public Cobol visitSpecialNames(Cobol.SpecialNames specialNames, P p) {
        Cobol.SpecialNames s = specialNames;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SPECIAL_NAMES_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWord((Cobol.Word) visit(s.getWord(), p));
        s = s.withDot((Cobol.Word) visit(s.getDot(), p));
        s = s.withClauses(ListUtils.map(s.getClauses(), it -> visit(it, p)));
        return s.withDot2((Cobol.Word) visit(s.getDot2(), p));
    }

    public Cobol visitSpecialRegister(Cobol.SpecialRegister specialRegister, P p) {
        Cobol.SpecialRegister s = specialRegister;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SPECIAL_REGISTER_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> visit(it, p)));
        return s.withIdentifier((Name) visit(s.getIdentifier(), p));
    }

    public Cobol visitStart(Cobol.Start start, P p) {
        Cobol.Start s = start;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.START_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withStart((Cobol.Word) visit(s.getStart(), p));
        s = s.withFileName((Cobol.Word) visit(s.getFileName(), p));
        s = s.withStartKey((Cobol.StartKey) visit(s.getStartKey(), p));
        s = s.withInvalidKeyPhrase((Cobol.StatementPhrase) visit(s.getInvalidKeyPhrase(), p));
        s = s.withNotInvalidKeyPhrase((Cobol.StatementPhrase) visit(s.getNotInvalidKeyPhrase(), p));
        return s.withEndStart((Cobol.Word) visit(s.getEndStart(), p));
    }

    public Cobol visitStartKey(Cobol.StartKey startKey, P p) {
        Cobol.StartKey s = startKey;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.START_KEY_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withQualifiedDataName((Cobol.QualifiedDataName) visit(s.getQualifiedDataName(), p));
    }

    public Cobol visitStatementPhrase(Cobol.StatementPhrase statementPhrase, P p) {
        Cobol.StatementPhrase s = statementPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STATEMENT_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withPhrases(ListUtils.map(s.getPhrases(), it -> (Cobol.Word) visit(it, p)));
        return s.withStatements(ListUtils.map(s.getStatements(), t -> (Statement) visit(t, p)));
    }

    public Cobol visitStatusKeyClause(Cobol.StatusKeyClause statusKeyClause, P p) {
        Cobol.StatusKeyClause s = statusKeyClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STATUS_KEY_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withName((Cobol.Word) visit(s.getName(), p));
    }

    public Cobol visitStop(Cobol.Stop stop, P p) {
        Cobol.Stop s = stop;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STOP_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withStatement(visit(s.getStatement(), p));
    }

    public Cobol visitStopStatementGiving(Cobol.StopStatementGiving stopStatementGiving, P p) {
        Cobol.StopStatementGiving s = stopStatementGiving;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STOP_STATEMENT_GIVING_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withName((Cobol.Word) visit(s.getName(), p));
    }

    public Cobol visitStringDelimitedByPhrase(Cobol.StringDelimitedByPhrase stringDelimitedByPhrase, P p) {
        Cobol.StringDelimitedByPhrase s = stringDelimitedByPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STRING_DELIMITED_BY_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withIdentifier((Name) visit(s.getIdentifier(), p));
    }

    public Cobol visitStringForPhrase(Cobol.StringForPhrase stringForPhrase, P p) {
        Cobol.StringForPhrase s = stringForPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STRING_FOR_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWord((Cobol.Word) visit(s.getWord(), p));
        return s.withIdentifier((Name) visit(s.getIdentifier(), p));
    }

    public Cobol visitStringIntoPhrase(Cobol.StringIntoPhrase stringIntoPhrase, P p) {
        Cobol.StringIntoPhrase s = stringIntoPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STRING_INTO_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withInto((Cobol.Word) visit(s.getInto(), p));
        return s.withIdentifier((Identifier) visit(s.getIdentifier(), p));
    }

    public Cobol visitStringSendingPhrase(Cobol.StringSendingPhrase stringSendingPhrase, P p) {
        Cobol.StringSendingPhrase s = stringSendingPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STRING_SENDING_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSendings(ListUtils.map(s.getSendings(), t -> visit(t, p)));
        return s.withPhrase(visit(s.getPhrase(), p));
    }

    public Cobol visitStringStatement(Cobol.StringStatement stringStatement, P p) {
        Cobol.StringStatement s = stringStatement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STRING_STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withString((Cobol.Word) visit(s.getString(), p));
        s = s.withStringSendingPhrases(ListUtils.map(s.getStringSendingPhrases(), t -> visit(t, p)));
        s = s.withStringIntoPhrase((Cobol.StringIntoPhrase) visit(s.getStringIntoPhrase(), p));
        s = s.withStringWithPointerPhrase((Cobol.StringWithPointerPhrase) visit(s.getStringWithPointerPhrase(), p));
        s = s.withOnOverflowPhrase((Cobol.StatementPhrase) visit(s.getOnOverflowPhrase(), p));
        s = s.withNotOnOverflowPhrase((Cobol.StatementPhrase) visit(s.getNotOnOverflowPhrase(), p));
        return s.withEndString((Cobol.Word) visit(s.getEndString(), p));
    }

    public Cobol visitStringWithPointerPhrase(Cobol.StringWithPointerPhrase stringWithPointerPhrase, P p) {
        Cobol.StringWithPointerPhrase s = stringWithPointerPhrase;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STRING_WITH_POINTER_PHRASE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withQualifiedDataName((Cobol.QualifiedDataName) visit(s.getQualifiedDataName(), p));
    }

    public Cobol visitSubscript(Cobol.Subscript subscript, P p) {
        Cobol.Subscript s = subscript;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SUBSCRIPT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withFirst(visit(s.getFirst(), p));
        return s.withSecond((Cobol.Word) visit(s.getSecond(), p));
    }

    public Cobol visitSubtract(Cobol.Subtract subtract, P p) {
        Cobol.Subtract s = subtract;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SUBTRACT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSubstract((Cobol.Word) visit(s.getSubstract(), p));
        s = s.withOperation(visit(s.getOperation(), p));
        s = s.withOnSizeErrorPhrase((Cobol.StatementPhrase) visit(s.getOnSizeErrorPhrase(), p));
        s = s.withNotOnSizeErrorPhrase((Cobol.StatementPhrase) visit(s.getNotOnSizeErrorPhrase(), p));
        return s.withEndSubtract((Cobol.Word) visit(s.getEndSubtract(), p));
    }

    public Cobol visitSubtractCorrespondingStatement(Cobol.SubtractCorrespondingStatement subtractCorrespondingStatement, P p) {
        Cobol.SubtractCorrespondingStatement s = subtractCorrespondingStatement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SUBTRACT_CORRESPONDING_STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withCorresponding((Cobol.Word) visit(s.getCorresponding(), p));
        s = s.withQualifiedDataName((Cobol.QualifiedDataName) visit(s.getQualifiedDataName(), p));
        s = s.withFrom((Cobol.Word) visit(s.getFrom(), p));
        return s.withSubtractMinuendCorresponding((Cobol.SubtractMinuendCorresponding) visit(s.getSubtractMinuendCorresponding(), p));
    }

    public Cobol visitSubtractFromGivingStatement(Cobol.SubtractFromGivingStatement subtractFromGivingStatement, P p) {
        Cobol.SubtractFromGivingStatement s = subtractFromGivingStatement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SUBTRACT_FROM_GIVING_STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSubtractSubtrahend(ListUtils.map(s.getSubtractSubtrahend(), t -> (Name) visit(t, p)));
        s = s.withFrom((Cobol.Word) visit(s.getFrom(), p));
        s = s.withSubtractMinuendGiving((Name) visit(s.getSubtractMinuendGiving(), p));
        s = s.withGiving((Cobol.Word) visit(s.getGiving(), p));
        return s.withSubtractGiving(ListUtils.map(s.getSubtractGiving(), t -> (Cobol.Roundable) visit(t, p)));
    }

    public Cobol visitSubtractFromStatement(Cobol.SubtractFromStatement subtractFromStatement, P p) {
        Cobol.SubtractFromStatement s = subtractFromStatement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SUBTRACT_FROM_STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSubtractSubtrahend(ListUtils.map(s.getSubtractSubtrahend(), t -> (Name) visit(t, p)));
        s = s.withFrom((Cobol.Word) visit(s.getFrom(), p));
        return s.withSubtractMinuend(ListUtils.map(s.getSubtractMinuend(), t -> (Cobol.Roundable) visit(t, p)));
    }

    public Cobol visitSubtractMinuendCorresponding(Cobol.SubtractMinuendCorresponding subtractMinuendCorresponding, P p) {
        Cobol.SubtractMinuendCorresponding s = subtractMinuendCorresponding;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SUBTRACT_MINUEND_CORRESPONDING_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withQualifiedDataName((Cobol.QualifiedDataName) visit(s.getQualifiedDataName(), p));
        return s.withRounded((Cobol.Word) visit(s.getRounded(), p));
    }

    public Cobol visitSymbolicCharacter(Cobol.SymbolicCharacter symbolicCharacter, P p) {
        Cobol.SymbolicCharacter s = symbolicCharacter;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_CHARACTER_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withSymbols(ListUtils.map(s.getSymbols(), t -> (Cobol.Word) visit(t, p)));
        s = s.withWord((Cobol.Word) visit(s.getWord(), p));
        return s.withLiterals(ListUtils.map(s.getLiterals(), t -> (Cobol.Word) visit(t, p)));
    }

    public Cobol visitSymbolicCharactersClause(Cobol.SymbolicCharactersClause symbolicCharactersClause, P p) {
        Cobol.SymbolicCharactersClause s = symbolicCharactersClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_CHARACTERS_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        s = s.withSymbols(ListUtils.map(s.getSymbols(), t -> (Cobol.SymbolicCharacter) visit(t, p)));
        s = s.withInAlphabet((Cobol.Word) visit(s.getInAlphabet(), p));
        return s.withAlphabetName((Identifier) visit(s.getAlphabetName(), p));
    }

    public Cobol visitSymbolicDestinationClause(Cobol.SymbolicDestinationClause symbolicDestinationClause, P p) {
        Cobol.SymbolicDestinationClause s = symbolicDestinationClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_DESTINATION_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withDataDescName((Cobol.Word) visit(s.getDataDescName(), p));
    }

    public Cobol visitSymbolicQueueClause(Cobol.SymbolicQueueClause symbolicQueueClause, P p) {
        Cobol.SymbolicQueueClause s = symbolicQueueClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_QUEUE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withDataDescName((Cobol.Word) visit(s.getDataDescName(), p));
    }

    public Cobol visitSymbolicSourceClause(Cobol.SymbolicSourceClause symbolicSourceClause, P p) {
        Cobol.SymbolicSourceClause s = symbolicSourceClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_SOURCE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withDataDescName((Cobol.Word) visit(s.getDataDescName(), p));
    }

    public Cobol visitSymbolicSubQueueClause(Cobol.SymbolicSubQueueClause symbolicSubQueueClause, P p) {
        Cobol.SymbolicSubQueueClause s = symbolicSubQueueClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_SUB_QUEUE_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withDataDescName((Cobol.Word) visit(s.getDataDescName(), p));
    }

    public Cobol visitSymbolicTerminalClause(Cobol.SymbolicTerminalClause symbolicTerminalClause, P p) {
        Cobol.SymbolicTerminalClause s = symbolicTerminalClause;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SYMBOLIC_TERMINAL_CLAUSE_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWords(ListUtils.map(s.getWords(), it -> (Cobol.Word) visit(it, p)));
        return s.withDataDescName((Cobol.Word) visit(s.getDataDescName(), p));
    }

    public Cobol visitTableCall(Cobol.TableCall tableCall, P p) {
        Cobol.TableCall t = tableCall;
        t = t.withPrefix(visitSpace(t.getPrefix(), Space.Location.TABLE_CLAUSE_PREFIX, p));
        t = t.withMarkers(visitMarkers(t.getMarkers(), p));
        t = t.withQualifiedDataName((Cobol.QualifiedDataName) visit(t.getQualifiedDataName(), p));
        t = t.withSubscripts(ListUtils.map(t.getSubscripts(), it -> (Cobol.Parenthesized) visit(it, p)));
        return t.withReferenceModifier((Cobol.ReferenceModifier) visit(t.getReferenceModifier(), p));
    }

    public Cobol visitTerminate(Cobol.Terminate terminate, P p) {
        Cobol.Terminate t = terminate;
        t = t.withPrefix(visitSpace(t.getPrefix(), Space.Location.TERMINATE_PREFIX, p));
        t = t.withMarkers(visitMarkers(t.getMarkers(), p));
        t = t.withTerminate((Cobol.Word) visit(t.getTerminate(), p));
        return t.withReportName((Cobol.QualifiedDataName) visit(t.getReportName(), p));
    }

    public Cobol visitTextLengthClause(Cobol.TextLengthClause textLengthClause, P p) {
        Cobol.TextLengthClause t = textLengthClause;
        t = t.withPrefix(visitSpace(t.getPrefix(), Space.Location.TEXT_LENGTH_CLAUSE_PREFIX, p));
        t = t.withMarkers(visitMarkers(t.getMarkers(), p));
        t = t.withWords(ListUtils.map(t.getWords(), it -> (Cobol.Word) visit(it, p)));
        return t.withDataDescName((Cobol.Word) visit(t.getDataDescName(), p));
    }

    public Cobol visitUnString(Cobol.UnString unString, P p) {
        Cobol.UnString u = unString;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withUnstring((Cobol.Word) visit(u.getUnstring(), p));
        u = u.withUnstringSendingPhrase((Cobol.UnstringSendingPhrase) visit(u.getUnstringSendingPhrase(), p));
        u = u.withUnstringIntoPhrase((Cobol.UnstringIntoPhrase) visit(u.getUnstringIntoPhrase(), p));
        u = u.withUnstringWithPointerPhrase((Cobol.UnstringWithPointerPhrase) visit(u.getUnstringWithPointerPhrase(), p));
        u = u.withUnstringTallyingPhrase((Cobol.UnstringTallyingPhrase) visit(u.getUnstringTallyingPhrase(), p));
        u = u.withOnOverflowPhrase((Cobol.StatementPhrase) visit(u.getOnOverflowPhrase(), p));
        u = u.withNotOnOverflowPhrase((Cobol.StatementPhrase) visit(u.getNotOnOverflowPhrase(), p));
        return u.withEndUnstring((Cobol.Word) visit(u.getEndUnstring(), p));
    }

    public Cobol visitUnstringCountIn(Cobol.UnstringCountIn unstringCountIn, P p) {
        Cobol.UnstringCountIn u = unstringCountIn;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_COUNT_IN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withIdentifier((Identifier) visit(u.getIdentifier(), p));
    }

    public Cobol visitUnstringDelimitedByPhrase(Cobol.UnstringDelimitedByPhrase unstringDelimitedByPhrase, P p) {
        Cobol.UnstringDelimitedByPhrase u = unstringDelimitedByPhrase;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_DELIMITED_BY_PHRASE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withName((Name) visit(u.getName(), p));
    }

    public Cobol visitUnstringDelimiterIn(Cobol.UnstringDelimiterIn unstringDelimiterIn, P p) {
        Cobol.UnstringDelimiterIn u = unstringDelimiterIn;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_DELIMITED_IN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withIdentifier((Identifier) visit(u.getIdentifier(), p));
    }

    public Cobol visitUnstringInto(Cobol.UnstringInto unstringInto, P p) {
        Cobol.UnstringInto u = unstringInto;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_INTO_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withIdentifier((Identifier) visit(u.getIdentifier(), p));
        u = u.withUnstringDelimiterIn((Cobol.UnstringDelimiterIn) visit(u.getUnstringDelimiterIn(), p));
        return u.withUnstringCountIn((Cobol.UnstringCountIn) visit(u.getUnstringCountIn(), p));
    }

    public Cobol visitUnstringIntoPhrase(Cobol.UnstringIntoPhrase unstringIntoPhrase, P p) {
        Cobol.UnstringIntoPhrase u = unstringIntoPhrase;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_INTO_PHRASE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withInto((Cobol.Word) visit(u.getInto(), p));
        return u.withUnstringIntos(ListUtils.map(u.getUnstringIntos(), t -> (Cobol.UnstringInto) visit(t, p)));
    }

    public Cobol visitUnstringOrAllPhrase(Cobol.UnstringOrAllPhrase unstringOrAllPhrase, P p) {
        Cobol.UnstringOrAllPhrase u = unstringOrAllPhrase;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_OR_ALL_PHRASE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withName((Name) visit(u.getName(), p));
    }

    public Cobol visitUnstringSendingPhrase(Cobol.UnstringSendingPhrase unstringSendingPhrase, P p) {
        Cobol.UnstringSendingPhrase u = unstringSendingPhrase;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_SENDING_PHRASE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withIdentifier((Identifier) visit(u.getIdentifier(), p));
        u = u.withUnstringDelimitedByPhrase((Cobol.UnstringDelimitedByPhrase) visit(u.getUnstringDelimitedByPhrase(), p));
        return u.withUnstringOrAllPhrases(ListUtils.map(u.getUnstringOrAllPhrases(), t -> (Cobol.UnstringOrAllPhrase) visit(t, p)));
    }

    public Cobol visitUnstringTallyingPhrase(Cobol.UnstringTallyingPhrase unstringTallyingPhrase, P p) {
        Cobol.UnstringTallyingPhrase u = unstringTallyingPhrase;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_TALLYING_PHRASE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withQualifiedDataName((Cobol.QualifiedDataName) visit(u.getQualifiedDataName(), p));
    }

    public Cobol visitUnstringWithPointerPhrase(Cobol.UnstringWithPointerPhrase unstringWithPointerPhrase, P p) {
        Cobol.UnstringWithPointerPhrase u = unstringWithPointerPhrase;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNSTRING_WITH_POINTER_PHRASE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withQualifiedDataName((Cobol.QualifiedDataName) visit(u.getQualifiedDataName(), p));
    }

    public Cobol visitUseAfterClause(Cobol.UseAfterClause useAfterClause, P p) {
        Cobol.UseAfterClause u = useAfterClause;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.USE_AFTER_CLAUSE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withUseAfterOn((Cobol.UseAfterOn) visit(u.getUseAfterOn(), p));
    }

    public Cobol visitUseAfterOn(Cobol.UseAfterOn useAfterOn, P p) {
        Cobol.UseAfterOn u = useAfterOn;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.USE_AFTER_ON_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withAfterOn(ListUtils.map(u.getAfterOn(), it -> (Cobol.Word) visit(it, p)));
        return u.withFileNames(ListUtils.map(u.getFileNames(), t -> (Name) visit(t, p)));
    }

    public Cobol visitUseDebugClause(Cobol.UseDebugClause useDebugClause, P p) {
        Cobol.UseDebugClause u = useDebugClause;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.USE_DEBUG_CLAUSE_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withUseDebugs(ListUtils.map(u.getUseDebugs(), t -> (Cobol.UseDebugOn) visit(t, p)));
    }

    public Cobol visitUseDebugOn(Cobol.UseDebugOn useDebugOn, P p) {
        Cobol.UseDebugOn u = useDebugOn;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.USE_DEBUG_ON_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWords(ListUtils.map(u.getWords(), it -> (Cobol.Word) visit(it, p)));
        return u.withName(visit(u.getName(), p));
    }

    public Cobol visitUseStatement(Cobol.UseStatement useStatement, P p) {
        Cobol.UseStatement u = useStatement;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.USE_STATEMENT_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withUse((Cobol.Word) visit(u.getUse(), p));
        return u.withClause(visit(u.getClause(), p));
    }

    public Cobol visitValueOfClause(Cobol.ValueOfClause valueOfClause, P p) {
        Cobol.ValueOfClause v = valueOfClause;
        v = v.withPrefix(visitSpace(v.getPrefix(), Space.Location.VALUE_OF_CLAUSE_PREFIX, p));
        v = v.withMarkers(visitMarkers(v.getMarkers(), p));
        v = v.withValueOf(ListUtils.map(v.getValueOf(), t -> (Cobol.Word) visit(t, p)));
        return v.withValuePairs(ListUtils.map(v.getValuePairs(), t -> (Cobol.ValuePair) visit(t, p)));
    }

    public Cobol visitValuePair(Cobol.ValuePair valuePair, P p) {
        Cobol.ValuePair v = valuePair;
        v = v.withPrefix(visitSpace(v.getPrefix(), Space.Location.VALUE_PAIR_PREFIX, p));
        v = v.withMarkers(visitMarkers(v.getMarkers(), p));
        v = v.withSystemName((Cobol.Word) visit(v.getSystemName(), p));
        v = v.withIs((Cobol.Word) visit(v.getIs(), p));
        return v.withName((Name) visit(v.getName(), p));
    }

    public Cobol visitValuedObjectComputerClause(Cobol.ValuedObjectComputerClause valuedObjectComputerClause, P p) {
        Cobol.ValuedObjectComputerClause v = valuedObjectComputerClause;
        v = v.withPrefix(visitSpace(v.getPrefix(), Space.Location.VALUE_OBJECT_COMPUTER_CLAUSE_PREFIX, p));
        v = v.withMarkers(visitMarkers(v.getMarkers(), p));
        v = v.withWords(ListUtils.map(v.getWords(), it -> (Cobol.Word) visit(it, p)));
        v = v.withValue(visit(v.getValue(), p));
        return v.withUnits((Cobol.Word) visit(v.getUnits(), p));
    }

    public Cobol visitWord(Cobol.Word word, P p) {
        Cobol.Word w = word;
        // Preprocessed COBOL preservation.
        w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), it -> getCobolPreprocessorVisitor().visitPreprocessorDirect(it, p)));

        if (word.getReplacement() != null) {
            if (word.getReplacement().getType() == Replacement.Type.EQUAL || word.getReplacement().getType() == Replacement.Type.REDUCTIVE) {
                w = w.withReplacement(w.getReplacement().withOriginalWords(
                        ListUtils.map(word.getReplacement().getOriginalWords(), it -> it.withOriginal(visitAndCast(it.getOriginal(), p)))));
            }
            return w;
        }

        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));

        w = w.withLines(ListUtils.map(w.getLines(), it -> visitLine(it, p)));
        w = w.withContinuation(visitContinuation(w.getContinuation(), p));

        // Column areas.
        w = w.withCommentArea(visitCommentArea(w.getCommentArea(), p));
        w = w.withIndicatorArea(visitIndicatorArea(w.getIndicatorArea(), p));
        return w.withSequenceArea(visitSequenceArea(w.getSequenceArea(), p));
    }

    public Cobol visitWorkingStorageSection(Cobol.WorkingStorageSection workingStorageSection, P p) {
        Cobol.WorkingStorageSection w = workingStorageSection;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORKING_STORAGE_SECTION_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        w = w.withWords(ListUtils.map(w.getWords(), it -> (Cobol.Word) visit(it, p)));
        w = w.withDot((Cobol.Word) visit(w.getDot(), p));
        return w.withDataDescriptions(ListUtils.map(w.getDataDescriptions(), it -> (Cobol.DataDescriptionEntry) visit(it, p)));
    }

    public Cobol visitWrite(Cobol.Write write, P p) {
        Cobol.Write w = write;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WRITE_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        w = w.withWrite((Cobol.Word) visit(w.getWrite(), p));
        w = w.withRecordName((Cobol.QualifiedDataName) visit(w.getRecordName(), p));
        w = w.withWriteFromPhrase((Cobol.WriteFromPhrase) visit(w.getWriteFromPhrase(), p));
        w = w.withWriteAdvancingPhrase((Cobol.WriteAdvancingPhrase) visit(w.getWriteAdvancingPhrase(), p));
        w = w.withWriteAtEndOfPagePhrase((Cobol.StatementPhrase) visit(w.getWriteAtEndOfPagePhrase(), p));
        w = w.withWriteNotAtEndOfPagePhrase((Cobol.StatementPhrase) visit(w.getWriteNotAtEndOfPagePhrase(), p));
        w = w.withInvalidKeyPhrase((Cobol.StatementPhrase) visit(w.getInvalidKeyPhrase(), p));
        w = w.withNotInvalidKeyPhrase((Cobol.StatementPhrase) visit(w.getNotInvalidKeyPhrase(), p));
        return w.withEndWrite((Cobol.Word) visit(w.getEndWrite(), p));
    }

    public Cobol visitWriteAdvancingLines(Cobol.WriteAdvancingLines writeAdvancingLines, P p) {
        Cobol.WriteAdvancingLines w = writeAdvancingLines;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WRITE_ADVANCING_LINES_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        w = w.withName((Name) visit(w.getName(), p));
        return w.withWord((Cobol.Word) visit(w.getWord(), p));
    }

    public Cobol visitWriteAdvancingMnemonic(Cobol.WriteAdvancingMnemonic writeAdvancingMnemonic, P p) {
        Cobol.WriteAdvancingMnemonic w = writeAdvancingMnemonic;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WRITE_ADVANCING_MNEMONIC_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        return w.withName((Name) visit(w.getName(), p));
    }

    public Cobol visitWriteAdvancingPage(Cobol.WriteAdvancingPage writeAdvancingPage, P p) {
        Cobol.WriteAdvancingPage w = writeAdvancingPage;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WRITE_ADVANCING_PAGE_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        return w.withPage((Cobol.Word) visit(w.getPage(), p));
    }

    public Cobol visitWriteAdvancingPhrase(Cobol.WriteAdvancingPhrase writeAdvancingPhrase, P p) {
        Cobol.WriteAdvancingPhrase w = writeAdvancingPhrase;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WRITE_ADVANCING_PHRASE_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        w = w.withWords(ListUtils.map(w.getWords(), it -> (Cobol.Word) visit(it, p)));
        return w.withWriteBy(visit(w.getWriteBy(), p));
    }

    public Cobol visitWriteFromPhrase(Cobol.WriteFromPhrase writeFromPhrase, P p) {
        Cobol.WriteFromPhrase w = writeFromPhrase;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WRITE_FROM_PHRASE_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        w = w.withFrom((Cobol.Word) visit(w.getFrom(), p));
        return w.withName((Name) visit(w.getName(), p));
    }

    public CobolLine visitLine(CobolLine line, P p) {
        CobolLine l = line;
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        SequenceArea sequenceArea = visitSequenceArea(l.getSequenceArea(), p);
        if (sequenceArea != null) {
            l = l.withSequenceArea(sequenceArea);
        }
        IndicatorArea indicatorArea = visitIndicatorArea(l.getIndicatorArea(), p);
        if (indicatorArea != null) {
            l = l.withIndicatorArea(indicatorArea);
        }
        CommentArea commentArea = visitCommentArea(l.getCommentArea(), p);
        if (commentArea != null) {
            l = l.withCommentArea(commentArea);
        }
        return l;
    }

	public @Nullable Continuation visitContinuation(@Nullable Continuation continuation, P p) {
        if (continuation == null) {
            return null;
        }
        org.openrewrite.cobol.tree.Continuation c = continuation;
        return c.withMarkers(visitMarkers(c.getMarkers(), p));
    }

	public @Nullable CommentArea visitCommentArea(@Nullable CommentArea commentArea, P p) {
        if (commentArea == null) {
            return null;
        }
        CommentArea c = commentArea;
        return c.withMarkers(visitMarkers(commentArea.getMarkers(), p));
    }

	public @Nullable IndicatorArea visitIndicatorArea(@Nullable IndicatorArea indicatorArea, P p) {
        if (indicatorArea == null) {
            return null;
        }
        IndicatorArea i = indicatorArea;
        return i.withMarkers(visitMarkers(i.getMarkers(), p));
    }

	public @Nullable SequenceArea visitSequenceArea(@Nullable SequenceArea sequenceArea, P p) {
        if (sequenceArea == null) {
            return null;
        }
        SequenceArea s = sequenceArea;
        return s.withMarkers(visitMarkers(s.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
