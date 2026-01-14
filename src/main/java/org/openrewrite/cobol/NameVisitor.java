/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.internal.ListUtils;

public class NameVisitor<P> extends CobolIsoVisitor<P> {

    @Override
    public Cobol.CommentEntry visitCommentEntry(Cobol.CommentEntry commentEntry, P p) {
        return commentEntry;
    }

    @Override
    public Cobol.Abbreviation visitAbbreviation(Cobol.Abbreviation abbreviation, P p) {
        Cobol.Abbreviation a = abbreviation;
        a = a.withArithmeticExpression(visitAndCast(a.getArithmeticExpression(), p));
        return a.withAbbreviation(visitAndCast(a.getAbbreviation(), p));
    }

    @Override
    public Cobol.Accept visitAccept(Cobol.Accept accept, P p) {
        Cobol.Accept a = accept;
        a = a.withIdentifier(visitAndCast(a.getIdentifier(), p));
        return a.withOperation(visitAndCast(a.getOperation(), p));
    }

    @Override
    public Cobol.AcceptFromDateStatement visitAcceptFromDateStatement(Cobol.AcceptFromDateStatement acceptFromDateStatement, P p) {
        return acceptFromDateStatement;
    }

    @Override
    public Cobol.AcceptFromEscapeKeyStatement visitAcceptFromEscapeKeyStatement(Cobol.AcceptFromEscapeKeyStatement acceptFromEscapeKeyStatement, P p) {
        return acceptFromEscapeKeyStatement;
    }

    @Override
    public Cobol.AcceptFromMnemonicStatement visitAcceptFromMnemonicStatement(Cobol.AcceptFromMnemonicStatement acceptFromMnemonicStatement, P p) {
        Cobol.AcceptFromMnemonicStatement a = acceptFromMnemonicStatement;
        return a.withMnemonicName(visitAndCast(a.getMnemonicName(), p));
    }

    @Override
    public Cobol.AcceptMessageCountStatement visitAcceptMessageCountStatement(Cobol.AcceptMessageCountStatement acceptMessageCountStatement, P p) {
        return acceptMessageCountStatement;
    }

    @Override
    public Cobol.AccessModeClause visitAccessModeClause(Cobol.AccessModeClause accessModeClause, P p) {
        return accessModeClause;
    }

    @Override
    public Cobol.Add visitAdd(Cobol.Add add, P p) {
        Cobol.Add a = add;
        a = a.withOperation(visitAndCast(a.getOperation(), p));
        a = a.withOnSizeError(visitAndCast(a.getOnSizeError(), p));
        return a.withNotOnSizeError(visitAndCast(a.getNotOnSizeError(), p));
    }

    @Override
    public Cobol.AddCorresponding visitAddCorresponding(Cobol.AddCorresponding addCorresponding, P p) {
        Cobol.AddCorresponding a = addCorresponding;
        a = a.withIdentifier(visitAndCast(a.getIdentifier(), p));
        return a.withRoundable(visitAndCast(a.getRoundable(), p));
    }

    @Override
    public Cobol.AddTo visitAddTo(Cobol.AddTo addTo, P p) {
        Cobol.AddTo a = addTo;
        a = a.withFrom(ListUtils.map(a.getFrom(), it -> visitAndCast(it, p)));
        return a.withRoundables(ListUtils.map(a.getRoundables(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AddToGiving visitAddToGiving(Cobol.AddToGiving addToGiving, P p) {
        Cobol.AddToGiving a = addToGiving;
        a = a.withFrom(ListUtils.map(a.getFrom(), it -> visitAndCast(it, p)));
        a = a.withNames(ListUtils.map(a.getNames(), it -> visitAndCast(it, p)));
        return a.withRoundables(ListUtils.map(a.getRoundables(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AlphabetAlso visitAlphabetAlso(Cobol.AlphabetAlso alphabetAlso, P p) {
        Cobol.AlphabetAlso a = alphabetAlso;
        return a.withLiterals(ListUtils.map(a.getLiterals(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AlphabetClause visitAlphabetClause(Cobol.AlphabetClause alphabetClause, P p) {
        Cobol.AlphabetClause a = alphabetClause;
        a = a.withName(visitAndCast(a.getName(), p));
        return a.withWords(ListUtils.map(a.getWords(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AlphabetLiteral visitAlphabetLiteral(Cobol.AlphabetLiteral alphabetLiteral, P p) {
        Cobol.AlphabetLiteral a = alphabetLiteral;
        a = a.withLiteral(visitAndCast(a.getLiteral(), p));
        a = a.withAlphabetThrough(visitAndCast(a.getAlphabetThrough(), p));
        return a.withAlphabetAlso(ListUtils.map(a.getAlphabetAlso(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AlphabetThrough visitAlphabetThrough(Cobol.AlphabetThrough alphabetThrough, P p) {
        Cobol.AlphabetThrough a = alphabetThrough;
        return a.withLiteral(visitAndCast(a.getLiteral(), p));
    }

    @Override
    public Cobol.AlteredGoTo visitAlteredGoTo(Cobol.AlteredGoTo alteredGoTo, P p) {
        return alteredGoTo;
    }

    @Override
    public Cobol.AlternateRecordKeyClause visitAlternateRecordKeyClause(Cobol.AlternateRecordKeyClause alternateRecordKeyClause, P p) {
        Cobol.AlternateRecordKeyClause a = alternateRecordKeyClause;
        a = a.withQualifiedDataName(visitAndCast(a.getQualifiedDataName(), p));
        return a.withPasswordClause(visitAndCast(a.getPasswordClause(), p));
    }

    @Override
    public Cobol.AlterProceedTo visitAlterProceedTo(Cobol.AlterProceedTo alterProceedTo, P p) {
        Cobol.AlterProceedTo a = alterProceedTo;
        a = a.withFrom(visitAndCast(a.getFrom(), p));
        return a.withTo(visitAndCast(a.getTo(), p));
    }

    @Override
    public Cobol.AlterStatement visitAlterStatement(Cobol.AlterStatement alterStatement, P p) {
        Cobol.AlterStatement a = alterStatement;
        return a.withAlterProceedTo(ListUtils.map(a.getAlterProceedTo(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AndOrCondition visitAndOrCondition(Cobol.AndOrCondition andOrCondition, P p) {
        Cobol.AndOrCondition a = andOrCondition;
        a = a.withCombinableCondition(visitAndCast(a.getCombinableCondition(), p));
        return a.withAbbreviations(ListUtils.map(a.getAbbreviations(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Argument visitArgument(Cobol.Argument argument, P p) {
        Cobol.Argument a = argument;
        a = a.withFirst(visitAndCast(a.getFirst(), p));
        return a.withIntegerLiteral(visitAndCast(a.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.ArithmeticExpression visitArithmeticExpression(Cobol.ArithmeticExpression arithmeticExpression, P p) {
        Cobol.ArithmeticExpression a = arithmeticExpression;
        a = a.withMultDivs(visitAndCast(a.getMultDivs(), p));
        return a.withPlusMinuses(ListUtils.map(a.getPlusMinuses(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.AssignClause visitAssignClause(Cobol.AssignClause assignClause, P p) {
        Cobol.AssignClause a = assignClause;
        return a.withName(visitAndCast(a.getName(), p));
    }

    @Override
    public Cobol.BlockContainsClause visitBlockContainsClause(Cobol.BlockContainsClause blockContainsClause, P p) {
        Cobol.BlockContainsClause b = blockContainsClause;
        b = b.withIntegerLiteral(visitAndCast(b.getIntegerLiteral(), p));
        return b.withBlockContainsTo(visitAndCast(b.getBlockContainsTo(), p));
    }

    @Override
    public Cobol.BlockContainsTo visitBlockContainsTo(Cobol.BlockContainsTo blockContainsTo, P p) {
        Cobol.BlockContainsTo b = blockContainsTo;
        return b.withIntegerLiteral(visitAndCast(b.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.Call visitCall(Cobol.Call call, P p) {
        Cobol.Call c = call;
        c = c.withIdentifier(visitAndCast(c.getIdentifier(), p));
        c = c.withCallUsingPhrase(visitAndCast(c.getCallUsingPhrase(), p));
        c = c.withCallGivingPhrase(visitAndCast(c.getCallGivingPhrase(), p));
        c = c.withOnOverflowPhrase(visitAndCast(c.getOnOverflowPhrase(), p));
        c = c.withOnExceptionClause(visitAndCast(c.getOnExceptionClause(), p));
        return c.withNotOnExceptionClause(visitAndCast(c.getNotOnExceptionClause(), p));
    }

    @Override
    public Cobol.CallBy visitCallBy(Cobol.CallBy callBy, P p) {
        Cobol.CallBy c = callBy;
        return c.withIdentifier(visitAndCast(c.getIdentifier(), p));
    }

    @Override
    public Cobol.CallGivingPhrase visitCallGivingPhrase(Cobol.CallGivingPhrase callGivingPhrase, P p) {
        Cobol.CallGivingPhrase c = callGivingPhrase;
        return c.withIdentifier(visitAndCast(c.getIdentifier(), p));
    }

    @Override
    public Cobol.CallPhrase visitCallPhrase(Cobol.CallPhrase callPhrase, P p) {
        Cobol.CallPhrase c = callPhrase;
        return c.withParameters(ListUtils.map(c.getParameters(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Cancel visitCancel(Cobol.Cancel cancel, P p) {
        Cobol.Cancel c = cancel;
        return c.withCancelCalls(ListUtils.map(c.getCancelCalls(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.CancelCall visitCancelCall(Cobol.CancelCall cancelCall, P p) {
        Cobol.CancelCall c = cancelCall;
        c = c.withLibraryName(visitAndCast(c.getLibraryName(), p));
        c = c.withIdentifier(visitAndCast(c.getIdentifier(), p));
        return c.withLiteral(visitAndCast(c.getLiteral(), p));
    }

    @Override
    public Cobol.ChannelClause visitChannelClause(Cobol.ChannelClause channelClause, P p) {
        Cobol.ChannelClause c = channelClause;
        c = c.withLiteral(visitAndCast(c.getLiteral(), p));
        return c.withMnemonicName(visitAndCast(c.getMnemonicName(), p));
    }

    @Override
    public Cobol.ClassClause visitClassClause(Cobol.ClassClause classClause, P p) {
        Cobol.ClassClause c = classClause;
        c = c.withClassName(visitAndCast(c.getClassName(), p));
        return c.withThroughs(ListUtils.map(c.getThroughs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ClassClauseThrough visitClassClauseThrough(Cobol.ClassClauseThrough classClauseThrough, P p) {
        Cobol.ClassClauseThrough c = classClauseThrough;
        c = c.withFrom(visitAndCast(c.getFrom(), p));
        return c.withTo(visitAndCast(c.getTo(), p));
    }

    @Override
    public Cobol.ClassCondition visitClassCondition(Cobol.ClassCondition classCondition, P p) {
        Cobol.ClassCondition c = classCondition;
        c = c.withName(visitAndCast(c.getName(), p));
        return c.withType(visitAndCast(c.getType(), p));
    }

    @Override
    public Cobol.Close visitClose(Cobol.Close close, P p) {
        Cobol.Close c = close;
        return c.withCloseFiles(ListUtils.map(c.getCloseFiles(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.CloseFile visitCloseFile(Cobol.CloseFile closeFile, P p) {
        Cobol.CloseFile c = closeFile;
        c = c.withFileName(visitAndCast(c.getFileName(), p));
        return c.withCloseStatement(visitAndCast(c.getCloseStatement(), p));
    }

    @Override
    public Cobol.ClosePortFileIOStatement visitClosePortFileIOStatement(Cobol.ClosePortFileIOStatement closePortFileIOStatement, P p) {
        Cobol.ClosePortFileIOStatement c = closePortFileIOStatement;
        return c.withClosePortFileIOUsing(ListUtils.map(c.getClosePortFileIOUsing(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ClosePortFileIOUsingAssociatedData visitClosePortFileIOUsingAssociatedData(Cobol.ClosePortFileIOUsingAssociatedData closePortFileIOUsingAssociatedData, P p) {
        Cobol.ClosePortFileIOUsingAssociatedData c = closePortFileIOUsingAssociatedData;
        return c.withIdentifier(visitAndCast(c.getIdentifier(), p));
    }

    @Override
    public Cobol.ClosePortFileIOUsingAssociatedDataLength visitClosePortFileIOUsingAssociatedDataLength(Cobol.ClosePortFileIOUsingAssociatedDataLength closePortFileIOUsingAssociatedDataLength, P p) {
        Cobol.ClosePortFileIOUsingAssociatedDataLength c = closePortFileIOUsingAssociatedDataLength;
        return c.withIdentifier(visitAndCast(c.getIdentifier(), p));
    }

    @Override
    public Cobol.ClosePortFileIOUsingCloseDisposition visitClosePortFileIOUsingCloseDisposition(Cobol.ClosePortFileIOUsingCloseDisposition closePortFileIOUsingCloseDisposition, P p) {
        return closePortFileIOUsingCloseDisposition;
    }

    @Override
    public Cobol.CloseReelUnitStatement visitCloseReelUnitStatement(Cobol.CloseReelUnitStatement closeReelUnitStatement, P p) {
        return closeReelUnitStatement;
    }

    @Override
    public Cobol.CloseRelativeStatement visitCloseRelativeStatement(Cobol.CloseRelativeStatement closeRelativeStatement, P p) {
        return closeRelativeStatement;
    }

    @Override
    public Cobol.CodeSetClause visitCodeSetClause(Cobol.CodeSetClause codeSetClause, P p) {
        Cobol.CodeSetClause c = codeSetClause;
        return c.withAlphabetName(visitAndCast(c.getAlphabetName(), p));
    }

    @Override
    public Cobol.CollatingSequenceAlphabet visitCollatingSequenceAlphabet(Cobol.CollatingSequenceAlphabet collatingSequenceAlphabet, P p) {
        Cobol.CollatingSequenceAlphabet c = collatingSequenceAlphabet;
        return c.withAlphabetName(visitAndCast(c.getAlphabetName(), p));
    }

    @Override
    public Cobol.CollatingSequenceClause visitCollatingSequenceClause(Cobol.CollatingSequenceClause collatingSequenceClause, P p) {
        Cobol.CollatingSequenceClause c = collatingSequenceClause;
        c = c.withAlphabetName(ListUtils.map(c.getAlphabetName(), it -> visitAndCast(it, p)));
        c = c.withAlphanumeric(visitAndCast(c.getAlphanumeric(), p));
        return c.withNational(visitAndCast(c.getNational(), p));
    }

    @Override
    public Cobol.CombinableCondition visitCombinableCondition(Cobol.CombinableCondition combinableCondition, P p) {
        Cobol.CombinableCondition c = combinableCondition;
        return c.withSimpleCondition(visitAndCast(c.getSimpleCondition(), p));
    }

    @Override
    public Cobol.CommitmentControlClause visitCommitmentControlClause(Cobol.CommitmentControlClause commitmentControlClause, P p) {
        Cobol.CommitmentControlClause c = commitmentControlClause;
        return c.withFileName(visitAndCast(c.getFileName(), p));
    }

    @Override
    public Cobol.CommunicationDescriptionEntryFormat1 visitCommunicationDescriptionEntryFormat1(Cobol.CommunicationDescriptionEntryFormat1 communicationDescriptionEntryFormat1, P p) {
        Cobol.CommunicationDescriptionEntryFormat1 c = communicationDescriptionEntryFormat1;
        c = c.withName(visitAndCast(c.getName(), p));
        return c.withInputs(ListUtils.map(c.getInputs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.CommunicationDescriptionEntryFormat2 visitCommunicationDescriptionEntryFormat2(Cobol.CommunicationDescriptionEntryFormat2 communicationDescriptionEntryFormat2, P p) {
        Cobol.CommunicationDescriptionEntryFormat2 c = communicationDescriptionEntryFormat2;
        c = c.withName(visitAndCast(c.getName(), p));
        return c.withOutputs(ListUtils.map(c.getOutputs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.CommunicationDescriptionEntryFormat3 visitCommunicationDescriptionEntryFormat3(Cobol.CommunicationDescriptionEntryFormat3 communicationDescriptionEntryFormat3, P p) {
        Cobol.CommunicationDescriptionEntryFormat3 c = communicationDescriptionEntryFormat3;
        c = c.withName(visitAndCast(c.getName(), p));
        return c.withInitialIOs(ListUtils.map(c.getInitialIOs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.CommunicationSection visitCommunicationSection(Cobol.CommunicationSection communicationSection, P p) {
        Cobol.CommunicationSection c = communicationSection;
        return c.withEntries(ListUtils.map(c.getEntries(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.CompilationUnit visitCompilationUnit(Cobol.CompilationUnit compilationUnit, P p) {
        return super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Cobol.Compute visitCompute(Cobol.Compute compute, P p) {
        Cobol.Compute c = compute;
        c = c.withRoundables(ListUtils.map(c.getRoundables(), it -> visitAndCast(it, p)));
        c = c.withArithmeticExpression(visitAndCast(c.getArithmeticExpression(), p));
        c = c.withOnSizeErrorPhrase(visitAndCast(c.getOnSizeErrorPhrase(), p));
        return c.withNotOnSizeErrorPhrase(visitAndCast(c.getNotOnSizeErrorPhrase(), p));
    }

    @Override
    public Cobol.Condition visitCondition(Cobol.Condition condition, P p) {
        Cobol.Condition c = condition;
        c = c.withCombinableCondition(visitAndCast(c.getCombinableCondition(), p));
        return c.withAndOrConditions(ListUtils.map(c.getAndOrConditions(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ConditionNameReference visitConditionNameReference(Cobol.ConditionNameReference conditionNameReference, P p) {
        Cobol.ConditionNameReference c = conditionNameReference;
        c = c.withName(visitAndCast(c.getName(), p));
        c = c.withInDatas(ListUtils.map(c.getInDatas(), it -> visitAndCast(it, p)));
        c = c.withInFile(visitAndCast(c.getInFile(), p));
        c = c.withReferences(ListUtils.map(c.getReferences(), it -> visitAndCast(it, p)));
        return c.withInMnemonics(ListUtils.map(c.getInMnemonics(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ConditionNameSubscriptReference visitConditionNameSubscriptReference(Cobol.ConditionNameSubscriptReference conditionNameSubscriptReference, P p) {
        Cobol.ConditionNameSubscriptReference c = conditionNameSubscriptReference;
        return c.withSubscripts(ListUtils.map(c.getSubscripts(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ConfigurationSection visitConfigurationSection(Cobol.ConfigurationSection configurationSection, P p) {
        Cobol.ConfigurationSection c = configurationSection;
        return c.withParagraphs(ListUtils.map(c.getParagraphs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Continue visitContinue(Cobol.Continue continuez, P p) {
        return continuez;
    }

    @Override
    public Cobol.CurrencyClause visitCurrencyClause(Cobol.CurrencyClause currencyClause, P p) {
        Cobol.CurrencyClause c = currencyClause;
        c = c.withLiteral(visitAndCast(c.getLiteral(), p));
        return c.withPictureSymbolLiteral(visitAndCast(c.getPictureSymbolLiteral(), p));
    }

    @Override
    public Cobol.DataAlignedClause visitDataAlignedClause(Cobol.DataAlignedClause dataAlignedClause, P p) {
        return dataAlignedClause;
    }

    @Override
    public Cobol.DataBaseSection visitDataBaseSection(Cobol.DataBaseSection dataBaseSection, P p) {
        Cobol.DataBaseSection d = dataBaseSection;
        return d.withEntries(ListUtils.map(d.getEntries(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataBaseSectionEntry visitDataBaseSectionEntry(Cobol.DataBaseSectionEntry dataBaseSectionEntry, P p) {
        Cobol.DataBaseSectionEntry d = dataBaseSectionEntry;
        d = d.withDb(visitAndCast(d.getDb(), p));
        d = d.withFrom(visitAndCast(d.getFrom(), p));
        return d.withTo(visitAndCast(d.getTo(), p));
    }

    @Override
    public Cobol.DataBlankWhenZeroClause visitDataBlankWhenZeroClause(Cobol.DataBlankWhenZeroClause dataBlankWhenZeroClause, P p) {
        return dataBlankWhenZeroClause;
    }

    @Override
    public Cobol.DataCommonOwnLocalClause visitDataCommonOwnLocalClause(Cobol.DataCommonOwnLocalClause dataCommonOwnLocalClause, P p) {
        return dataCommonOwnLocalClause;
    }

    @Override
    public Cobol.DataDescriptionEntry visitDataDescriptionEntry(Cobol.DataDescriptionEntry dataDescriptionEntry, P p) {
        Cobol.DataDescriptionEntry d = dataDescriptionEntry;
        d = d.withName(visitAndCast(d.getName(), p));
        return d.withClauses(ListUtils.map(d.getClauses(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataDivision visitDataDivision(Cobol.DataDivision dataDivision, P p) {
        Cobol.DataDivision d = dataDivision;
        return d.withSections(ListUtils.map(d.getSections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataExternalClause visitDataExternalClause(Cobol.DataExternalClause dataExternalClause, P p) {
        Cobol.DataExternalClause d = dataExternalClause;
        return d.withLiteral(visitAndCast(d.getLiteral(), p));
    }

    @Override
    public Cobol.DataGlobalClause visitDataGlobalClause(Cobol.DataGlobalClause dataGlobalClause, P p) {
        return dataGlobalClause;
    }

    @Override
    public Cobol.DataIntegerStringClause visitDataIntegerStringClause(Cobol.DataIntegerStringClause dataIntegerStringClause, P p) {
        return dataIntegerStringClause;
    }

    @Override
    public Cobol.DataJustifiedClause visitDataJustifiedClause(Cobol.DataJustifiedClause dataJustifiedClause, P p) {
        return dataJustifiedClause;
    }

    @Override
    public Cobol.DataOccursClause visitDataOccursClause(Cobol.DataOccursClause dataOccursClause, P p) {
        Cobol.DataOccursClause d = dataOccursClause;
        d = d.withName(visitAndCast(d.getName(), p));
        d = d.withDataOccursTo(visitAndCast(d.getDataOccursTo(), p));
        d = d.withDataOccursDepending(visitAndCast(d.getDataOccursDepending(), p));
        return d.withSortIndexed(ListUtils.map(d.getSortIndexed(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataOccursDepending visitDataOccursDepending(Cobol.DataOccursDepending dataOccursDepending, P p) {
        Cobol.DataOccursDepending d = dataOccursDepending;
        return d.withQualifiedDataName(visitAndCast(d.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.DataOccursIndexed visitDataOccursIndexed(Cobol.DataOccursIndexed dataOccursIndexed, P p) {
        Cobol.DataOccursIndexed d = dataOccursIndexed;
        return d.withIndexNames(ListUtils.map(d.getIndexNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataOccursSort visitDataOccursSort(Cobol.DataOccursSort dataOccursSort, P p) {
        Cobol.DataOccursSort d = dataOccursSort;
        return d.withQualifiedDataNames(ListUtils.map(d.getQualifiedDataNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataOccursTo visitDataOccursTo(Cobol.DataOccursTo dataOccursTo, P p) {
        Cobol.DataOccursTo d = dataOccursTo;
        return d.withIntegerLiteral(visitAndCast(d.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.DataPictureClause visitDataPictureClause(Cobol.DataPictureClause dataPictureClause, P p) {
        Cobol.DataPictureClause d = dataPictureClause;
        return d.withPictures(ListUtils.map(d.getPictures(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataReceivedByClause visitDataReceivedByClause(Cobol.DataReceivedByClause dataReceivedByClause, P p) {
        return dataReceivedByClause;
    }

    @Override
    public Cobol.DataRecordAreaClause visitDataRecordAreaClause(Cobol.DataRecordAreaClause dataRecordAreaClause, P p) {
        return dataRecordAreaClause;
    }

    @Override
    public Cobol.DataRecordsClause visitDataRecordsClause(Cobol.DataRecordsClause dataRecordsClause, P p) {
        Cobol.DataRecordsClause d = dataRecordsClause;
        return d.withDataName(ListUtils.map(d.getDataName(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataRedefinesClause visitDataRedefinesClause(Cobol.DataRedefinesClause dataRedefinesClause, P p) {
        Cobol.DataRedefinesClause d = dataRedefinesClause;
        return d.withDataName(visitAndCast(d.getDataName(), p));
    }

    @Override
    public Cobol.DataRenamesClause visitDataRenamesClause(Cobol.DataRenamesClause dataRenamesClause, P p) {
        Cobol.DataRenamesClause d = dataRenamesClause;
        d = d.withFromName(visitAndCast(d.getFromName(), p));
        return d.withToName(visitAndCast(d.getToName(), p));
    }

    @Override
    public Cobol.DataSignClause visitDataSignClause(Cobol.DataSignClause dataSignClause, P p) {
        return dataSignClause;
    }

    @Override
    public Cobol.DataSynchronizedClause visitDataSynchronizedClause(Cobol.DataSynchronizedClause dataSynchronizedClause, P p) {
        return dataSynchronizedClause;
    }

    @Override
    public Cobol.DataThreadLocalClause visitDataThreadLocalClause(Cobol.DataThreadLocalClause dataThreadLocalClause, P p) {
        return dataThreadLocalClause;
    }

    @Override
    public Cobol.DataTypeClause visitDataTypeClause(Cobol.DataTypeClause dataTypeClause, P p) {
        Cobol.DataTypeClause d = dataTypeClause;
        return d.withParenthesized(visitAndCast(d.getParenthesized(), p));
    }

    @Override
    public Cobol.DataTypeDefClause visitDataTypeDefClause(Cobol.DataTypeDefClause dataTypeDefClause, P p) {
        return dataTypeDefClause;
    }

    @Override
    public Cobol.DataUsageClause visitDataUsageClause(Cobol.DataUsageClause dataUsageClause, P p) {
        return dataUsageClause;
    }

    @Override
    public Cobol.DataUsingClause visitDataUsingClause(Cobol.DataUsingClause dataUsingClause, P p) {
        Cobol.DataUsingClause d = dataUsingClause;
        return d.withName(visitAndCast(d.getName(), p));
    }

    @Override
    public Cobol.DataValueClause visitDataValueClause(Cobol.DataValueClause dataValueClause, P p) {
        Cobol.DataValueClause d = dataValueClause;
        return d.withCobols(ListUtils.map(d.getCobols(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DataValueInterval visitDataValueInterval(Cobol.DataValueInterval dataValueInterval, P p) {
        Cobol.DataValueInterval d = dataValueInterval;
        d = d.withFrom(visitAndCast(d.getFrom(), p));
        return d.withTo(visitAndCast(d.getTo(), p));
    }

    @Override
    public Cobol.DataValueIntervalTo visitDataValueIntervalTo(Cobol.DataValueIntervalTo dataValueIntervalTo, P p) {
        Cobol.DataValueIntervalTo d = dataValueIntervalTo;
        return d.withLiteral(visitAndCast(d.getLiteral(), p));
    }

    @Override
    public Cobol.DataWithLowerBoundsClause visitDataWithLowerBoundsClause(Cobol.DataWithLowerBoundsClause dataWithLowerBoundsClause, P p) {
        return dataWithLowerBoundsClause;
    }

    @Override
    public Cobol.DecimalPointClause visitDecimalPointClause(Cobol.DecimalPointClause decimalPointClause, P p) {
        return decimalPointClause;
    }

    @Override
    public Cobol.DefaultComputationalSignClause visitDefaultComputationalSignClause(Cobol.DefaultComputationalSignClause defaultComputationalSignClause, P p) {
        return defaultComputationalSignClause;
    }

    @Override
    public Cobol.DefaultDisplaySignClause visitDefaultDisplaySignClause(Cobol.DefaultDisplaySignClause defaultDisplaySignClause, P p) {
        return defaultDisplaySignClause;
    }

    @Override
    public Cobol.Delete visitDelete(Cobol.Delete delete, P p) {
        Cobol.Delete d = delete;
        d = d.withFileName(visitAndCast(d.getFileName(), p));
        d = d.withInvalidKey(visitAndCast(d.getInvalidKey(), p));
        return d.withNotInvalidKey(visitAndCast(d.getNotInvalidKey(), p));
    }

    @Override
    public Cobol.DestinationCountClause visitDestinationCountClause(Cobol.DestinationCountClause destinationCountClause, P p) {
        Cobol.DestinationCountClause d = destinationCountClause;
        return d.withDataDescName(visitAndCast(d.getDataDescName(), p));
    }

    @Override
    public Cobol.DestinationTableClause visitDestinationTableClause(Cobol.DestinationTableClause destinationTableClause, P p) {
        Cobol.DestinationTableClause d = destinationTableClause;
        d = d.withIntegerLiteral(visitAndCast(d.getIntegerLiteral(), p));
        return d.withIndexNames(ListUtils.map(d.getIndexNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Disable visitDisable(Cobol.Disable disable, P p) {
        Cobol.Disable d = disable;
        d = d.withCdName(visitAndCast(d.getCdName(), p));
        return d.withKeyName(visitAndCast(d.getKeyName(), p));
    }

    @Override
    public Cobol.Display visitDisplay(Cobol.Display display, P p) {
        Cobol.Display d = display;
        d = d.withOperands(ListUtils.map(d.getOperands(), it -> visitAndCast(it, p)));
        d = d.withDisplayAt(visitAndCast(d.getDisplayAt(), p));
        d = d.withDisplayUpon(visitAndCast(d.getDisplayUpon(), p));
        d = d.withOnExceptionClause(visitAndCast(d.getOnExceptionClause(), p));
        return d.withNotOnExceptionClause(visitAndCast(d.getNotOnExceptionClause(), p));
    }

    @Override
    public Cobol.DisplayAt visitDisplayAt(Cobol.DisplayAt displayAt, P p) {
        Cobol.DisplayAt d = displayAt;
        return d.withName(visitAndCast(d.getName(), p));
    }

    @Override
    public Cobol.DisplayUpon visitDisplayUpon(Cobol.DisplayUpon displayUpon, P p) {
        Cobol.DisplayUpon d = displayUpon;
        return d.withName(visitAndCast(d.getName(), p));
    }

    @Override
    public Cobol.Divide visitDivide(Cobol.Divide divide, P p) {
        Cobol.Divide d = divide;
        d = d.withName(visitAndCast(d.getName(), p));
        d = d.withAction(visitAndCast(d.getAction(), p));
        d = d.withDivideRemainder(visitAndCast(d.getDivideRemainder(), p));
        d = d.withOnSizeErrorPhrase(visitAndCast(d.getOnSizeErrorPhrase(), p));
        return d.withNotOnSizeErrorPhrase(visitAndCast(d.getNotOnSizeErrorPhrase(), p));
    }

    @Override
    public Cobol.DivideGiving visitDivideGiving(Cobol.DivideGiving divideGiving, P p) {
        Cobol.DivideGiving d = divideGiving;
        return d.withName(visitAndCast(d.getName(), p));
    }

    @Override
    public Cobol.DivideGivingPhrase visitDivideGivingPhrase(Cobol.DivideGivingPhrase divideGivingPhrase, P p) {
        Cobol.DivideGivingPhrase d = divideGivingPhrase;
        return d.withRoundables(ListUtils.map(d.getRoundables(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DivideInto visitDivideInto(Cobol.DivideInto divideInto, P p) {
        Cobol.DivideInto d = divideInto;
        return d.withRoundables(ListUtils.map(d.getRoundables(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.DivideRemainder visitDivideRemainder(Cobol.DivideRemainder divideRemainder, P p) {
        Cobol.DivideRemainder d = divideRemainder;
        return d.withName(visitAndCast(d.getName(), p));
    }

    @Override
    public Cobol.Enable visitEnable(Cobol.Enable enable, P p) {
        Cobol.Enable e = enable;
        e = e.withCdName(visitAndCast(e.getCdName(), p));
        return e.withKeyName(visitAndCast(e.getKeyName(), p));
    }

    @Override
    public Cobol.EndKeyClause visitEndKeyClause(Cobol.EndKeyClause endKeyClause, P p) {
        Cobol.EndKeyClause e = endKeyClause;
        return e.withName(visitAndCast(e.getName(), p));
    }

    @Override
    public Cobol.EndProgram visitEndProgram(Cobol.EndProgram endProgram, P p) {
        Cobol.EndProgram e = endProgram;
        return e.withProgramName(visitAndCast(e.getProgramName(), p));
    }

    @Override
    public Cobol.Entry visitEntry(Cobol.Entry entry, P p) {
        Cobol.Entry e = entry;
        e = e.withLiteral(visitAndCast(e.getLiteral(), p));
        return e.withIdentifiers(ListUtils.map(e.getIdentifiers(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.EnvironmentDivision visitEnvironmentDivision(Cobol.EnvironmentDivision environmentDivision, P p) {
        Cobol.EnvironmentDivision e = environmentDivision;
        return e.withBody(ListUtils.map(e.getBody(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.EnvironmentSwitchNameClause visitEnvironmentSwitchNameClause(Cobol.EnvironmentSwitchNameClause environmentSwitchNameClause, P p) {
        Cobol.EnvironmentSwitchNameClause e = environmentSwitchNameClause;
        e = e.withEnvironmentName(visitAndCast(e.getEnvironmentName(), p));
        e = e.withMnemonicName(visitAndCast(e.getMnemonicName(), p));
        return e.withEnvironmentSwitchNameSpecialNamesStatusPhrase(visitAndCast(e.getEnvironmentSwitchNameSpecialNamesStatusPhrase(), p));
    }

    @Override
    public Cobol.EnvironmentSwitchNameSpecialNamesStatusPhrase visitEnvironmentSwitchNameSpecialNamesStatusPhrase(Cobol.EnvironmentSwitchNameSpecialNamesStatusPhrase environmentSwitchNameSpecialNamesStatusPhrase, P p) {
        Cobol.EnvironmentSwitchNameSpecialNamesStatusPhrase e = environmentSwitchNameSpecialNamesStatusPhrase;
        return e.withCobols(ListUtils.map(e.getCobols(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ErrorKeyClause visitErrorKeyClause(Cobol.ErrorKeyClause errorKeyClause, P p) {
        Cobol.ErrorKeyClause e = errorKeyClause;
        return e.withName(visitAndCast(e.getName(), p));
    }

    @Override
    public Cobol.Evaluate visitEvaluate(Cobol.Evaluate evaluate, P p) {
        Cobol.Evaluate e = evaluate;
        e = e.withSelect(visitAndCast(e.getSelect(), p));
        e = e.withAlsoSelect(ListUtils.map(e.getAlsoSelect(), it -> visitAndCast(it, p)));
        e = e.withWhenPhrase(ListUtils.map(e.getWhenPhrase(), it -> visitAndCast(it, p)));
        return e.withWhenOther(visitAndCast(e.getWhenOther(), p));
    }

    @Override
    public Cobol.EvaluateAlso visitEvaluateAlso(Cobol.EvaluateAlso evaluateAlso, P p) {
        Cobol.EvaluateAlso e = evaluateAlso;
        return e.withSelect(visitAndCast(e.getSelect(), p));
    }

    @Override
    public Cobol.EvaluateAlsoCondition visitEvaluateAlsoCondition(Cobol.EvaluateAlsoCondition evaluateAlsoCondition, P p) {
        Cobol.EvaluateAlsoCondition e = evaluateAlsoCondition;
        return e.withCondition(visitAndCast(e.getCondition(), p));
    }

    @Override
    public Cobol.EvaluateCondition visitEvaluateCondition(Cobol.EvaluateCondition evaluateCondition, P p) {
        Cobol.EvaluateCondition e = evaluateCondition;
        e = e.withCondition(visitAndCast(e.getCondition(), p));
        return e.withEvaluateThrough(visitAndCast(e.getEvaluateThrough(), p));
    }

    @Override
    public Cobol.EvaluateThrough visitEvaluateThrough(Cobol.EvaluateThrough evaluateThrough, P p) {
        Cobol.EvaluateThrough e = evaluateThrough;
        return e.withValue(visitAndCast(e.getValue(), p));
    }

    @Override
    public Cobol.EvaluateWhen visitEvaluateWhen(Cobol.EvaluateWhen evaluateWhen, P p) {
        Cobol.EvaluateWhen e = evaluateWhen;
        e = e.withCondition(visitAndCast(e.getCondition(), p));
        return e.withAlsoCondition(ListUtils.map(e.getAlsoCondition(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.EvaluateWhenPhrase visitEvaluateWhenPhrase(Cobol.EvaluateWhenPhrase evaluateWhenPhrase, P p) {
        Cobol.EvaluateWhenPhrase e = evaluateWhenPhrase;
        e = e.withWhens(ListUtils.map(e.getWhens(), it -> visitAndCast(it, p)));
        return e.withStatements(ListUtils.map(e.getStatements(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ExecCicsStatement visitExecCicsStatement(Cobol.ExecCicsStatement execCicsStatement, P p) {
        // TODO: https://github.com/moderneinc/rewrite-cobol/issues/70.
        // The grammar rule means that the ExecCicsStatement is a single token.
        return execCicsStatement;
    }

    @Override
    public Cobol.ExecSqlImsStatement visitExecSqlImsStatement(Cobol.ExecSqlImsStatement execSqlImsStatement, P p) {
        // TODO: https://github.com/moderneinc/rewrite-cobol/issues/70.
        // The grammar rule means that the ExecCicsStatement is a single token.
        return execSqlImsStatement;
    }

    @Override
    public Cobol.ExecSqlStatement visitExecSqlStatement(Cobol.ExecSqlStatement execSqlStatement, P p) {
        // TODO: https://github.com/moderneinc/rewrite-cobol/issues/70.
        // The grammar rule means that the ExecCicsStatement is a single token.
        return execSqlStatement;
    }

    @Override
    public Cobol.Exhibit visitExhibit(Cobol.Exhibit exhibit, P p) {
        Cobol.Exhibit e = exhibit;
        return e.withOperands(ListUtils.map(e.getOperands(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Exit visitExit(Cobol.Exit exit, P p) {
        return exit;
    }

    @Override
    public Cobol.ExternalClause visitExternalClause(Cobol.ExternalClause externalClause, P p) {
        return externalClause;
    }

    @Override
    public Cobol.FigurativeConstant visitFigurativeConstant(Cobol.FigurativeConstant figurativeConstant, P p) {
        Cobol.FigurativeConstant f = figurativeConstant;
        return f.withLiteral(visitAndCast(f.getLiteral(), p));
    }

    @Override
    public Cobol.FileControlEntry visitFileControlEntry(Cobol.FileControlEntry fileControlEntry, P p) {
        Cobol.FileControlEntry f = fileControlEntry;
        f = f.withSelectClause(visitAndCast(f.getSelectClause(), p));
        return f.withControlClauses(ListUtils.map(f.getControlClauses(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.FileControlParagraph visitFileControlParagraph(Cobol.FileControlParagraph fileControlParagraph, P p) {
        Cobol.FileControlParagraph f = fileControlParagraph;
        return f.withControlEntries(ListUtils.map(f.getControlEntries(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.FileDescriptionEntry visitFileDescriptionEntry(Cobol.FileDescriptionEntry fileDescriptionEntry, P p) {
        Cobol.FileDescriptionEntry f = fileDescriptionEntry;
        f = f.withName(visitAndCast(f.getName(), p));
        f = f.withClauses(ListUtils.map(f.getClauses(), it -> visitAndCast(it, p)));
        return f.withDataDescriptions(ListUtils.map(f.getDataDescriptions(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.FileSection visitFileSection(Cobol.FileSection fileSection, P p) {
        Cobol.FileSection f = fileSection;
        return f.withFileDescriptionEntry(ListUtils.map(f.getFileDescriptionEntry(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.FileStatusClause visitFileStatusClause(Cobol.FileStatusClause fileStatusClause, P p) {
        Cobol.FileStatusClause f = fileStatusClause;
        return f.withQualifiedDataNames(ListUtils.map(f.getQualifiedDataNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.FunctionCall visitFunctionCall(Cobol.FunctionCall functionCall, P p) {
        Cobol.FunctionCall f = functionCall;
        f = f.withFunctionName(visitAndCast(f.getFunctionName(), p));
        f = f.withArguments(ListUtils.map(f.getArguments(), it -> visitAndCast(it, p)));
        return f.withReferenceModifier(visitAndCast(f.getReferenceModifier(), p));
    }

    @Override
    public Cobol.Generate visitGenerate(Cobol.Generate generate, P p) {
        Cobol.Generate g = generate;
        return g.withReportName(visitAndCast(g.getReportName(), p));
    }

    @Override
    public Cobol.GlobalClause visitGlobalClause(Cobol.GlobalClause globalClause, P p) {
        return globalClause;
    }

    @Override
    public Cobol.GoBack visitGoBack(Cobol.GoBack goBack, P p) {
        return goBack;
    }

    @Override
    public Cobol.GoTo visitGoTo(Cobol.GoTo _goTo, P p) {
        Cobol.GoTo g = _goTo;
        return g.withStatement(visitAndCast(g.getStatement(), p));
    }

    @Override
    public Cobol.GoToDependingOnStatement visitGoToDependingOnStatement(Cobol.GoToDependingOnStatement goToDependingOnStatement, P p) {
        Cobol.GoToDependingOnStatement g = goToDependingOnStatement;
        g = g.withProcedureNames(ListUtils.map(g.getProcedureNames(), it -> visitAndCast(it, p)));
        return g.withIdentifier(visitAndCast(g.getIdentifier(), p));
    }

    @Override
    public Cobol.IdentificationDivision visitIdentificationDivision(Cobol.IdentificationDivision identificationDivision, P p) {
        Cobol.IdentificationDivision i = identificationDivision;
        i = i.withProgramIdParagraph(visitAndCast(i.getProgramIdParagraph(), p));
        return i.withParagraphs(ListUtils.map(i.getParagraphs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.IdentificationDivisionParagraph visitIdentificationDivisionParagraph(Cobol.IdentificationDivisionParagraph identificationDivisionParagraph, P p) {
        Cobol.IdentificationDivisionParagraph i = identificationDivisionParagraph;
        return i.withCommentEntry(visitAndCast(i.getCommentEntry(), p));
    }

    @Override
    public Cobol.If visitIf(Cobol.If _if, P p) {
        Cobol.If i = _if;
        i = i.withCondition(visitAndCast(i.getCondition(), p));
        i = i.withIfThen(visitAndCast(i.getIfThen(), p));
        return i.withIfElse(visitAndCast(i.getIfElse(), p));
    }

    @Override
    public Cobol.IfElse visitIfElse(Cobol.IfElse ifElse, P p) {
        Cobol.IfElse i = ifElse;
        return i.withStatements(ListUtils.map(i.getStatements(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.IfThen visitIfThen(Cobol.IfThen ifThen, P p) {
        Cobol.IfThen i = ifThen;
        return i.withStatements(ListUtils.map(i.getStatements(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InData visitInData(Cobol.InData inData, P p) {
        Cobol.InData i = inData;
        return i.withName(visitAndCast(i.getName(), p));
    }

    @Override
    public Cobol.InFile visitInFile(Cobol.InFile inFile, P p) {
        Cobol.InFile i = inFile;
        return i.withName(visitAndCast(i.getName(), p));
    }

    @Override
    public Cobol.Initialize visitInitialize(Cobol.Initialize initialize, P p) {
        Cobol.Initialize i = initialize;
        i = i.withIdentifiers(ListUtils.map(i.getIdentifiers(), it -> visitAndCast(it, p)));
        return i.withInitializeReplacingPhrase(visitAndCast(i.getInitializeReplacingPhrase(), p));
    }

    @Override
    public Cobol.InitializeReplacingBy visitInitializeReplacingBy(Cobol.InitializeReplacingBy initializeReplacingBy, P p) {
        Cobol.InitializeReplacingBy i = initializeReplacingBy;
        return i.withIdentifier(visitAndCast(i.getIdentifier(), p));
    }

    @Override
    public Cobol.InitializeReplacingPhrase visitInitializeReplacingPhrase(Cobol.InitializeReplacingPhrase initializeReplacingPhrase, P p) {
        Cobol.InitializeReplacingPhrase i = initializeReplacingPhrase;
        return i.withInitializeReplacingBy(ListUtils.map(i.getInitializeReplacingBy(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Initiate visitInitiate(Cobol.Initiate initiate, P p) {
        Cobol.Initiate i = initiate;
        return i.withReportNames(ListUtils.map(i.getReportNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InLibrary visitInLibrary(Cobol.InLibrary inLibrary, P p) {
        Cobol.InLibrary i = inLibrary;
        i = i.withWord(visitAndCast(i.getWord(), p));
        return i.withName(visitAndCast(i.getName(), p));
    }

    @Override
    public Cobol.InMnemonic visitInMnemonic(Cobol.InMnemonic inMnemonic, P p) {
        Cobol.InMnemonic i = inMnemonic;
        return i.withName(visitAndCast(i.getName(), p));
    }

    @Override
    public Cobol.InputOutputSection visitInputOutputSection(Cobol.InputOutputSection inputOutputSection, P p) {
        Cobol.InputOutputSection i = inputOutputSection;
        return i.withParagraphs(ListUtils.map(i.getParagraphs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InSection visitInSection(Cobol.InSection inSection, P p) {
        Cobol.InSection i = inSection;
        return i.withName(visitAndCast(i.getName(), p));
    }

    @Override
    public Cobol.Inspect visitInspect(Cobol.Inspect inspect, P p) {
        Cobol.Inspect i = inspect;
        i = i.withIdentifier(visitAndCast(i.getIdentifier(), p));
        return i.withPhrase(visitAndCast(i.getPhrase(), p));
    }

    @Override
    public Cobol.InspectAllLeading visitInspectAllLeading(Cobol.InspectAllLeading inspectAllLeading, P p) {
        Cobol.InspectAllLeading i = inspectAllLeading;
        i = i.withName(visitAndCast(i.getName(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectAllLeadings visitInspectAllLeadings(Cobol.InspectAllLeadings inspectAllLeadings, P p) {
        Cobol.InspectAllLeadings i = inspectAllLeadings;
        return i.withLeadings(ListUtils.map(i.getLeadings(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectBeforeAfter visitInspectBeforeAfter(Cobol.InspectBeforeAfter inspectBeforeAfter, P p) {
        Cobol.InspectBeforeAfter i = inspectBeforeAfter;
        return i.withIdentifier(visitAndCast(i.getIdentifier(), p));
    }

    @Override
    public Cobol.InspectBy visitInspectBy(Cobol.InspectBy inspectBy, P p) {
        Cobol.InspectBy i = inspectBy;
        return i.withIdentifier(visitAndCast(i.getIdentifier(), p));
    }

    @Override
    public Cobol.InspectCharacters visitInspectCharacters(Cobol.InspectCharacters inspectCharacters, P p) {
        Cobol.InspectCharacters i = inspectCharacters;
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectConvertingPhrase visitInspectConvertingPhrase(Cobol.InspectConvertingPhrase inspectConvertingPhrase, P p) {
        Cobol.InspectConvertingPhrase i = inspectConvertingPhrase;
        i = i.withIdentifier(visitAndCast(i.getIdentifier(), p));
        i = i.withInspectTo(visitAndCast(i.getInspectTo(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectFor visitInspectFor(Cobol.InspectFor inspectFor, P p) {
        Cobol.InspectFor i = inspectFor;
        i = i.withIdentifier(visitAndCast(i.getIdentifier(), p));
        return i.withInspects(ListUtils.map(i.getInspects(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectReplacingAllLeading visitInspectReplacingAllLeading(Cobol.InspectReplacingAllLeading inspectReplacingAllLeading, P p) {
        Cobol.InspectReplacingAllLeading i = inspectReplacingAllLeading;
        i = i.withIdentifier(visitAndCast(i.getIdentifier(), p));
        i = i.withInspectBy(visitAndCast(i.getInspectBy(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectReplacingAllLeadings visitInspectReplacingAllLeadings(Cobol.InspectReplacingAllLeadings inspectReplacingAllLeadings, P p) {
        Cobol.InspectReplacingAllLeadings i = inspectReplacingAllLeadings;
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectReplacingCharacters visitInspectReplacingCharacters(Cobol.InspectReplacingCharacters inspectReplacingCharacters, P p) {
        Cobol.InspectReplacingCharacters i = inspectReplacingCharacters;
        i = i.withInspectBy(visitAndCast(i.getInspectBy(), p));
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectReplacingPhrase visitInspectReplacingPhrase(Cobol.InspectReplacingPhrase inspectReplacingPhrase, P p) {
        Cobol.InspectReplacingPhrase i = inspectReplacingPhrase;
        return i.withInspections(ListUtils.map(i.getInspections(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectTallyingPhrase visitInspectTallyingPhrase(Cobol.InspectTallyingPhrase inspectTallyingPhrase, P p) {
        Cobol.InspectTallyingPhrase i = inspectTallyingPhrase;
        return i.withInspectFors(ListUtils.map(i.getInspectFors(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectTallyingReplacingPhrase visitInspectTallyingReplacingPhrase(Cobol.InspectTallyingReplacingPhrase inspectTallyingReplacingPhrase, P p) {
        Cobol.InspectTallyingReplacingPhrase i = inspectTallyingReplacingPhrase;
        i = i.withInspectFors(ListUtils.map(i.getInspectFors(), it -> visitAndCast(it, p)));
        return i.withReplacingPhrases(ListUtils.map(i.getReplacingPhrases(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.InspectTo visitInspectTo(Cobol.InspectTo inspectTo, P p) {
        Cobol.InspectTo i = inspectTo;
        return i.withIdentifier(visitAndCast(i.getIdentifier(), p));
    }

    @Override
    public Cobol.InTable visitInTable(Cobol.InTable inTable, P p) {
        Cobol.InTable i = inTable;
        return i.withTableCall(visitAndCast(i.getTableCall(), p));
    }

    @Override
    public Cobol.IoControlParagraph visitIoControlParagraph(Cobol.IoControlParagraph ioControlParagraph, P p) {
        Cobol.IoControlParagraph i = ioControlParagraph;
        i = i.withFileName(visitAndCast(i.getFileName(), p));
        return i.withClauses(ListUtils.map(i.getClauses(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LabelRecordsClause visitLabelRecordsClause(Cobol.LabelRecordsClause labelRecordsClause, P p) {
        Cobol.LabelRecordsClause l = labelRecordsClause;
        return l.withDataNames(ListUtils.map(l.getDataNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LibraryAttributeClauseFormat1 visitLibraryAttributeClauseFormat1(Cobol.LibraryAttributeClauseFormat1 libraryAttributeClauseFormat1, P p) {
        return libraryAttributeClauseFormat1;
    }

    @Override
    public Cobol.LibraryAttributeClauseFormat2 visitLibraryAttributeClauseFormat2(Cobol.LibraryAttributeClauseFormat2 libraryAttributeClauseFormat2, P p) {
        Cobol.LibraryAttributeClauseFormat2 l = libraryAttributeClauseFormat2;
        l = l.withLibraryAttributeFunction(visitAndCast(l.getLibraryAttributeFunction(), p));
        l = l.withLibraryAttributeParameter(visitAndCast(l.getLibraryAttributeParameter(), p));
        return l.withLibraryAttributeTitle(visitAndCast(l.getLibraryAttributeTitle(), p));
    }

    @Override
    public Cobol.LibraryAttributeFunction visitLibraryAttributeFunction(Cobol.LibraryAttributeFunction libraryAttributeFunction, P p) {
        Cobol.LibraryAttributeFunction l = libraryAttributeFunction;
        return l.withLiteral(visitAndCast(l.getLiteral(), p));
    }

    @Override
    public Cobol.LibraryAttributeParameter visitLibraryAttributeParameter(Cobol.LibraryAttributeParameter libraryAttributeParameter, P p) {
        Cobol.LibraryAttributeParameter l = libraryAttributeParameter;
        return l.withLiteral(visitAndCast(l.getLiteral(), p));
    }

    @Override
    public Cobol.LibraryAttributeTitle visitLibraryAttributeTitle(Cobol.LibraryAttributeTitle libraryAttributeTitle, P p) {
        Cobol.LibraryAttributeTitle l = libraryAttributeTitle;
        return l.withLiteral(visitAndCast(l.getLiteral(), p));
    }

    @Override
    public Cobol.LibraryDescriptionEntryFormat1 visitLibraryDescriptionEntryFormat1(Cobol.LibraryDescriptionEntryFormat1 libraryDescriptionEntryFormat1, P p) {
        Cobol.LibraryDescriptionEntryFormat1 l = libraryDescriptionEntryFormat1;
        l = l.withLibraryName(visitAndCast(l.getLibraryName(), p));
        l = l.withLibraryAttributeClauseFormat1(visitAndCast(l.getLibraryAttributeClauseFormat1(), p));
        return l.withLibraryEntryProcedureClauseFormat1(visitAndCast(l.getLibraryEntryProcedureClauseFormat1(), p));
    }

    @Override
    public Cobol.LibraryDescriptionEntryFormat2 visitLibraryDescriptionEntryFormat2(Cobol.LibraryDescriptionEntryFormat2 libraryDescriptionEntryFormat2, P p) {
        Cobol.LibraryDescriptionEntryFormat2 l = libraryDescriptionEntryFormat2;
        l = l.withLibraryName(visitAndCast(l.getLibraryName(), p));
        l = l.withLibraryIsGlobalClause(visitAndCast(l.getLibraryIsGlobalClause(), p));
        l = l.withLibraryIsCommonClause(visitAndCast(l.getLibraryIsCommonClause(), p));
        return l.withClauseFormats(ListUtils.map(l.getClauseFormats(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LibraryEntryProcedureClauseFormat1 visitLibraryEntryProcedureClauseFormat1(Cobol.LibraryEntryProcedureClauseFormat1 libraryEntryProcedureClauseFormat1, P p) {
        Cobol.LibraryEntryProcedureClauseFormat1 l = libraryEntryProcedureClauseFormat1;
        l = l.withProgramName(visitAndCast(l.getProgramName(), p));
        return l.withLibraryEntryProcedureForClause(visitAndCast(l.getLibraryEntryProcedureForClause(), p));
    }

    @Override
    public Cobol.LibraryEntryProcedureClauseFormat2 visitLibraryEntryProcedureClauseFormat2(Cobol.LibraryEntryProcedureClauseFormat2 libraryEntryProcedureClauseFormat2, P p) {
        Cobol.LibraryEntryProcedureClauseFormat2 l = libraryEntryProcedureClauseFormat2;
        l = l.withProgramName(visitAndCast(l.getProgramName(), p));
        l = l.withLibraryEntryProcedureForClause(visitAndCast(l.getLibraryEntryProcedureForClause(), p));
        l = l.withLibraryEntryProcedureWithClause(visitAndCast(l.getLibraryEntryProcedureWithClause(), p));
        l = l.withLibraryEntryProcedureUsingClause(visitAndCast(l.getLibraryEntryProcedureUsingClause(), p));
        return l.withLibraryEntryProcedureGivingClause(visitAndCast(l.getLibraryEntryProcedureGivingClause(), p));
    }

    @Override
    public Cobol.LibraryEntryProcedureForClause visitLibraryEntryProcedureForClause(Cobol.LibraryEntryProcedureForClause libraryEntryProcedureForClause, P p) {
        Cobol.LibraryEntryProcedureForClause l = libraryEntryProcedureForClause;
        return l.withLiteral(visitAndCast(l.getLiteral(), p));
    }

    @Override
    public Cobol.LibraryEntryProcedureGivingClause visitLibraryEntryProcedureGivingClause(Cobol.LibraryEntryProcedureGivingClause libraryEntryProcedureGivingClause, P p) {
        Cobol.LibraryEntryProcedureGivingClause l = libraryEntryProcedureGivingClause;
        return l.withDataName(visitAndCast(l.getDataName(), p));
    }

    @Override
    public Cobol.LibraryEntryProcedureUsingClause visitLibraryEntryProcedureUsingClause(Cobol.LibraryEntryProcedureUsingClause libraryEntryProcedureUsingClause, P p) {
        Cobol.LibraryEntryProcedureUsingClause l = libraryEntryProcedureUsingClause;
        return l.withNames(ListUtils.map(l.getNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LibraryEntryProcedureWithClause visitLibraryEntryProcedureWithClause(Cobol.LibraryEntryProcedureWithClause libraryEntryProcedureWithClause, P p) {
        Cobol.LibraryEntryProcedureWithClause l = libraryEntryProcedureWithClause;
        return l.withNames(ListUtils.map(l.getNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LibraryIsCommonClause visitLibraryIsCommonClause(Cobol.LibraryIsCommonClause libraryIsCommonClause, P p) {
        return libraryIsCommonClause;
    }

    @Override
    public Cobol.LibraryIsGlobalClause visitLibraryIsGlobalClause(Cobol.LibraryIsGlobalClause libraryIsGlobalClause, P p) {
        return libraryIsGlobalClause;
    }

    @Override
    public Cobol.LinageClause visitLinageClause(Cobol.LinageClause linageClause, P p) {
        Cobol.LinageClause l = linageClause;
        l = l.withName(visitAndCast(l.getName(), p));
        return l.withLinageAt(ListUtils.map(l.getLinageAt(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LinageFootingAt visitLinageFootingAt(Cobol.LinageFootingAt linageFootingAt, P p) {
        Cobol.LinageFootingAt l = linageFootingAt;
        return l.withName(visitAndCast(l.getName(), p));
    }

    @Override
    public Cobol.LinageLinesAtBottom visitLinageLinesAtBottom(Cobol.LinageLinesAtBottom linageLinesAtBottom, P p) {
        Cobol.LinageLinesAtBottom l = linageLinesAtBottom;
        return l.withName(visitAndCast(l.getName(), p));
    }

    @Override
    public Cobol.LinageLinesAtTop visitLinageLinesAtTop(Cobol.LinageLinesAtTop linageLinesAtTop, P p) {
        Cobol.LinageLinesAtTop l = linageLinesAtTop;
        return l.withName(visitAndCast(l.getName(), p));
    }

    @Override
    public Cobol.LinkageSection visitLinkageSection(Cobol.LinkageSection linkageSection, P p) {
        Cobol.LinkageSection l = linkageSection;
        return l.withDataDescriptions(ListUtils.map(l.getDataDescriptions(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.LocalStorageSection visitLocalStorageSection(Cobol.LocalStorageSection localStorageSection, P p) {
        Cobol.LocalStorageSection l = localStorageSection;
        l = l.withLocalName(visitAndCast(l.getLocalName(), p));
        return l.withDataDescriptions(ListUtils.map(l.getDataDescriptions(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Merge visitMerge(Cobol.Merge merge, P p) {
        Cobol.Merge m = merge;
        m = m.withFileName(visitAndCast(m.getFileName(), p));
        m = m.withMergeOnKeyClause(ListUtils.map(m.getMergeOnKeyClause(), it -> visitAndCast(it, p)));
        m = m.withMergeCollatingSequencePhrase(visitAndCast(m.getMergeCollatingSequencePhrase(), p));
        m = m.withMergeUsing(ListUtils.map(m.getMergeUsing(), it -> visitAndCast(it, p)));
        m = m.withMergeOutputProcedurePhrase(visitAndCast(m.getMergeOutputProcedurePhrase(), p));
        return m.withMergeGivingPhrase(ListUtils.map(m.getMergeGivingPhrase(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Mergeable visitMergeable(Cobol.Mergeable mergeable, P p) {
        Cobol.Mergeable m = mergeable;
        return m.withName(visitAndCast(m.getName(), p));
    }

    @Override
    public Cobol.MergeCollatingSequencePhrase visitMergeCollatingSequencePhrase(Cobol.MergeCollatingSequencePhrase mergeCollatingSequencePhrase, P p) {
        Cobol.MergeCollatingSequencePhrase m = mergeCollatingSequencePhrase;
        m = m.withName(ListUtils.map(m.getName(), it -> visitAndCast(it, p)));
        m = m.withMergeCollatingAlphanumeric(visitAndCast(m.getMergeCollatingAlphanumeric(), p));
        return m.withMergeCollatingNational(visitAndCast(m.getMergeCollatingNational(), p));
    }

    @Override
    public Cobol.MergeGiving visitMergeGiving(Cobol.MergeGiving mergeGiving, P p) {
        Cobol.MergeGiving m = mergeGiving;
        return m.withName(visitAndCast(m.getName(), p));
    }

    @Override
    public Cobol.MergeGivingPhrase visitMergeGivingPhrase(Cobol.MergeGivingPhrase mergeGivingPhrase, P p) {
        Cobol.MergeGivingPhrase m = mergeGivingPhrase;
        return m.withMergeGiving(ListUtils.map(m.getMergeGiving(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MergeOnKeyClause visitMergeOnKeyClause(Cobol.MergeOnKeyClause mergeOnKeyClause, P p) {
        Cobol.MergeOnKeyClause m = mergeOnKeyClause;
        return m.withQualifiedDataName(ListUtils.map(m.getQualifiedDataName(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MergeOutputProcedurePhrase visitMergeOutputProcedurePhrase(Cobol.MergeOutputProcedurePhrase mergeOutputProcedurePhrase, P p) {
        Cobol.MergeOutputProcedurePhrase m = mergeOutputProcedurePhrase;
        m = m.withProcedureName(visitAndCast(m.getProcedureName(), p));
        return m.withMergeOutputThrough(visitAndCast(m.getMergeOutputThrough(), p));
    }

    @Override
    public Cobol.MergeOutputThrough visitMergeOutputThrough(Cobol.MergeOutputThrough mergeOutputThrough, P p) {
        Cobol.MergeOutputThrough m = mergeOutputThrough;
        return m.withProcedureName(visitAndCast(m.getProcedureName(), p));
    }

    @Override
    public Cobol.MergeUsing visitMergeUsing(Cobol.MergeUsing mergeUsing, P p) {
        Cobol.MergeUsing m = mergeUsing;
        return m.withFileNames(ListUtils.map(m.getFileNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MessageCountClause visitMessageCountClause(Cobol.MessageCountClause messageCountClause, P p) {
        Cobol.MessageCountClause m = messageCountClause;
        return m.withDataDescName(visitAndCast(m.getDataDescName(), p));
    }

    @Override
    public Cobol.MessageDateClause visitMessageDateClause(Cobol.MessageDateClause messageDateClause, P p) {
        Cobol.MessageDateClause m = messageDateClause;
        return m.withDataDescName(visitAndCast(m.getDataDescName(), p));
    }

    @Override
    public Cobol.MessageTimeClause visitMessageTimeClause(Cobol.MessageTimeClause messageTimeClause, P p) {
        Cobol.MessageTimeClause m = messageTimeClause;
        return m.withDataDescName(visitAndCast(m.getDataDescName(), p));
    }

    @Override
    public Cobol.MoveCorrespondingToStatement visitMoveCorrespondingToStatement(Cobol.MoveCorrespondingToStatement moveCorrespondingToStatement, P p) {
        Cobol.MoveCorrespondingToStatement m = moveCorrespondingToStatement;
        m = m.withMoveCorrespondingToSendingArea(visitAndCast(m.getMoveCorrespondingToSendingArea(), p));
        return m.withIdentifiers(ListUtils.map(m.getIdentifiers(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MoveStatement visitMoveStatement(Cobol.MoveStatement moveStatement, P p) {
        Cobol.MoveStatement m = moveStatement;
        return m.withMoveToStatement(visitAndCast(m.getMoveToStatement(), p));
    }

    @Override
    public Cobol.MoveToStatement visitMoveToStatement(Cobol.MoveToStatement moveToStatement, P p) {
        Cobol.MoveToStatement m = moveToStatement;
        m = m.withFrom(visitAndCast(m.getFrom(), p));
        return m.withNames(ListUtils.map(m.getNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MultDiv visitMultDiv(Cobol.MultDiv multDiv, P p) {
        Cobol.MultDiv m = multDiv;
        return m.withPowers(visitAndCast(m.getPowers(), p));
    }

    @Override
    public Cobol.MultDivs visitMultDivs(Cobol.MultDivs multDivs, P p) {
        Cobol.MultDivs m = multDivs;
        m = m.withPowers(visitAndCast(m.getPowers(), p));
        return m.withMultDivs(ListUtils.map(m.getMultDivs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MultipleFileClause visitMultipleFileClause(Cobol.MultipleFileClause multipleFileClause, P p) {
        Cobol.MultipleFileClause m = multipleFileClause;
        return m.withFilePositions(ListUtils.map(m.getFilePositions(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MultipleFilePosition visitMultipleFilePosition(Cobol.MultipleFilePosition multipleFilePosition, P p) {
        Cobol.MultipleFilePosition m = multipleFilePosition;
        m = m.withFileName(visitAndCast(m.getFileName(), p));
        return m.withIntegerLiteral(visitAndCast(m.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.Multiply visitMultiply(Cobol.Multiply multiply, P p) {
        Cobol.Multiply m = multiply;
        m = m.withMultiplicand(visitAndCast(m.getMultiplicand(), p));
        m = m.withMultiply(visitAndCast(m.getMultiply(), p));
        m = m.withOnSizeErrorPhrase(visitAndCast(m.getOnSizeErrorPhrase(), p));
        return m.withNotOnSizeErrorPhrase(visitAndCast(m.getNotOnSizeErrorPhrase(), p));
    }

    @Override
    public Cobol.MultiplyGiving visitMultiplyGiving(Cobol.MultiplyGiving multiplyGiving, P p) {
        Cobol.MultiplyGiving m = multiplyGiving;
        m = m.withOperand(visitAndCast(m.getOperand(), p));
        return m.withResult(ListUtils.map(m.getResult(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.MultiplyRegular visitMultiplyRegular(Cobol.MultiplyRegular multiplyRegular, P p) {
        Cobol.MultiplyRegular m = multiplyRegular;
        return m.withOperand(ListUtils.map(m.getOperand(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.NextSentence visitNextSentence(Cobol.NextSentence nextSentence, P p) {
        Cobol.NextSentence n = nextSentence;
        return n.withWords(ListUtils.map(n.getWords(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ObjectComputer visitObjectComputer(Cobol.ObjectComputer objectComputer, P p) {
        Cobol.ObjectComputer o = objectComputer;
        return o.withComputer(visitAndCast(o.getComputer(), p));
    }

    @Override
    public Cobol.ObjectComputerDefinition visitObjectComputerDefinition(Cobol.ObjectComputerDefinition objectComputerDefinition, P p) {
        Cobol.ObjectComputerDefinition o = objectComputerDefinition;
        o = o.withComputerName(visitAndCast(o.getComputerName(), p));
        return o.withSpecifications(ListUtils.map(o.getSpecifications(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.OdtClause visitOdtClause(Cobol.OdtClause odtClause, P p) {
        Cobol.OdtClause o = odtClause;
        return o.withMnemonicName(visitAndCast(o.getMnemonicName(), p));
    }

    @Override
    public Cobol.Open visitOpen(Cobol.Open open, P p) {
        Cobol.Open o = open;
        return o.withOpen(ListUtils.map(o.getOpen(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Openable visitOpenable(Cobol.Openable openable, P p) {
        Cobol.Openable o = openable;
        o = o.withFileName(visitAndCast(o.getFileName(), p));
        return o.withFileName(visitAndCast(o.getFileName(), p));
    }

    @Override
    public Cobol.OpenInputOutputStatement visitOpenInputOutputStatement(Cobol.OpenInputOutputStatement openInputOutputStatement, P p) {
        Cobol.OpenInputOutputStatement o = openInputOutputStatement;
        return o.withOpenInput(ListUtils.map(o.getOpenInput(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.OpenIOExtendStatement visitOpenIOExtendStatement(Cobol.OpenIOExtendStatement openIOExtendStatement, P p) {
        Cobol.OpenIOExtendStatement o = openIOExtendStatement;
        return o.withFileNames(ListUtils.map(o.getFileNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.OrganizationClause visitOrganizationClause(Cobol.OrganizationClause organizationClause, P p) {
        return organizationClause;
    }

    @Override
    public Cobol.PaddingCharacterClause visitPaddingCharacterClause(Cobol.PaddingCharacterClause paddingCharacterClause, P p) {
        Cobol.PaddingCharacterClause pp = paddingCharacterClause;
        return pp.withName(visitAndCast(pp.getName(), p));
    }

    @Override
    public Cobol.Paragraph visitParagraph(Cobol.Paragraph paragraph, P p) {
        Cobol.Paragraph pp = paragraph;
        pp = pp.withParagraphName(visitAndCast(pp.getParagraphName(), p));
        pp = pp.withAlteredGoTo(visitAndCast(pp.getAlteredGoTo(), p));
        return pp.withSentences(ListUtils.map(pp.getSentences(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Paragraphs visitParagraphs(Cobol.Paragraphs paragraphs, P p) {
        Cobol.Paragraphs pp = paragraphs;
        pp = pp.withSentences(ListUtils.map(pp.getSentences(), it -> visitAndCast(it, p)));
        return pp.withParagraphs(ListUtils.map(pp.getParagraphs(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.Parenthesized visitParenthesized(Cobol.Parenthesized parenthesized, P p) {
        Cobol.Parenthesized pp = parenthesized;
        return pp.withContents(ListUtils.map(pp.getContents(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.PasswordClause visitPasswordClause(Cobol.PasswordClause passwordClause, P p) {
        Cobol.PasswordClause pp = passwordClause;
        return pp.withDataName(visitAndCast(pp.getDataName(), p));
    }

    @Override
    public Cobol.Perform visitPerform(Cobol.Perform perform, P p) {
        Cobol.Perform pp = perform;
        return pp.withStatement(visitAndCast(pp.getStatement(), p));
    }

    @Override
    public Cobol.Performable visitPerformable(Cobol.Performable performable, P p) {
        Cobol.Performable pp = performable;
        return pp.withExpression(visitAndCast(pp.getExpression(), p));
    }

    @Override
    public Cobol.PerformInlineStatement visitPerformInlineStatement(Cobol.PerformInlineStatement performInlineStatement, P p) {
        Cobol.PerformInlineStatement pp = performInlineStatement;
        pp = pp.withPerformType(visitAndCast(pp.getPerformType(), p));
        return pp.withStatements(ListUtils.map(pp.getStatements(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.PerformProcedureStatement visitPerformProcedureStatement(Cobol.PerformProcedureStatement performProcedureStatement, P p) {
        Cobol.PerformProcedureStatement pp = performProcedureStatement;
        pp = pp.withProcedureName(visitAndCast(pp.getProcedureName(), p));
        pp = pp.withThroughProcedure(visitAndCast(pp.getThroughProcedure(), p));
        return pp.withPerformType(visitAndCast(pp.getPerformType(), p));
    }

    @Override
    public Cobol.PerformTestClause visitPerformTestClause(Cobol.PerformTestClause performTestClause, P p) {
        return performTestClause;
    }

    @Override
    public Cobol.PerformTimes visitPerformTimes(Cobol.PerformTimes performTimes, P p) {
        Cobol.PerformTimes pp = performTimes;
        return pp.withValue(visitAndCast(pp.getValue(), p));
    }

    @Override
    public Cobol.PerformUntil visitPerformUntil(Cobol.PerformUntil performUntil, P p) {
        Cobol.PerformUntil pp = performUntil;
        pp = pp.withPerformTestClause(visitAndCast(pp.getPerformTestClause(), p));
        return pp.withCondition(visitAndCast(pp.getCondition(), p));
    }

    @Override
    public Cobol.PerformVarying visitPerformVarying(Cobol.PerformVarying performVarying, P p) {
        Cobol.PerformVarying pp = performVarying;
        return pp.withCobols(ListUtils.map(pp.getCobols(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.PerformVaryingClause visitPerformVaryingClause(Cobol.PerformVaryingClause performVaryingClause, P p) {
        Cobol.PerformVaryingClause pp = performVaryingClause;
        pp = pp.withPerformVaryingPhrase(visitAndCast(pp.getPerformVaryingPhrase(), p));
        return pp.withPerformAfter(ListUtils.map(pp.getPerformAfter(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.PerformVaryingPhrase visitPerformVaryingPhrase(Cobol.PerformVaryingPhrase performVaryingPhrase, P p) {
        Cobol.PerformVaryingPhrase pp = performVaryingPhrase;
        pp = pp.withName(visitAndCast(pp.getName(), p));
        pp = pp.withFrom(visitAndCast(pp.getFrom(), p));
        pp = pp.withBy(visitAndCast(pp.getBy(), p));
        return pp.withUntil(visitAndCast(pp.getUntil(), p));
    }

    @Override
    public Cobol.Picture visitPicture(Cobol.Picture picture, P p) {
        Cobol.Picture pp = picture;
        pp = pp.withWords(ListUtils.map(pp.getWords(), it -> visitAndCast(it, p)));
        return pp.withParenthesized(visitAndCast(pp.getParenthesized(), p));
    }

    @Override
    public Cobol.PictureString visitPictureString(Cobol.PictureString pictureString, P p) {
        Cobol.PictureString pp = pictureString;
        return pp.withPictures(ListUtils.map(pp.getPictures(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.PlusMinus visitPlusMinus(Cobol.PlusMinus plusMinus, P p) {
        Cobol.PlusMinus pp = plusMinus;
        return pp.withMultDivs(visitAndCast(pp.getMultDivs(), p));
    }

    @Override
    public Cobol.Power visitPower(Cobol.Power power, P p) {
        Cobol.Power pp = power;
        return pp.withExpression(visitAndCast(pp.getExpression(), p));
    }

    @Override
    public Cobol.Powers visitPowers(Cobol.Powers powers, P p) {
        Cobol.Powers pp = powers;
        pp = pp.withExpression(visitAndCast(pp.getExpression(), p));
        return pp.withPowers(ListUtils.map(pp.getPowers(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProcedureDeclarative visitProcedureDeclarative(Cobol.ProcedureDeclarative procedureDeclarative, P p) {
        Cobol.ProcedureDeclarative pp = procedureDeclarative;
        pp = pp.withProcedureSectionHeader(visitAndCast(pp.getProcedureSectionHeader(), p));
        pp = pp.withUseStatement(visitAndCast(pp.getUseStatement(), p));
        return pp.withParagraphs(visitAndCast(pp.getParagraphs(), p));
    }

    @Override
    public Cobol.ProcedureDeclaratives visitProcedureDeclaratives(Cobol.ProcedureDeclaratives procedureDeclaratives, P p) {
        Cobol.ProcedureDeclaratives pp = procedureDeclaratives;
        return pp.withProcedureDeclarative(ListUtils.map(pp.getProcedureDeclarative(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProcedureDivision visitProcedureDivision(Cobol.ProcedureDivision procedureDivision, P p) {
        Cobol.ProcedureDivision pp = procedureDivision;
        pp = pp.withProcedureDivisionUsingClause(visitAndCast(pp.getProcedureDivisionUsingClause(), p));
        pp = pp.withProcedureDivisionGivingClause(visitAndCast(pp.getProcedureDivisionGivingClause(), p));
        pp = pp.withProcedureDeclaratives(visitAndCast(pp.getProcedureDeclaratives(), p));
        return pp.withBody(visitAndCast(pp.getBody(), p));
    }

    @Override
    public Cobol.ProcedureDivisionBody visitProcedureDivisionBody(Cobol.ProcedureDivisionBody procedureDivisionBody, P p) {
        Cobol.ProcedureDivisionBody pp = procedureDivisionBody;
        pp = pp.withParagraphs(visitAndCast(pp.getParagraphs(), p));
        return pp.withProcedureSection(ListUtils.map(pp.getProcedureSection(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProcedureDivisionByReference visitProcedureDivisionByReference(Cobol.ProcedureDivisionByReference procedureDivisionByReference, P p) {
        Cobol.ProcedureDivisionByReference pp = procedureDivisionByReference;
        return pp.withReference(visitAndCast(pp.getReference(), p));
    }

    @Override
    public Cobol.ProcedureDivisionByReferencePhrase visitProcedureDivisionByReferencePhrase(Cobol.ProcedureDivisionByReferencePhrase procedureDivisionByReferencePhrase, P p) {
        Cobol.ProcedureDivisionByReferencePhrase pp = procedureDivisionByReferencePhrase;
        return pp.withProcedureDivisionByReference(ListUtils.map(pp.getProcedureDivisionByReference(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProcedureDivisionByValuePhrase visitProcedureDivisionByValuePhrase(Cobol.ProcedureDivisionByValuePhrase procedureDivisionByValuePhrase, P p) {
        Cobol.ProcedureDivisionByValuePhrase pp = procedureDivisionByValuePhrase;
        return pp.withPhrases(ListUtils.map(pp.getPhrases(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProcedureDivisionGivingClause visitProcedureDivisionGivingClause(Cobol.ProcedureDivisionGivingClause procedureDivisionGivingClause, P p) {
        Cobol.ProcedureDivisionGivingClause pp = procedureDivisionGivingClause;
        return pp.withDataName(visitAndCast(pp.getDataName(), p));
    }

    @Override
    public Cobol.ProcedureDivisionUsingClause visitProcedureDivisionUsingClause(Cobol.ProcedureDivisionUsingClause procedureDivisionUsingClause, P p) {
        Cobol.ProcedureDivisionUsingClause pp = procedureDivisionUsingClause;
        return pp.withProcedureDivisionUsingParameter(ListUtils.map(pp.getProcedureDivisionUsingParameter(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProcedureName visitProcedureName(Cobol.ProcedureName procedureName, P p) {
        Cobol.ProcedureName pp = procedureName;
        pp = pp.withParagraphName(visitAndCast(pp.getParagraphName(), p));
        pp = pp.withInSection(visitAndCast(pp.getInSection(), p));
        return pp.withSectionName(visitAndCast(pp.getSectionName(), p));
    }

    @Override
    public Cobol.ProcedureSection visitProcedureSection(Cobol.ProcedureSection procedureSection, P p) {
        Cobol.ProcedureSection pp = procedureSection;
        pp = pp.withProcedureSectionHeader(visitAndCast(pp.getProcedureSectionHeader(), p));
        return pp.withParagraphs(visitAndCast(pp.getParagraphs(), p));
    }

    @Override
    public Cobol.ProcedureSectionHeader visitProcedureSectionHeader(Cobol.ProcedureSectionHeader procedureSectionHeader, P p) {
        Cobol.ProcedureSectionHeader pp = procedureSectionHeader;
        pp = pp.withSectionName(visitAndCast(pp.getSectionName(), p));
        return pp.withIdentifier(visitAndCast(pp.getIdentifier(), p));
    }

    @Override
    public Cobol.ProgramIdParagraph visitProgramIdParagraph(Cobol.ProgramIdParagraph programIdParagraph, P p) {
        Cobol.ProgramIdParagraph pp = programIdParagraph;
        pp = pp.withProgramName(visitAndCast(pp.getProgramName(), p));
        return pp.withCommentEntry(visitAndCast(pp.getCommentEntry(), p));
    }

    @Override
    public Cobol.ProgramLibrarySection visitProgramLibrarySection(Cobol.ProgramLibrarySection programLibrarySection, P p) {
        Cobol.ProgramLibrarySection pp = programLibrarySection;
        return pp.withLibraryDescriptionEntries(ListUtils.map(pp.getLibraryDescriptionEntries(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ProgramUnit visitProgramUnit(Cobol.ProgramUnit programUnit, P p) {
        Cobol.ProgramUnit pp = programUnit;
        pp = pp.withIdentificationDivision(visitAndCast(pp.getIdentificationDivision(), p));
        pp = pp.withEnvironmentDivision(visitAndCast(pp.getEnvironmentDivision(), p));
        pp = pp.withDataDivision(visitAndCast(pp.getDataDivision(), p));
        pp = pp.withProcedureDivision(visitAndCast(pp.getProcedureDivision(), p));
        pp = pp.withProgramUnits(ListUtils.map(pp.getProgramUnits(), it -> visitAndCast(it, p)));
        return pp.withEndProgram(visitAndCast(pp.getEndProgram(), p));
    }

    @Override
    public Cobol.Purge visitPurge(Cobol.Purge purge, P p) {
        Cobol.Purge pp = purge;
        return pp.withNames(ListUtils.map(pp.getNames(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.QualifiedDataName visitQualifiedDataName(Cobol.QualifiedDataName qualifiedDataName, P p) {
        Cobol.QualifiedDataName q = qualifiedDataName;
        return q.withDataName(visitAndCast(q.getDataName(), p));
    }

    @Override
    public Cobol.QualifiedDataNameFormat1 visitQualifiedDataNameFormat1(Cobol.QualifiedDataNameFormat1 qualifiedDataNameFormat1, P p) {
        Cobol.QualifiedDataNameFormat1 q = qualifiedDataNameFormat1;
        q = q.withName(visitAndCast(q.getName(), p));
        q = q.withQualifiedInData(ListUtils.map(q.getQualifiedInData(), it -> visitAndCast(it, p)));
        return q.withInFile(visitAndCast(q.getInFile(), p));
    }

    @Override
    public Cobol.QualifiedDataNameFormat2 visitQualifiedDataNameFormat2(Cobol.QualifiedDataNameFormat2 qualifiedDataNameFormat2, P p) {
        Cobol.QualifiedDataNameFormat2 q = qualifiedDataNameFormat2;
        q = q.withParagraphName(visitAndCast(q.getParagraphName(), p));
        return q.withInSection(visitAndCast(q.getInSection(), p));
    }

    @Override
    public Cobol.QualifiedDataNameFormat3 visitQualifiedDataNameFormat3(Cobol.QualifiedDataNameFormat3 qualifiedDataNameFormat3, P p) {
        Cobol.QualifiedDataNameFormat3 q = qualifiedDataNameFormat3;
        q = q.withTextName(visitAndCast(q.getTextName(), p));
        return q.withInLibrary(visitAndCast(q.getInLibrary(), p));
    }

    @Override
    public Cobol.QualifiedDataNameFormat4 visitQualifiedDataNameFormat4(Cobol.QualifiedDataNameFormat4 qualifiedDataNameFormat4, P p) {
        Cobol.QualifiedDataNameFormat4 q = qualifiedDataNameFormat4;
        return q.withInFile(visitAndCast(q.getInFile(), p));
    }

    @Override
    public Cobol.QualifiedInData visitQualifiedInData(Cobol.QualifiedInData qualifiedInData, P p) {
        Cobol.QualifiedInData q = qualifiedInData;
        return q.withIn(visitAndCast(q.getIn(), p));
    }

    @Override
    public Cobol.Read visitRead(Cobol.Read read, P p) {
        Cobol.Read r = read;
        r = r.withFileName(visitAndCast(r.getFileName(), p));
        r = r.withReadInto(visitAndCast(r.getReadInto(), p));
        r = r.withReadWith(visitAndCast(r.getReadWith(), p));
        r = r.withReadKey(visitAndCast(r.getReadKey(), p));
        r = r.withInvalidKeyPhrase(visitAndCast(r.getInvalidKeyPhrase(), p));
        r = r.withNotInvalidKeyPhrase(visitAndCast(r.getNotInvalidKeyPhrase(), p));
        r = r.withAtEndPhrase(visitAndCast(r.getAtEndPhrase(), p));
        return r.withNotAtEndPhrase(visitAndCast(r.getNotAtEndPhrase(), p));
    }

    @Override
    public Cobol.ReadInto visitReadInto(Cobol.ReadInto readInto, P p) {
        Cobol.ReadInto r = readInto;
        return r.withIdentifier(visitAndCast(r.getIdentifier(), p));
    }

    @Override
    public Cobol.ReadKey visitReadKey(Cobol.ReadKey readKey, P p) {
        Cobol.ReadKey r = readKey;
        return r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.ReadWith visitReadWith(Cobol.ReadWith readWith, P p) {
        return readWith;
    }

    @Override
    public Cobol.Receivable visitReceivable(Cobol.Receivable receivable, P p) {
        Cobol.Receivable r = receivable;
        return r.withValue(visitAndCast(r.getValue(), p));
    }

    @Override
    public Cobol.Receive visitReceive(Cobol.Receive receive, P p) {
        Cobol.Receive r = receive;
        r = r.withFromOrInto(visitAndCast(r.getFromOrInto(), p));
        r = r.withOnExceptionClause(visitAndCast(r.getOnExceptionClause(), p));
        return r.withNotOnExceptionClause(visitAndCast(r.getNotOnExceptionClause(), p));
    }

    @Override
    public Cobol.ReceiveFrom visitReceiveFrom(Cobol.ReceiveFrom receiveFrom, P p) {
        Cobol.ReceiveFrom r = receiveFrom;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReceiveFromStatement visitReceiveFromStatement(Cobol.ReceiveFromStatement receiveFromStatement, P p) {
        Cobol.ReceiveFromStatement r = receiveFromStatement;
        r = r.withDataName(visitAndCast(r.getDataName(), p));
        r = r.withReceiveFrom(visitAndCast(r.getReceiveFrom(), p));
        return r.withBeforeWithThreadSizeStatus(ListUtils.map(r.getBeforeWithThreadSizeStatus(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ReceiveIntoStatement visitReceiveIntoStatement(Cobol.ReceiveIntoStatement receiveIntoStatement, P p) {
        Cobol.ReceiveIntoStatement r = receiveIntoStatement;
        r = r.withCdName(visitAndCast(r.getCdName(), p));
        r = r.withIdentifier(visitAndCast(r.getIdentifier(), p));
        r = r.withReceiveNoData(visitAndCast(r.getReceiveNoData(), p));
        return r.withReceiveWithData(visitAndCast(r.getReceiveWithData(), p));
    }

    @Override
    public Cobol.ReceiveWith visitReceiveWith(Cobol.ReceiveWith receiveWith, P p) {
        return receiveWith;
    }

    @Override
    public Cobol.RecordContainsClause visitRecordContainsClause(Cobol.RecordContainsClause recordContainsClause, P p) {
        Cobol.RecordContainsClause r = recordContainsClause;
        return r.withClause(visitAndCast(r.getClause(), p));
    }

    @Override
    public Cobol.RecordContainsClauseFormat1 visitRecordContainsClauseFormat1(Cobol.RecordContainsClauseFormat1 recordContainsClauseFormat1, P p) {
        Cobol.RecordContainsClauseFormat1 r = recordContainsClauseFormat1;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.RecordContainsClauseFormat2 visitRecordContainsClauseFormat2(Cobol.RecordContainsClauseFormat2 recordContainsClauseFormat2, P p) {
        Cobol.RecordContainsClauseFormat2 r = recordContainsClauseFormat2;
        r = r.withFromClause(ListUtils.map(r.getFromClause(), it -> visitAndCast(it, p)));
        return r.withQualifiedDataName(ListUtils.map(r.getQualifiedDataName(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.RecordContainsClauseFormat3 visitRecordContainsClauseFormat3(Cobol.RecordContainsClauseFormat3 recordContainsClauseFormat3, P p) {
        Cobol.RecordContainsClauseFormat3 r = recordContainsClauseFormat3;
        r = r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
        return r.withRecordContainsTo(visitAndCast(r.getRecordContainsTo(), p));
    }

    @Override
    public Cobol.RecordContainsTo visitRecordContainsTo(Cobol.RecordContainsTo recordContainsTo, P p) {
        Cobol.RecordContainsTo r = recordContainsTo;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.RecordDelimiterClause visitRecordDelimiterClause(Cobol.RecordDelimiterClause recordDelimiterClause, P p) {
        Cobol.RecordDelimiterClause r = recordDelimiterClause;
        return r.withName(visitAndCast(r.getName(), p));
    }

    @Override
    public Cobol.RecordingModeClause visitRecordingModeClause(Cobol.RecordingModeClause recordingModeClause, P p) {
        Cobol.RecordingModeClause r = recordingModeClause;
        return r.withMode(visitAndCast(r.getMode(), p));
    }

    @Override
    public Cobol.RecordKeyClause visitRecordKeyClause(Cobol.RecordKeyClause recordKeyClause, P p) {
        Cobol.RecordKeyClause r = recordKeyClause;
        r = r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
        return r.withPasswordClause(visitAndCast(r.getPasswordClause(), p));
    }

    @Override
    public Cobol.ReferenceModifier visitReferenceModifier(Cobol.ReferenceModifier referenceModifier, P p) {
        Cobol.ReferenceModifier r = referenceModifier;
        r = r.withCharacterPosition(visitAndCast(r.getCharacterPosition(), p));
        return r.withLength(visitAndCast(r.getLength(), p));
    }

    @Override
    public Cobol.RelationalOperator visitRelationalOperator(Cobol.RelationalOperator relationalOperator, P p) {
        return relationalOperator;
    }

    @Override
    public Cobol.RelationArithmeticComparison visitRelationArithmeticComparison(Cobol.RelationArithmeticComparison relationArithmeticComparison, P p) {
        Cobol.RelationArithmeticComparison r = relationArithmeticComparison;
        r = r.withArithmeticExpressionA(visitAndCast(r.getArithmeticExpressionA(), p));
        r = r.withRelationalOperator(visitAndCast(r.getRelationalOperator(), p));
        return r.withArithmeticExpressionB(visitAndCast(r.getArithmeticExpressionB(), p));
    }

    @Override
    public Cobol.RelationCombinedComparison visitRelationCombinedComparison(Cobol.RelationCombinedComparison relationCombinedComparison, P p) {
        Cobol.RelationCombinedComparison r = relationCombinedComparison;
        r = r.withArithmeticExpression(visitAndCast(r.getArithmeticExpression(), p));
        r = r.withRelationalOperator(visitAndCast(r.getRelationalOperator(), p));
        return r.withCombinedCondition(visitAndCast(r.getCombinedCondition(), p));
    }

    @Override
    public Cobol.RelationCombinedCondition visitRelationCombinedCondition(Cobol.RelationCombinedCondition relationCombinedCondition, P p) {
        Cobol.RelationCombinedCondition r = relationCombinedCondition;
        return r.withRelationalArithmeticExpressions(ListUtils.map(r.getRelationalArithmeticExpressions(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.RelationSignCondition visitRelationSignCondition(Cobol.RelationSignCondition relationSignCondition, P p) {
        Cobol.RelationSignCondition r = relationSignCondition;
        return r.withArithmeticExpression(visitAndCast(r.getArithmeticExpression(), p));
    }

    @Override
    public Cobol.RelativeKeyClause visitRelativeKeyClause(Cobol.RelativeKeyClause relativeKeyClause, P p) {
        Cobol.RelativeKeyClause r = relativeKeyClause;
        return r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.Release visitRelease(Cobol.Release release, P p) {
        Cobol.Release r = release;
        r = r.withRecordName(visitAndCast(r.getRecordName(), p));
        return r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.ReportClause visitReportClause(Cobol.ReportClause reportClause, P p) {
        Cobol.ReportClause r = reportClause;
        return r.withReportName(ListUtils.map(r.getReportName(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ReportDescription visitReportDescription(Cobol.ReportDescription reportDescription, P p) {
        Cobol.ReportDescription r = reportDescription;
        r = r.withReportDescriptionEntry(visitAndCast(r.getReportDescriptionEntry(), p));
        return r.withGroupDescriptionEntries(ListUtils.map(r.getGroupDescriptionEntries(), it -> visitAndCast(it, p)));
    }

    @Override
    public Cobol.ReportDescriptionEntry visitReportDescriptionEntry(Cobol.ReportDescriptionEntry reportDescriptionEntry, P p) {
        Cobol.ReportDescriptionEntry r = reportDescriptionEntry;
        r = r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
        r = r.withReportDescriptionGlobalClause(visitAndCast(r.getReportDescriptionGlobalClause(), p));
        r = r.withReportDescriptionPageLimitClause(visitAndCast(r.getReportDescriptionPageLimitClause(), p));
        r = r.withReportDescriptionHeadingClause(visitAndCast(r.getReportDescriptionHeadingClause(), p));
        r = r.withReportDescriptionFirstDetailClause(visitAndCast(r.getReportDescriptionFirstDetailClause(), p));
        r = r.withReportDescriptionLastDetailClause(visitAndCast(r.getReportDescriptionLastDetailClause(), p));
        return r.withReportDescriptionFootingClause(visitAndCast(r.getReportDescriptionFootingClause(), p));
    }

    @Override
    public Cobol.ReportDescriptionFirstDetailClause visitReportDescriptionFirstDetailClause(Cobol.ReportDescriptionFirstDetailClause reportDescriptionFirstDetailClause, P p) {
        Cobol.ReportDescriptionFirstDetailClause r = reportDescriptionFirstDetailClause;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportDescriptionFootingClause visitReportDescriptionFootingClause(Cobol.ReportDescriptionFootingClause reportDescriptionFootingClause, P p) {
        Cobol.ReportDescriptionFootingClause r = reportDescriptionFootingClause;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportDescriptionGlobalClause visitReportDescriptionGlobalClause(Cobol.ReportDescriptionGlobalClause reportDescriptionGlobalClause, P p) {
        return reportDescriptionGlobalClause;
    }

    @Override
    public Cobol.ReportDescriptionHeadingClause visitReportDescriptionHeadingClause(Cobol.ReportDescriptionHeadingClause reportDescriptionHeadingClause, P p) {
        Cobol.ReportDescriptionHeadingClause r = reportDescriptionHeadingClause;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportDescriptionLastDetailClause visitReportDescriptionLastDetailClause(Cobol.ReportDescriptionLastDetailClause reportDescriptionLastDetailClause, P p) {
        Cobol.ReportDescriptionLastDetailClause r = reportDescriptionLastDetailClause;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportDescriptionPageLimitClause visitReportDescriptionPageLimitClause(Cobol.ReportDescriptionPageLimitClause reportDescriptionPageLimitClause, P p) {
        Cobol.ReportDescriptionPageLimitClause r = reportDescriptionPageLimitClause;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.ReportGroupBlankWhenZeroClause visitReportGroupBlankWhenZeroClause(Cobol.ReportGroupBlankWhenZeroClause reportGroupBlankWhenZeroClause, P p) {
        return reportGroupBlankWhenZeroClause;
    }

    @Override
    public Cobol.ReportGroupColumnNumberClause visitReportGroupColumnNumberClause(Cobol.ReportGroupColumnNumberClause reportGroupColumnNumberClause, P p) {
        Cobol.ReportGroupColumnNumberClause r = reportGroupColumnNumberClause;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportGroupDescriptionEntryFormat1 visitReportGroupDescriptionEntryFormat1(Cobol.ReportGroupDescriptionEntryFormat1 reportGroupDescriptionEntryFormat1, P p) {
        Cobol.ReportGroupDescriptionEntryFormat1 r = reportGroupDescriptionEntryFormat1;
        r = r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
        r = r.withDataName(visitAndCast(r.getDataName(), p));
        r = r.withGroupLineNumberClause(visitAndCast(r.getGroupLineNumberClause(), p));
        r = r.withGroupNextGroupClause(visitAndCast(r.getGroupNextGroupClause(), p));
        r = r.withGroupTypeClause(visitAndCast(r.getGroupTypeClause(), p));
        return r.withGroupUsageClause(visitAndCast(r.getGroupUsageClause(), p));
    }

    @Override
    public Cobol.ReportGroupDescriptionEntryFormat2 visitReportGroupDescriptionEntryFormat2(Cobol.ReportGroupDescriptionEntryFormat2 reportGroupDescriptionEntryFormat2, P p) {
        Cobol.ReportGroupDescriptionEntryFormat2 r = reportGroupDescriptionEntryFormat2;
        r = r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
        r = r.withDataName(visitAndCast(r.getDataName(), p));
        r = r.withReportGroupLineNumberClause(visitAndCast(r.getReportGroupLineNumberClause(), p));
        return r.withGroupUsageClause(visitAndCast(r.getGroupUsageClause(), p));
    }

    @Override
    public Cobol.ReportGroupDescriptionEntryFormat3 visitReportGroupDescriptionEntryFormat3(Cobol.ReportGroupDescriptionEntryFormat3 reportGroupDescriptionEntryFormat3, P p) {
        Cobol.ReportGroupDescriptionEntryFormat3 r = reportGroupDescriptionEntryFormat3;
        r = r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
        r = r.withDataName(visitAndCast(r.getDataName(), p));
        return r.withClauses(ListUtils.map(r.getClauses(), c -> visitAndCast(c, p)));
    }

    @Override
    public Cobol.ReportGroupIndicateClause visitReportGroupIndicateClause(Cobol.ReportGroupIndicateClause reportGroupIndicateClause, P p) {
        return reportGroupIndicateClause;
    }

    @Override
    public Cobol.ReportGroupJustifiedClause visitReportGroupJustifiedClause(Cobol.ReportGroupJustifiedClause reportGroupJustifiedClause, P p) {
        return reportGroupJustifiedClause;
    }

    @Override
    public Cobol.ReportGroupLineNumberClause visitReportGroupLineNumberClause(Cobol.ReportGroupLineNumberClause reportGroupLineNumberClause, P p) {
        Cobol.ReportGroupLineNumberClause r = reportGroupLineNumberClause;
        return r.withClause(visitAndCast(r.getClause(), p));
    }

    @Override
    public Cobol.ReportGroupLineNumberNextPage visitReportGroupLineNumberNextPage(Cobol.ReportGroupLineNumberNextPage reportGroupLineNumberNextPage, P p) {
        Cobol.ReportGroupLineNumberNextPage r = reportGroupLineNumberNextPage;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.ReportGroupLineNumberPlus visitReportGroupLineNumberPlus(Cobol.ReportGroupLineNumberPlus reportGroupLineNumberPlus, P p) {
        Cobol.ReportGroupLineNumberPlus r = reportGroupLineNumberPlus;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.ReportGroupNextGroupClause visitReportGroupNextGroupClause(Cobol.ReportGroupNextGroupClause reportGroupNextGroupClause, P p) {
        Cobol.ReportGroupNextGroupClause r = reportGroupNextGroupClause;
        return r.withClause(visitAndCast(r.getClause(), p));
    }

    @Override
    public Cobol.ReportGroupNextGroupNextPage visitReportGroupNextGroupNextPage(Cobol.ReportGroupNextGroupNextPage reportGroupNextGroupNextPage, P p) {
        return reportGroupNextGroupNextPage;
    }

    @Override
    public Cobol.ReportGroupNextGroupPlus visitReportGroupNextGroupPlus(Cobol.ReportGroupNextGroupPlus reportGroupNextGroupPlus, P p) {
        Cobol.ReportGroupNextGroupPlus r = reportGroupNextGroupPlus;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.ReportGroupPictureClause visitReportGroupPictureClause(Cobol.ReportGroupPictureClause reportGroupPictureClause, P p) {
        Cobol.ReportGroupPictureClause r = reportGroupPictureClause;
        return r.withPictureString(visitAndCast(r.getPictureString(), p));
    }

    @Override
    public Cobol.ReportGroupResetClause visitReportGroupResetClause(Cobol.ReportGroupResetClause reportGroupResetClause, P p) {
        Cobol.ReportGroupResetClause r = reportGroupResetClause;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportGroupSignClause visitReportGroupSignClause(Cobol.ReportGroupSignClause reportGroupSignClause, P p) {
        return reportGroupSignClause;
    }

    @Override
    public Cobol.ReportGroupSourceClause visitReportGroupSourceClause(Cobol.ReportGroupSourceClause reportGroupSourceClause, P p) {
        Cobol.ReportGroupSourceClause r = reportGroupSourceClause;
        return r.withIdentifier(visitAndCast(r.getIdentifier(), p));
    }

    @Override
    public Cobol.ReportGroupSumClause visitReportGroupSumClause(Cobol.ReportGroupSumClause reportGroupSumClause, P p) {
        Cobol.ReportGroupSumClause r = reportGroupSumClause;
        return r.withCobols(ListUtils.map(r.getCobols(), c -> visitAndCast(c, p)));
    }

    @Override
    public Cobol.ReportGroupTypeClause visitReportGroupTypeClause(Cobol.ReportGroupTypeClause reportGroupTypeClause, P p) {
        Cobol.ReportGroupTypeClause r = reportGroupTypeClause;
        return r.withType(visitAndCast(r.getType(), p));
    }

    @Override
    public Cobol.ReportGroupTypeControlFooting visitReportGroupTypeControlFooting(Cobol.ReportGroupTypeControlFooting reportGroupTypeControlFooting, P p) {
        Cobol.ReportGroupTypeControlFooting r = reportGroupTypeControlFooting;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportGroupTypeControlHeading visitReportGroupTypeControlHeading(Cobol.ReportGroupTypeControlHeading reportGroupTypeControlHeading, P p) {
        Cobol.ReportGroupTypeControlHeading r = reportGroupTypeControlHeading;
        return r.withDataName(visitAndCast(r.getDataName(), p));
    }

    @Override
    public Cobol.ReportGroupTypeDetail visitReportGroupTypeDetail(Cobol.ReportGroupTypeDetail reportGroupTypeDetail, P p) {
        return reportGroupTypeDetail;
    }

    @Override
    public Cobol.ReportGroupTypePageFooting visitReportGroupTypePageFooting(Cobol.ReportGroupTypePageFooting reportGroupTypePageFooting, P p) {
        return reportGroupTypePageFooting;
    }

    @Override
    public Cobol.ReportGroupTypePageHeading visitReportGroupTypePageHeading(Cobol.ReportGroupTypePageHeading reportGroupTypePageHeading, P p) {
        return reportGroupTypePageHeading;
    }

    @Override
    public Cobol.ReportGroupTypeReportFooting visitReportGroupTypeReportFooting(Cobol.ReportGroupTypeReportFooting reportGroupTypeReportFooting, P p) {
        return reportGroupTypeReportFooting;
    }

    @Override
    public Cobol.ReportGroupTypeReportHeading visitReportGroupTypeReportHeading(Cobol.ReportGroupTypeReportHeading reportGroupTypeReportHeading, P p) {
        return reportGroupTypeReportHeading;
    }

    @Override
    public Cobol.ReportGroupUsageClause visitReportGroupUsageClause(Cobol.ReportGroupUsageClause reportGroupUsageClause, P p) {
        return reportGroupUsageClause;
    }

    @Override
    public Cobol.ReportGroupValueClause visitReportGroupValueClause(Cobol.ReportGroupValueClause reportGroupValueClause, P p) {
        Cobol.ReportGroupValueClause r = reportGroupValueClause;
        return r.withLiteral(visitAndCast(r.getLiteral(), p));
    }

    @Override
    public Cobol.ReportName visitReportName(Cobol.ReportName reportName, P p) {
        Cobol.ReportName r = reportName;
        return r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.ReportSection visitReportSection(Cobol.ReportSection reportSection, P p) {
        Cobol.ReportSection r = reportSection;
        return r.withDescriptions(ListUtils.map(r.getDescriptions(), d -> visitAndCast(d, p)));
    }

    @Override
    public Cobol.RerunClause visitRerunClause(Cobol.RerunClause rerunClause, P p) {
        Cobol.RerunClause r = rerunClause;
        r = r.withName(visitAndCast(r.getName(), p));
        return r.withAction(visitAndCast(r.getAction(), p));
    }

    @Override
    public Cobol.RerunEveryClock visitRerunEveryClock(Cobol.RerunEveryClock rerunEveryClock, P p) {
        Cobol.RerunEveryClock r = rerunEveryClock;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.RerunEveryOf visitRerunEveryOf(Cobol.RerunEveryOf rerunEveryOf, P p) {
        Cobol.RerunEveryOf r = rerunEveryOf;
        return r.withFileName(visitAndCast(r.getFileName(), p));
    }

    @Override
    public Cobol.RerunEveryRecords visitRerunEveryRecords(Cobol.RerunEveryRecords rerunEveryRecords, P p) {
        Cobol.RerunEveryRecords r = rerunEveryRecords;
        return r.withIntegerLiteral(visitAndCast(r.getIntegerLiteral(), p));
    }

    @Override
    public Cobol.ReserveClause visitReserveClause(Cobol.ReserveClause reserveClause, P p) {
        Cobol.ReserveClause r = reserveClause;
        return r.withWords(ListUtils.map(r.getWords(), w -> visitAndCast(w, p)));
    }

    @Override
    public Cobol.ReserveNetworkClause visitReserveNetworkClause(Cobol.ReserveNetworkClause reserveNetworkClause, P p) {
        return reserveNetworkClause;
    }

    @Override
    public Cobol.Return visitReturn(Cobol.Return returnz, P p) {
        Cobol.Return r = returnz;
        r = r.withFileName(visitAndCast(r.getFileName(), p));
        r = r.withInto(visitAndCast(r.getInto(), p));
        r = r.withAtEndPhrase(visitAndCast(r.getAtEndPhrase(), p));
        return r.withNotAtEndPhrase(visitAndCast(r.getNotAtEndPhrase(), p));
    }

    @Override
    public Cobol.ReturnInto visitReturnInto(Cobol.ReturnInto returnInto, P p) {
        Cobol.ReturnInto r = returnInto;
        return r.withQualifiedDataName(visitAndCast(r.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.Rewrite visitRewrite(Cobol.Rewrite rewrite, P p) {
        Cobol.Rewrite r = rewrite;
        r = r.withRecordName(visitAndCast(r.getRecordName(), p));
        r = r.withRewriteFrom(visitAndCast(r.getRewriteFrom(), p));
        r = r.withInvalidKeyPhrase(visitAndCast(r.getInvalidKeyPhrase(), p));
        return r.withNotInvalidKeyPhrase(visitAndCast(r.getNotInvalidKeyPhrase(), p));
    }

    @Override
    public Cobol.RewriteFrom visitRewriteFrom(Cobol.RewriteFrom rewriteFrom, P p) {
        Cobol.RewriteFrom r = rewriteFrom;
        return r.withIdentifier(visitAndCast(r.getIdentifier(), p));
    }

    @Override
    public Cobol.Roundable visitRoundable(Cobol.Roundable roundable, P p) {
        Cobol.Roundable r = roundable;
        return r.withIdentifier(visitAndCast(r.getIdentifier(), p));
    }

    @Override
    public Cobol.SameClause visitSameClause(Cobol.SameClause sameClause, P p) {
        Cobol.SameClause s = sameClause;
        return s.withFileNames(ListUtils.map(s.getFileNames(), f -> visitAndCast(f, p)));
    }

    @Override
    public Cobol.ScreenDescriptionAutoClause visitScreenDescriptionAutoClause(Cobol.ScreenDescriptionAutoClause screenDescriptionAutoClause, P p) {
        return screenDescriptionAutoClause;
    }

    @Override
    public Cobol.ScreenDescriptionBackgroundColorClause visitScreenDescriptionBackgroundColorClause(Cobol.ScreenDescriptionBackgroundColorClause screenDescriptionBackgroundColorClause, P p) {
        Cobol.ScreenDescriptionBackgroundColorClause s = screenDescriptionBackgroundColorClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionBellClause visitScreenDescriptionBellClause(Cobol.ScreenDescriptionBellClause screenDescriptionBellClause, P p) {
        return screenDescriptionBellClause;
    }

    @Override
    public Cobol.ScreenDescriptionBlankClause visitScreenDescriptionBlankClause(Cobol.ScreenDescriptionBlankClause screenDescriptionBlankClause, P p) {
        return screenDescriptionBlankClause;
    }

    @Override
    public Cobol.ScreenDescriptionBlankWhenZeroClause visitScreenDescriptionBlankWhenZeroClause(Cobol.ScreenDescriptionBlankWhenZeroClause screenDescriptionBlankWhenZeroClause, P p) {
        return screenDescriptionBlankWhenZeroClause;
    }

    @Override
    public Cobol.ScreenDescriptionBlinkClause visitScreenDescriptionBlinkClause(Cobol.ScreenDescriptionBlinkClause screenDescriptionBlinkClause, P p) {
        return screenDescriptionBlinkClause;
    }

    @Override
    public Cobol.ScreenDescriptionColumnClause visitScreenDescriptionColumnClause(Cobol.ScreenDescriptionColumnClause screenDescriptionColumnClause, P p) {
        Cobol.ScreenDescriptionColumnClause s = screenDescriptionColumnClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionControlClause visitScreenDescriptionControlClause(Cobol.ScreenDescriptionControlClause screenDescriptionControlClause, P p) {
        Cobol.ScreenDescriptionControlClause s = screenDescriptionControlClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionEntry visitScreenDescriptionEntry(Cobol.ScreenDescriptionEntry screenDescriptionEntry, P p) {
        Cobol.ScreenDescriptionEntry s = screenDescriptionEntry;
        s = s.withName(visitAndCast(s.getName(), p));
        return s.withClauses(ListUtils.map(s.getClauses(), c -> visitAndCast(c, p)));
    }

    @Override
    public Cobol.ScreenDescriptionEraseClause visitScreenDescriptionEraseClause(Cobol.ScreenDescriptionEraseClause screenDescriptionEraseClause, P p) {
        return screenDescriptionEraseClause;
    }

    @Override
    public Cobol.ScreenDescriptionForegroundColorClause visitScreenDescriptionForegroundColorClause(Cobol.ScreenDescriptionForegroundColorClause screenDescriptionForegroundColorClause, P p) {
        Cobol.ScreenDescriptionForegroundColorClause s = screenDescriptionForegroundColorClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionFromClause visitScreenDescriptionFromClause(Cobol.ScreenDescriptionFromClause screenDescriptionFromClause, P p) {
        Cobol.ScreenDescriptionFromClause s = screenDescriptionFromClause;
        s = s.withValue(visitAndCast(s.getValue(), p));
        return s.withScreenDescriptionToClause(visitAndCast(s.getScreenDescriptionToClause(), p));
    }

    @Override
    public Cobol.ScreenDescriptionFullClause visitScreenDescriptionFullClause(Cobol.ScreenDescriptionFullClause screenDescriptionFullClause, P p) {
        return screenDescriptionFullClause;
    }

    @Override
    public Cobol.ScreenDescriptionGridClause visitScreenDescriptionGridClause(Cobol.ScreenDescriptionGridClause screenDescriptionGridClause, P p) {
        return screenDescriptionGridClause;
    }

    @Override
    public Cobol.ScreenDescriptionJustifiedClause visitScreenDescriptionJustifiedClause(Cobol.ScreenDescriptionJustifiedClause screenDescriptionJustifiedClause, P p) {
        return screenDescriptionJustifiedClause;
    }

    @Override
    public Cobol.ScreenDescriptionLightClause visitScreenDescriptionLightClause(Cobol.ScreenDescriptionLightClause screenDescriptionLightClause, P p) {
        return screenDescriptionLightClause;
    }

    @Override
    public Cobol.ScreenDescriptionLineClause visitScreenDescriptionLineClause(Cobol.ScreenDescriptionLineClause screenDescriptionLineClause, P p) {
        Cobol.ScreenDescriptionLineClause s = screenDescriptionLineClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionPictureClause visitScreenDescriptionPictureClause(Cobol.ScreenDescriptionPictureClause screenDescriptionPictureClause, P p) {
        Cobol.ScreenDescriptionPictureClause s = screenDescriptionPictureClause;
        return s.withPictureString(visitAndCast(s.getPictureString(), p));
    }

    @Override
    public Cobol.ScreenDescriptionReverseVideoClause visitScreenDescriptionReverseVideoClause(Cobol.ScreenDescriptionReverseVideoClause screenDescriptionReverseVideoClause, P p) {
        return screenDescriptionReverseVideoClause;
    }

    @Override
    public Cobol.ScreenDescriptionPromptClause visitScreenDescriptionPromptClause(Cobol.ScreenDescriptionPromptClause screenDescriptionPromptClause, P p) {
        Cobol.ScreenDescriptionPromptClause s = screenDescriptionPromptClause;
        s = s.withName(visitAndCast(s.getName(), p));
        return s.withScreenDescriptionPromptOccursClause(visitAndCast(s.getScreenDescriptionPromptOccursClause(), p));
    }

    @Override
    public Cobol.ScreenDescriptionPromptOccursClause visitScreenDescriptionPromptOccursClause(Cobol.ScreenDescriptionPromptOccursClause screenDescriptionPromptOccursClause, P p) {
        Cobol.ScreenDescriptionPromptOccursClause s = screenDescriptionPromptOccursClause;
        return s.withInteger(visitAndCast(s.getInteger(), p));
    }

    @Override
    public Cobol.ScreenDescriptionRequiredClause visitScreenDescriptionRequiredClause(Cobol.ScreenDescriptionRequiredClause screenDescriptionRequiredClause, P p) {
        return screenDescriptionRequiredClause;
    }

    @Override
    public Cobol.ScreenDescriptionSecureClause visitScreenDescriptionSecureClause(Cobol.ScreenDescriptionSecureClause screenDescriptionSecureClause, P p) {
        return screenDescriptionSecureClause;
    }

    @Override
    public Cobol.ScreenDescriptionSignClause visitScreenDescriptionSignClause(Cobol.ScreenDescriptionSignClause screenDescriptionSignClause, P p) {
        return screenDescriptionSignClause;
    }

    @Override
    public Cobol.ScreenDescriptionSizeClause visitScreenDescriptionSizeClause(Cobol.ScreenDescriptionSizeClause screenDescriptionSizeClause, P p) {
        Cobol.ScreenDescriptionSizeClause s = screenDescriptionSizeClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionToClause visitScreenDescriptionToClause(Cobol.ScreenDescriptionToClause screenDescriptionToClause, P p) {
        Cobol.ScreenDescriptionToClause s = screenDescriptionToClause;
        return s.withIdentifier(visitAndCast(s.getIdentifier(), p));
    }

    @Override
    public Cobol.ScreenDescriptionValueClause visitScreenDescriptionValueClause(Cobol.ScreenDescriptionValueClause screenDescriptionValueClause, P p) {
        Cobol.ScreenDescriptionValueClause s = screenDescriptionValueClause;
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.ScreenDescriptionUnderlineClause visitScreenDescriptionUnderlineClause(Cobol.ScreenDescriptionUnderlineClause screenDescriptionUnderlineClause, P p) {
        return screenDescriptionUnderlineClause;
    }

    @Override
    public Cobol.ScreenDescriptionUsageClause visitScreenDescriptionUsageClause(Cobol.ScreenDescriptionUsageClause screenDescriptionUsageClause, P p) {
        return screenDescriptionUsageClause;
    }

    @Override
    public Cobol.SearchVarying visitSearchVarying(Cobol.SearchVarying searchVarying, P p) {
        Cobol.SearchVarying s = searchVarying;
        return s.withQualifiedDataName(visitAndCast(s.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.ScreenDescriptionUsingClause visitScreenDescriptionUsingClause(Cobol.ScreenDescriptionUsingClause screenDescriptionUsingClause, P p) {
        Cobol.ScreenDescriptionUsingClause s = screenDescriptionUsingClause;
        return s.withIdentifier(visitAndCast(s.getIdentifier(), p));
    }

    @Override
    public Cobol.Search visitSearch(Cobol.Search search, P p) {
        Cobol.Search s = search;
        s = s.withQualifiedDataName(visitAndCast(s.getQualifiedDataName(), p));
        s = s.withSearchVarying(visitAndCast(s.getSearchVarying(), p));
        s = s.withAtEndPhrase(visitAndCast(s.getAtEndPhrase(), p));
        return s.withSearchWhen(ListUtils.map(s.getSearchWhen(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.ScreenDescriptionZeroFillClause visitScreenDescriptionZeroFillClause(Cobol.ScreenDescriptionZeroFillClause screenDescriptionZeroFillClause, P p) {
        return screenDescriptionZeroFillClause;
    }

    @Override
    public Cobol.ScreenSection visitScreenSection(Cobol.ScreenSection screenSection, P p) {
        Cobol.ScreenSection s = screenSection;
        return s.withDescriptions(ListUtils.map(s.getDescriptions(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.SearchWhen visitSearchWhen(Cobol.SearchWhen searchWhen, P p) {
        Cobol.SearchWhen s = searchWhen;
        s = s.withCondition(visitAndCast(s.getCondition(), p));
        return s.withStatements(ListUtils.map(s.getStatements(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.Send visitSend(Cobol.Send send, P p) {
        Cobol.Send s = send;
        s = s.withStatement(visitAndCast(s.getStatement(), p));
        s = s.withOnExceptionClause(visitAndCast(s.getOnExceptionClause(), p));
        return s.withNotOnExceptionClause(visitAndCast(s.getNotOnExceptionClause(), p));
    }

    @Override
    public Cobol.SelectClause visitSelectClause(Cobol.SelectClause selectClause, P p) {
        Cobol.SelectClause s = selectClause;
        return s.withFileName(visitAndCast(s.getFileName(), p));
    }

    @Override
    public Cobol.SendAdvancingLines visitSendAdvancingLines(Cobol.SendAdvancingLines sendAdvancingLines, P p) {
        Cobol.SendAdvancingLines s = sendAdvancingLines;
        return s.withName(visitAndCast(s.getName(), p));
    }

    @Override
    public Cobol.SendPhrase visitSendPhrase(Cobol.SendPhrase sendPhrase, P p) {
        Cobol.SendPhrase s = sendPhrase;
        return s.withTarget(visitAndCast(s.getTarget(), p));
    }

    @Override
    public Cobol.Sentence visitSentence(Cobol.Sentence sentence, P p) {
        Cobol.Sentence s = sentence;
        return s.withStatements(ListUtils.map(s.getStatements(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.Set visitSet(Cobol.Set set, P p) {
        Cobol.Set s = set;
        s = s.withTo(ListUtils.map(s.getTo(), e -> visitAndCast(e, p)));
        return s.withUpDown(visitAndCast(s.getUpDown(), p));
    }

    @Override
    public Cobol.SetTo visitSetTo(Cobol.SetTo setTo, P p) {
        Cobol.SetTo s = setTo;
        s = s.withIdentifiers(ListUtils.map(s.getIdentifiers(), e -> visitAndCast(e, p)));
        return s.withValues(ListUtils.map(s.getValues(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.Sort visitSort(Cobol.Sort sort, P p) {
        Cobol.Sort s = sort;
        s = s.withFileName(visitAndCast(s.getFileName(), p));
        s = s.withSortOnKeyClause(ListUtils.map(s.getSortOnKeyClause(), e -> visitAndCast(e, p)));
        s = s.withSortDuplicatesPhrase(visitAndCast(s.getSortDuplicatesPhrase(), p));
        s = s.withSortCollatingSequencePhrase(visitAndCast(s.getSortCollatingSequencePhrase(), p));
        s = s.withSortInputProcedurePhrase(visitAndCast(s.getSortInputProcedurePhrase(), p));
        s = s.withSortUsing(ListUtils.map(s.getSortUsing(), e -> visitAndCast(e, p)));
        s = s.withSortOutputProcedurePhrase(visitAndCast(s.getSortOutputProcedurePhrase(), p));
        return s.withSortGiving(ListUtils.map(s.getSortGiving(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.Sortable visitSortable(Cobol.Sortable sortable, P p) {
        Cobol.Sortable s = sortable;
        return s.withNames(ListUtils.map(s.getNames(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.SendStatementSync visitSendStatementSync(Cobol.SendStatementSync sendStatementSync, P p) {
        Cobol.SendStatementSync s = sendStatementSync;
        s = s.withName(visitAndCast(s.getName(), p));
        s = s.withSendFromPhrase(visitAndCast(s.getSendFromPhrase(), p));
        s = s.withSendWithPhrase(visitAndCast(s.getSendWithPhrase(), p));
        s = s.withSendReplacingPhrase(visitAndCast(s.getSendReplacingPhrase(), p));
        return s.withSendAdvancingPhrase(visitAndCast(s.getSendAdvancingPhrase(), p));
    }

    @Override
    public Cobol.SetUpDown visitSetUpDown(Cobol.SetUpDown setUpDown, P p) {
        Cobol.SetUpDown s = setUpDown;
        s = s.withTo(ListUtils.map(s.getTo(), e -> visitAndCast(e, p)));
        return s.withValue(visitAndCast(s.getValue(), p));
    }

    @Override
    public Cobol.SpecialNames visitSpecialNames(Cobol.SpecialNames specialNames, P p) {
        Cobol.SpecialNames s = specialNames;
        return s.withClauses(ListUtils.map(s.getClauses(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.SortCollatingSequencePhrase visitSortCollatingSequencePhrase(Cobol.SortCollatingSequencePhrase sortCollatingSequencePhrase, P p) {
        Cobol.SortCollatingSequencePhrase s = sortCollatingSequencePhrase;
        s = s.withAlphabetNames(ListUtils.map(s.getAlphabetNames(), e -> visitAndCast(e, p)));
        s = s.withSortCollatingAlphanumeric(visitAndCast(s.getSortCollatingAlphanumeric(), p));
        return s.withSortCollatingNational(visitAndCast(s.getSortCollatingNational(), p));
    }

    @Override
    public Cobol.SortGiving visitSortGiving(Cobol.SortGiving sortGiving, P p) {
        Cobol.SortGiving s = sortGiving;
        return s.withFileName(visitAndCast(s.getFileName(), p));
    }

    @Override
    public Cobol.SortProcedurePhrase visitSortProcedurePhrase(Cobol.SortProcedurePhrase sortProcedurePhrase, P p) {
        Cobol.SortProcedurePhrase s = sortProcedurePhrase;
        s = s.withProcedureName(visitAndCast(s.getProcedureName(), p));
        return s.withSortInputThrough(visitAndCast(s.getSortInputThrough(), p));
    }

    @Override
    public Cobol.Start visitStart(Cobol.Start start, P p) {
        Cobol.Start s = start;
        s = s.withFileName(visitAndCast(s.getFileName(), p));
        s = s.withStartKey(visitAndCast(s.getStartKey(), p));
        s = s.withInvalidKeyPhrase(visitAndCast(s.getInvalidKeyPhrase(), p));
        return s.withNotInvalidKeyPhrase(visitAndCast(s.getNotInvalidKeyPhrase(), p));
    }

    @Override
    public Cobol.SourceComputer visitSourceComputer(Cobol.SourceComputer sourceComputer, P p) {
        Cobol.SourceComputer s = sourceComputer;
        return s.withComputer(visitAndCast(s.getComputer(), p));
    }

    @Override
    public Cobol.StartKey visitStartKey(Cobol.StartKey startKey, P p) {
        Cobol.StartKey s = startKey;
        return s.withQualifiedDataName(visitAndCast(s.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.SourceComputerDefinition visitSourceComputerDefinition(Cobol.SourceComputerDefinition sourceComputerDefinition, P p) {
        Cobol.SourceComputerDefinition s = sourceComputerDefinition;
        return s.withComputerName(visitAndCast(s.getComputerName(), p));
    }

    @Override
    public Cobol.Stop visitStop(Cobol.Stop stop, P p) {
        Cobol.Stop s = stop;
        return s.withStatement(visitAndCast(s.getStatement(), p));
    }

    @Override
    public Cobol.StatementPhrase visitStatementPhrase(Cobol.StatementPhrase statementPhrase, P p) {
        Cobol.StatementPhrase s = statementPhrase;
        return s.withStatements(ListUtils.map(s.getStatements(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.StatusKeyClause visitStatusKeyClause(Cobol.StatusKeyClause statusKeyClause, P p) {
        Cobol.StatusKeyClause s = statusKeyClause;
        return s.withName(visitAndCast(s.getName(), p));
    }

    @Override
    public Cobol.StopStatementGiving visitStopStatementGiving(Cobol.StopStatementGiving stopStatementGiving, P p) {
        Cobol.StopStatementGiving s = stopStatementGiving;
        return s.withName(visitAndCast(s.getName(), p));
    }

    @Override
    public Cobol.StringDelimitedByPhrase visitStringDelimitedByPhrase(Cobol.StringDelimitedByPhrase stringDelimitedByPhrase, P p) {
        Cobol.StringDelimitedByPhrase s = stringDelimitedByPhrase;
        return s.withIdentifier(visitAndCast(s.getIdentifier(), p));
    }

    @Override
    public Cobol.StringForPhrase visitStringForPhrase(Cobol.StringForPhrase stringForPhrase, P p) {
        Cobol.StringForPhrase s = stringForPhrase;
        return s.withIdentifier(visitAndCast(s.getIdentifier(), p));
    }

    @Override
    public Cobol.StringIntoPhrase visitStringIntoPhrase(Cobol.StringIntoPhrase stringIntoPhrase, P p) {
        Cobol.StringIntoPhrase s = stringIntoPhrase;
        return s.withIdentifier(visitAndCast(s.getIdentifier(), p));
    }

    @Override
    public Cobol.StringSendingPhrase visitStringSendingPhrase(Cobol.StringSendingPhrase stringSendingPhrase, P p) {
        Cobol.StringSendingPhrase s = stringSendingPhrase;
        s = s.withSendings(ListUtils.map(s.getSendings(), e -> visitAndCast(e, p)));
        return s.withPhrase(visitAndCast(s.getPhrase(), p));
    }

    @Override
    public Cobol.StringStatement visitStringStatement(Cobol.StringStatement stringStatement, P p) {
        Cobol.StringStatement s = stringStatement;
        s = s.withStringSendingPhrases(ListUtils.map(s.getStringSendingPhrases(), e -> visitAndCast(e, p)));
        s = s.withStringIntoPhrase(visitAndCast(s.getStringIntoPhrase(), p));
        s = s.withStringWithPointerPhrase(visitAndCast(s.getStringWithPointerPhrase(), p));
        s = s.withOnOverflowPhrase(visitAndCast(s.getOnOverflowPhrase(), p));
        return s.withNotOnOverflowPhrase(visitAndCast(s.getNotOnOverflowPhrase(), p));
    }

    @Override
    public Cobol.Subscript visitSubscript(Cobol.Subscript subscript, P p) {
        Cobol.Subscript s = subscript;
        s = s.withFirst(visitAndCast(s.getFirst(), p));
        return s.withSecond(visitAndCast(s.getSecond(), p));
    }

    @Override
    public Cobol.Subtract visitSubtract(Cobol.Subtract subtract, P p) {
        Cobol.Subtract s = subtract;
        s = s.withOperation(visitAndCast(s.getOperation(), p));
        s = s.withOnSizeErrorPhrase(visitAndCast(s.getOnSizeErrorPhrase(), p));
        return s.withNotOnSizeErrorPhrase(visitAndCast(s.getOnSizeErrorPhrase(), p));
    }

    @Override
    public Cobol.StringWithPointerPhrase visitStringWithPointerPhrase(Cobol.StringWithPointerPhrase stringWithPointerPhrase, P p) {
        Cobol.StringWithPointerPhrase s = stringWithPointerPhrase;
        return s.withQualifiedDataName(visitAndCast(s.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.SubtractCorrespondingStatement visitSubtractCorrespondingStatement(Cobol.SubtractCorrespondingStatement subtractCorrespondingStatement, P p) {
        Cobol.SubtractCorrespondingStatement s = subtractCorrespondingStatement;
        s = s.withQualifiedDataName(visitAndCast(s.getQualifiedDataName(), p));
        return s.withSubtractMinuendCorresponding(visitAndCast(s.getSubtractMinuendCorresponding(), p));
    }

    @Override
    public Cobol.SubtractFromGivingStatement visitSubtractFromGivingStatement(Cobol.SubtractFromGivingStatement subtractFromGivingStatement, P p) {
        Cobol.SubtractFromGivingStatement s = subtractFromGivingStatement;
        s = s.withSubtractSubtrahend(ListUtils.map(s.getSubtractSubtrahend(), e -> visitAndCast(e, p)));
        s = s.withSubtractMinuendGiving(visitAndCast(s.getSubtractMinuendGiving(), p));
        return s.withSubtractGiving(ListUtils.map(s.getSubtractGiving(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.SubtractFromStatement visitSubtractFromStatement(Cobol.SubtractFromStatement subtractFromStatement, P p) {
        Cobol.SubtractFromStatement s = subtractFromStatement;
        s = s.withSubtractSubtrahend(ListUtils.map(s.getSubtractSubtrahend(), e -> visitAndCast(e, p)));
        return s.withSubtractMinuend(ListUtils.map(s.getSubtractMinuend(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.SubtractMinuendCorresponding visitSubtractMinuendCorresponding(Cobol.SubtractMinuendCorresponding subtractMinuendCorresponding, P p) {
        Cobol.SubtractMinuendCorresponding s = subtractMinuendCorresponding;
        return s.withQualifiedDataName(visitAndCast(s.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.SymbolicCharacter visitSymbolicCharacter(Cobol.SymbolicCharacter symbolicCharacter, P p) {
        Cobol.SymbolicCharacter s = symbolicCharacter;
        s = s.withSymbols(ListUtils.map(s.getSymbols(), e -> visitAndCast(e, p)));
        return s.withLiterals(ListUtils.map(s.getLiterals(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.SymbolicCharactersClause visitSymbolicCharactersClause(Cobol.SymbolicCharactersClause symbolicCharactersClause, P p) {
        Cobol.SymbolicCharactersClause s = symbolicCharactersClause;
        s = s.withSymbols(ListUtils.map(s.getSymbols(), e -> visitAndCast(e, p)));
        return s.withAlphabetName(visitAndCast(s.getAlphabetName(), p));
    }

    @Override
    public Cobol.SymbolicDestinationClause visitSymbolicDestinationClause(Cobol.SymbolicDestinationClause symbolicDestinationClause, P p) {
        Cobol.SymbolicDestinationClause s = symbolicDestinationClause;
        return s.withDataDescName(visitAndCast(s.getDataDescName(), p));
    }

    @Override
    public Cobol.SymbolicQueueClause visitSymbolicQueueClause(Cobol.SymbolicQueueClause symbolicQueueClause, P p) {
        Cobol.SymbolicQueueClause s = symbolicQueueClause;
        return s.withDataDescName(visitAndCast(s.getDataDescName(), p));
    }

    @Override
    public Cobol.SymbolicSourceClause visitSymbolicSourceClause(Cobol.SymbolicSourceClause symbolicSourceClause, P p) {
        Cobol.SymbolicSourceClause s = symbolicSourceClause;
        return s.withDataDescName(visitAndCast(s.getDataDescName(), p));
    }

    @Override
    public Cobol.SymbolicSubQueueClause visitSymbolicSubQueueClause(Cobol.SymbolicSubQueueClause symbolicSubQueueClause, P p) {
        Cobol.SymbolicSubQueueClause s = symbolicSubQueueClause;
        return s.withDataDescName(visitAndCast(s.getDataDescName(), p));
    }

    @Override
    public Cobol.SymbolicTerminalClause visitSymbolicTerminalClause(Cobol.SymbolicTerminalClause symbolicTerminalClause, P p) {
        Cobol.SymbolicTerminalClause s = symbolicTerminalClause;
        return s.withDataDescName(visitAndCast(s.getDataDescName(), p));
    }

    @Override
    public Cobol.TableCall visitTableCall(Cobol.TableCall tableCall, P p) {
        Cobol.TableCall t = tableCall;
        t = t.withQualifiedDataName(visitAndCast(t.getQualifiedDataName(), p));
        t = t.withSubscripts(ListUtils.map(t.getSubscripts(), e -> visitAndCast(e, p)));
        return t.withReferenceModifier(visitAndCast(t.getReferenceModifier(), p));
    }

    @Override
    public Cobol.Terminate visitTerminate(Cobol.Terminate terminate, P p) {
        Cobol.Terminate t = terminate;
        return t.withReportName(visitAndCast(t.getReportName(), p));
    }

    @Override
    public Cobol.TextLengthClause visitTextLengthClause(Cobol.TextLengthClause textLengthClause, P p) {
        Cobol.TextLengthClause t = textLengthClause;
        return t.withDataDescName(visitAndCast(t.getDataDescName(), p));
    }

    @Override
    public Cobol.UnstringTallyingPhrase visitUnstringTallyingPhrase(Cobol.UnstringTallyingPhrase unstringTallyingPhrase, P p) {
        Cobol.UnstringTallyingPhrase u = unstringTallyingPhrase;
        return u.withQualifiedDataName(visitAndCast(u.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.UnString visitUnString(Cobol.UnString unString, P p) {
        Cobol.UnString u = unString;
        u = u.withUnstringSendingPhrase(visitAndCast(u.getUnstringSendingPhrase(), p));
        u = u.withUnstringIntoPhrase(visitAndCast(u.getUnstringIntoPhrase(), p));
        u = u.withUnstringWithPointerPhrase(visitAndCast(u.getUnstringWithPointerPhrase(), p));
        u = u.withUnstringTallyingPhrase(visitAndCast(u.getUnstringTallyingPhrase(), p));
        u = u.withOnOverflowPhrase(visitAndCast(u.getOnOverflowPhrase(), p));
        return u.withNotOnOverflowPhrase(visitAndCast(u.getNotOnOverflowPhrase(), p));
    }

    @Override
    public Cobol.UnstringCountIn visitUnstringCountIn(Cobol.UnstringCountIn unstringCountIn, P p) {
        Cobol.UnstringCountIn u = unstringCountIn;
        return u.withIdentifier(visitAndCast(u.getIdentifier(), p));
    }

    @Override
    public Cobol.UnstringDelimitedByPhrase visitUnstringDelimitedByPhrase(Cobol.UnstringDelimitedByPhrase unstringDelimitedByPhrase, P p) {
        Cobol.UnstringDelimitedByPhrase u = unstringDelimitedByPhrase;
        return u.withName(visitAndCast(u.getName(), p));
    }

    @Override
    public Cobol.UnstringDelimiterIn visitUnstringDelimiterIn(Cobol.UnstringDelimiterIn unstringDelimiterIn, P p) {
        Cobol.UnstringDelimiterIn u = unstringDelimiterIn;
        return u.withIdentifier(visitAndCast(u.getIdentifier(), p));
    }

    @Override
    public Cobol.UnstringInto visitUnstringInto(Cobol.UnstringInto unstringInto, P p) {
        Cobol.UnstringInto u = unstringInto;
        u = u.withIdentifier(visitAndCast(u.getIdentifier(), p));
        u = u.withUnstringDelimiterIn(visitAndCast(u.getUnstringDelimiterIn(), p));
        return u.withUnstringCountIn(visitAndCast(u.getUnstringCountIn(), p));
    }

    @Override
    public Cobol.UnstringIntoPhrase visitUnstringIntoPhrase(Cobol.UnstringIntoPhrase unstringIntoPhrase, P p) {
        Cobol.UnstringIntoPhrase u = unstringIntoPhrase;
        return u.withUnstringIntos(ListUtils.map(u.getUnstringIntos(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.UnstringOrAllPhrase visitUnstringOrAllPhrase(Cobol.UnstringOrAllPhrase unstringOrAllPhrase, P p) {
        Cobol.UnstringOrAllPhrase u = unstringOrAllPhrase;
        return u.withName(visitAndCast(u.getName(), p));
    }

    @Override
    public Cobol.UnstringSendingPhrase visitUnstringSendingPhrase(Cobol.UnstringSendingPhrase unstringSendingPhrase, P p) {
        Cobol.UnstringSendingPhrase u = unstringSendingPhrase;
        u = u.withIdentifier(visitAndCast(u.getIdentifier(), p));
        u = u.withUnstringDelimitedByPhrase(visitAndCast(u.getUnstringDelimitedByPhrase(), p));
        return u.withUnstringOrAllPhrases(ListUtils.map(u.getUnstringOrAllPhrases(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.UnstringWithPointerPhrase visitUnstringWithPointerPhrase(Cobol.UnstringWithPointerPhrase unstringWithPointerPhrase, P p) {
        Cobol.UnstringWithPointerPhrase u = unstringWithPointerPhrase;
        return u.withQualifiedDataName(visitAndCast(u.getQualifiedDataName(), p));
    }

    @Override
    public Cobol.UseAfterClause visitUseAfterClause(Cobol.UseAfterClause useAfterClause, P p) {
        Cobol.UseAfterClause u = useAfterClause;
        return u.withUseAfterOn(visitAndCast(u.getUseAfterOn(), p));
    }

    @Override
    public Cobol.UseAfterOn visitUseAfterOn(Cobol.UseAfterOn useAfterOn, P p) {
        Cobol.UseAfterOn u = useAfterOn;
        return u.withFileNames(ListUtils.map(u.getFileNames(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.UseDebugClause visitUseDebugClause(Cobol.UseDebugClause useDebugClause, P p) {
        Cobol.UseDebugClause u = useDebugClause;
        return u.withUseDebugs(ListUtils.map(u.getUseDebugs(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.UseDebugOn visitUseDebugOn(Cobol.UseDebugOn useDebugOn, P p) {
        Cobol.UseDebugOn u = useDebugOn;
        return u.withName(visitAndCast(u.getName(), p));
    }

    @Override
    public Cobol.UseStatement visitUseStatement(Cobol.UseStatement useStatement, P p) {
        Cobol.UseStatement u = useStatement;
        return u.withClause(visitAndCast(u.getClause(), p));
    }

    @Override
    public Cobol.ValuedObjectComputerClause visitValuedObjectComputerClause(Cobol.ValuedObjectComputerClause valuedObjectComputerClause, P p) {
        Cobol.ValuedObjectComputerClause v = valuedObjectComputerClause;
        return v.withValue(visitAndCast(v.getValue(), p));
    }

    @Override
    public Cobol.ValueOfClause visitValueOfClause(Cobol.ValueOfClause valueOfClause, P p) {
        Cobol.ValueOfClause v = valueOfClause;
        return v.withValuePairs(ListUtils.map(v.getValuePairs(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.ValuePair visitValuePair(Cobol.ValuePair valuePair, P p) {
        Cobol.ValuePair v = valuePair;
        v = v.withSystemName(visitAndCast(v.getSystemName(), p));
        return v.withName(visitAndCast(v.getName(), p));
    }

    @Override
    public Cobol.Write visitWrite(Cobol.Write write, P p) {
        Cobol.Write w = write;
        w = w.withRecordName(visitAndCast(w.getRecordName(), p));
        w = w.withWriteFromPhrase(visitAndCast(w.getWriteFromPhrase(), p));
        w = w.withWriteAdvancingPhrase(visitAndCast(w.getWriteAdvancingPhrase(), p));
        w = w.withWriteAtEndOfPagePhrase(visitAndCast(w.getWriteAtEndOfPagePhrase(), p));
        w = w.withWriteNotAtEndOfPagePhrase(visitAndCast(w.getWriteNotAtEndOfPagePhrase(), p));
        w = w.withInvalidKeyPhrase(visitAndCast(w.getInvalidKeyPhrase(), p));
        return w.withNotInvalidKeyPhrase(visitAndCast(w.getNotInvalidKeyPhrase(), p));
    }

    @Override
    public Cobol.WorkingStorageSection visitWorkingStorageSection(Cobol.WorkingStorageSection workingStorageSection, P p) {
        Cobol.WorkingStorageSection w = workingStorageSection;
        return w.withDataDescriptions(ListUtils.map(w.getDataDescriptions(), e -> visitAndCast(e, p)));
    }

    @Override
    public Cobol.WriteAdvancingLines visitWriteAdvancingLines(Cobol.WriteAdvancingLines writeAdvancingLines, P p) {
        Cobol.WriteAdvancingLines w = writeAdvancingLines;
        return w.withName(visitAndCast(w.getName(), p));
    }
}
