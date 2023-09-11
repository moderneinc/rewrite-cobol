/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
// Generated from java-escape by ANTLR 4.11.1
package org.openrewrite.jcl.internal.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class JCLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, JCL_STATEMENT=4, JCL_STREAM=5, JES2=6, JES3=7,
		CM=8, COMMENT=9, UNKNOWN=10, CA_START=11, STRINGLITERAL=12, TEXT=13, JCL_TC_START=14,
		JCL_TC_STOP=15, JCL_STRINGLITERAL=16, JCL_TEXT=17, STREAM_STRINGLITERAL=18,
		STREAM_TEXT=19, JES2_STRINGLITERAL=20, JES2_TEXT=21, JES3_STRINGLITERAL=22,
		JES3_TEXT=23, CM_STRINGLITERAL=24, CM_TEXT=25, COMMENT_WS=26, COMMENT_STRINGLITERAL=27,
		COMMENT_TEXT=28, UNKNOWN_STRINGLITERAL=29, UNKNOWN_TEXT=30;
	public static final int
		RULE_compilationUnit = 0, RULE_statement = 1, RULE_jcl = 2, RULE_jclWord = 3,
		RULE_jclCommentArea = 4, RULE_jclTrailingComment = 5, RULE_jes2 = 6, RULE_jes2Word = 7,
		RULE_jes2CommentArea = 8, RULE_jes3 = 9, RULE_jes3Word = 10, RULE_jes3CommentArea = 11,
		RULE_stream = 12, RULE_streamWord = 13, RULE_streamCommentArea = 14, RULE_controlM = 15,
		RULE_controlMWord = 16, RULE_controlMCommentArea = 17, RULE_comment = 18,
		RULE_commentWord = 19, RULE_commentCommentArea = 20, RULE_unknown = 21,
		RULE_unknownWord = 22, RULE_unknownCommentArea = 23;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "statement", "jcl", "jclWord", "jclCommentArea", "jclTrailingComment",
			"jes2", "jes2Word", "jes2CommentArea", "jes3", "jes3Word", "jes3CommentArea",
			"stream", "streamWord", "streamCommentArea", "controlM", "controlMWord",
			"controlMCommentArea", "comment", "commentWord", "commentCommentArea",
			"unknown", "unknownWord", "unknownCommentArea"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\uFEFF'", null, null, null, "'^^STREAM^^'", null, null, "'^^CM^^'",
			"'^^COMMENT^^'", "'^^UNKNOWN^^'", "'^^CA_START^^'", null, null, "'^^TC_START^^'",
			"'^^TC_STOP^^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "UTF_8_BOM", "WS", "EOL", "JCL_STATEMENT", "JCL_STREAM", "JES2",
			"JES3", "CM", "COMMENT", "UNKNOWN", "CA_START", "STRINGLITERAL", "TEXT",
			"JCL_TC_START", "JCL_TC_STOP", "JCL_STRINGLITERAL", "JCL_TEXT", "STREAM_STRINGLITERAL",
			"STREAM_TEXT", "JES2_STRINGLITERAL", "JES2_TEXT", "JES3_STRINGLITERAL",
			"JES3_TEXT", "CM_STRINGLITERAL", "CM_TEXT", "COMMENT_WS", "COMMENT_STRINGLITERAL",
			"COMMENT_TEXT", "UNKNOWN_STRINGLITERAL", "UNKNOWN_TEXT"
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
	public String getGrammarFileName() { return "java-escape"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JCLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(JCLParser.EOF, 0); }
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
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
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
			setState(51);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 2080309248L) != 0) {
				{
				{
				setState(48);
				statement();
				}
				}
				setState(53);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(54);
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
		public JclContext jcl() {
			return getRuleContext(JclContext.class,0);
		}
		public Jes2Context jes2() {
			return getRuleContext(Jes2Context.class,0);
		}
		public Jes3Context jes3() {
			return getRuleContext(Jes3Context.class,0);
		}
		public StreamContext stream() {
			return getRuleContext(StreamContext.class,0);
		}
		public ControlMContext controlM() {
			return getRuleContext(ControlMContext.class,0);
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
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(63);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case JCL_STRINGLITERAL:
			case JCL_TEXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(56);
				jcl();
				}
				break;
			case JES2_STRINGLITERAL:
			case JES2_TEXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				jes2();
				}
				break;
			case JES3_STRINGLITERAL:
			case JES3_TEXT:
				enterOuterAlt(_localctx, 3);
				{
				setState(58);
				jes3();
				}
				break;
			case STREAM_STRINGLITERAL:
			case STREAM_TEXT:
				enterOuterAlt(_localctx, 4);
				{
				setState(59);
				stream();
				}
				break;
			case CM_STRINGLITERAL:
			case CM_TEXT:
				enterOuterAlt(_localctx, 5);
				{
				setState(60);
				controlM();
				}
				break;
			case COMMENT_STRINGLITERAL:
			case COMMENT_TEXT:
				enterOuterAlt(_localctx, 6);
				{
				setState(61);
				comment();
				}
				break;
			case UNKNOWN_STRINGLITERAL:
			case UNKNOWN_TEXT:
				enterOuterAlt(_localctx, 7);
				{
				setState(62);
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
	public static class JclContext extends ParserRuleContext {
		public JclWordContext jclWord() {
			return getRuleContext(JclWordContext.class,0);
		}
		public JclTrailingCommentContext jclTrailingComment() {
			return getRuleContext(JclTrailingCommentContext.class,0);
		}
		public JclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jcl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJcl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJcl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJcl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JclContext jcl() throws RecognitionException {
		JclContext _localctx = new JclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_jcl);
		try {
			setState(67);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(65);
				jclWord();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(66);
				jclTrailingComment();
				}
				break;
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
	public static class JclWordContext extends ParserRuleContext {
		public TerminalNode JCL_TEXT() { return getToken(JCLParser.JCL_TEXT, 0); }
		public TerminalNode JCL_STRINGLITERAL() { return getToken(JCLParser.JCL_STRINGLITERAL, 0); }
		public JclCommentAreaContext jclCommentArea() {
			return getRuleContext(JclCommentAreaContext.class,0);
		}
		public JclWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jclWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJclWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJclWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJclWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JclWordContext jclWord() throws RecognitionException {
		JclWordContext _localctx = new JclWordContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_jclWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			_la = _input.LA(1);
			if ( !(_la==JCL_STRINGLITERAL || _la==JCL_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(71);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(70);
				jclCommentArea();
				}
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
	public static class JclCommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public JclWordContext jclWord() {
			return getRuleContext(JclWordContext.class,0);
		}
		public JclCommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jclCommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJclCommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJclCommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJclCommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JclCommentAreaContext jclCommentArea() throws RecognitionException {
		JclCommentAreaContext _localctx = new JclCommentAreaContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_jclCommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			match(CA_START);
			setState(74);
			jclWord();
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
	public static class JclTrailingCommentContext extends ParserRuleContext {
		public List<JclWordContext> jclWord() {
			return getRuleContexts(JclWordContext.class);
		}
		public JclWordContext jclWord(int i) {
			return getRuleContext(JclWordContext.class,i);
		}
		public TerminalNode JCL_TC_START() { return getToken(JCLParser.JCL_TC_START, 0); }
		public TerminalNode JCL_TC_STOP() { return getToken(JCLParser.JCL_TC_STOP, 0); }
		public JclCommentAreaContext jclCommentArea() {
			return getRuleContext(JclCommentAreaContext.class,0);
		}
		public JclTrailingCommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jclTrailingComment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJclTrailingComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJclTrailingComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJclTrailingComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JclTrailingCommentContext jclTrailingComment() throws RecognitionException {
		JclTrailingCommentContext _localctx = new JclTrailingCommentContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_jclTrailingComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			jclWord();
			setState(77);
			match(JCL_TC_START);
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==JCL_STRINGLITERAL || _la==JCL_TEXT) {
				{
				{
				setState(78);
				jclWord();
				}
				}
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(84);
			match(JCL_TC_STOP);
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(85);
				jclCommentArea();
				}
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
	public static class Jes2Context extends ParserRuleContext {
		public Jes2WordContext jes2Word() {
			return getRuleContext(Jes2WordContext.class,0);
		}
		public Jes2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jes2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJes2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJes2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJes2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Jes2Context jes2() throws RecognitionException {
		Jes2Context _localctx = new Jes2Context(_ctx, getState());
		enterRule(_localctx, 12, RULE_jes2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			jes2Word();
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
	public static class Jes2WordContext extends ParserRuleContext {
		public TerminalNode JES2_TEXT() { return getToken(JCLParser.JES2_TEXT, 0); }
		public TerminalNode JES2_STRINGLITERAL() { return getToken(JCLParser.JES2_STRINGLITERAL, 0); }
		public Jes2CommentAreaContext jes2CommentArea() {
			return getRuleContext(Jes2CommentAreaContext.class,0);
		}
		public Jes2WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jes2Word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJes2Word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJes2Word(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJes2Word(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Jes2WordContext jes2Word() throws RecognitionException {
		Jes2WordContext _localctx = new Jes2WordContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_jes2Word);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			_la = _input.LA(1);
			if ( !(_la==JES2_STRINGLITERAL || _la==JES2_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(91);
				jes2CommentArea();
				}
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
	public static class Jes2CommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public Jes2WordContext jes2Word() {
			return getRuleContext(Jes2WordContext.class,0);
		}
		public Jes2CommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jes2CommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJes2CommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJes2CommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJes2CommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Jes2CommentAreaContext jes2CommentArea() throws RecognitionException {
		Jes2CommentAreaContext _localctx = new Jes2CommentAreaContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_jes2CommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			match(CA_START);
			setState(95);
			jes2Word();
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
	public static class Jes3Context extends ParserRuleContext {
		public Jes3WordContext jes3Word() {
			return getRuleContext(Jes3WordContext.class,0);
		}
		public Jes3Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jes3; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJes3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJes3(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJes3(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Jes3Context jes3() throws RecognitionException {
		Jes3Context _localctx = new Jes3Context(_ctx, getState());
		enterRule(_localctx, 18, RULE_jes3);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			jes3Word();
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
	public static class Jes3WordContext extends ParserRuleContext {
		public TerminalNode JES3_TEXT() { return getToken(JCLParser.JES3_TEXT, 0); }
		public TerminalNode JES3_STRINGLITERAL() { return getToken(JCLParser.JES3_STRINGLITERAL, 0); }
		public Jes3CommentAreaContext jes3CommentArea() {
			return getRuleContext(Jes3CommentAreaContext.class,0);
		}
		public Jes3WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jes3Word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJes3Word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJes3Word(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJes3Word(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Jes3WordContext jes3Word() throws RecognitionException {
		Jes3WordContext _localctx = new Jes3WordContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_jes3Word);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			_la = _input.LA(1);
			if ( !(_la==JES3_STRINGLITERAL || _la==JES3_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(100);
				jes3CommentArea();
				}
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
	public static class Jes3CommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public Jes3WordContext jes3Word() {
			return getRuleContext(Jes3WordContext.class,0);
		}
		public Jes3CommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jes3CommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterJes3CommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitJes3CommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitJes3CommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Jes3CommentAreaContext jes3CommentArea() throws RecognitionException {
		Jes3CommentAreaContext _localctx = new Jes3CommentAreaContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_jes3CommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(CA_START);
			setState(104);
			jes3Word();
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
	public static class StreamContext extends ParserRuleContext {
		public StreamWordContext streamWord() {
			return getRuleContext(StreamWordContext.class,0);
		}
		public StreamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stream; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterStream(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitStream(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitStream(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StreamContext stream() throws RecognitionException {
		StreamContext _localctx = new StreamContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_stream);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			streamWord();
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
	public static class StreamWordContext extends ParserRuleContext {
		public TerminalNode STREAM_TEXT() { return getToken(JCLParser.STREAM_TEXT, 0); }
		public TerminalNode STREAM_STRINGLITERAL() { return getToken(JCLParser.STREAM_STRINGLITERAL, 0); }
		public StreamCommentAreaContext streamCommentArea() {
			return getRuleContext(StreamCommentAreaContext.class,0);
		}
		public StreamWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_streamWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterStreamWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitStreamWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitStreamWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StreamWordContext streamWord() throws RecognitionException {
		StreamWordContext _localctx = new StreamWordContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_streamWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			_la = _input.LA(1);
			if ( !(_la==STREAM_STRINGLITERAL || _la==STREAM_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(109);
				streamCommentArea();
				}
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
	public static class StreamCommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public StreamWordContext streamWord() {
			return getRuleContext(StreamWordContext.class,0);
		}
		public StreamCommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_streamCommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterStreamCommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitStreamCommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitStreamCommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StreamCommentAreaContext streamCommentArea() throws RecognitionException {
		StreamCommentAreaContext _localctx = new StreamCommentAreaContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_streamCommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(CA_START);
			setState(113);
			streamWord();
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
	public static class ControlMContext extends ParserRuleContext {
		public ControlMWordContext controlMWord() {
			return getRuleContext(ControlMWordContext.class,0);
		}
		public ControlMContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_controlM; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterControlM(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitControlM(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitControlM(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ControlMContext controlM() throws RecognitionException {
		ControlMContext _localctx = new ControlMContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_controlM);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			controlMWord();
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
	public static class ControlMWordContext extends ParserRuleContext {
		public TerminalNode CM_TEXT() { return getToken(JCLParser.CM_TEXT, 0); }
		public TerminalNode CM_STRINGLITERAL() { return getToken(JCLParser.CM_STRINGLITERAL, 0); }
		public ControlMCommentAreaContext controlMCommentArea() {
			return getRuleContext(ControlMCommentAreaContext.class,0);
		}
		public ControlMWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_controlMWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterControlMWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitControlMWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitControlMWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ControlMWordContext controlMWord() throws RecognitionException {
		ControlMWordContext _localctx = new ControlMWordContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_controlMWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			_la = _input.LA(1);
			if ( !(_la==CM_STRINGLITERAL || _la==CM_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(118);
				controlMCommentArea();
				}
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
	public static class ControlMCommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public ControlMWordContext controlMWord() {
			return getRuleContext(ControlMWordContext.class,0);
		}
		public ControlMCommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_controlMCommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterControlMCommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitControlMCommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitControlMCommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ControlMCommentAreaContext controlMCommentArea() throws RecognitionException {
		ControlMCommentAreaContext _localctx = new ControlMCommentAreaContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_controlMCommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			match(CA_START);
			setState(122);
			controlMWord();
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
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
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
		public TerminalNode COMMENT_TEXT() { return getToken(JCLParser.COMMENT_TEXT, 0); }
		public TerminalNode COMMENT_STRINGLITERAL() { return getToken(JCLParser.COMMENT_STRINGLITERAL, 0); }
		public CommentCommentAreaContext commentCommentArea() {
			return getRuleContext(CommentCommentAreaContext.class,0);
		}
		public CommentWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterCommentWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitCommentWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitCommentWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentWordContext commentWord() throws RecognitionException {
		CommentWordContext _localctx = new CommentWordContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_commentWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			_la = _input.LA(1);
			if ( !(_la==COMMENT_STRINGLITERAL || _la==COMMENT_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(127);
				commentCommentArea();
				}
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
	public static class CommentCommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public CommentWordContext commentWord() {
			return getRuleContext(CommentWordContext.class,0);
		}
		public CommentCommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentCommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterCommentCommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitCommentCommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitCommentCommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentCommentAreaContext commentCommentArea() throws RecognitionException {
		CommentCommentAreaContext _localctx = new CommentCommentAreaContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_commentCommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(CA_START);
			setState(131);
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
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterUnknown(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitUnknown(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitUnknown(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownContext unknown() throws RecognitionException {
		UnknownContext _localctx = new UnknownContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_unknown);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
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
		public TerminalNode UNKNOWN_TEXT() { return getToken(JCLParser.UNKNOWN_TEXT, 0); }
		public TerminalNode UNKNOWN_STRINGLITERAL() { return getToken(JCLParser.UNKNOWN_STRINGLITERAL, 0); }
		public UnknownCommentAreaContext unknownCommentArea() {
			return getRuleContext(UnknownCommentAreaContext.class,0);
		}
		public UnknownWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterUnknownWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitUnknownWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitUnknownWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownWordContext unknownWord() throws RecognitionException {
		UnknownWordContext _localctx = new UnknownWordContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_unknownWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			_la = _input.LA(1);
			if ( !(_la==UNKNOWN_STRINGLITERAL || _la==UNKNOWN_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(136);
				unknownCommentArea();
				}
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
	public static class UnknownCommentAreaContext extends ParserRuleContext {
		public TerminalNode CA_START() { return getToken(JCLParser.CA_START, 0); }
		public UnknownWordContext unknownWord() {
			return getRuleContext(UnknownWordContext.class,0);
		}
		public UnknownCommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownCommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).enterUnknownCommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JCLParserListener ) ((JCLParserListener)listener).exitUnknownCommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JCLParserVisitor ) return ((JCLParserVisitor<? extends T>)visitor).visitUnknownCommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownCommentAreaContext unknownCommentArea() throws RecognitionException {
		UnknownCommentAreaContext _localctx = new UnknownCommentAreaContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_unknownCommentArea);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(CA_START);
			setState(140);
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

	public static final String _serializedATN =
		"\u0004\u0001\u001e\u008f\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0001\u0000\u0005"+
		"\u00002\b\u0000\n\u0000\f\u00005\t\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001@\b\u0001\u0001\u0002\u0001\u0002\u0003\u0002D\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0003\u0003H\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005P\b\u0005"+
		"\n\u0005\f\u0005S\t\u0005\u0001\u0005\u0001\u0005\u0003\u0005W\b\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0003\u0007]\b\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0003\nf\b\n"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0003"+
		"\ro\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0003\u0010x\b\u0010\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u0081"+
		"\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u008a\b\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0000\u0000\u0018\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.\u0000\u0007"+
		"\u0001\u0000\u0010\u0011\u0001\u0000\u0014\u0015\u0001\u0000\u0016\u0017"+
		"\u0001\u0000\u0012\u0013\u0001\u0000\u0018\u0019\u0001\u0000\u001b\u001c"+
		"\u0001\u0000\u001d\u001e\u0087\u00003\u0001\u0000\u0000\u0000\u0002?\u0001"+
		"\u0000\u0000\u0000\u0004C\u0001\u0000\u0000\u0000\u0006E\u0001\u0000\u0000"+
		"\u0000\bI\u0001\u0000\u0000\u0000\nL\u0001\u0000\u0000\u0000\fX\u0001"+
		"\u0000\u0000\u0000\u000eZ\u0001\u0000\u0000\u0000\u0010^\u0001\u0000\u0000"+
		"\u0000\u0012a\u0001\u0000\u0000\u0000\u0014c\u0001\u0000\u0000\u0000\u0016"+
		"g\u0001\u0000\u0000\u0000\u0018j\u0001\u0000\u0000\u0000\u001al\u0001"+
		"\u0000\u0000\u0000\u001cp\u0001\u0000\u0000\u0000\u001es\u0001\u0000\u0000"+
		"\u0000 u\u0001\u0000\u0000\u0000\"y\u0001\u0000\u0000\u0000$|\u0001\u0000"+
		"\u0000\u0000&~\u0001\u0000\u0000\u0000(\u0082\u0001\u0000\u0000\u0000"+
		"*\u0085\u0001\u0000\u0000\u0000,\u0087\u0001\u0000\u0000\u0000.\u008b"+
		"\u0001\u0000\u0000\u000002\u0003\u0002\u0001\u000010\u0001\u0000\u0000"+
		"\u000025\u0001\u0000\u0000\u000031\u0001\u0000\u0000\u000034\u0001\u0000"+
		"\u0000\u000046\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u000067\u0005"+
		"\u0000\u0000\u00017\u0001\u0001\u0000\u0000\u00008@\u0003\u0004\u0002"+
		"\u00009@\u0003\f\u0006\u0000:@\u0003\u0012\t\u0000;@\u0003\u0018\f\u0000"+
		"<@\u0003\u001e\u000f\u0000=@\u0003$\u0012\u0000>@\u0003*\u0015\u0000?"+
		"8\u0001\u0000\u0000\u0000?9\u0001\u0000\u0000\u0000?:\u0001\u0000\u0000"+
		"\u0000?;\u0001\u0000\u0000\u0000?<\u0001\u0000\u0000\u0000?=\u0001\u0000"+
		"\u0000\u0000?>\u0001\u0000\u0000\u0000@\u0003\u0001\u0000\u0000\u0000"+
		"AD\u0003\u0006\u0003\u0000BD\u0003\n\u0005\u0000CA\u0001\u0000\u0000\u0000"+
		"CB\u0001\u0000\u0000\u0000D\u0005\u0001\u0000\u0000\u0000EG\u0007\u0000"+
		"\u0000\u0000FH\u0003\b\u0004\u0000GF\u0001\u0000\u0000\u0000GH\u0001\u0000"+
		"\u0000\u0000H\u0007\u0001\u0000\u0000\u0000IJ\u0005\u000b\u0000\u0000"+
		"JK\u0003\u0006\u0003\u0000K\t\u0001\u0000\u0000\u0000LM\u0003\u0006\u0003"+
		"\u0000MQ\u0005\u000e\u0000\u0000NP\u0003\u0006\u0003\u0000ON\u0001\u0000"+
		"\u0000\u0000PS\u0001\u0000\u0000\u0000QO\u0001\u0000\u0000\u0000QR\u0001"+
		"\u0000\u0000\u0000RT\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000\u0000"+
		"TV\u0005\u000f\u0000\u0000UW\u0003\b\u0004\u0000VU\u0001\u0000\u0000\u0000"+
		"VW\u0001\u0000\u0000\u0000W\u000b\u0001\u0000\u0000\u0000XY\u0003\u000e"+
		"\u0007\u0000Y\r\u0001\u0000\u0000\u0000Z\\\u0007\u0001\u0000\u0000[]\u0003"+
		"\u0010\b\u0000\\[\u0001\u0000\u0000\u0000\\]\u0001\u0000\u0000\u0000]"+
		"\u000f\u0001\u0000\u0000\u0000^_\u0005\u000b\u0000\u0000_`\u0003\u000e"+
		"\u0007\u0000`\u0011\u0001\u0000\u0000\u0000ab\u0003\u0014\n\u0000b\u0013"+
		"\u0001\u0000\u0000\u0000ce\u0007\u0002\u0000\u0000df\u0003\u0016\u000b"+
		"\u0000ed\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000f\u0015\u0001"+
		"\u0000\u0000\u0000gh\u0005\u000b\u0000\u0000hi\u0003\u0014\n\u0000i\u0017"+
		"\u0001\u0000\u0000\u0000jk\u0003\u001a\r\u0000k\u0019\u0001\u0000\u0000"+
		"\u0000ln\u0007\u0003\u0000\u0000mo\u0003\u001c\u000e\u0000nm\u0001\u0000"+
		"\u0000\u0000no\u0001\u0000\u0000\u0000o\u001b\u0001\u0000\u0000\u0000"+
		"pq\u0005\u000b\u0000\u0000qr\u0003\u001a\r\u0000r\u001d\u0001\u0000\u0000"+
		"\u0000st\u0003 \u0010\u0000t\u001f\u0001\u0000\u0000\u0000uw\u0007\u0004"+
		"\u0000\u0000vx\u0003\"\u0011\u0000wv\u0001\u0000\u0000\u0000wx\u0001\u0000"+
		"\u0000\u0000x!\u0001\u0000\u0000\u0000yz\u0005\u000b\u0000\u0000z{\u0003"+
		" \u0010\u0000{#\u0001\u0000\u0000\u0000|}\u0003&\u0013\u0000}%\u0001\u0000"+
		"\u0000\u0000~\u0080\u0007\u0005\u0000\u0000\u007f\u0081\u0003(\u0014\u0000"+
		"\u0080\u007f\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000\u0000"+
		"\u0081\'\u0001\u0000\u0000\u0000\u0082\u0083\u0005\u000b\u0000\u0000\u0083"+
		"\u0084\u0003&\u0013\u0000\u0084)\u0001\u0000\u0000\u0000\u0085\u0086\u0003"+
		",\u0016\u0000\u0086+\u0001\u0000\u0000\u0000\u0087\u0089\u0007\u0006\u0000"+
		"\u0000\u0088\u008a\u0003.\u0017\u0000\u0089\u0088\u0001\u0000\u0000\u0000"+
		"\u0089\u008a\u0001\u0000\u0000\u0000\u008a-\u0001\u0000\u0000\u0000\u008b"+
		"\u008c\u0005\u000b\u0000\u0000\u008c\u008d\u0003,\u0016\u0000\u008d/\u0001"+
		"\u0000\u0000\u0000\f3?CGQV\\enw\u0080\u0089";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}