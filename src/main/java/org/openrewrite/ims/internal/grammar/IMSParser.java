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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-ims/IMSParser.g4 by ANTLR 4.13.2
package org.openrewrite.ims.internal.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class IMSParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, IMS_NAMED=4, IMS_STATEMENT=5, IMS_CONTINUATION=6, 
		COMMENT=7, UNKNOWN=8, CA_START=9, STRINGLITERAL=10, TEXT=11, IMS_STRINGLITERAL=12, 
		IMS_TEXT=13, COMMENT_TEXT=14, UNKNOWN_WS=15, UNKNOWN_STRINGLITERAL=16, 
		UNKNOWN_TEXT=17;
	public static final int
		RULE_compilationUnit = 0, RULE_statement = 1, RULE_ims = 2, RULE_imsWord = 3, 
		RULE_imsSequenceArea = 4, RULE_comment = 5, RULE_commentWord = 6, RULE_commentSequenceArea = 7, 
		RULE_unknown = 8, RULE_unknownWord = 9, RULE_unknownSequenceArea = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "statement", "ims", "imsWord", "imsSequenceArea", 
			"comment", "commentWord", "commentSequenceArea", "unknown", "unknownWord", 
			"unknownSequenceArea"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\uFEFF'", null, null, null, null, null, "'^^COMMENT^^'", "'^^UNKNOWN^^'", 
			"'^^CA_START^^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "UTF_8_BOM", "WS", "EOL", "IMS_NAMED", "IMS_STATEMENT", "IMS_CONTINUATION", 
			"COMMENT", "UNKNOWN", "CA_START", "STRINGLITERAL", "TEXT", "IMS_STRINGLITERAL", 
			"IMS_TEXT", "COMMENT_TEXT", "UNKNOWN_WS", "UNKNOWN_STRINGLITERAL", "UNKNOWN_TEXT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "IMSParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public IMSParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(IMSParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 225792L) != 0)) {
				{
				{
				setState(22);
				statement();
				}
				}
				setState(27);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(28);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public ImsContext ims() {
			return getRuleContext(ImsContext.class,0);
		}
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public UnknownContext unknown() {
			return getRuleContext(UnknownContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(33);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IMS_STRINGLITERAL:
			case IMS_TEXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(30);
				ims();
				}
				break;
			case COMMENT_TEXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(31);
				comment();
				}
				break;
			case CA_START:
			case UNKNOWN_STRINGLITERAL:
			case UNKNOWN_TEXT:
				enterOuterAlt(_localctx, 3);
				{
				setState(32);
				unknown();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImsContext extends ParserRuleContext {
		public ImsWordContext imsWord() {
			return getRuleContext(ImsWordContext.class,0);
		}
		public ImsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ims; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterIms(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitIms(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitIms(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImsContext ims() throws RecognitionException {
		ImsContext _localctx = new ImsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_ims);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			imsWord();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImsWordContext extends ParserRuleContext {
		public TerminalNode IMS_TEXT() { return getToken(IMSParser.IMS_TEXT, 0); }
		public TerminalNode IMS_STRINGLITERAL() { return getToken(IMSParser.IMS_STRINGLITERAL, 0); }
		public ImsSequenceAreaContext imsSequenceArea() {
			return getRuleContext(ImsSequenceAreaContext.class,0);
		}
		public ImsWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imsWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterImsWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitImsWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitImsWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImsWordContext imsWord() throws RecognitionException {
		ImsWordContext _localctx = new ImsWordContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_imsWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			_la = _input.LA(1);
			if ( !(_la==IMS_STRINGLITERAL || _la==IMS_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(39);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(38);
				imsSequenceArea();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImsSequenceAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(IMSParser.CA_START, 0); }
		public TerminalNode IMS_TEXT() { return getToken(IMSParser.IMS_TEXT, 0); }
		public TerminalNode IMS_STRINGLITERAL() { return getToken(IMSParser.IMS_STRINGLITERAL, 0); }
		public ImsSequenceAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imsSequenceArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterImsSequenceArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitImsSequenceArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitImsSequenceArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImsSequenceAreaContext imsSequenceArea() throws RecognitionException {
		ImsSequenceAreaContext _localctx = new ImsSequenceAreaContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_imsSequenceArea);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			match(CA_START);
			setState(42);
			_la = _input.LA(1);
			if ( !(_la==IMS_STRINGLITERAL || _la==IMS_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentContext extends ParserRuleContext {
		public CommentWordContext commentWord() {
			return getRuleContext(CommentWordContext.class,0);
		}
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			commentWord();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentWordContext extends ParserRuleContext {
		public TerminalNode COMMENT_TEXT() { return getToken(IMSParser.COMMENT_TEXT, 0); }
		public CommentSequenceAreaContext commentSequenceArea() {
			return getRuleContext(CommentSequenceAreaContext.class,0);
		}
		public CommentWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterCommentWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitCommentWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitCommentWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentWordContext commentWord() throws RecognitionException {
		CommentWordContext _localctx = new CommentWordContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_commentWord);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			match(COMMENT_TEXT);
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(47);
				commentSequenceArea();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentSequenceAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(IMSParser.CA_START, 0); }
		public TerminalNode COMMENT_TEXT() { return getToken(IMSParser.COMMENT_TEXT, 0); }
		public CommentSequenceAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentSequenceArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterCommentSequenceArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitCommentSequenceArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitCommentSequenceArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentSequenceAreaContext commentSequenceArea() throws RecognitionException {
		CommentSequenceAreaContext _localctx = new CommentSequenceAreaContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_commentSequenceArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			match(CA_START);
			setState(51);
			match(COMMENT_TEXT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnknownContext extends ParserRuleContext {
		public UnknownWordContext unknownWord() {
			return getRuleContext(UnknownWordContext.class,0);
		}
		public UnknownContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknown; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterUnknown(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitUnknown(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitUnknown(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownContext unknown() throws RecognitionException {
		UnknownContext _localctx = new UnknownContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_unknown);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			unknownWord();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnknownWordContext extends ParserRuleContext {
		public TerminalNode UNKNOWN_TEXT() { return getToken(IMSParser.UNKNOWN_TEXT, 0); }
		public TerminalNode UNKNOWN_STRINGLITERAL() { return getToken(IMSParser.UNKNOWN_STRINGLITERAL, 0); }
		public UnknownSequenceAreaContext unknownSequenceArea() {
			return getRuleContext(UnknownSequenceAreaContext.class,0);
		}
		public UnknownWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterUnknownWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitUnknownWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitUnknownWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownWordContext unknownWord() throws RecognitionException {
		UnknownWordContext _localctx = new UnknownWordContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_unknownWord);
		int _la;
		try {
			setState(60);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNKNOWN_STRINGLITERAL:
			case UNKNOWN_TEXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(55);
				_la = _input.LA(1);
				if ( !(_la==UNKNOWN_STRINGLITERAL || _la==UNKNOWN_TEXT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(57);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(56);
					unknownSequenceArea();
					}
					break;
				}
				}
				break;
			case CA_START:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				unknownSequenceArea();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnknownSequenceAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(IMSParser.CA_START, 0); }
		public TerminalNode UNKNOWN_TEXT() { return getToken(IMSParser.UNKNOWN_TEXT, 0); }
		public TerminalNode UNKNOWN_STRINGLITERAL() { return getToken(IMSParser.UNKNOWN_STRINGLITERAL, 0); }
		public UnknownSequenceAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownSequenceArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).enterUnknownSequenceArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IMSParserListener ) ((IMSParserListener)listener).exitUnknownSequenceArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IMSParserVisitor ) return ((IMSParserVisitor<? extends T>)visitor).visitUnknownSequenceArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownSequenceAreaContext unknownSequenceArea() throws RecognitionException {
		UnknownSequenceAreaContext _localctx = new UnknownSequenceAreaContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_unknownSequenceArea);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			match(CA_START);
			setState(63);
			_la = _input.LA(1);
			if ( !(_la==UNKNOWN_STRINGLITERAL || _la==UNKNOWN_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0011B\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000\u0005\u0000\u0018"+
		"\b\u0000\n\u0000\f\u0000\u001b\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001\"\b\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0003\u0003(\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0003\u0006"+
		"1\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t"+
		"\u0001\t\u0003\t:\b\t\u0001\t\u0003\t=\b\t\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0000\u0000\u000b\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014"+
		"\u0000\u0002\u0001\u0000\f\r\u0001\u0000\u0010\u0011=\u0000\u0019\u0001"+
		"\u0000\u0000\u0000\u0002!\u0001\u0000\u0000\u0000\u0004#\u0001\u0000\u0000"+
		"\u0000\u0006%\u0001\u0000\u0000\u0000\b)\u0001\u0000\u0000\u0000\n,\u0001"+
		"\u0000\u0000\u0000\f.\u0001\u0000\u0000\u0000\u000e2\u0001\u0000\u0000"+
		"\u0000\u00105\u0001\u0000\u0000\u0000\u0012<\u0001\u0000\u0000\u0000\u0014"+
		">\u0001\u0000\u0000\u0000\u0016\u0018\u0003\u0002\u0001\u0000\u0017\u0016"+
		"\u0001\u0000\u0000\u0000\u0018\u001b\u0001\u0000\u0000\u0000\u0019\u0017"+
		"\u0001\u0000\u0000\u0000\u0019\u001a\u0001\u0000\u0000\u0000\u001a\u001c"+
		"\u0001\u0000\u0000\u0000\u001b\u0019\u0001\u0000\u0000\u0000\u001c\u001d"+
		"\u0005\u0000\u0000\u0001\u001d\u0001\u0001\u0000\u0000\u0000\u001e\"\u0003"+
		"\u0004\u0002\u0000\u001f\"\u0003\n\u0005\u0000 \"\u0003\u0010\b\u0000"+
		"!\u001e\u0001\u0000\u0000\u0000!\u001f\u0001\u0000\u0000\u0000! \u0001"+
		"\u0000\u0000\u0000\"\u0003\u0001\u0000\u0000\u0000#$\u0003\u0006\u0003"+
		"\u0000$\u0005\u0001\u0000\u0000\u0000%\'\u0007\u0000\u0000\u0000&(\u0003"+
		"\b\u0004\u0000\'&\u0001\u0000\u0000\u0000\'(\u0001\u0000\u0000\u0000("+
		"\u0007\u0001\u0000\u0000\u0000)*\u0005\t\u0000\u0000*+\u0007\u0000\u0000"+
		"\u0000+\t\u0001\u0000\u0000\u0000,-\u0003\f\u0006\u0000-\u000b\u0001\u0000"+
		"\u0000\u0000.0\u0005\u000e\u0000\u0000/1\u0003\u000e\u0007\u00000/\u0001"+
		"\u0000\u0000\u000001\u0001\u0000\u0000\u00001\r\u0001\u0000\u0000\u0000"+
		"23\u0005\t\u0000\u000034\u0005\u000e\u0000\u00004\u000f\u0001\u0000\u0000"+
		"\u000056\u0003\u0012\t\u00006\u0011\u0001\u0000\u0000\u000079\u0007\u0001"+
		"\u0000\u00008:\u0003\u0014\n\u000098\u0001\u0000\u0000\u00009:\u0001\u0000"+
		"\u0000\u0000:=\u0001\u0000\u0000\u0000;=\u0003\u0014\n\u0000<7\u0001\u0000"+
		"\u0000\u0000<;\u0001\u0000\u0000\u0000=\u0013\u0001\u0000\u0000\u0000"+
		">?\u0005\t\u0000\u0000?@\u0007\u0001\u0000\u0000@\u0015\u0001\u0000\u0000"+
		"\u0000\u0006\u0019!\'09<";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}