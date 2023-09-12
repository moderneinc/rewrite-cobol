/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
// Generated from java-escape by ANTLR 4.11.1
package org.openrewrite.controlm.internal.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ControlMParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, CM=4, CA_START=5, STRINGLITERAL=6, TEXT=7, CM_STRINGLITERAL=8,
		CM_TEXT=9;
	public static final int
		RULE_compilationUnit = 0, RULE_statement = 1, RULE_controlM = 2, RULE_controlMWord = 3,
		RULE_controlMCommentArea = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "statement", "controlM", "controlMWord", "controlMCommentArea"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\uFEFF'", null, null, "'^^CM^^'", "'^^CA_START^^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "UTF_8_BOM", "WS", "EOL", "CM", "CA_START", "STRINGLITERAL", "TEXT",
			"CM_STRINGLITERAL", "CM_TEXT"
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

	public ControlMParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ControlMParser.EOF, 0); }
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
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ControlMParserVisitor ) return ((ControlMParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
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
			setState(13);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM_STRINGLITERAL || _la==CM_TEXT) {
				{
				{
				setState(10);
				statement();
				}
				}
				setState(15);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(16);
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
		public ControlMContext controlM() {
			return getRuleContext(ControlMContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ControlMParserVisitor ) return ((ControlMParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(18);
			controlM();
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
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).enterControlM(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).exitControlM(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ControlMParserVisitor ) return ((ControlMParserVisitor<? extends T>)visitor).visitControlM(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ControlMContext controlM() throws RecognitionException {
		ControlMContext _localctx = new ControlMContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_controlM);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(20);
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
		public TerminalNode CM_TEXT() { return getToken(ControlMParser.CM_TEXT, 0); }
		public TerminalNode CM_STRINGLITERAL() { return getToken(ControlMParser.CM_STRINGLITERAL, 0); }
		public ControlMCommentAreaContext controlMCommentArea() {
			return getRuleContext(ControlMCommentAreaContext.class,0);
		}
		public ControlMWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_controlMWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).enterControlMWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).exitControlMWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ControlMParserVisitor ) return ((ControlMParserVisitor<? extends T>)visitor).visitControlMWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ControlMWordContext controlMWord() throws RecognitionException {
		ControlMWordContext _localctx = new ControlMWordContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_controlMWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22);
			_la = _input.LA(1);
			if ( !(_la==CM_STRINGLITERAL || _la==CM_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(24);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CA_START) {
				{
				setState(23);
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
		public TerminalNode CA_START() { return getToken(ControlMParser.CA_START, 0); }
		public TerminalNode CM_TEXT() { return getToken(ControlMParser.CM_TEXT, 0); }
		public TerminalNode CM_STRINGLITERAL() { return getToken(ControlMParser.CM_STRINGLITERAL, 0); }
		public ControlMCommentAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_controlMCommentArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).enterControlMCommentArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ControlMParserListener ) ((ControlMParserListener)listener).exitControlMCommentArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ControlMParserVisitor ) return ((ControlMParserVisitor<? extends T>)visitor).visitControlMCommentArea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ControlMCommentAreaContext controlMCommentArea() throws RecognitionException {
		ControlMCommentAreaContext _localctx = new ControlMCommentAreaContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_controlMCommentArea);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			match(CA_START);
			setState(27);
			_la = _input.LA(1);
			if ( !(_la==CM_STRINGLITERAL || _la==CM_TEXT) ) {
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
		"\u0004\u0001\t\u001e\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0001"+
		"\u0000\u0005\u0000\f\b\u0000\n\u0000\f\u0000\u000f\t\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0003\u0003\u0019\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0000\u0000\u0005\u0000\u0002\u0004\u0006\b\u0000\u0001\u0001"+
		"\u0000\b\t\u001a\u0000\r\u0001\u0000\u0000\u0000\u0002\u0012\u0001\u0000"+
		"\u0000\u0000\u0004\u0014\u0001\u0000\u0000\u0000\u0006\u0016\u0001\u0000"+
		"\u0000\u0000\b\u001a\u0001\u0000\u0000\u0000\n\f\u0003\u0002\u0001\u0000"+
		"\u000b\n\u0001\u0000\u0000\u0000\f\u000f\u0001\u0000\u0000\u0000\r\u000b"+
		"\u0001\u0000\u0000\u0000\r\u000e\u0001\u0000\u0000\u0000\u000e\u0010\u0001"+
		"\u0000\u0000\u0000\u000f\r\u0001\u0000\u0000\u0000\u0010\u0011\u0005\u0000"+
		"\u0000\u0001\u0011\u0001\u0001\u0000\u0000\u0000\u0012\u0013\u0003\u0004"+
		"\u0002\u0000\u0013\u0003\u0001\u0000\u0000\u0000\u0014\u0015\u0003\u0006"+
		"\u0003\u0000\u0015\u0005\u0001\u0000\u0000\u0000\u0016\u0018\u0007\u0000"+
		"\u0000\u0000\u0017\u0019\u0003\b\u0004\u0000\u0018\u0017\u0001\u0000\u0000"+
		"\u0000\u0018\u0019\u0001\u0000\u0000\u0000\u0019\u0007\u0001\u0000\u0000"+
		"\u0000\u001a\u001b\u0005\u0005\u0000\u0000\u001b\u001c\u0007\u0000\u0000"+
		"\u0000\u001c\t\u0001\u0000\u0000\u0000\u0002\r\u0018";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}